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
class RoleContaminationInvestigationTest {
    @Test fun legacyFalsePastIsRetainedInArchiveButCannotConfirmItself() {
        val context=ApplicationProvider.getApplicationContext<Context>();context.deleteDatabase("geschichten.db")
        val rows=JsonParser.parseString(File("../docs/validation/active-memory-0.8.1/models-final/huihui-qwen3-4b-answers.json").readText(Charsets.UTF_8)).asJsonArray
        val falseReply=rows.first {it.asJsonObject["case"].asString=="unknown"}.asJsonObject["answer"].asString
        StoryRepository(context).use {repo ->
            val p=CharacterProfile(id="contamination-mira",name="Mira",role="Begleitung von Rian",genre="Fantasy",traits="Aufmerksam",personality="Mira spricht klares Deutsch.",scenario="Hof",storyTitle="Hof",openingMessage="„Ich höre zu.“",custom=true)
            repo.upsertCharacter(p);val s=repo.createStory(p.id);repo.backfillMemory(s.id)
            var user=repo.appendMessage(s.id,ChatRole.USER,"Ich heiße Rian.")
            repo.commitReply(s.id,repo.bundle(s.id)!!.memory.version,user.id,user.id,"„Verstanden.“")
            user=repo.appendMessage(s.id,ChatRole.USER,"Welche Farbe hatte das Geschenk meiner Schwester, über das wir angeblich gesprochen haben?")
            // Seed the already accepted 0.8.1 legacy response; current commit must reject it.
            assertThrows(IllegalStateException::class.java){repo.commitReply(s.id,repo.bundle(s.id)!!.memory.version,user.id,user.id,falseReply)}
            repo.appendMessage(s.id,ChatRole.CHARACTER,falseReply)
            repo.backfillMemory(s.id)
            val after=repo.bundle(s.id)!!
            val falseMessage=after.messages.last()
            val hits=repo.searchArchive(s.id,"Was haben wir an einem kühlen Morgen über das Geschenk besprochen?")
            val report=mapOf("falseReply" to falseReply,"sourceId" to falseMessage.id,"processed" to (falseMessage.id in after.memory.scannedSources),"giftFacts" to after.memory.current.filter {it.value.contains("Geschenk")},"recalledWrongOriginal" to hits.any {it.message.id==falseMessage.id},"retrieved" to hits)
            val file=File("../docs/validation/team-0.8.6/regression-output/contamination-after.json");file.parentFile!!.mkdirs();file.writeText(GsonBuilder().setPrettyPrinting().create().toJson(report),Charsets.UTF_8)
            assertFalse(hits.any {it.message.id==falseMessage.id})
            assertFalse(after.memory.current.any {it.value.contains("Geschenk")})
            assertEquals(falseReply,repo.bundle(s.id)!!.messages.last().text)
            repo.appendMessage(s.id,ChatRole.USER,"Eine Frage zur Mauer.");repo.appendMessage(s.id,ChatRole.CHARACTER,falseReply)
            repo.backfillMemory(s.id)
            assertFalse(repo.searchArchive(s.id,"Geschenk an einem kühlen Morgen besprochen").any {it.message.role==ChatRole.CHARACTER && it.text.contains("Geschenk")})
            assertFalse(RoleSourcePolicy.visible(repo.bundle(s.id)!!,falseMessage).contains("kühlen Morgen"))
            repo.appendMessage(s.id,ChatRole.USER,"Ich bestätige: Wir haben über das Geschenk meiner Schwester an einem kühlen Morgen gesprochen.")
            repo.appendMessage(s.id,ChatRole.CHARACTER,"„Verstanden.“")
            assertTrue(repo.searchArchive(s.id,"Geschenk kühlen Morgen gesprochen").any {it.message.role==ChatRole.USER && it.text.contains("Ich bestätige")})
        }
        context.deleteDatabase("geschichten.db")
    }
}
