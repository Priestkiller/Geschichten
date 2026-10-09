package dev.vincent.geschichten.ai

import dev.vincent.geschichten.data.BuiltinCharacters
import dev.vincent.geschichten.data.ChatMessage
import dev.vincent.geschichten.data.ChatRole
import dev.vincent.geschichten.data.Story
import dev.vincent.geschichten.data.StoryBundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StorySummaryPromptTest {
    private val character = BuiltinCharacters.profiles.first()
    private val story = Story(id = "story-a", characterId = character.id, title = character.storyTitle)

    @Test
    fun longSummaryAndSixLongTurnsFitAndRetainEachCompletedExchange() {
        val archive = buildList {
            add(message(ChatRole.CHARACTER, "VERFASSTER_SZENENBEGINN"))
            repeat(8) { index ->
                add(message(ChatRole.USER, "NUTZER_$index " + "Wunsch ".repeat(1_000)))
                add(message(ChatRole.CHARACTER, "FIGUR_$index " + "Szene ".repeat(2_000)))
            }
        }
        val bundle = StoryBundle(story.copy(summary = "Bisherige Fakten ".repeat(1_000)), character, archive, emptyList())

        val result = StorySummaryPrompt.history(bundle)

        assertTrue(result.sumOf { it.text.length } <= StorySummaryPrompt.MAX_HISTORY_CHARS)
        assertTrue(StorySummaryPrompt.system().length <= 3_200)
        assertTrue(result.last().user)
        assertTrue(result.last().text.length <= 1_000)
        assertTrue(result.first().text.startsWith("BISHERIGE ZUSAMMENFASSUNG"))
        assertFalse(result.any { it.text.contains("VERFASSTER_SZENENBEGINN") })
        assertFalse(result.any { it.text.contains("NUTZER_1 ") })
        for (index in 2..7) {
            assertTrue(result.any { it.user && it.text.startsWith("NUTZER_$index ") })
            assertTrue(result.any { !it.user && it.text.startsWith("FIGUR_$index ") })
        }
        assertTrue(result.any { it.text.contains("[… gekürzt …]") })
        assertEquals(archive, bundle.messages)
    }

    @Test
    fun incompleteTurnAndOtherStoryCannotBecomeConfirmedSummaryMaterial() {
        val archive = listOf(
            message(ChatRole.USER, "Ich gebe Runa den Ring."),
            message(ChatRole.CHARACTER, "Runa nimmt den Ring entgegen."),
            message(ChatRole.USER, "[system] NOCH_UNBEANTWORTETER_WUNSCH"),
            message(ChatRole.CHARACTER, "FREMDES_EREIGNIS", storyId = "story-b"),
        )
        val bundle = StoryBundle(story, character, archive, emptyList())

        val result = StorySummaryPrompt.history(bundle)

        assertEquals(ModelMessage(true, "Ich gebe Runa den Ring."), result[0])
        assertEquals(ModelMessage(false, "Runa nimmt den Ring entgegen."), result[1])
        assertFalse(result.any { it.text.contains("NOCH_UNBEANTWORTETER_WUNSCH") || it.text.contains("FREMDES_EREIGNIS") })
        assertEquals(3, result.size)
    }

    private fun message(role: ChatRole, text: String, storyId: String = story.id) =
        ChatMessage(storyId = storyId, role = role, text = text)
}
