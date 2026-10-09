package dev.vincent.geschichten.memory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.JsonArray
import kotlinx.coroutines.runBlocking
import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class FactAcceptanceTest {
    @Test fun frozenNewAcceptanceUsesAutomaticInterpretationSQLiteRestartAndProductionPlanner()=runBlocking {
        val context=ApplicationProvider.getApplicationContext<Context>();context.deleteDatabase("geschichten.db")
        val rows=FactGoldHarness.rows("new-cases-frozen.json").filter {it["split"].asString=="heldout"}
        val results=mutableListOf<Map<String,Any>>();val native=JsonArray()
        val questions=mapOf("accept-gift" to "Wem gehört der Anhänger und wo liegt er jetzt?","accept-handover" to "Wem gehört das Amulett und wo liegt es?",
            "accept-body" to "Welche Verletzungen haben wir beide?","accept-healed" to "Ist meine rechte Hand noch verletzt?",
            "accept-hypothesis" to "Habe ich dir bereits etwas geschenkt oder gegeben?","accept-pickup" to "Wem gehört der Trank und wer hält ihn jetzt?",
            "accept-ambiguous" to "Habe ich dir die Schriftrolle gegeben?","accept-perfect" to "Wem gehört der Kristall und wo ist er jetzt?")
        for(row in rows) {
            val parsed=FactGoldHarness.fresh(row);lateinit var b:StoryBundle
            StoryRepository(context).use {r->
                r.upsertCharacter(parsed.character);val s=r.createStory(parsed.character.id)
                for(m in parsed.messages) {
                    val user=r.appendMessage(s.id,m.role,m.text);val saved=r.bundle(s.id)!!
                    r.commitReply(s.id,saved.memory.version,user.id,"req-${user.id}","„Verstanden.“")
                }
                r.appendMessage(s.id,ChatRole.USER,questions.getValue(row["case"].asString));b=r.bundle(s.id)!!
            }
            StoryRepository(context).use {r->assertEquals(b,r.bundle(b.story.id))}
            val interpretation=FactGoldHarness.issues(row,parsed);val storage=FactGoldHarness.issues(row,b)
            val plan=MemoryPrompt.plan(b,false,count={s,h->(s.length+h.sumOf {it.text.length})/4})
            val lines=plan.system.substringAfter("AKTUELLER STAND AUS FIGURENSICHT:")
            val provision=row.getAsJsonArray("gold").filter {!lines.contains(it.asJsonArray[3].asString) && it.asJsonArray[3].asString!="Du"}
            results+=mapOf("case" to row["case"].asString,"source" to row["source"],"gold" to row["gold"],"interpretationIssues" to interpretation,"storageIssues" to storage,"provisionIssues" to provision,"stored" to b.memory,"system" to plan.system,"history" to plan.history,"restartPreserved" to true)
            val actual=row.deepCopy();actual.add("bundle",FactGoldHarness.gson.toJsonTree(b));actual.addProperty("expected",row["gold"].toString()+"; unknown="+row["unknown"])
            native.add(actual)
        }
        FactGoldHarness.write("heldout-database-audit.json",results)
        File("../docs/validation/team-0.8.6/regression-output/heldout-scenes.json").writeText(FactGoldHarness.gson.toJson(native))
        assertTrue(results.filter {listOf("interpretationIssues","storageIssues","provisionIssues").any {k->(it[k] as List<*>).isNotEmpty()}}.toString(),results.all {listOf("interpretationIssues","storageIssues","provisionIssues").all {k->(it[k] as List<*>).isEmpty()}})
    }
}
