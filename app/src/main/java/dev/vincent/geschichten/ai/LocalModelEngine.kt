package dev.vincent.geschichten.ai

import android.content.Context
import android.os.StatFs
import android.os.Build
import android.os.SystemClock
import android.os.PowerManager
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.ExperimentalFlags
import com.google.ai.edge.litertlm.LogSeverity
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.google.ai.edge.litertlm.RepetitionPenaltyConfig
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.ThinkingConfig
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LocalModelException(message: String, cause: Throwable? = null) : IOException(message, cause)

/**
 * The app's text-generation provider. It calls LiteRT-LM or llama.cpp JNI on this device and
 * has no cloud-chat path. Network is used only by the explicit setup download.
 *
 * A single operation lock prevents loading/deleting native handles while a
 * conversation is active. LiteRT conversations stay fresh; GGUF reuses only exact
 * matching prefill blocks and discards divergent, failed or cancelled native history.
 */
class LocalModelEngine(context: Context, selectionName: String = "model_selection", defaultModelId: String = LocalModelCatalog.DEFAULT_ID) : StoryGeneration {
    private val appContext = context.applicationContext
    private val selection = appContext.getSharedPreferences(selectionName, Context.MODE_PRIVATE)
    private var model = LocalModelCatalog.find(selection.getString("model_id", null)) ?: LocalModelCatalog.restored(defaultModelId)
    // Android backup must never include a multi-gigabyte downloadable model.
    private val modelDirectory = File(appContext.noBackupFilesDir, "models")
    private val modelFile get() = File(modelDirectory, model.fileName)
    private val partialFile get() = File(modelDirectory, "${model.fileName}.part")
    private val operation = Mutex()
    private val closed = AtomicBoolean(false)
    private val connection = AtomicReference<HttpURLConnection?>(null)
    private val downloadJob = AtomicReference<Job?>(null)
    private val generationJob = AtomicReference<Job?>(null)
    private val activeConversation = AtomicReference<Conversation?>(null)
    private val conversationSignal = Any()
    private val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var tokenizer: ExactTokenizer? = null
    private var engine: Engine? = null // Access only while holding operation.
    private var ggufEngine: GgufEngine? = null
    private val activeGguf = AtomicReference<GgufEngine?>(null)
    private val deviceName = ModelPerformance.deviceName(Build.MODEL)
    private var optimizing = false
    private var performanceDetail: String? = selection.getString(performanceKey() + "_result", null)
    private var firstTextMs: Long? = null
    private var activeThreads = 4
    private var loadedBackend = "CPU"
    private fun performanceKey() = "cpu_${model.id}_${model.sha256.take(12)}_${Build.FINGERPRINT.hashCode()}"
    private fun preferredThreads() = selection.getInt(performanceKey(), 4).takeIf { it in 1..6 } ?: 4
    private val _state = MutableStateFlow(initialState())
    override val state: StateFlow<ModelState> = _state.asStateFlow()

    /** Serializes switching with native inference and keeps every model's files and partial download. */
    suspend fun selectModel(id: String) = withContext(Dispatchers.IO) {
        val next = LocalModelCatalog.find(id) ?: throw LocalModelException("Diese KI steht nicht zur Auswahl.")
        operation.withLock {
            ensureOpen()
            if (next.id == model.id) return@withLock
            check(selection.edit().putString("model_id", next.id).commit()) {
                "Die KI-Auswahl konnte nicht gespeichert werden. Bitte prüfe den freien Speicher."
            }
            releaseEngine()
            model = next
            performanceDetail = selection.getString(performanceKey() + "_result", null)
            firstTextMs = null
            _state.value = initialState()
        }
    }

    private fun downloadedModelIds(): Set<String> = LocalModelCatalog.models
        .filter { spec -> File(modelDirectory, spec.fileName).let { it.isFile && it.length() == spec.bytes } }
        .mapTo(mutableSetOf()) { it.id }

    /** Only inactive catalog models may be removed; shares the lock with switching and inference. */
    suspend fun deleteStoredModel(id: String) = withContext(Dispatchers.IO) {
        val spec = LocalModelCatalog.find(id) ?: throw LocalModelException("Diese KI steht nicht zur Auswahl.")
        operation.withLock {
            ensureOpen()
            if (id == model.id) throw LocalModelException("Wechsle zuerst zu einer anderen KI, bevor du diese Datei löschst.")
            try {
                for (name in listOf(spec.fileName, "${spec.fileName}.part")) {
                    val file = File(modelDirectory, name)
                    if (file.exists() && !file.delete()) throw LocalModelException("Die KI-Datei konnte nicht gelöscht werden.")
                }
            } finally { _state.value = _state.value.copy(downloadedModelIds = downloadedModelIds()) }
        }
    }

