package dev.vincent.geschichten.memory

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class RolePersistenceTest {
    @Test fun aNegatedUserInjuryIsStoredAndProvidedAsNegatedNotAffirmative() {
        val context=ApplicationProvider.getApplicationContext<Context>();context.deleteDatabase("geschichten.db")
        StoryRepository(context).use {r ->
            val s=r.createStory("mira");r.backfillMemory(s.id)
            val user=r.appendMessage(s.id,ChatRole.USER,"Ich heiße Rian. Meine linke Hand ist nicht verletzt.")
            val before=r.bundle(s.id)!!;val preview=before.copy(memory=MemoryRules.preview(before,user))
            assertEquals("linke hand: unverletzt",preview.memory.current.first {it.field=="injury:linke hand"}.value)
            assertTrue(MemoryPrompt.core(preview,false).contains("linke hand: unverletzt"))
            assertThrows(IllegalStateException::class.java){MemoryReplyGuard.validate(preview,"„Deine linke Hand ist verletzt.“")}
            r.commitReply(s.id,before.memory.version,user.id,user.id,"„Deine linke Hand ist nicht verletzt.“")
            val saved=r.bundle(s.id)!!
            assertEquals("linke hand: unverletzt",saved.memory.current.first {it.field=="injury:linke hand"}.value)
            assertEquals(user.id,saved.memory.current.first {it.field=="injury:linke hand"}.sourceId)
        }
        context.deleteDatabase("geschichten.db")
    }
    @Test fun ownerAndCarrierStaySeparateAcrossTheAdditiveSchemaUpgrade() {
        val context=ApplicationProvider.getApplicationContext<Context>();context.deleteDatabase("geschichten.db")
        StoryRepository(context).use {r ->
            val s=r.createStory("mira");r.backfillMemory(s.id)
            val original=r.bundle(s.id)!!.messages.first().text
            val user=r.appendMessage(s.id,ChatRole.USER,"Ich heiße Rian. Der Schlüssel gehört mir. Du trägst den Schlüssel für mich.")
            r.commitReply(s.id,r.bundle(s.id)!!.memory.version,user.id,user.id,"„Ich trage den Schlüssel. Der Schlüssel gehört Rian.“")
            val b=r.bundle(s.id)!!
            assertEquals("Du",b.memory.current.first {it.field=="owner"}.value)
            assertEquals("Mira",b.memory.current.first {it.field=="holder"}.value)
            assertEquals(original,b.messages.first().text)
            assertEquals(10,android.database.sqlite.SQLiteDatabase.openDatabase(context.getDatabasePath("geschichten.db").path,null,android.database.sqlite.SQLiteDatabase.OPEN_READONLY).use {it.version})
            val id=b.memory.current.first {it.field=="holder"}.entity.id
            val next=r.appendMessage(s.id,ChatRole.USER,"Mira gibt Rian den Schlüssel.")
            r.commitReply(s.id,r.bundle(s.id)!!.memory.version,next.id,next.id,"„Jetzt hast du ihn.“")
            val after=r.bundle(s.id)!!
            assertEquals(id,after.memory.current.first {it.field=="owner"}.entity.id)
            assertEquals("Du",after.memory.current.first {it.field=="owner"}.value)
            assertEquals("Du",after.memory.current.first {it.field=="holder"}.value)
        }
        context.deleteDatabase("geschichten.db")
    }
}
