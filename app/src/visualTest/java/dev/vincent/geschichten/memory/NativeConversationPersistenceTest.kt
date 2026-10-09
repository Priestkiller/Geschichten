package dev.vincent.geschichten.memory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.JsonParser
import dev.vincent.geschichten.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** Bounded real model -> atomic SQLite commit -> user's new action -> actual next prompt. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class NativeConversationPersistenceTest {
    @Test fun materializeNextConversationRoundFromUneditedNativeReplies()=runBlocking {
        assumeTrue(System.getenv("GESCHICHTEN_CONVERSATION_ROUND")!=null)
        val round=System.getenv("GESCHICHTEN_CONVERSATION_ROUND").toInt();require(round in 1..3)
        val root=AnswerCases.root;val gson=AnswerCases.gson
        val spec=JsonParser.parseString(File(root,"conversation-frozen.json").readText()).asJsonObject
        val source=JsonParser.parseString(File(root,"new-scenes-frozen.json").readText()).asJsonArray.first {it.asJsonObject["case"].asString==spec["baseCase"].asString}.asJsonObject
        val b=gson.fromJson(source["bundle"],StoryBundle::class.java)
        val initial=JsonParser.parseString(File(root,"raw-new-free-qwen3-official-4b.json").readText()).asJsonArray.first {it.asJsonObject["case"].asString==spec["baseCase"].asString}.asJsonObject
        assertTrue(initial["completed"].asBoolean);assertTrue(initial["guardAccepted"].asBoolean)
        val context=ApplicationProvider.getApplicationContext<Context>();context.deleteDatabase("geschichten.db")
        StoryRepository(context).use {r ->
            r.upsertCharacter(b.character);val s=r.createStory(b.character.id)
            b.messages.drop(1).forEach {r.appendMessage(s.id,it.role,it.text)}
            var saved=r.bundle(s.id)!!
            r.commitReply(s.id,saved.memory.version,saved.messages.last().id,"first",initial["answer"].asString)
            if(round>=2) {
                val firstRound=spec.getAsJsonArray("rounds")[0].asJsonObject
                val m=r.appendMessage(s.id,ChatRole.USER,firstRound["input"].asString)
                val raw=JsonParser.parseString(File(root,"raw-conversation-1-qwen3-official-4b.json").readText()).asJsonArray.single().asJsonObject
                assertTrue(raw["completed"].asBoolean)
                if(!raw["guardAccepted"].asBoolean) {
                    saved=r.bundle(s.id)!!
                    val provisional=saved.copy(memory=MemoryRules.preview(saved,m))
                    for((field,v) in firstRound.getAsJsonObject("gold").entrySet())assertEquals(v.asString,provisional.memory.current.single {it.field==field}.value)
                    assertThrows(IllegalStateException::class.java) {r.commitReply(s.id,saved.memory.version,m.id,"blocked",raw["answer"].asString)}
                    assertEquals(saved,r.bundle(s.id))
                    assertFalse(r.bundle(s.id)!!.messages.any {it.role==ChatRole.CHARACTER && it.text==raw["answer"].asString})
                    File(root,"conversation-blocked-database.json").writeText(gson.toJson(mapOf("successfulAnswer" to false,"badReplyPersisted" to false,"existingStatePreserved" to true,"pendingOriginalPreserved" to true,"rawReply" to raw,"provisionalInput" to provisional,"storedAfterRejectedCommit" to r.bundle(s.id))))
                    return@runBlocking
                }
                saved=r.bundle(s.id)!!;r.commitReply(s.id,saved.memory.version,m.id,"second",raw["answer"].asString)
            }
            if(round==3) {
                val finalRound=spec.getAsJsonArray("rounds")[1].asJsonObject
                val m=r.appendMessage(s.id,ChatRole.USER,finalRound["input"].asString)
                val raw=JsonParser.parseString(File(root,"raw-conversation-2-qwen3-official-4b.json").readText()).asJsonArray.single().asJsonObject
                assertTrue(raw["completed"].asBoolean);assertTrue(raw["guardAccepted"].asBoolean)
                saved=r.bundle(s.id)!!;r.commitReply(s.id,saved.memory.version,m.id,"third",raw["answer"].asString)
                val final=r.bundle(s.id)!!
                for((field,v) in finalRound.getAsJsonObject("gold").entrySet())assertEquals(v.asString,final.memory.current.single {it.field==field}.value)
                assertEquals(final,StoryRepository(context).use {it.bundle(s.id)})
                File(root,"conversation-final-database.json").writeText(gson.toJson(final))
                return@runBlocking
            }
            val next=spec.getAsJsonArray("rounds")[round-1].asJsonObject
            val m=r.appendMessage(s.id,ChatRole.USER,next["input"].asString)
            saved=r.bundle(s.id)!!
            val input=saved.copy(memory=MemoryRules.preview(saved,m))
            for((field,v) in next.getAsJsonObject("gold").entrySet())assertEquals(v.asString,input.memory.current.single {it.field==field}.value)
            assertEquals(saved,StoryRepository(context).use {it.bundle(s.id)})
            val plan=MemoryPrompt.plan(input,false,count={a,h->(a.length+h.sumOf {it.text.length})/4})
            val audit=mapOf("case" to next["case"].asString,"bundle" to input,"expected" to next["gold"].toString(),"committedUneditedNativeReplies" to round,"history" to plan.history,"system" to plan.system)
            val output=File(root,"conversation-$round-scenes-frozen.json");check(!output.exists()){ "Frozen conversation input already exists" };output.writeText(gson.toJson(listOf(audit)))
        }
    }
}