    /** Download/resume an immutable model. Completed files are never overwritten in place. */
    suspend fun downloadModel() = withContext(Dispatchers.IO) {
        operation.withLock {
            ensureOpen()
            if (modelFile.isFile && modelFile.length() == model.bytes) {
                _state.value = if (engine != null || ggufEngine != null) readyState() else downloadedState()
                return@withLock
            }
            val job = currentCoroutineContext()[Job]!!
            downloadJob.set(job)
            try {
                prepareDirectory()
                if (modelFile.exists() && !modelFile.delete()) {
                    throw LocalModelException("Die alte KI-Datei konnte nicht entfernt werden.")
                }
                if (partialFile.length() > model.bytes && !partialFile.delete()) {
                    throw LocalModelException("Der unvollständige Download konnte nicht zurückgesetzt werden.")
                }
                var offset = partialFile.length()
                ensureFreeSpace(model.bytes - offset)
                publishDownload(offset)
                if (offset < model.bytes) {
                    val conn = openConnection(offset)
                    try {
                        val body = try {
                            ModelDownloadProtocol.validate(
                                status = conn.responseCode,
                                offset = offset,
                                expectedBytes = model.bytes,
                                contentRange = conn.getHeaderField("Content-Range"),
                                contentLength = conn.contentLengthLong,
                                contentEncoding = conn.getHeaderField("Content-Encoding"),
                            )
                        } catch (error: IOException) {
                            throw LocalModelException(error.message ?: "Download-Daten ungültig.", error)
                        }
                        if (body.writeOffset == 0L && offset > 0) {
                            // Range ignored: discard the prefix before writing the full response.
                            FileOutputStream(partialFile, false).use { it.fd.sync() }
                            offset = 0
                            ensureFreeSpace(model.bytes)
                        }
                        copyDownload(conn, body.writeOffset, body.byteCount)
                    } finally {
                        connection.compareAndSet(conn, null)
                        conn.disconnect()
                    }
                }
                currentCoroutineContext().ensureActive()
                verifyChecksum(partialFile)
                currentCoroutineContext().ensureActive()
                // Both files are on the same app-private filesystem. A crash/cancel
                // can leave .part, never a partly installed .litertlm final file.
                Files.move(partialFile.toPath(), modelFile.toPath(), StandardCopyOption.ATOMIC_MOVE)
                _state.value = downloadedState()
            } catch (cancelled: CancellationException) {
                _state.value = initialState("Download pausiert. Du kannst ihn hier fortsetzen.")
                throw cancelled
            } catch (error: Exception) {
                // disconnect() can surface as IOException instead of cancellation.
                if (!job.isActive) {
                    _state.value = initialState("Download pausiert. Du kannst ihn hier fortsetzen.")
                    throw CancellationException("Download pausiert.", error)
                }
                val failure = downloadFailure(error)
                _state.value = initialState().copy(stage = ModelStage.ERROR, detail = failure.message.orEmpty())
                throw failure
            } finally {
                downloadJob.compareAndSet(job, null)
                connection.getAndSet(null)?.disconnect()
            }
        }
    }

    fun cancelDownload() {
        downloadJob.get()?.cancel(CancellationException("Download pausiert."))
        connection.getAndSet(null)?.disconnect()
    }

    /** Verify on disk, then initialize real native inference. Never loads a partial file. */
    suspend fun loadModel() = withContext(Dispatchers.IO) {
        operation.withLock { loadModelLocked() }
    }

