package dev.vincent.geschichten.memory
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class NewScenePersistenceAuditTest {
    @Test fun allFrozenScenesPreserveOriginalsAndExposePersistedStateSeparatelyFromModelUse() {
        val root=File("../docs/validation/quality-search-0.8.3");val gson=GsonBuilder().setPrettyPrinting().create()
        val inputs=JsonParser.parseString(File(root,"materialized-scenes.json").readText()).asJsonArray
        val context=ApplicationProvider.getApplicationContext<Context>();context.deleteDatabase("geschichten.db")
        val results=mutableListOf<Map<String,Any>>()
        StoryRepository(context).use {r ->
            for(row in inputs) {
                val fixture=gson.fromJson(row.asJsonObject["bundle"],StoryBundle::class.java)
                r.upsertCharacter(fixture.character);val story=r.createStory(fixture.character.id)
                fixture.messages.drop(1).forEach {r.appendMessage(story.id,it.role,it.text)}
                while(r.backfillMemory(story.id,8)>0){}
                val saved=r.bundle(story.id)!!
                assertEquals(fixture.messages.map {it.text.trim()},saved.messages.map {it.text})
                val snapshot=MemoryRules.preview(saved,saved.messages.last())
                fun keys(facts:List<StateFact>)=facts.filter {it.status==FactStatus.CURRENT}.associate {"${it.entity.kind}:${it.entity.name}:${it.field}" to it.value}
                var interpreted=fixture.memory.copy(facts=emptyList(),knowledge=emptyList())
                fixture.messages.forEachIndexed {i,m->interpreted=MemoryRules.preview(fixture.copy(messages=fixture.messages.take(i+1),memory=interpreted),m)}
                val expected=keys(interpreted.facts);val actual=keys(snapshot.facts)
                val differences=expected.filter {(k,v)->actual[k]!=v}
                // Persistence equality is separate from independent narrative gold below.
                assertTrue("${row.asJsonObject["case"].asString}: $differences",differences.isEmpty())
                FactGoldHarness.rows("gold-regressions-reviewed.json").firstOrNull {it["case"].asString==row.asJsonObject["case"].asString}?.let {gold->assertEquals(emptyList<String>(),FactGoldHarness.issues(gold,saved.copy(memory=snapshot)))}
                results+=mapOf("case" to row.asJsonObject["case"].asString,"originalsPreserved" to true,"persistedStateMatchesAutomaticInterpretation" to true,"current" to snapshot.current,"providedCore" to MemoryPrompt.core(saved.copy(memory=snapshot),false),"pendingSources" to snapshot.pendingSources,"expectedNarrativeFacts" to row.asJsonObject["expected"].asString)
            }
        }
        File("../docs/validation/team-0.8.6/regression-output/database-scenes-audit.json").writeText(gson.toJson(results))
    }
}
