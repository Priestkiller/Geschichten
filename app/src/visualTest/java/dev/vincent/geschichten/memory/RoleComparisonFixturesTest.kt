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

/** Manually reviewed named event references are experimental controls, not extraction. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class RoleComparisonFixturesTest {
    @Test fun exportFrozen081AndNewNamesWithManualReferences() {
        val output=File("../docs/validation/roles-0.8.2/controlled-inputs.json")
        if(output.exists()) {
            assertEquals(5,JsonParser.parseString(output.readText(Charsets.UTF_8)).asJsonArray.size())
            return
        }
        val gson=GsonBuilder().setPrettyPrinting().create()
        val original=JsonParser.parseString(File("../docs/validation/active-memory-0.8.1/model-cases-final.json").readText(Charsets.UTF_8)).asJsonArray
        val rows=mutableListOf<Map<String,Any>>()
        val anchor=gson.fromJson(original.first {it.asJsonObject["case"].asString=="archive"}.asJsonObject["bundle"],StoryBundle::class.java)
        rows+=mapOf("case" to "letter-rian-mira","bundle" to anchor,"manualEvent" to "Vergangenheit: Rian übergab Mira den Brief am Brunnen. Handelnde Person: Rian. Gegenstand: Brief. Empfängerin: Mira. Rians Hand war verletzt. Grund der Übergabe: Rian konnte mit der verletzten Hand das Siegel nicht unbeschädigt öffnen. Mira erhielt den Brief und kennt diesen Grund. Losungswort am Brunnen: Morgenstern.","expected" to "Morgenstern; Rian gab Mira den Brief, weil Rians Hand verletzt war; Mira antwortet Rian mit du/deine statt ich/meine.","repeat" to true)
        val context=ApplicationProvider.getApplicationContext<Context>();context.deleteDatabase("geschichten.db")
        StoryRepository(context).use {repo ->
            fun fixture(label:String,char:String,player:String,event:String,question:String,reference:String,expected:String,role:ChatRole=ChatRole.USER) {
                val p=CharacterProfile(id="role-$label",name=char,role="Reisebegleitung von $player",genre="Fantasy",traits="Praktisch, aufmerksam",personality="$char ist 29 Jahre alt, spricht klares Deutsch und hört $player aufmerksam zu.",scenario="$char und $player sind im Innenhof.",storyTitle="Innenhof",openingMessage="*$char sieht $player an.* „Ich höre zu.“",custom=true)
                repo.upsertCharacter(p);val s=repo.createStory(p.id);repo.backfillMemory(s.id)
                fun turn(user:String,reply:String="*$char nickt.* „Verstanden.“") {val m=repo.appendMessage(s.id,ChatRole.USER,user);repo.commitReply(s.id,repo.bundle(s.id)!!.memory.version,m.id,m.id,reply)}
                turn("Ich heiße $player. Wir sind im Innenhof.")
                if(role==ChatRole.USER) turn(event) else turn("Was tust du?",event)
                repeat(12){i ->turn("Wir betrachten Stein Nummer $i.","*$char betrachtet Stein $i.* "+"Ein unauffälliger Stein liegt da. ".repeat(30))}
                repo.appendMessage(s.id,ChatRole.USER,question)
                val b=repo.bundle(s.id)!!
                rows+=mapOf("case" to label,"bundle" to b.copy(memory=MemoryRules.preview(b,b.messages.last())),"manualEvent" to reference,"expected" to expected,"repeat" to false)
            }
            fixture("reverse-kora-levin","Kora","Levin","*Kora gibt Levin das Amulett.* „Ich gebe dir das Amulett, weil meine rechte Schulter verletzt ist und ich es nicht tragen kann.“","Warum hast du mir das Amulett gegeben und wessen rechte Schulter ist verletzt?","Vergangenheit: Kora gab Levin das Amulett. Handelnde Person: Kora. Empfänger: Levin. Koras rechte Schulter ist verletzt. Kora konnte das Amulett deshalb nicht tragen. Levin hat es jetzt. Kora kennt ihre eigene Handlung und Verletzung.","Kora gab Levin das Amulett; Koras Schulter verletzt, nicht Levins.",ChatRole.CHARACTER)
            fixture("injury-juna-tarek","Tarek","Juna","Meine linke Hand ist verletzt. Deine rechte Schulter ist unverletzt. Ich kann die schwere Tür deshalb nicht öffnen.","Wessen Hand ist verletzt und warum kann ich die Tür nicht öffnen?","Vergangenheit und fortbestehender Zustand: Junas linke Hand ist verletzt. Tareks rechte Schulter ist unverletzt. Juna kann wegen Junas verletzter Hand die schwere Tür nicht öffnen. Tarek hat diese Angaben von Juna erfahren.","Juna/deine Hand verletzt; Tarek übernimmt Junas Verletzung nicht.")
            fixture("family-niko-selma","Selma","Niko","Meine Schwester heißt Alma. Dein Bruder heißt Aron. Meine Schwester Alma hat dir eine Karte geschickt.","Wie heißen meine Schwester und dein Bruder, und wer hat die Karte geschickt?","Familienzuordnung: Alma ist Nikos Schwester. Aron ist Selmas Bruder. Vergangenes Ereignis: Alma schickte Selma die Karte. Senderin: Alma. Empfängerin: Selma. Selma kennt diese Angaben durch Niko.","Nikos Schwester Alma, Selmas Bruder Aron, Senderin Alma an Selma.")
            fixture("owner-malik-daria","Daria","Malik","Das Amulett gehört mir. Du trägst das Amulett für mich.","Wer trägt das Amulett gerade und wem gehört es?","Aktueller Stand: Daria trägt das Amulett für Malik. Trägerin: Daria. Eigentümer: Malik. Tragen und Eigentum sind zwei verschiedene Beziehungen. Daria kennt beide Angaben von Malik.","Daria/ich trägt; Malik/dir gehört. Kein Eigentumswechsel durch Tragen.")
        }
        context.deleteDatabase("geschichten.db")
        output.parentFile!!.mkdirs();output.writeText(gson.toJson(rows),Charsets.UTF_8)
        assertEquals(5,rows.size)
    }
}