    @OptIn(ExperimentalApi::class)
    private suspend fun loadModelLocked(threads: Int = preferredThreads(), verify: Boolean = true, allowGpuFallback: Boolean = true) {
            ensureOpen()
            if (engine != null || ggufEngine != null) {
                _state.value = readyState()
                return
            }
            try {
                if (!modelFile.isFile || modelFile.length() != model.bytes) {
                    throw LocalModelException("Bitte richte die Offline-KI zuerst in der App ein.")
                }
                if (verify) verifyChecksum(modelFile)
                ensureOpen()
                activeThreads = threads
                if (model.format == ModelFormat.GGUF) {
                    _state.value = downloadedState().copy(stage = ModelStage.LOADING, detail = "Deine Offline-KI wird geladen …")
                    val candidate = GgufEngine(modelFile.absolutePath, model.contextTokens, model.templateFallback, threads)
                    try {
                        currentCoroutineContext().ensureActive()
                        ensureOpen()
                        ggufEngine = candidate
                        loadedBackend = "CPU"
                        _state.value = readyState()
                    } catch (error: Throwable) {
                        if (ggufEngine === candidate) ggufEngine = null
                        candidate.close()
                        throw error
                    }
                    return
                }
                Engine.setNativeMinLogSeverity(LogSeverity.ERROR)
                val cache = File(appContext.cacheDir, "litertlm").apply { mkdirs() }
                // CPU is the compatible S24/Exynos baseline. GPU is only a fallback
                // if initialization rejects CPU. No NPU libraries are requested.
                var lastFailure: Exception? = null
                val backends = listOf("CPU" to Backend.CPU(threadCount = threads)) +
                    if (allowGpuFallback) listOf("GPU" to Backend.GPU()) else emptyList()
                for ((name, backend) in backends) {
                    currentCoroutineContext().ensureActive()
                    ensureOpen()
                    _state.value = downloadedState().copy(
                        stage = ModelStage.LOADING,
                        detail = if (name == "CPU") "Deine Offline-KI wird geladen …"
                        else "Die KI wird für dein Handy vorbereitet …",
                    )
                    // Needed to distinguish hitting the output cap from a completed answer.
                    // Numeric runtime counters only; no transcript or timing logs are written.
                    ExperimentalFlags.enableBenchmark = true
                    val candidate = Engine(
                        EngineConfig(
                            modelPath = modelFile.absolutePath,
                            backend = backend,
                            maxNumTokens = model.contextTokens,
                            cacheDir = cache.absolutePath,
                            // Null means text only: do not initialize vision/audio.
                            visionBackend = null,
                            audioBackend = null,
                        )
                    )
                    try {
                        candidate.initialize()
                        currentCoroutineContext().ensureActive()
                        ensureOpen()
                        tokenizer = ExactTokenizer(appContext,model.id)
                        engine = candidate
                        loadedBackend = name
                        _state.value = readyState()
                        return
                    } catch (cancelled: CancellationException) {
                        closeEngine(candidate)
                        throw cancelled
                    } catch (error: Exception) {
                        closeEngine(candidate)
                        lastFailure = error
                    } catch (error: LinkageError) {
                        closeEngine(candidate)
                        throw LocalModelException("Die KI-Laufzeit konnte auf diesem Gerät nicht gestartet werden.", error)
                    } catch (error: OutOfMemoryError) {
                        if (engine === candidate) engine = null
                        closeEngine(candidate)
                        throw error
                    }
                }
                throw LocalModelException(
                    "Die KI konnte auf deinem Handy noch nicht geladen werden. Schließe andere große Apps und versuche es erneut.",
                    lastFailure,
                )
            } catch (cancelled: CancellationException) {
                _state.value = initialState()
                throw cancelled
            } catch (error: OutOfMemoryError) {
                val failure = LocalModelException("Der freie Arbeitsspeicher reicht gerade nicht. Schließe andere Apps und lade die KI erneut.", error)
                _state.value = downloadedState().copy(stage = ModelStage.ERROR, detail = failure.message.orEmpty())
                throw failure
            } catch (error: LinkageError) {
                val failure = LocalModelException("Die KI-Laufzeit ist auf diesem Gerät nicht verfügbar.", error)
                _state.value = downloadedState().copy(stage = ModelStage.ERROR, detail = failure.message.orEmpty())
                throw failure
            } catch (error: Exception) {
                val failure = error as? LocalModelException
                    ?: LocalModelException("Die Offline-KI konnte nicht geladen werden. Bitte erneut versuchen.", error)
                _state.value = initialState().copy(stage = ModelStage.ERROR, detail = failure.message.orEmpty())
                throw failure
            }
    }

