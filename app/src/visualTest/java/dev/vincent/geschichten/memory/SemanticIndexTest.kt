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
class SemanticIndexTest {
    // These vectors test database logic only; real inference quality is measured by the host probes.
    private val vector=FloatArray(768).apply {this[0]=1f}
    private fun excerpt(story: String="a")=ArchiveRecall.sections(ChatMessage(id="source",storyId=story,role=ChatRole.USER,text="Alva bekommt in engen Räumen Panik.",createdAt=123)).single()
    private fun reset(): Context=ApplicationProvider.getApplicationContext<Context>().also {java.io.File(it.noBackupFilesDir,"semantic-index.db").delete()}
    @Test fun derivedIndexCanBeCreatedReopenedAndRebuiltWithoutTouchingStories() {
        val context=reset();val e=excerpt()
        StoryRepository(context).use {r ->val story=r.createStory("mira");val before=r.bundle(story.id)!!
            SemanticIndex(context).use {index ->index.put(e,7,vector,"READY");assertTrue(index.ready(e));assertEquals(1,index.readableDatabase.version)}
            SemanticIndex(context).use {index ->assertEquals(e,index.search("a",listOf(e),vector).single().excerpt);index.clear("a");assertFalse(index.ready(e))}
            assertEquals(before,r.bundle(story.id))
            assertEquals(10,android.database.sqlite.SQLiteDatabase.openDatabase(context.getDatabasePath("geschichten.db").path,null,android.database.sqlite.SQLiteDatabase.OPEN_READONLY).use {it.version})
        }
    }
    @Test fun exclusionsDeletedSourcesOtherStoriesAndModifiedTextAreImmediatelyIgnored() {
        val context=reset();val e=excerpt()
        SemanticIndex(context).use {index ->index.put(e,1,vector,"READY")
            assertTrue(index.search("a",emptyList(),vector).isEmpty())
            assertTrue(index.search("b",listOf(e),vector).isEmpty())
            val changed=e.copy(message=e.message.copy(text="Alva liebt weite Räume."),end=23)
            assertFalse(index.ready(changed));assertTrue(index.search("a",listOf(changed),vector).isEmpty())
        }
    }
    @Test fun incompatibleSpacesDimensionsOrPendingAndCancelledRowsAreNotRetrieved() {
        val context=reset();val e=excerpt()
        SemanticIndex(context).use {index ->
            index.put(e,1,null,"PENDING");assertFalse(index.ready(e));assertTrue(index.search("a",listOf(e),vector).isEmpty())
            index.put(e,2,vector,"READY");index.writableDatabase.execSQL("UPDATE sections SET space='different-model'")
            assertFalse(index.ready(e));assertTrue(index.search("a",listOf(e),vector).isEmpty())
            index.put(e,3,vector,"READY");index.writableDatabase.execSQL("UPDATE sections SET dimension=128")
            assertFalse(index.ready(e));assertTrue(index.search("a",listOf(e),vector).isEmpty())
            assertThrows(IllegalArgumentException::class.java){index.put(e,4,FloatArray(128),"READY")}
        }
    }
    @Test fun unchangedSourceKeepsItsVectorWhileRevisionRequiresRenewal() {
        val context=reset();val e=excerpt()
        SemanticIndex(context).use {index ->index.put(e,10,vector,"READY");assertTrue(index.ready(e));assertTrue(index.ready(e.copy(score=22)))
            assertFalse(index.ready(e.copy(message=e.message.copy(createdAt=124))))
            index.put(e,11,null,"PENDING");assertTrue(index.search("a",listOf(e),vector).isEmpty())
        }
    }
}
