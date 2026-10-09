package dev.vincent.geschichten.memory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.GsonBuilder
import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** Real repository fixtures for the independent native model probe, not fake inference. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class ActiveMemoryModelFixturesTest {
    @Test fun exportStoryScopedFullArchiveAndImmediateCorrectionFixtures() {
        val context=ApplicationProvider.getApplicationContext<Context>();context.deleteDatabase("geschichten.db")
        StoryRepository(context).use {r ->
            val p=CharacterProfile(id="fixture-mira",name="Mira",role="Gefährtin von Rian",genre="Fantasy",traits="Praktisch, herzlich, vorsichtig",personality="Mira ist 27 Jahre alt und spricht klares Deutsch. Sie ist praktisch, herzlich und vorsichtig. Rian ist ihre Reisebegleitung.",scenario="Mira und Rian sind im Hof.",storyTitle="Der Brunnen",openingMessage="*Mira sieht Rian an.* „Womit beginnen wir?“",custom=true)
            r.upsertCharacter(p);val s=r.createStory(p.id);r.backfillMemory(s.id)
            fun turn(input:String,reply:String="*Mira nickt.* „Verstanden.“") {
                val m=r.appendMessage(s.id,ChatRole.USER,input);r.commitReply(s.id,r.bundle(s.id)!!.memory.version,m.id,m.id,reply)
            }
            turn("Ich heiße Rian. Mira hat den roten Schlüssel. Wir sind im Hof.")
            val original="Vorspann. ".repeat(500)+"Das Losungswort am Brunnen lautet Morgenstern. Ich gebe dir am Brunnen den Brief, weil ich mit meiner verletzten Hand das Siegel nicht unbeschädigt öffnen kann. "+"Nachspann. ".repeat(500)
            turn(original)
            repeat(12){i ->turn("Wir betrachten Mauer Nummer $i.","*Mira betrachtet die Mauer $i.* "+"Ein unauffälliger Stein liegt da. ".repeat(30))}
            val rows=mutableListOf<Map<String,Any>>()
            fun capture(label:String,input:String) {
                r.appendMessage(s.id,ChatRole.USER,input);val b=r.bundle(s.id)!!
                rows+=mapOf("case" to label,"bundle" to b.copy(memory=MemoryRules.preview(b,b.messages.last())))
                r.deleteLastUserMessageIfUnanswered(s.id)
            }
            capture("archive","Wie lautet das Losungswort am Brunnen und warum habe ich dir den Brief anvertraut?")
            val color=r.bundle(s.id)!!.memory.current.first {it.field=="color"}
            r.editStateFact(s.id,color.id,"silbern",true)
            capture("correction","Der Schlüssel ist jetzt blau. Welche Farbe hat der Schlüssel jetzt und wer hat ihn?")
            assertEquals("silbern",r.bundle(s.id)!!.memory.current.first {it.field=="color"}.value)
            capture("unknown","Welche Farbe hatte das Geschenk meiner Schwester, über das wir angeblich gesprochen haben? Wenn wir nichts davon wissen, frag bitte nach.")
            val file=File("../docs/validation/active-memory-0.8.1/model-cases.json");file.parentFile.mkdirs()
            file.writeText(GsonBuilder().setPrettyPrinting().create().toJson(rows),Charsets.UTF_8)
            assertEquals(3,rows.size)
            assertTrue(r.searchArchive(s.id,"Losungswort Brunnen Brief").any {it.text.contains("Morgenstern")})
            assertEquals("Mira",r.bundle(s.id)!!.memory.current.first {it.entity.name=="Brief" && it.field=="holder"}.value)
        }
        context.deleteDatabase("geschichten.db")
    }
}