    override suspend fun countPrompt(system: String, history: List<ModelMessage>): Int = withContext(Dispatchers.IO) {
        operation.withLock { countPromptLocked(system,history) }
    }
    @OptIn(ExperimentalApi::class)
    private fun countPromptLocked(system: String, history: List<ModelMessage>): Int {
        ensureOpen()
        ggufEngine?.let { return it.countPrompt(system,history) }
        val loaded=engine ?: throw LocalModelException("Starte zuerst die Offline-KI.")
        require(history.isNotEmpty() && history.last().user)
        return LiteRtStoryTemplate.create(loaded,model.id,ConversationConfig(
            systemInstruction=Contents.of(system), initialMessages=history.dropLast(1).map { if(it.user) Message.user(it.text) else Message.model(it.text) },
            automaticToolCalling=false,prefillPrefaceOnInit=false,thinkingConfig=ThinkingConfig(false,0),
        )).use { c -> tokenizer!!.count(c.renderMessageIntoString(Message.user(history.last().text))) }
    }

    /** onToken receives only incremental answer text; no thoughts/channels are shown or stored. */
    override suspend fun generate(
        systemPrompt: String,
        history: List<ModelMessage>,
        recordTiming: Boolean,
        onToken: (String) -> Unit,
    ): String = withContext(Dispatchers.IO) {
        operation.withLock {
            val started = SystemClock.elapsedRealtime()
            var first: Long? = null
            val result = generateLocked(systemPrompt, history, { chunk ->
                if (first == null && chunk.isNotBlank()) first = SystemClock.elapsedRealtime() - started
                onToken(chunk)
            })
            if (recordTiming) firstTextMs = first
            _state.value = readyState()
            result
        }
    }

    @OptIn(ExperimentalApi::class)
    private suspend fun generateLocked(systemPrompt: String, history: List<ModelMessage>, onToken: (String) -> Unit,
                                       maxTokens: Int = model.outputTokens): String {
            ensureOpen()
            if (engine == null && ggufEngine == null) throw LocalModelException("Bitte lade zuerst die Offline-KI.")
            val messages = history.filter { it.text.isNotBlank() }
            if (messages.isEmpty() || !messages.last().user) {
                throw LocalModelException("Es fehlt eine Nachricht, auf die die Figur antworten kann.")
            }
            val plannedTokens=countPromptLocked(systemPrompt,messages)
            if(plannedTokens + maxTokens > model.contextTokens) throw LocalModelException("Die Eingabe und Antwortreserve passen nicht in das Kontextfenster.")
            ggufEngine?.let { return generateGguf(it, systemPrompt, messages, onToken, maxTokens) }
            val loaded = engine!!
            val job = currentCoroutineContext()[Job]!!
            generationJob.set(job)
            var conversation: Conversation? = null
            val finished = CompletableDeferred<Unit>()
            val stream = Channel<String>(Channel.UNLIMITED)
            var started = false
            return try {
                _state.value = readyState().copy(stage = ModelStage.GENERATING, detail = "Deine Figur schreibt …")
                val c = LiteRtStoryTemplate.create(loaded, model.id,
                    ConversationConfig(
                        systemInstruction = Contents.of(systemPrompt),
                        initialMessages = messages.dropLast(1).map {
                            if (it.user) Message.user(it.text) else Message.model(it.text)
                        },
                        samplerConfig = SamplerConfig(topK = 40, topP = 0.9, temperature = 0.75, seed = Random.nextInt(1, Int.MAX_VALUE)),
                        automaticToolCalling = false,
                        maxOutputToken = maxTokens,
                        thinkingConfig = ThinkingConfig(enableThinking = false, thinkingTokenBudget = 0),
                    )
                )
                conversation = c
                synchronized(conversationSignal) { activeConversation.set(c) }
                currentCoroutineContext().ensureActive()
                val answer = StringBuilder()
                c.sendMessageAsync(
                    text = messages.last().text,
                    callback = object : MessageCallback {
                        override fun onMessage(message: Message) {
                            // Only primary Content.Text; Message.channels (including
                            // thoughts) never reaches the UI or the saved transcript.
                            val finalText = message.contents.contents
                                .filterIsInstance<Content.Text>()
                                .joinToString("") { it.text }
                            if (finalText.isNotEmpty()) stream.trySend(finalText)
                        }
                        override fun onDone() {
                            stream.close()
                            finished.complete(Unit)
                        }
                        override fun onError(throwable: Throwable) {
                            stream.close(throwable)
                            finished.complete(Unit)
                        }
                    },
                    repetitionPenaltyConfig = RepetitionPenaltyConfig(repetitionPenalty = 1.08f, windowSize = 256),
                    maxOutputToken = maxTokens,
                    thinkingConfig = ThinkingConfig(enableThinking = false, thinkingTokenBudget = 0),
                )
                started = true
                // An unlimited channel prevents lost chunks if the UI is busy. The
                // native output limit bounds this buffer to at most 512 tokens.
                for (finalText in stream) {
                    currentCoroutineContext().ensureActive()
                    answer.append(finalText)
                    onToken(finalText)
                }
                currentCoroutineContext().ensureActive()
                if (maxTokens == model.outputTokens) {
                    StoryOutputLimit.requireCompleted(c.getBenchmarkInfo().lastDecodeTokenCount, maxTokens)
                }
                if (answer.isBlank()) {
                    throw LocalModelException("Die Figur hat noch keine Antwort erzeugt. Bitte versuche es erneut.")
                }
                _state.value = readyState()
                answer.toString().trim()
            } catch (cancelled: CancellationException) {
                _state.value = readyState().copy(detail = "Antwort angehalten. Du kannst weiterschreiben.")
                throw cancelled
            } catch (error: Exception) {
                if (!job.isActive) throw CancellationException("Antwort angehalten.", error)
                val failure = generationFailure(error)
                _state.value = readyState().copy(stage = ModelStage.ERROR, detail = failure.message.orEmpty())
                throw failure
            } catch (error: OutOfMemoryError) {
                val failure = LocalModelException("Der Arbeitsspeicher reicht gerade nicht für die Antwort. Schließe andere Apps und lade die KI erneut.", error)
                _state.value = readyState().copy(stage = ModelStage.ERROR, detail = failure.message.orEmpty())
                throw failure
            } finally {
                generationJob.compareAndSet(job, null)
                conversation?.let { c ->
                    // Coroutine cancellation alone doesn't cancel native inference.
                    // Signal it, await its terminal callback, THEN free resources.
                    // The mutex prevents load/unload racing the remaining callback.
                    withContext(NonCancellable) {
                        synchronized(conversationSignal) {
                            activeConversation.compareAndSet(c, null)
                            if (c.isAlive) runCatching { c.cancelProcess() }
                        }
                        if (started) finished.await()
                        if (c.isAlive) runCatching { c.close() }
                    }
                }
                stream.cancel()
            }
    }

