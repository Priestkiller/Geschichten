package dev.vincent.geschichten.ui

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryTextTest {
    @Test fun everyAuthoredIntroductionKeepsItsFullVisibleTextAndHighlightsEveryDialogueInGold() {
        for (profile in dev.vincent.geschichten.data.BuiltinCharacters.profiles) {
            val formatted = storyText(profile.openingMessage, characterSpeech = true)
            assertEquals(storyText(profile.openingMessage).text, formatted.text)
            val dialogues = Regex("„[^“]+“").findAll(formatted.text).toList()
            assertTrue(dialogues.isNotEmpty())
            for (speech in dialogues) assertTrue("Gold dialogue for ${profile.id}", formatted.spanStyles.any {
                it.item.color == GoldLight && it.item.fontWeight == FontWeight.Bold &&
                    it.start <= speech.range.first && it.end > speech.range.last
            })
        }
    }

    @Test fun unmarkedGermanAndEnglishQuotesAreGoldAndBoldWhileNarrationIsItalic() {
        val original = "Grask legt das Register hin.\n\n„Lies diesen Namen.“\n\nEr wartet. \"Den hier.\""
        val formatted = storyText(original, characterSpeech = true)
        assertEquals(original, formatted.text)
        val dialogue = formatted.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }
        assertEquals(listOf("„Lies diesen Namen.“", "\"Den hier.\""), dialogue.map { formatted.text.substring(it.start, it.end) })
        assertTrue(dialogue.all { it.item.color == GoldLight && it.item.fontStyle == FontStyle.Normal })
        assertTrue(formatted.spanStyles.any { it.start == 0 && it.end == original.length && it.item.fontStyle == FontStyle.Italic && it.item.color == Muted })
    }

    @Test fun dialogueInsideMarkedActionStillHasItsOwnSpeechStyleAndParagraphsStayIntact() {
        val formatted = storyText("*Er nickt. „Danke.“*\n\n**„Weiter.“**", characterSpeech = true)
        assertEquals("Er nickt. „Danke.“\n\n„Weiter.“", formatted.text)
        assertEquals(2, formatted.spanStyles.count { it.item.fontWeight == FontWeight.Bold && it.item.color == GoldLight })
        assertFalse(formatted.text.contains('*'))
    }

    @Test fun userQuotesAndIncompleteStreamingQuotesDoNotBecomeCompletedCharacterSpeech() {
        val user = "Ich frage: „Warum?“"
        assertEquals(user, storyText(user).text)
        assertTrue(storyText(user).spanStyles.isEmpty())
        val partial = "*Grask hebt den Kopf.*\n\n„Ich brauche"
        val formatted = storyText(partial, characterSpeech = true)
        assertEquals("Grask hebt den Kopf.\n\n„Ich brauche", formatted.text)
        assertFalse(formatted.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
    }
}
