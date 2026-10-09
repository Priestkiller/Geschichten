package dev.vincent.geschichten.memory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.JsonParser
import dev.vincent.geschichten.ai.EmbeddingModel
import dev.vincent.geschichten.data.StoryBundle
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** Explicit opt-in host JNI: exercises the real enabled worker, download verification,
 * batches, SQLite and timed search. Host timings never represent S24 measurements. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class NativeSemanticFlowTest {
    @Test fun enabledProductionWorkerAndTimedSearchReachTheActualOriginalSections()=runBlocking {
        assumeTrue(System.getenv("GESCHICHTEN_NATIVE_SEARCH_PROBE")=="true")
        val root=AnswerCases.root;val context=ApplicationProvider.getApplicationContext<Context>()
        val dir=File(context.noBackupFilesDir,"search-models").apply {mkdirs()}
        val model=File(dir,EmbeddingModel.FILE_NAME)
        File("../docs/validation/quality-search-0.8.3/model-files/${EmbeddingModel.FILE_NAME}").copyTo(model,overwrite=true)
        context.getSharedPreferences("semantic_search",0).edit().clear().commit()
        val rows=JsonParser.parseString(File(root,"baseline-scenes.json").readText()).asJsonArray
        val results=mutableListOf<Map<String,Any>>()
        SemanticSearch(context).use {search ->
            search.download();search.setEnabled(true)
            for(row in rows) {
                val r=row.asJsonObject;val b=AnswerCases.gson.fromJson(r["bundle"],StoryBundle::class.java)
                search.rebuild(b.story.id)
                val start=System.nanoTime();var batches=0
                do {val more=search.indexBatch(b);batches++;if(!more)break}while(batches<10)
                val indexMs=(System.nanoTime()-start)/1_000_000
                val at=System.nanoTime();val found=search.search(b);val searchMs=(System.nanoTime()-at)/1_000_000
                results+=mapOf("case" to r["case"].asString,"enabled" to search.state.value.enabled,"state" to search.state.value,"batches" to batches,"indexMs" to indexMs,"searchMs" to searchMs,"fallback" to (found==null),"selected" to found.orEmpty())
                assertTrue(found==null || found.all {ArchiveRecall.eligible(b,it,b.messages.last().text)})
            }
            search.setEnabled(false)
            val first=AnswerCases.gson.fromJson(rows[0].asJsonObject["bundle"],StoryBundle::class.java)
            assertNull(search.search(first))
        }
        File(root,"native-semantic-flow.json").writeText(AnswerCases.gson.toJson(results))
        assertTrue("Timed search must actually run instead of silently falling back",results.any {it["fallback"]==false})
    }
}
