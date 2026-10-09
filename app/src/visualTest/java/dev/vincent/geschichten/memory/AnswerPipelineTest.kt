package dev.vincent.geschichten.memory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.*
import dev.vincent.geschichten.ai.EmbeddingModel
import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** Real SQLite, production eligibility/index/ranking; recorded native embeddings, no fake vectors. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class AnswerPipelineTest {
    @Test fun compareWholeRetrievalPipelineOnIdenticalAutomaticallyStoredScenes() {
        val root=File("../docs/validation/team-0.8.6/legacy-pipeline")
        val phase=File(root,"phase.txt").takeIf {it.exists()}?.readText()?.trim() ?: "baseline"
        val context=ApplicationProvider.getApplicationContext<Context>();context.deleteDatabase("geschichten.db")
        val gson=GsonBuilder().setPrettyPrinting().create()
        val cache=File(root,"real-vectors.json").takeIf {it.exists()}?.let {JsonParser.parseString(it.readText()).asJsonObject}
        val baseline=if(phase=="final")JsonParser.parseString(File(root,"baseline-scenes.json").readText()).asJsonArray.map {it.asJsonObject}.associateBy {it["case"].asString} else emptyMap()
        val scenes=mutableListOf<JsonObject>();val traces=mutableListOf<Map<String,Any>>()
        val lexicalRows=mutableListOf<Map<String,Any>>();val hybridRows=mutableListOf<Map<String,Any>>()
        val embedInputs=linkedSetOf<String>()
        StoryRepository(context).use {repo -> SemanticIndex(context).use {index ->
            for(v in JsonParser.parseString(File(root,"comparison-frozen.json").readText()).asJsonArray) {
                val row=v.asJsonObject;val original=gson.fromJson(row["bundle"],StoryBundle::class.java)
                repo.upsertCharacter(original.character);val s=repo.createStory(original.character.id)
                for(m in original.messages.drop(1))repo.appendMessage(s.id,m.role,m.text)
                repeat(40) {repo.backfillMemory(s.id,8)}
                val stored=repo.bundle(s.id)!!
                val fresh=stored.copy(memory=MemoryRules.preview(stored,stored.messages.last()))
                // Reuse exactly the baseline SQLite snapshot for the answer comparison,
                // including IDs/timestamps. Independently verify fresh storage equivalence.
                val b=baseline[row["case"].asString]?.let {gson.fromJson(it["bundle"],StoryBundle::class.java)} ?: fresh
                fun state(values:List<StateFact>)=values.map {listOf(it.entity.kind.name,it.entity.name,it.field,it.value,it.status.name)}.toSet()
                assertEquals(state(b.memory.forCharacter()),state(fresh.memory.forCharacter()))
                val question=b.messages.last().text;val allowed=ArchiveRecall.candidates(b,question)
                embedInputs+=EmbeddingModel.query(question);allowed.forEach {embedInputs+=EmbeddingModel.document(it.text)}
                val lexical=ArchiveRecall.search(b,question)
                fun vector(text:String)=cache?.getAsJsonArray(SemanticIndex.hash(text))?.map {it.asFloat}?.toFloatArray()
                val missing=allowed.filter {vector(EmbeddingModel.document(it.text))==null}
                val q=vector(EmbeddingModel.query(question))
                index.clear(b.story.id)
                allowed.forEach {e ->vector(EmbeddingModel.document(e.text))?.let {index.put(e,b.memory.version,it,"READY")}}
                val semantic=if(q==null) emptyList() else index.search(b.story.id,allowed,q)
                if(cache!=null)assertTrue("${row["case"]}: actual semantic hits required for this comparison",semantic.isNotEmpty())
                val hybrid=HybridRecall.merge(lexical,semantic)
                assertTrue(hybrid.all {ArchiveRecall.eligible(b,it,question)})
                val actual=row.deepCopy();actual.add("bundle",gson.toJsonTree(b));scenes+=actual
                traces+=mapOf("case" to row["case"].asString,"requiredSpans" to row["requiredSpans"],"allowed" to allowed,"missingIndexedSections" to missing,"missingQuery" to (q==null),"semanticHits" to semantic,"lexical" to lexical,"hybrid" to hybrid,"restarted" to (StoryRepository(context).use {it.bundle(s.id)}==stored),"sameBaselineState" to (phase=="final"),"freshStorageEquivalent" to true)
                lexicalRows+=mapOf("case" to row["case"].asString,"variants" to listOf(mapOf("candidates" to lexical)))
                hybridRows+=mapOf("case" to row["case"].asString,"variants" to listOf(mapOf("candidates" to hybrid)))
            }
        }}
        fun write(name:String,value:Any) {File(root,name).writeText(gson.toJson(value))}
        if(cache==null)write("embed-inputs.json",embedInputs.toList()) else {
            assertTrue(traces.all {(it["missingIndexedSections"] as List<*>).isEmpty() && it["missingQuery"]==false})
            write("$phase-scenes.json",scenes);write("$phase-retrieval-trace.json",traces)
            write("$phase-lexical.json",lexicalRows);write("$phase-hybrid.json",hybridRows)
        }
    }
}
