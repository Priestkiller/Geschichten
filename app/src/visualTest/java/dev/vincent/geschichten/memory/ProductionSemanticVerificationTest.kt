package dev.vincent.geschichten.memory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.google.gson.JsonParser
import dev.vincent.geschichten.ai.EmbeddingModel
import dev.vincent.geschichten.data.StoryBundle
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class ProductionSemanticVerificationTest {
    @Test fun actualRecordedNativeVectorsUseTheProductionSQLitePermissionsAndRankingForAllFrozenScenes() {
        val root=File("../docs/validation/quality-search-0.8.3")
        val vectors=JsonParser.parseString(File(root,"real-vectors.json").readText()).asJsonObject
        fun vector(text:String)=vectors.getAsJsonArray(SemanticIndex.hash(text)).map {it.asFloat}.toFloatArray()
        val fixtures=JsonParser.parseString(File(root,"materialized-scenes.json").readText()).asJsonArray
        val expected=listOf("development","heldout").flatMap {split ->JsonParser.parseString(File(root,"retrieval-$split-results.json").readText()).asJsonArray.map {it.asJsonObject}}.associateBy {it["case"].asString}
        val context=ApplicationProvider.getApplicationContext<Context>();val gson=Gson()
        val differences=mutableListOf<Map<String,Any>>()
        SemanticIndex(context).use {index ->
            for(row in fixtures) {
                val case=row.asJsonObject["case"].asString
                val original=gson.fromJson(row.asJsonObject["bundle"],StoryBundle::class.java)
                var state=original.memory.copy(facts=emptyList(),knowledge=emptyList())
                original.messages.dropLast(1).forEachIndexed {i,m->state=MemoryRules.preview(original.copy(messages=original.messages.take(i+1),memory=state),m)}
                val b=original.copy(memory=state);val question=b.messages.last().text
                index.clear(b.story.id)
                val allowed=ArchiveRecall.candidates(b,question)
                // These are unchanged real CPU vectors from embeddinggemma-300M-Q8_0,
                // not a fake embedding provider. Do not invoke network/native JNI in Robolectric.
                for(e in allowed)index.put(e,b.memory.version,vector(EmbeddingModel.document(e.text)),"READY")
                val semantic=index.search(b.story.id,allowed,vector(EmbeddingModel.query(question)))
                val hybrid=HybridRecall.merge(ArchiveRecall.search(b,question),semantic)
                val variant=expected.getValue(case).getAsJsonArray("variants").first {it.asJsonObject["threshold"].asDouble==.30}.asJsonObject
                val planned=variant.getAsJsonArray("candidates").map {gson.fromJson(it,ArchiveExcerpt::class.java)}
                // Independent scalar reference over unchanged real vectors and the revised live permissions.
                val queryVector=vector(EmbeddingModel.query(question))
                val reference=allowed.map {e->
                    val v=vector(EmbeddingModel.document(e.text));var dot=0.0;var a=0.0;var z=0.0
                    for(i in v.indices){dot+=queryVector[i].toDouble()*v[i];a+=queryVector[i].toDouble()*queryVector[i];z+=v[i].toDouble()*v[i]}
                    HybridRecall.Hit(e,(dot/kotlin.math.sqrt(a*z)).toFloat())
                }.filter {it.similarity>=.30f}.sortedByDescending {it.similarity}.take(32)
                assertEquals("$case native-vector cosine ranking",reference.map {HybridRecall.key(it.excerpt)},semantic.map {HybridRecall.key(it.excerpt)})
                assertEquals(HybridRecall.merge(ArchiveRecall.search(b,question),reference).map {HybridRecall.key(it)},hybrid.map {HybridRecall.key(it)})
                differences+=mapOf("case" to case,"beforeSelected" to planned.map {HybridRecall.key(it)},"afterSelected" to hybrid.map {HybridRecall.key(it)},"permissionRevision" to "Corrected original-derived state; vectors/model/ranking unchanged")
                assertTrue(hybrid.all {ArchiveRecall.eligible(b,it,question)})
            }
        }
        FactGoldHarness.write("semantic-regression.json",differences)
    }
}
