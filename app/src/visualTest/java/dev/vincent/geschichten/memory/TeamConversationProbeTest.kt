package dev.vincent.geschichten.memory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.*
import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** Optional bounded real dialogue. A failed/unusable native round is never replaced by a gold reply. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class TeamConversationProbeTest {
    @Test fun materializeAndCommitRealTeamConversation() {
        val mode=System.getenv("GESCHICHTEN_TEAM_DIALOGUE")
        assumeTrue(mode!=null)
        val root=File("../docs/validation/team-0.8.6")
        val gson=GsonBuilder().setPrettyPrinting().create()
        val context=ApplicationProvider.getApplicationContext<Context>()
        val plan=JsonParser.parseString(File(root,"dialogue-plan-frozen.json").readText()).asJsonObject
        val round=System.getenv("GESCHICHTEN_TEAM_ROUND").toInt()
        require(round in 1..2 && mode in setOf("prepare","commit"))
        context.deleteDatabase("geschichten.db")
        val db=context.getDatabasePath("geschichten.db")
        if(mode=="commit" || round==2) {
            db.parentFile!!.mkdirs()
            File(root,if(mode=="commit")"dialogue-$round-before.db" else "dialogue-1-after.db").copyTo(db,true)
        }
        var storyId=""
        StoryRepository(context).use {repo ->
            if(mode=="prepare" && round==1) {
                val base=JsonParser.parseString(File(root,"raw-team-C.json").readText()).asJsonArray.first {it.asJsonObject["case"].asString=="dolch"}.asJsonObject
                assertTrue(base["completed"].asBoolean)
                val b=gson.fromJson(base.getAsJsonObject("input")["bundle"],StoryBundle::class.java)
                assertTrue(base.getAsJsonObject("outcome").getAsJsonArray("proposals").isEmpty)
                repo.upsertCharacter(b.character)
                val story=repo.createStory(b.character.id);storyId=story.id
                b.messages.drop(1).forEach {repo.appendMessage(story.id,it.role,it.text)}
                repeat(20){repo.backfillMemory(story.id,8)}
                val saved=repo.bundle(story.id)!!
                // Unedited genuine preceding answer; only source IDs change during reconstruction.
                repo.commitReply(story.id,saved.memory.version,saved.messages.last().id,"native-base",base["answer"].asString)
                repo.addStateFact(story.id,EntityKind.ITEM,"Dolch","owner",plan["manualOwner"].asString,true)
            } else storyId=repo.stories().single().id
            if(mode=="prepare") {
                val spec=plan.getAsJsonArray("rounds")[round-1].asJsonObject
                val msg=repo.appendMessage(storyId,ChatRole.USER,spec["input"].asString)
                val saved=repo.bundle(storyId)!!
                val b=saved.copy(memory=MemoryRules.preview(saved,msg))
                assertEquals(plan["manualOwner"].asString,b.memory.current.single {it.field=="owner"}.value)
                val row=mapOf("case" to spec["case"].asString,"bundle" to b,"recall" to ArchiveRecall.search(b,msg.text),"expected" to spec["expected"].asString)
                val out=File(root,"dialogue-$round-scenes-frozen.json");check(!out.exists());out.writeText(gson.toJson(listOf(row)))
                File(root,"dialogue-$round-stored-before.json").writeText(gson.toJson(saved))
            } else {
                val raw=JsonParser.parseString(File(root,"raw-dialogue-$round.json").readText()).asJsonArray.single().asJsonObject
                val before=repo.bundle(storyId)!!
                if(!raw["completed"].asBoolean) {
                    File(root,"dialogue-$round-blocked.json").writeText(gson.toJson(mapOf("usable" to false,"raw" to raw,"unchangedDatabase" to before)))
                    assertEquals(before,repo.bundle(storyId))
                    return@use
                }
                val judgement=JsonParser.parseString(File(root,"dialogue-$round-independent-review.json").readText()).asJsonObject
                if(!judgement["wholeReplyUsable"].asBoolean) {
                    File(root,"dialogue-$round-rejected.json").writeText(gson.toJson(mapOf("usable" to false,"judgement" to judgement,"unchangedDatabase" to before)))
                    return@use
                }
                val outcome=gson.fromJson(raw["outcome"],TeamOutcome::class.java)
                assertEquals(before.story.id,outcome.work.story)
                assertEquals(before.memory.version,outcome.work.version)
                assertTrue(repo.commitReply(storyId,before.memory.version,before.messages.last().id,outcome.work.request,outcome.reply,outcome.recall,outcome.work,outcome.proposals))
                val after=repo.bundle(storyId)!!
                assertEquals(outcome.reply,after.messages.last().text)
                assertEquals(plan["manualOwner"].asString,after.memory.current.single {it.field=="owner"}.value)
                File(root,"dialogue-$round-stored-after.json").writeText(gson.toJson(after))
                File(root,"dialogue-$round-accepted.json").writeText(gson.toJson(mapOf("usable" to true,"realAnswerUnchanged" to true,"atomicCommit" to true,"originalsPreserved" to (after.messages.dropLast(1)==before.messages))))
            }
        }
        db.copyTo(File(root,"dialogue-$round-${if(mode=="prepare")"before" else "after"}.db"),true)
        StoryRepository(context).use {repo ->assertNotNull(repo.bundle(storyId))}
    }
}
