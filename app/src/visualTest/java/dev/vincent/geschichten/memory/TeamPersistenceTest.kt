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

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class TeamPersistenceTest {
    private fun setup(r:StoryRepository):StoryBundle {
        val p=CharacterProfile(id="team-enna",name="Enna",role="Begleitung",genre="Fantasy",traits="Aufmerksam",personality="Spricht freundlich",scenario="Werkstatt",storyTitle="Werkstatt",openingMessage="„Ich höre zu.“",custom=true)
        r.upsertCharacter(p);val s=r.createStory(p.id)
        val first=r.appendMessage(s.id,ChatRole.USER,"Ich heiße Silas.");r.commitReply(s.id,r.bundle(s.id)!!.memory.version,first.id,first.id,"„Verstanden.“")
        r.appendMessage(s.id,ChatRole.USER,"Meine Schwester heißt Ruth. Ruth hat dir den Kristall geschenkt.")
        return r.bundle(s.id)!!
    }
    private fun proposal(b:StoryBundle,w:TeamWork):TeamInterpretation {
        val quote="Meine Schwester heißt Ruth."
        val raw=JsonObject().apply {
            addProperty("story",w.story);addProperty("request",w.request);addProperty("version",w.version)
            add("facts",JsonArray().apply {add(JsonObject().apply {addProperty("type","family");addProperty("actor","Silas");addProperty("target","Ruth");addProperty("item","");addProperty("field","");addProperty("value","Schwester");addProperty("source","S0");addProperty("quote",quote)})});add("unknown",JsonArray())
        }
        return TeamProtocol.interpret(b,w,raw.toString())
    }
    @Test fun onlyFinalReplyAndValidatedOriginalDerivedChangesShareAtomicCommitAndRestart() {
        val context=ApplicationProvider.getApplicationContext<Context>();context.deleteDatabase("geschichten.db")
        lateinit var saved:StoryBundle
        StoryRepository(context).use {r ->
            val original=setup(r);val b=original.copy(memory=MemoryRules.preview(original,original.messages.last()));val w=TeamProtocol.work(b,"team-commit",emptyList());val result=proposal(b,w)
            assertEquals(1,result.proposals.size);assertTrue(r.bundle(b.story.id)!!.memory.current.none {it.id==result.proposals.single().id})
            val reply="„Ruth ist deine Schwester und hat mir den Kristall geschenkt.“"
            assertTrue(r.commitReply(b.story.id,original.memory.version,original.messages.last().id,w.request,reply,emptyList(),w,result.proposals))
            saved=r.bundle(b.story.id)!!;assertTrue(saved.memory.current.any {it.value=="Ruth ist Schwester von Silas." && it.sourceId==original.messages.last().id})
            assertFalse(r.commitReply(b.story.id,original.memory.version,original.messages.last().id,w.request,reply,emptyList(),w,result.proposals))
            assertEquals(original.messages.map {it.text},saved.messages.dropLast(1).map {it.text})
        }
        StoryRepository(context).use {assertEquals(saved,it.bundle(saved.story.id))}
    }
    @Test fun forgedChangesAndLateResultsCannotOverwriteManualCorrectionOrPersistDrafts() {
        val context=ApplicationProvider.getApplicationContext<Context>();context.deleteDatabase("geschichten.db")
        StoryRepository(context).use {r ->
            val original=setup(r);val b=original.copy(memory=MemoryRules.preview(original,original.messages.last()));val w=TeamProtocol.work(b,"late",emptyList());val result=proposal(b,w)
            val forged=result.proposals.map {it.copy(value="Ruth ist Schwester von Enna.")}
            assertThrows(IllegalArgumentException::class.java){r.commitReply(b.story.id,original.memory.version,original.messages.last().id,w.request,"„Ich höre zu.“",emptyList(),w,forged)}
            assertEquals(original,r.bundle(b.story.id))
            r.addStateFact(b.story.id,EntityKind.ITEM,"Kristall","owner","Uta",true)
            val manual=r.bundle(b.story.id)!!
            assertThrows(IllegalStateException::class.java){r.commitReply(b.story.id,original.memory.version,original.messages.last().id,w.request,"„Ich höre zu.“",emptyList(),w,result.proposals)}
            assertEquals(manual,r.bundle(b.story.id));assertTrue(manual.memory.current.any {it.manual && it.value=="Uta"})
        }
    }
}
