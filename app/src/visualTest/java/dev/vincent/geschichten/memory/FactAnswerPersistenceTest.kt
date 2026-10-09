package dev.vincent.geschichten.memory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class FactAnswerPersistenceTest {
    private val context get()=ApplicationProvider.getApplicationContext<Context>()
    @Test fun frozenAnswersUseRealStorageRestartAndAtomicCommit() {
        context.deleteDatabase("geschichten.db");val audit=mutableListOf<Map<String,Any>>();val scenes=mutableListOf<Map<String,Any>>()
        for(row in AnswerCases.rows()) {
            val profile=AnswerCases.empty(row).character
            val story=StoryRepository(context).use {r ->
                r.upsertCharacter(profile);val s=r.createStory(profile.id)
                fun turn(text:String) {val m=r.appendMessage(s.id,ChatRole.USER,text);r.commitReply(s.id,r.bundle(s.id)!!.memory.version,m.id,m.id,"„Verstanden.“")}
                turn("Ich heiße ${row["player"].asString}.");turn(row["source"].asString)
                if(row["case"].asString=="new-last-known")turn("Ich lege den Kristall heimlich auf den Tisch.")
                r.appendMessage(s.id,ChatRole.USER,row["question"].asString);s
            }
            StoryRepository(context).use {r ->
                val b=r.bundle(story.id)!!;val provisional=b.copy(memory=MemoryRules.preview(b,b.messages.last()))
                val answer=FactAnswers.resolve(provisional)
                val active=row["activate"].asBoolean
                val correct=if(active)answer?.text==row["expected"].asString else answer==null
                var guard=true
                if(answer!=null) {
                    try {MemoryReplyGuard.validate(provisional,answer.text)}catch(e:Exception){guard=false}
                    assertTrue("${row["case"]} guard",guard)
                    r.commitReply(story.id,b.memory.version,b.messages.last().id,"facts-${b.messages.last().id}",answer.text)
                    assertEquals(answer.text,r.bundle(story.id)!!.messages.last().text)
                }
                audit+=mapOf("case" to row["case"].asString,"question" to row["question"].asString,"expected" to row["expected"].asString,"storage" to b.memory,"actualAnswerInput" to (answer?.facts.orEmpty()),"actualAnswer" to (answer?.text ?: "fallback"),"activationCorrect" to correct,"guardAccepted" to guard,"committed" to (answer!=null))
                if(active)scenes+=mapOf("case" to row["case"].asString,"expected" to row["expected"].asString,"bundle" to provisional)
            }
        }
        File(AnswerCases.root,"new-database-audit.json").writeText(AnswerCases.gson.toJson(audit))
        File(AnswerCases.root,"new-scenes.json").writeText(AnswerCases.gson.toJson(scenes))
        assertTrue(audit.filter {it["activationCorrect"]!=true}.toString(),audit.all {it["activationCorrect"]==true})
    }
    @Test fun ongoingConversationUsesLatestPlacementAndOwnerAcrossCorrectionsAndStories() {
        context.deleteDatabase("geschichten.db")
        StoryRepository(context).use {r ->
            val row=AnswerCases.rows().first();val p=AnswerCases.empty(row).character;r.upsertCharacter(p)
            val s=r.createStory(p.id);val other=r.createStory(p.id)
            fun turn(text:String) {val m=r.appendMessage(s.id,ChatRole.USER,text);r.commitReply(s.id,r.bundle(s.id)!!.memory.version,m.id,m.id,"„Verstanden.“")}
            fun ask(text:String):String {val m=r.appendMessage(s.id,ChatRole.USER,text);val b=r.bundle(s.id)!!;val a=checkNotNull(FactAnswers.resolve(b.copy(memory=MemoryRules.preview(b,m))));r.commitReply(s.id,b.memory.version,m.id,m.id,a.text);return a.text}
            turn("Ich heiße Bennet.");turn("Der Dolch gehört mir. Ich lege den Dolch auf die Bank.")
            assertEquals("„Der Dolch liegt auf der Bank.“",ask("Wo liegt der Dolch jetzt?"))
            turn("Du nimmst den Dolch von der Bank auf.")
            assertEquals("„Ich halte den Dolch.“",ask("Wer hält den Dolch jetzt?"))
            turn("Du legst den Dolch unter das Regal.")
            assertEquals("„Der Dolch liegt unter dem Regal.“",ask("Wo liegt der Dolch jetzt?"))
            val owner=r.bundle(s.id)!!.memory.current.single {it.field=="owner"}
            r.editStateFact(s.id,owner.id,"Liora",false,false,true)
            assertEquals("„Der Dolch gehört mir.“",ask("Wem gehört der Dolch?"))
            assertNull(FactAnswers.resolve(r.bundle(other.id)!!,"Wem gehört der Dolch?"))
            val before=r.bundle(s.id)!!;val m=r.appendMessage(s.id,ChatRole.USER,"Wer hält den Dolch jetzt?")
            r.deleteLastUserMessageIfUnanswered(s.id)
            assertEquals(before.messages,r.bundle(s.id)!!.messages)
            assertTrue(r.bundle(s.id)!!.memory.current.any {it.manual && it.value=="Liora"})
            File(AnswerCases.root,"ongoing-conversation.json").writeText(AnswerCases.gson.toJson(r.bundle(s.id)))
        }
    }
}
