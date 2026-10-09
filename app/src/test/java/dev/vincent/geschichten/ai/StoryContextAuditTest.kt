package dev.vincent.geschichten.ai

import dev.vincent.geschichten.data.*
import org.junit.Assert.*
import org.junit.Test

class StoryContextAuditTest {
    private val initial = "Rian und Mira sind im verschlossenen Turmzimmer. Mira trägt den bronzenen Schlüssel am Gürtel. Rians linke Hand ist verletzt. Ziel: den Turm verlassen."
    private val character = BuiltinCharacters.profiles.single { it.id == "mira" }.copy(
        id = "audit-mira", role = "Gefährtin", genre = "Fantasy", personality = "Mira ist herzlich und praktisch.",
        scenario = initial, openingMessage = "*Mira berührt ihren Schlüssel.* „Wie geht es deiner Hand, Rian?“", custom = true,
    )
    private val story = Story(id = "audit-story", characterId = character.id, title = "Turm", startContext = initial)
    private fun message(role: ChatRole, text: String, id: String) = ChatMessage(id = id, storyId = story.id, role = role, text = text)
    private fun bundle(messages: List<ChatMessage>, summary: String = "") = StoryBundle(story.copy(summary = summary), character, messages, emptyList())

    @Test fun droppedUnsummarizedKeyTransferTriggersAnEarlyCheckpointBeforeSixTurns() {
        val archive = buildList {
            add(message(ChatRole.CHARACTER, character.openingMessage, "opening"))
            add(message(ChatRole.USER, "Mira gibt Rian den Schlüssel. Rian hat ihn jetzt.", "key-transfer"))
            add(message(ChatRole.CHARACTER, "„Du hast ihn, Rian.“", "key-answer"))
            repeat(2) {
                add(message(ChatRole.USER, "Eine Wandfrage. ".repeat(45), "u-$it"))
                add(message(ChatRole.CHARACTER, "*Mira betrachtet die Wand.* ".repeat(50), "a-$it"))
            }
            add(message(ChatRole.USER, "Wer hat den Schlüssel?", "pending"))
        }
        val saved = bundle(archive)
        val window = StoryPrompt.window(saved)
        assertTrue(window.omittedMessageIds.contains("key-transfer"))
        assertEquals("character-budget", window.stopReason)
        assertEquals(3, StorySummaryPlan.nextCheckpoint(saved, 0, beforeReply = true))
        assertNull(StorySummaryPlan.nextCheckpoint(saved, 3, beforeReply = true))
        assertNull(StorySummaryPlan.nextCheckpoint(saved, 0, beforeReply = false))
        assertEquals(archive, saved.messages)
        assertEquals(1, window.messages.count { it.text == "Wer hat den Schlüssel?" })
    }

    @Test fun normalCheckpointCountsOnlyCompletedTurnsAndDoesNotSkipIntervals() {
        val completed = (0..12).flatMap { listOf(message(ChatRole.USER, "Frage $it", "u-$it"), message(ChatRole.CHARACTER, "Antwort $it", "a-$it")) }
        assertEquals(6, StorySummaryPlan.nextCheckpoint(bundle(completed), 0, false))
        assertEquals(12, StorySummaryPlan.nextCheckpoint(bundle(completed), 6, false))
        assertNull(StorySummaryPlan.nextCheckpoint(bundle(completed), 12, false))
        val incomplete = completed.take(10) + message(ChatRole.USER, "Offen", "pending")
        assertNull(StorySummaryPlan.nextCheckpoint(bundle(incomplete), 0, false))
    }

    @Test fun summaryKeepsMiddleOfPlayerMessageAheadOfVerboseCharacterReplies() {
        val critical = "DER_SCHLUESSEL_LIEGT_JETZT_IN_DER_TRUHE."
        val archive = (0..5).flatMap {
            listOf(message(ChatRole.USER, if (it == 0) "Einleitende Beobachtungen. ".repeat(16) + critical + " Weitere Beobachtungen. ".repeat(16) else "Wir sprechen weiter.", "u-$it"),
                message(ChatRole.CHARACTER, "*Mira betrachtet die Mauer.* " + "Der Stein bleibt kühl. ".repeat(65), "a-$it"))
        }
        val input = StorySummaryPrompt.history(bundle(archive))
        assertTrue(input.any { it.user && critical in it.text })
        assertTrue(input.any { it.text.startsWith("AUSGANGSLAGE") && "linke Hand" in it.text })
        assertTrue(input.sumOf { it.text.length } <= StorySummaryPrompt.MAX_HISTORY_CHARS)
        assertEquals(archive, bundle(archive).messages)
    }

    @Test fun correctionsAndMovesAreUnchangedStructuralSourcesAndOldLocationIsLabelledAsANote() {
        val texts = listOf("Mira gibt Rian den Schlüssel.", "Rian legt ihn in die Truhe.", "Korrektur: silbern, nicht bronzen. Der Ort bleibt die Truhe.",
            "Rian nimmt ihn, öffnet die Tür und geht mit Mira in den Hof. Rian behält den Schlüssel. Seine linke Hand bleibt verletzt.")
        val archive = buildList {
            add(message(ChatRole.CHARACTER, character.openingMessage, "opening"))
            texts.forEachIndexed { i, text -> add(message(ChatRole.USER, text, "u-$i")); add(message(ChatRole.CHARACTER, "„Verstanden.“", "a-$i")) }
            add(message(ChatRole.USER, "Wo sind wir?", "pending"))
        }
        val input = bundle(archive).copy(memories = listOf(MemoryEntry(storyId = story.id, kind = MemoryKind.LOCATION, text = "Startvorgabe: Turmzimmer")))
        val actual = StoryPrompt.history(input)
        texts.forEach { text -> assertEquals(1, actual.count { it.user && it.text == text }) }
        assertEquals("Wo sind wir?", actual.last().text)
        val system = StoryPrompt.system(input)
        assertFalse(system.contains("Current location:"))
        assertTrue(system.contains("recent conversation takes priority"))
        assertTrue(system.contains("explicit corrections override"))
        assertTrue(system.contains("never established"))
    }

    @Test fun summariesAreNeverSilentlyCutAndTokenCapNeverPassesAsComplete() {
        assertEquals("Rian ist im Hof.", StorySummaryPrompt.checkedSummary(" Rian ist im Hof. "))
        assertThrows(IllegalArgumentException::class.java) { StorySummaryPrompt.checkedSummary("Rian ".repeat(101)) }
        assertThrows(IllegalArgumentException::class.java) { StorySummaryPrompt.checkedSummary(" ") }
        StoryOutputLimit.requireCompleted(511, 512)
        assertThrows(LocalModelException::class.java) { StoryOutputLimit.requireCompleted(512, 512) }
    }
}
