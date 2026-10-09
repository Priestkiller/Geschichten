package dev.vincent.geschichten.memory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.*
import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** The original correct native draft can now commit without replacing the manual owner. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class OwnershipAdverbPersistenceTest {
    @Test fun uneditedNativeDraftCommitsAndSurvivesRestartWithManualCorrection() {
        val context=ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("geschichten.db")
        val db=context.getDatabasePath("geschichten.db");db.parentFile!!.mkdirs()
        File("../docs/validation/team-0.8.6/dialogue-2-before.db").copyTo(db,true)
        val raw=JsonParser.parseString(File("../docs/validation/team-0.8.6/raw-dialogue-2.json").readText()).asJsonArray.single().asJsonObject
        val expected=Gson().fromJson(raw.getAsJsonObject("input")["bundle"],StoryBundle::class.java)
        val reply=raw.getAsJsonArray("steps").first {it.asJsonObject["task"].asString=="narrator"}.asJsonObject["answer"].asString
        lateinit var final:StoryBundle
        StoryRepository(context).use {repo ->
            val before=repo.bundle(expected.story.id)!!
            assertEquals(expected.memory.version,before.memory.version)
            val manual=before.memory.current.single {it.field=="owner"}
            assertTrue(manual.manual);assertEquals("Oda",manual.value)
            assertTrue(repo.commitReply(before.story.id,before.memory.version,before.messages.last().id,"ownership-adverb-fixed",reply))
            final=repo.bundle(before.story.id)!!
            assertEquals(reply,final.messages.last().text)
            assertEquals(before.messages,final.messages.dropLast(1))
            assertEquals(manual,final.memory.current.single {it.field=="owner"})
            assertEquals("Du",final.memory.current.single {it.field=="holder"}.value)
        }
        StoryRepository(context).use {assertEquals(final,it.bundle(expected.story.id))}
        File("../docs/validation/helper-diagnosis-0.8.6/repaired-draft-stored.json").writeText(GsonBuilder().setPrettyPrinting().create().toJson(final))
    }
}
