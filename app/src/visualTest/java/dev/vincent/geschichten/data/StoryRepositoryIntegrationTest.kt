package dev.vincent.geschichten.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.vincent.geschichten.ai.StoryPrompt
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Exercises the real SQLite repository under Robolectric, using a fresh application database.
 * The short messages below are authored test fixtures. No inference, VM, model, or network is used.
 * These opt-in test sources are excluded from the production APK.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class StoryRepositoryIntegrationTest {
    private lateinit var context: Context
    private lateinit var repository: StoryRepository

    @Before
    fun createFreshDatabase() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase("geschichten.db")
        repository = StoryRepository(context)
    }

    @After
    fun closeAndDeleteDatabase() {
        repository.close()
        context.deleteDatabase("geschichten.db")
    }

    @Test
    fun reopeningPreservesMessagesAndKeepsTwoStoriesAndMemoryIdsIsolated() {
        val first = repository.createStory("runa")
        val second = repository.createStory("runa")
        val firstNote = MemoryEntry(storyId = first.id, kind = MemoryKind.FACT, text = "SAPHIRRING_NUR_IN_A")
        val secondNote = MemoryEntry(storyId = second.id, kind = MemoryKind.FACT, text = "EISENSCHLUESSEL_NUR_IN_B")
        repository.upsertMemory(firstNote)
        repository.upsertMemory(secondNote)
        repository.appendMessage(first.id, ChatRole.USER, "Ich gebe Runa den Saphirring.")
        repository.appendMessage(first.id, ChatRole.CHARACTER, "Runa nimmt den Saphirring entgegen.")
        repository.updateSummary(first.id, "Runa besitzt den Saphirring.")

        repository.close()
        repository = StoryRepository(context)
        val firstReloaded = requireNotNull(repository.bundle(first.id))
        val secondReloaded = requireNotNull(repository.bundle(second.id))

        assertEquals(2, repository.stories().size)
        assertEquals(3, firstReloaded.messages.size) // Authored opening plus the two saved test messages.
        assertEquals("Runa nimmt den Saphirring entgegen.", firstReloaded.messages.last().text)
        assertTrue(firstReloaded.messages.zipWithNext().all { (a, b) -> a.createdAt < b.createdAt })
        assertEquals("Runa besitzt den Saphirring.", firstReloaded.story.summary)
        assertTrue(secondReloaded.story.summary.isEmpty())
        assertEquals(1, secondReloaded.messages.size)
        assertTrue(firstReloaded.memories.all { it.storyId == first.id })
        assertTrue(secondReloaded.memories.all { it.storyId == second.id })
        assertTrue(firstReloaded.memories.map { it.id }.toSet().intersect(secondReloaded.memories.map { it.id }.toSet()).isEmpty())
        assertTrue(StoryPrompt.system(firstReloaded).contains("SAPHIRRING_NUR_IN_A"))
        assertFalse(StoryPrompt.system(firstReloaded).contains("EISENSCHLUESSEL_NUR_IN_B"))

        assertThrows(IllegalArgumentException::class.java) {
            repository.upsertMemory(firstNote.copy(storyId = second.id, text = "Unerlaubte Verschiebung"))
        }
        assertEquals(firstNote.text, repository.bundle(first.id)!!.memories.single { it.id == firstNote.id }.text)
        assertFalse(repository.bundle(second.id)!!.memories.any { it.id == firstNote.id })
    }

    @Test
    fun editingAnOlderLocationMakesItCurrentWithAMonotonicEditTimestamp() {
        val story = repository.createStory("runa")
        val firstLocation = MemoryEntry(storyId = story.id, kind = MemoryKind.LOCATION, text = "ORT_A", createdAt = 1)
        val secondLocation = MemoryEntry(storyId = story.id, kind = MemoryKind.LOCATION, text = "ORT_B", createdAt = 2)
        repository.upsertMemory(firstLocation)
        repository.upsertMemory(secondLocation)
        val before = requireNotNull(repository.bundle(story.id))
        val savedFirst = before.memories.single { it.id == firstLocation.id }
        val savedSecond = before.memories.single { it.id == secondLocation.id }
        assertTrue(savedSecond.updatedAt > savedFirst.updatedAt)

        repository.upsertMemory(savedFirst.copy(text = "ORT_C_NACH_BEARBEITUNG"))
        val after = requireNotNull(repository.bundle(story.id))
        val edited = after.memories.single { it.id == firstLocation.id }

        assertEquals(savedFirst.createdAt, edited.createdAt)
        assertTrue(edited.updatedAt > savedSecond.updatedAt)
        assertTrue(after.memories.any { it.id == secondLocation.id && it.text == "ORT_B" })
        val prompt = StoryPrompt.system(after)
        assertTrue(prompt.contains("ORT_C_NACH_BEARBEITUNG"))
        assertFalse(prompt.contains("ORT_B"))
    }

    @Test
    fun clearingHistoryCascadesMessagesAndMemoriesAndPreservesCustomAndBuiltInFigures() {
        val custom = BuiltinCharacters.profiles.first().copy(id = "custom-clear-test", name = "Eigene Figur", custom = true)
        repository.upsertCharacter(custom)
        val figures = repository.characters()
        val first = repository.createStory("grask")
        val second = repository.createStory(custom.id)
        repository.appendMessage(first.id, ChatRole.USER, "Ich lese das Register.")
        repository.upsertMemory(MemoryEntry(storyId = second.id, kind = MemoryKind.FACT, text = "GESPEICHERTE_NOTIZ"))
        assertEquals(2, repository.clearHistory())
        assertTrue(repository.stories().isEmpty())
        assertEquals(null, repository.bundle(first.id))
        assertEquals(null, repository.bundle(second.id))
        assertEquals(figures, repository.characters())
        context.openOrCreateDatabase("geschichten.db", 0, null).use { db ->
            for (table in listOf("stories", "messages", "memories")) {
                db.rawQuery("SELECT COUNT(*) FROM $table", null).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals("No orphan rows in $table", 0, cursor.getInt(0))
                }
            }
        }
        assertEquals(0, repository.clearHistory())
        repository.close()
        repository = StoryRepository(context)
        assertTrue(repository.stories().isEmpty())
        assertEquals(figures, repository.characters())
        val newStory = repository.createStory(custom.id)
        assertEquals(custom.openingMessage, repository.bundle(newStory.id)!!.messages.single().text)
    }

    @Test
    fun failedSendRecoveryDeletesOnlyTheUnansweredUserMessage() {
        val story = repository.createStory("runa")
        repository.appendMessage(story.id, ChatRole.USER, "Wie heißt du?")
        repository.appendMessage(story.id, ChatRole.CHARACTER, "Runa.")
        val answered = requireNotNull(repository.bundle(story.id)).messages

        repository.deleteLastUserMessageIfUnanswered(story.id)
        assertEquals(answered, repository.bundle(story.id)!!.messages)

        val pending = repository.appendMessage(story.id, ChatRole.USER, "Noch unbeantwortet.")
        assertEquals(pending.id, repository.bundle(story.id)!!.messages.last().id)
        repository.deleteLastUserMessageIfUnanswered(story.id)
        assertEquals(answered, repository.bundle(story.id)!!.messages)

        repository.deleteLastUserMessageIfUnanswered(story.id)
        assertEquals(answered, repository.bundle(story.id)!!.messages)
    }
}
