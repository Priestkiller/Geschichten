package dev.vincent.geschichten.memory

import android.content.Context
import android.os.StatFs
import dev.vincent.geschichten.ai.EmbeddingModel
import dev.vincent.geschichten.ai.NativeEmbeddingEncoder
import dev.vincent.geschichten.data.StoryBundle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicReference

data class SemanticSearchState(val enabled: Boolean=false,val ready: Boolean=false,val busy: Boolean=false,
                               val progress: Int=0,val indexed: Int=0,val total: Int=0,
                               val detail: String="Ausgeschaltet. Die bisherige Wortsuche bleibt aktiv.")

/** Optional local retrieval only. A single worker, short native inputs and disposable index.
 * Never writes to the story database or selects/deletes any text model. */
internal class SemanticSearch(context: Context) : AutoCloseable {
    private val app=context.applicationContext
    private val preferences=app.getSharedPreferences("semantic_search",0)
    private val directory=File(app.noBackupFilesDir,"search-models").apply {mkdirs()}
    private val file=File(directory,EmbeddingModel.FILE_NAME)
    private val index=SemanticIndex(app)
    private val operation=Mutex()
    private val active=AtomicReference<NativeEmbeddingEncoder?>(null)
    private val connection=AtomicReference<HttpURLConnection?>(null)
    private val _state=MutableStateFlow(SemanticSearchState(enabled=preferences.getBoolean("enabled",false),ready=file.length()==EmbeddingModel.BYTES,
        detail=if(preferences.getBoolean("enabled",false)) "Testoption aktiv. Ohne vorbereitete Suchvektoren bleibt die Wortsuche aktiv."
            else "Ausgeschaltet. Die bisherige Wortsuche bleibt aktiv."))
    val state=_state.asStateFlow()
    fun setEnabled(enabled: Boolean) {
        check(preferences.edit().putBoolean("enabled",enabled).commit())
        _state.value=_state.value.copy(enabled=enabled,detail=if(enabled) "Testoption aktiv. Fehlende Suchvektoren werden mit der bisherigen Wortsuche überbrückt." else "Ausgeschaltet. Die bisherige Wortsuche bleibt aktiv.")
        if(!enabled)cancel()
    }
    fun cancel() {active.get()?.cancel();connection.getAndSet(null)?.disconnect()}
    private suspend fun verifyFile() {
        check(file.isFile && file.length()==EmbeddingModel.BYTES) {"Das Suchmodell fehlt oder ist unvollständig. Die Wortsuche bleibt aktiv."}
        val marker="${file.lastModified()}:${file.length()}:${EmbeddingModel.SHA256}"
        if(preferences.getString("verified",null)==marker)return
        val hash=MessageDigest.getInstance("SHA-256")
        file.inputStream().use {input ->val buffer=ByteArray(128*1024);while(true){currentCoroutineContext().ensureActive();val n=input.read(buffer);if(n<0)break;hash.update(buffer,0,n)}}
        check(hash.digest().joinToString(""){"%02x".format(it)}==EmbeddingModel.SHA256) {"Die Suchmodell-Prüfsumme stimmt nicht. Die Wortsuche bleibt aktiv."}
        preferences.edit().putString("verified",marker).apply()
    }
    suspend fun download()=withContext(Dispatchers.IO) {
        operation.lock()
        try {
            if(file.exists()){verifyFile();_state.value=_state.value.copy(ready=true,detail="Suchmodell lokal bereit.");return@withContext}
            check(StatFs(directory.absolutePath).availableBytes>EmbeddingModel.BYTES+8*1024*1024) {"Für das Suchmodell fehlen etwa 334 MB freier Speicher."}
            _state.value=_state.value.copy(busy=true,progress=0,detail="Suchmodell herunterladen …")
            val partial=File(directory,"${EmbeddingModel.FILE_NAME}.part")
            var url=URL(EmbeddingModel.URL);var conn:HttpURLConnection?=null
            for(attempt in 0..7) {
                currentCoroutineContext().ensureActive();check(url.protocol=="https") {"Unsichere Download-Weiterleitung."}
                val next=url.openConnection() as HttpURLConnection;connection.set(next);next.instanceFollowRedirects=false;next.connectTimeout=25_000;next.readTimeout=25_000
                val code=next.responseCode
                if(code in 300..399){val location=next.getHeaderField("Location") ?: error("Download-Weiterleitung fehlt.");url=URL(url,location);next.disconnect();continue}
                check(code==200) {"Suchmodell-Download nicht verfügbar (HTTP $code)."};conn=next;break
            }
            val response=conn ?: error("Zu viele Download-Weiterleitungen.")
            val hash=MessageDigest.getInstance("SHA-256");var done=0L
            response.inputStream.use {input ->partial.outputStream().use {output ->val buffer=ByteArray(128*1024);while(true){currentCoroutineContext().ensureActive();val n=input.read(buffer);if(n<0)break;done+=n;check(done<=EmbeddingModel.BYTES);output.write(buffer,0,n);hash.update(buffer,0,n);_state.value=_state.value.copy(progress=(done*100/EmbeddingModel.BYTES).toInt())}}}
            check(done==EmbeddingModel.BYTES && hash.digest().joinToString(""){"%02x".format(it)}==EmbeddingModel.SHA256) {"Der Suchmodell-Download ist unvollständig oder beschädigt."}
            currentCoroutineContext().ensureActive();Files.move(partial.toPath(),file.toPath())
            preferences.edit().putString("verified","${file.lastModified()}:${file.length()}:${EmbeddingModel.SHA256}").apply()
            _state.value=_state.value.copy(ready=true,detail="Suchmodell lokal bereit. Aktiviere die Testoption und bereite die Erinnerungen vor.")
        } catch(cancelled: CancellationException){_state.value=_state.value.copy(detail="Suchauswertung oder Download angehalten. Originalnachrichten bleiben erhalten.");throw cancelled}
        catch(e: Exception){_state.value=_state.value.copy(detail=e.message ?: "Suchmodell nicht verfügbar; Wortsuche aktiv.");throw e}
        finally {connection.getAndSet(null)?.disconnect();_state.value=_state.value.copy(busy=false);operation.unlock()}
    }
    /** At most 16 changed sections per operation; callers yield between batches. */
    suspend fun indexBatch(bundle: StoryBundle): Boolean=withContext(Dispatchers.IO) {
        if(!_state.value.enabled || !_state.value.ready || !operation.tryLock())return@withContext false
        try {
            verifyFile()
            val job=currentCoroutineContext()
            val candidates=ArchiveRecall.candidates(bundle,"damals",checkCancellation={job.ensureActive()}).sortedByDescending {it.message.createdAt}
            val missing=candidates.filterNot {index.ready(it)}
            _state.value=_state.value.copy(busy=missing.isNotEmpty(),indexed=candidates.size-missing.size,total=candidates.size,detail=if(missing.isEmpty()) "Erinnerungen dieser Geschichte vorbereitet." else "Erinnerungen dieser Geschichte vorbereiten …")
            if(missing.isEmpty())return@withContext false
            val encoder=NativeEmbeddingEncoder(file.absolutePath);active.set(encoder)
            // Abort decoding in a separate dispatcher even while JNI occupies this worker.
            val timer=CoroutineScope(Dispatchers.Default).launch {delay(6_000);encoder.cancel()}
            try {
                for(e in missing.take(16)) {
                    currentCoroutineContext().ensureActive();check(_state.value.enabled)
                    index.put(e,bundle.memory.version,null,"PENDING")
                    val vector=encoder.encode(EmbeddingModel.document(e.text))
                    currentCoroutineContext().ensureActive();check(_state.value.enabled)
                    index.put(e,bundle.memory.version,vector,"READY")
                    _state.value=_state.value.copy(indexed=_state.value.indexed+1)
                }
            }finally {timer.cancel();active.compareAndSet(encoder,null);encoder.close()}
            _state.value=_state.value.copy(detail=if(missing.size<=16) "Erinnerungen dieser Geschichte vorbereitet." else "Weitere Erinnerungen werden abschnittsweise vorbereitet.")
            missing.size>16
        }catch(cancelled: CancellationException){throw cancelled}
        catch(e: Exception){_state.value=_state.value.copy(detail=(e.message ?: "Suchauswertung angehalten.")+" Die Wortsuche bleibt aktiv.");false}
        finally {_state.value=_state.value.copy(busy=false);operation.unlock()}
    }
    suspend fun search(bundle: StoryBundle): List<ArchiveExcerpt>?=withContext(Dispatchers.IO) {
        if(!_state.value.enabled || !_state.value.ready || !operation.tryLock())return@withContext null
        try {
            withTimeoutOrNull(1_500) {
                verifyFile();val question=bundle.messages.lastOrNull()?.text ?: return@withTimeoutOrNull null
                if(MemoryRules.privateAction(question))return@withTimeoutOrNull emptyList()
                val evidence=RoleEvidence.trusted(bundle)
                val job=currentCoroutineContext()
                val allowed=ArchiveRecall.candidates(bundle,question,evidence){job.ensureActive()}
                if(allowed.none {index.ready(it)})return@withTimeoutOrNull null
                currentCoroutineContext().ensureActive()
                val encoder=NativeEmbeddingEncoder(file.absolutePath);active.set(encoder)
                val timer=CoroutineScope(Dispatchers.Default).launch {delay(1_400);encoder.cancel()}
                try {
                    currentCoroutineContext().ensureActive()
                    val vector=encoder.encode(EmbeddingModel.query(question));currentCoroutineContext().ensureActive()
                    val semantic=index.search(bundle.story.id,allowed,vector)
                    HybridRecall.merge(ArchiveRecall.search(bundle,question,evidence=evidence),semantic)
                }finally {timer.cancel();active.compareAndSet(encoder,null);encoder.close()}
            }
        }catch(cancelled: CancellationException){throw cancelled}
        catch(e: Exception){_state.value=_state.value.copy(detail=(e.message ?: "Bedeutungssuche nicht verfügbar.")+" Die Wortsuche bleibt aktiv.");null}
        finally {operation.unlock()}
    }
    suspend fun rebuild(story: String)=withContext(Dispatchers.IO){operation.lock();try{index.clear(story)}finally{operation.unlock()}}
    override fun close() {cancel();index.close()}
}