    /** Runs only on-device timing samples. It never touches a story, draft or saved reply. */
    suspend fun optimizeForDevice() = withContext(Dispatchers.IO) {
        operation.withLock {
            ensureOpen()
            if (engine == null && ggufEngine == null) throw LocalModelException("Starte zuerst die KI, um ihre Geschwindigkeit zu prüfen.")
            if (loadedBackend != "CPU") throw LocalModelException("Diese KI nutzt bereits den Grafikchip. Der Prozessorvergleich ist hier nicht verfügbar.")
            val previous = preferredThreads()
            val oldDetail = performanceDetail
            val timings = linkedMapOf<Int, Long>()
            optimizing = true
            try {
                for (threads in ModelPerformance.candidates(Runtime.getRuntime().availableProcessors())) {
                    currentCoroutineContext().ensureActive()
                    val thermal = appContext.getSystemService(PowerManager::class.java)?.currentThermalStatus ?: 0
                    if (thermal >= PowerManager.THERMAL_STATUS_MODERATE) throw LocalModelException("Das Handy ist gerade zu warm für eine verlässliche Messung. Lass es abkühlen und versuche es erneut.")
                    performanceDetail = "${model.name}: Messung mit $threads Rechenthreads …"
                    releaseEngine()
                    // The current app-private file was verified immediately before this operation.
                    // Switching, downloading and deleting remain excluded by the same mutex.
                    loadModelLocked(threads, verify = false, allowGpuFallback = false)
                    generateLocked("Beantworte kurz auf Deutsch.", listOf(ModelMessage(true, "Nenne eine Farbe.")), {}, 8)
                    var sum = 0L
                    repeat(2) {
                        currentCoroutineContext().ensureActive()
                        ggufEngine?.clearContext() // Same cold prefill for every measured sample.
                        val started = SystemClock.elapsedRealtime()
                        var first: Long? = null
                        generateLocked(ModelPerformance.testSystem, listOf(ModelMessage(true, ModelPerformance.testQuestion)), { chunk ->
                            if (first == null && chunk.isNotBlank()) first = (SystemClock.elapsedRealtime() - started).coerceAtLeast(1)
                        }, 8)
                        sum += first ?: throw LocalModelException("Diese Messung hat keinen Antworttext geliefert.")
                    }
                    timings[threads] = (sum / 2).coerceAtLeast(1)
                }
                val selected = ModelPerformance.select(timings)
                releaseEngine()
                loadModelLocked(selected, verify = false)
                val resultDetail = "Gemessen auf $deviceName: " + timings.entries.joinToString(" · ") { "${it.key} Threads: ${String.format(Locale.GERMANY, "%.1f", it.value / 1000.0)} s" } +
                    ". Gespeichert: $selected Threads. Kleine Unterschiede gelten als Messschwankung; die Zeiten sind eine Momentaufnahme."
                check(selection.edit().putInt(performanceKey(), selected).putString(performanceKey() + "_result", resultDetail).commit()) { "Die Geschwindigkeitseinstellung konnte nicht gespeichert werden." }
                performanceDetail = resultDetail
            } catch (error: Throwable) {
                performanceDetail = oldDetail
                releaseEngine()
                // Restore the previously working setting, even when the timing run was cancelled.
                withContext(NonCancellable) { if (!closed.get()) runCatching { loadModelLocked(previous, verify = false) } }
                throw error
            } finally {
                optimizing = false
                _state.value = if (engine != null || ggufEngine != null) readyState() else initialState()
            }
        }
    }

    override fun cancelGeneration() {
        generationJob.get()?.cancel(CancellationException("Antwort angehalten."))
        // Native cancellation is safe to signal from a second thread, but never
        // delete the native handle here while generate() is collecting callbacks.
        synchronized(conversationSignal) {
            activeGguf.get()?.cancel()
            activeConversation.get()?.let { runCatching { it.cancelProcess() } }
        }
    }

    suspend fun unload() {
        cancelGeneration()
        withContext(Dispatchers.IO) {
            operation.withLock {
                releaseEngine()
                _state.value = initialState()
            }
        }
    }

    override suspend fun clearConversationCache() = withContext(Dispatchers.IO) {
        operation.withLock { ggufEngine?.clearContext(); Unit }
    }

    /** Non-blocking from ViewModel.onCleared(); outstanding work owns its teardown. */
    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        cancelDownload()
        cancelGeneration()
        cleanupScope.launch {
            try {
                operation.withLock { releaseEngine() }
            } finally {
                cleanupScope.cancel()
            }
        }
    }

    private suspend fun openConnection(offset: Long): HttpURLConnection {
        var url = URL(model.url)
        repeat(8) {
            currentCoroutineContext().ensureActive()
            if (url.protocol != "https" || url.userInfo != null || url.host.isBlank()) {
                throw LocalModelException("Der Download wurde wegen einer unsicheren Weiterleitung angehalten.")
            }
            val conn = url.openConnection() as HttpURLConnection
            connection.set(conn)
            conn.instanceFollowRedirects = false
            conn.connectTimeout = 25_000
            conn.readTimeout = 45_000
            conn.useCaches = false
            conn.setRequestProperty("Accept-Encoding", "identity")
            conn.setRequestProperty("Cache-Control", "no-cache")
            conn.setRequestProperty("User-Agent", "Geschichten-Android/0.1")
            conn.setRequestProperty("Range", "bytes=$offset-${model.bytes - 1}")
            val status = conn.responseCode
            if (status in setOf(301, 302, 303, 307, 308)) {
                val location = conn.getHeaderField("Location")
                connection.compareAndSet(conn, null)
                conn.disconnect()
                if (location.isNullOrBlank()) throw LocalModelException("Download-Weiterleitung fehlt.")
                url = URL(url, location)
            } else {
                return conn
            }
        }
        throw LocalModelException("Zu viele Download-Weiterleitungen. Bitte später erneut versuchen.")
    }

    private suspend fun copyDownload(conn: HttpURLConnection, offset: Long, bodyBytes: Long) {
        var received = 0L
        var lastPublish = 0L
        val buffer = ByteArray(1024 * 1024)
        FileOutputStream(partialFile, offset > 0).use { output ->
            conn.inputStream.buffered().use { input ->
                while (received < bodyBytes) {
                    currentCoroutineContext().ensureActive()
                    val wanted = minOf(buffer.size.toLong(), bodyBytes - received).toInt()
                    val count = input.read(buffer, 0, wanted)
                    if (count == -1) throw LocalModelException("Die Verbindung wurde unterbrochen. Du kannst den Download fortsetzen.")
                    output.write(buffer, 0, count)
                    received += count
                    val now = System.nanoTime()
                    if (now - lastPublish >= 100_000_000L || received == bodyBytes) {
                        publishDownload(offset + received)
                        lastPublish = now
                    }
                }
                currentCoroutineContext().ensureActive()
                if (input.read() != -1) {
                    throw LocalModelException("Der Server hat mehr Daten als erwartet geliefert. Bitte den Download erneut prüfen lassen.")
                }
            }
            output.fd.sync()
        }
        if (partialFile.length() != model.bytes) {
            throw LocalModelException("Die KI-Datei ist noch nicht vollständig. Bitte den Download fortsetzen.")
        }
    }

    private suspend fun verifyChecksum(file: File) {
        if (file.length() != model.bytes) {
            throw LocalModelException("Die KI-Datei ist unvollständig. Bitte erneut herunterladen.")
        }
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(1024 * 1024)
        var bytes = 0L
        var lastPublish = 0L
        FileInputStream(file).buffered().use { input ->
            while (true) {
                currentCoroutineContext().ensureActive()
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
                bytes += count
                val now = System.nanoTime()
                if (now - lastPublish >= 150_000_000L || bytes == model.bytes) {
                    _state.value = ModelState(
                        modelId = model.id,
                        downloadedModelIds = downloadedModelIds(),
                        stage = ModelStage.VERIFYING,
                        progress = bytes.toFloat() / model.bytes,
                        downloadedBytes = model.bytes,
                        totalBytes = model.bytes,
                        detail = "Die KI-Datei wird auf Vollständigkeit geprüft …",
                    )
                    lastPublish = now
                }
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
        if (actual != model.sha256 || bytes != model.bytes) {
            if (!file.delete()) {
                throw LocalModelException("Die KI-Datei ist beschädigt. Bitte starte die Einrichtung erneut, um sie zu ersetzen.")
            }
            throw LocalModelException("Die KI-Datei war beschädigt und wurde verworfen. Bitte lade sie erneut herunter.")
        }
    }

    private fun prepareDirectory() {
        if (!modelDirectory.isDirectory && !modelDirectory.mkdirs()) {
            throw LocalModelException("Der Speicherordner für die Offline-KI konnte nicht angelegt werden.")
        }
    }

    private fun ensureFreeSpace(remainingBytes: Long) {
        val available = StatFs(modelDirectory.absolutePath).availableBytes
        val required = remainingBytes + ModelArtifact.FREE_SPACE_RESERVE
        if (available < required) {
            val gigabytes = String.format(Locale.GERMANY, "%.1f", required / 1_000_000_000.0)
            throw LocalModelException("Bitte mache mindestens $gigabytes GB auf dem Handy frei, um die KI einzurichten.")
        }
    }

    private fun publishDownload(bytes: Long) {
        _state.value = ModelState(
            modelId = model.id,
            downloadedModelIds = downloadedModelIds(),
            stage = ModelStage.DOWNLOADING,
            progress = bytes.toFloat() / model.bytes,
            downloadedBytes = bytes,
            totalBytes = model.bytes,
            detail = "Deine Offline-KI wird heruntergeladen …",
        )
    }

    private fun initialState(detail: String? = null): ModelState {
        if (modelFile.isFile && modelFile.length() == model.bytes) return downloadedState()
        val partial = partialFile.length().coerceIn(0, model.bytes)
        return ModelState(
            modelId = model.id,
            downloadedModelIds = downloadedModelIds(),
            stage = ModelStage.MISSING,
            progress = partial.toFloat() / model.bytes,
            downloadedBytes = partial,
            totalBytes = model.bytes,
            detail = detail ?: if (partial > 0) "Du kannst den Download der Offline-KI fortsetzen."
            else "Einmal einrichten, danach ohne Internet schreiben.",
            deviceName = deviceName,
            cpuThreads = preferredThreads(),
        )
    }

    private fun downloadedState() = ModelState(
        modelId = model.id,
        downloadedModelIds = downloadedModelIds(),
        stage = ModelStage.DOWNLOADED,
        progress = 1f,
        downloadedBytes = model.bytes,
        totalBytes = model.bytes,
        detail = "Die KI ist gespeichert und kann offline geladen werden.",
        deviceName = deviceName,
        cpuThreads = if (engine != null || ggufEngine != null) activeThreads else preferredThreads(),
        optimizing = optimizing,
        performanceDetail = performanceDetail,
        firstTextMs = firstTextMs,
    )

    private fun readyState() = downloadedState().copy(
        stage = if (optimizing) ModelStage.LOADING else ModelStage.READY,
        detail = "Offline bereit · ${model.name}",
    )

    private fun ensureOpen() {
        if (closed.get()) throw CancellationException("Die App wurde geschlossen.")
    }

    private fun releaseEngine() {
        tokenizer?.close()
        tokenizer = null
        ggufEngine?.close()
        ggufEngine = null
        engine?.let { closeEngine(it) }
        engine = null
    }

    private fun closeEngine(value: Engine) {
        if (value.isInitialized()) runCatching { value.close() }
    }

    private suspend fun generateGguf(loaded: GgufEngine, systemPrompt: String, history: List<ModelMessage>, onToken: (String) -> Unit, maxTokens: Int): String {
        val job = currentCoroutineContext()[Job]!!
        generationJob.set(job)
        try {
            synchronized(conversationSignal) {
                loaded.resetCancellation()
                activeGguf.set(loaded)
            }
            currentCoroutineContext().ensureActive()
            _state.value = readyState().copy(stage = ModelStage.GENERATING, detail = "Deine Figur schreibt …")
            val answer = loaded.generate(systemPrompt, history, maxTokens, allowTokenLimit = maxTokens < model.outputTokens) { text ->
                if (job.isActive) onToken(text)
            }
            currentCoroutineContext().ensureActive()
            if (answer.isBlank()) throw LocalModelException("Die Figur hat noch keine Antwort erzeugt. Bitte versuche es erneut.")
            _state.value = readyState()
            return answer
        } catch (cancelled: CancellationException) {
            _state.value = readyState().copy(detail = "Antwort angehalten. Du kannst weiterschreiben.")
            throw cancelled
        } catch (error: Exception) {
            if (!job.isActive) {
                _state.value = readyState().copy(detail = "Antwort angehalten. Du kannst weiterschreiben.")
                throw CancellationException("Antwort angehalten.", error)
            }
            val failure = if (error is IOException) LocalModelException(error.message ?: "Die Antwort konnte nicht fertig geschrieben werden.", error)
                else generationFailure(error)
            _state.value = readyState().copy(stage = ModelStage.ERROR, detail = failure.message.orEmpty())
            throw failure
        } catch (error: OutOfMemoryError) {
            val failure = LocalModelException("Der Arbeitsspeicher reicht gerade nicht für die Antwort. Schließe andere Apps und lade die KI erneut.", error)
            _state.value = readyState().copy(stage = ModelStage.ERROR, detail = failure.message.orEmpty())
            throw failure
        } finally {
            generationJob.compareAndSet(job, null)
            synchronized(conversationSignal) { activeGguf.compareAndSet(loaded, null) }
        }
    }

    private fun downloadFailure(error: Exception): LocalModelException = when (error) {
        is LocalModelException -> error
        is SocketTimeoutException -> LocalModelException("Die Verbindung war zu langsam. Du kannst den Download fortsetzen.", error)
        else -> LocalModelException("Der Download wurde unterbrochen. Prüfe Verbindung und freien Speicher und versuche es erneut.", error)
    }

    private fun generationFailure(error: Exception): LocalModelException {
        if (error is LocalModelException) return error
        val text = error.message.orEmpty().lowercase(Locale.ROOT)
        return if (text.contains("token") && (text.contains("limit") || text.contains("exceed") || text.contains("capacity") || text.contains("max"))) {
            LocalModelException("Der Gesprächsausschnitt ist für die Offline-KI zu lang. Bitte kürze die letzte Nachricht oder einige Erinnerungen.", error)
        } else {
            LocalModelException("Die Antwort konnte nicht fertig geschrieben werden. Bitte versuche es erneut oder lade die KI neu.", error)
        }
    }
}
