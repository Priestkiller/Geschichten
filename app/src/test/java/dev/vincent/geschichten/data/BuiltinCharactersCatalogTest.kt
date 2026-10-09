package dev.vincent.geschichten.data

import dev.vincent.geschichten.ai.StoryPrompt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltinCharactersCatalogTest {
    private val expectedIds = listOf(
        "runa", "elara", "leon", "mira", "aelwyn", "borin", "kael", "nyra", "sylwen", "varen",
        "thora", "orin", "vaelgor", "fenrik", "soryn", "seris", "nessa", "korr", "pyra", "aruun",
        "astrid", "eirik", "sigrid", "torben", "liv", "halvard", "solveig", "bjarke", "yngvar", "maelis",
        "johanna", "cem", "vera", "anton", "nora", "kaspar", "ines", "malik", "hedda",
        "tarek", "sana", "ivo", "lyra", "noam", "keira", "rohan", "ada", "silas", "thalora", "veshra",
        "caerion", "linnet", "tessa", "thamund", "irilwen", "hildis", "dorik", "eldran", "saelith", "berenor",
        "soren_vale", "riven", "eris_wynn", "nyx_rho", "zhara_voss", "jalen_7", "ena", "liora_cass", "mael_voss", "daren_moss",
        "kira_rook", "naya_cruz", "maren_flux", "selene_kade", "elys_voss", "dante_raze", "bruno_vega", "renji_sato", "bastion", "ari_maddox",
        "morga", "grask", "varkesha", "drazhul", "raukha", "nharok", "velyss", "skarn", "siraxa", "throgg",
    )

    @Test
    fun ninetyDistinctFiguresHaveMatchingPortraitIdsAndExplicitAdultIdentityInTheActiveProfileWindow() {
        val profiles = BuiltinCharacters.profiles
        assertEquals(expectedIds, profiles.map { it.id })
        assertEquals(90, profiles.map { it.id }.toSet().size)
        assertEquals(90, profiles.map { it.name }.toSet().size)
        for (profile in profiles) {
            assertEquals(profile.id, profile.avatarKey)
            assertTrue("Missing role for ${profile.id}", profile.role.isNotBlank())
            assertTrue("Missing voice for ${profile.id}", profile.traits.isNotBlank())
            assertTrue("Opening cannot fit editor for ${profile.id}", profile.openingMessage.length in 100..CharacterIntroductions.MAX_OPENING_CHARS)
            val firstAge = Regex("\\b(\\d+)(?:-jährig| Jahre(?:n)?\\b)").find(profile.personality.take(160))?.groupValues?.get(1)?.toInt()
            assertTrue("Adult age must be explicit at start of ${profile.id}", firstAge != null && firstAge >= 18)
            if (profile.id !in setOf("runa", "elara", "leon", "mira")) {
                assertTrue("Adult status must survive context clipping for ${profile.id}", profile.personality.take(480).contains("erwachsen"))
            }
        }
    }

    @Test
    fun originalFourMigrationBaselinesRemainIdenticalToTheDeliveredCatalog() {
        assertEquals(DeliveredV1CatalogFixture.profiles, BuiltinCharacters.previousProfiles.take(4))
    }

    @Test
    fun allTwentyOlderMigrationBaselinesRemainIdentical() {
        assertEquals(DeliveredV2CatalogFixture.profiles, BuiltinCharacters.previousProfiles.take(20))
    }

    @Test
    fun allFiftyReleasedBaselinesStayExactWhileOnlyAuthoredTextIsRevised() {
        assertEquals(DeliveredV3CatalogFixture.profiles, BuiltinCharacters.previousProfiles)
        val current = BuiltinCharacters.profiles.associateBy { it.id }
        for (previous in DeliveredV3CatalogFixture.profiles) {
            val revised = current.getValue(previous.id)
            assertTrue("Distinct personality for ${previous.id}", revised.personality != previous.personality)
            assertTrue("Distinct opening for ${previous.id}", revised.openingMessage != previous.openingMessage)
            assertTrue("Editable full personality for ${previous.id}", revised.personality.length <= CharacterIntroductions.MAX_PERSONALITY_CHARS)
            assertTrue("Editable full scene for ${previous.id}", revised.scenario.length <= 1000)
            assertEquals(previous, revised.copy(
                personality = previous.personality, openingMessage = previous.openingMessage,
                scenario = previous.scenario,
            ))
        }
    }

    @Test
    fun everyExistingCategoryHasTenFiguresWithFiveWomenAndFiveMen() {
        val femaleIds = setOf(
            "runa", "elara", "mira", "aelwyn", "nyra", "sylwen", "thora", "seris", "nessa", "pyra",
            "astrid", "sigrid", "liv", "solveig", "johanna", "vera", "nora", "ines", "hedda",
            "sana", "lyra", "keira", "ada", "thalora", "veshra",
            "linnet", "tessa", "irilwen", "hildis", "saelith",
            "eris_wynn", "nyx_rho", "zhara_voss", "ena", "liora_cass",
            "kira_rook", "naya_cruz", "maren_flux", "selene_kade", "elys_voss",
            "morga", "varkesha", "raukha", "velyss", "siraxa",
        )
        val categories = BuiltinCharacters.profiles.groupBy { it.genre }
        assertEquals(setOf("Nordische Fantasy", "Fantasy", "Krimi", "Science-Fiction", "Kreaturen", "Mittelerde", "Blade Runner", "Cyberpunk 2077", "Monster"), categories.keys)
        for ((category, figures) in categories) {
            assertEquals("Ten figures in $category", 10, figures.size)
            assertEquals("Five female figures in $category", 5, figures.count { it.id in femaleIds })
            assertEquals("Five male figures in $category", 5, figures.count { it.id !in femaleIds })
        }
    }

    @Test
    fun allFiftyDelivered031ProfilesRemainFieldIdenticalAndInTheSameOrder() {
        assertEquals(DeliveredV4CatalogFixture.profiles, BuiltinCharacters.preIntroductionProfiles.take(50))
        assertEquals(40, WorldCharacters.profiles.size)
        assertTrue(WorldCharacters.profiles.all { it.personality.length <= 630 && it.scenario.length <= 480 })
    }

    @Test
    fun allNinetyEntriesKeepIdentityAndHaveAnIndividualPlayerRolePastAndLiveEncounter() {
        assertEquals(DeliveredV5CatalogFixture.profiles, BuiltinCharacters.preIntroductionProfiles)
        assertEquals(DeliveredV6CatalogFixture.profiles, BuiltinCharacters.preChatProfiles)
        val current = BuiltinCharacters.profiles
        assertEquals(90, current.map { it.openingMessage }.toSet().size)
        assertEquals(90, current.map { it.scenario }.toSet().size)
        for ((previous, revised) in DeliveredV5CatalogFixture.profiles.zip(current)) {
            assertEquals(previous, revised.copy(openingMessage = previous.openingMessage,
                personality = previous.personality, scenario = previous.scenario))
            val paragraphs = revised.openingMessage.split("\n\n")
            assertTrue("Substantial entry for ${revised.id}", revised.openingMessage.length >= 2800)
            assertTrue("Past and live scene for ${revised.id}", paragraphs.size >= 8)
            assertTrue("Player role comes first for ${revised.id}", paragraphs.first().contains("du", ignoreCase = true))
            assertTrue("Full personality for ${revised.id}", revised.personality.split("\n\n").size >= 3)
            assertTrue(revised.scenario.startsWith("Startvorgabe:"))
            assertTrue("Emphasized dialogue for ${revised.id}", revised.openingMessage.contains("**„"))
        }
    }

    @Test
    fun everyEntryKeepsItsActualLastQuestionAndLongestFirstUserMessageInTheInferenceWindow() {
        for (profile in BuiltinCharacters.profiles) {
            val story = Story(id = "prologue-${profile.id}", characterId = profile.id, title = profile.storyTitle,
                startContext = CharacterIntroductions.contextFor(profile))
            val opening = ChatMessage(storyId = story.id, role = ChatRole.CHARACTER, text = profile.openingMessage)
            val input = ChatMessage(storyId = story.id, role = ChatRole.USER, text = "a".repeat(StoryPrompt.MAX_USER_MESSAGE_CHARS))
            val other = ChatMessage(storyId = "other-story", role = ChatRole.CHARACTER, text = "Fremder Prolog.")
            val history = StoryPrompt.history(StoryBundle(story, profile, listOf(other, opening, input), emptyList()))
            assertEquals(3, history.size)
            assertEquals(input.text, history.last().text)
            val lastDialogue = Regex("„[^“]+“").findAll(profile.openingMessage).last().value
            assertTrue("Actual last question for ${profile.id}", history[1].text.contains(lastDialogue))
            assertTrue("Saved encounter retained for ${profile.id}", profile.openingMessage.endsWith(history[1].text))
            assertTrue(history.sumOf { it.text.length } <= StoryPrompt.MAX_HISTORY_CHARS)
            assertEquals(listOf(true, false, true), history.map { it.user })
            assertEquals(profile.openingMessage, opening.text)
        }
    }

    @Test
    fun editingANewWorldStartDoesNotReintroduceItsOriginalThreatOrTimeline() {
        val custom = BuiltinCharacters.profiles.single { it.id == "kira_rook" }.copy(
            scenario = "Kira lebt ohne Cyberware in einem ruhigen Küstendorf.",
            openingMessage = "Willkommen am Meer.", custom = true,
        )
        val notes = BuiltinCharacters.initialMemories(custom, "custom-world", 1)
        assertEquals(setOf(MemoryKind.FACT, MemoryKind.EVENT), notes.map { it.kind }.toSet())
        assertTrue(notes.none { it.text.contains("Relic") || it.text.contains("Dante") || it.text.contains("Watson") })
        assertTrue(notes.any { it.text.contains("Küstendorf") })
    }

    @Test
    fun editingAnAddedFiguresStartDoesNotRestoreTheOldLocationOrQuest() {
        val edited = BuiltinCharacters.profiles.single { it.id == "thalora" }.copy(
            scenario = "Thalora wartet im Museum auf einen Besucher.",
            openingMessage = "Willkommen im Museum.",
            custom = true,
        )
        val memories = BuiltinCharacters.initialMemories(edited, "new-custom-story", 1)
        assertEquals(setOf(MemoryKind.FACT, MemoryKind.EVENT), memories.map { it.kind }.toSet())
        assertTrue(memories.none { it.text.contains("Perlenwacht") || it.text.contains("Leuchtkorallen") })
        assertTrue(memories.any { it.text.contains("Museum") })
    }

    @Test
    fun everyFigureCreatesFourIsolatedStartingNotesAndFitsTheStoryContext() {
        for (profile in BuiltinCharacters.profiles) {
            val firstStory = Story(id = "story-a-${profile.id}", characterId = profile.id, title = profile.storyTitle)
            val secondStory = Story(id = "story-b-${profile.id}", characterId = profile.id, title = profile.storyTitle)
            val first = BuiltinCharacters.initialMemories(profile, firstStory.id, 1)
            val second = BuiltinCharacters.initialMemories(profile, secondStory.id, 1)
            assertEquals("Seed kinds for ${profile.id}", MemoryKind.entries.toSet(), first.map { it.kind }.toSet())
            assertEquals(4, first.size)
            assertTrue(first.all { it.storyId == firstStory.id && it.text.startsWith("Startvorgabe:") })
            assertTrue(second.all { it.storyId == secondStory.id })
            assertTrue(first.map { it.id }.toSet().intersect(second.map { it.id }.toSet()).isEmpty())
            val context = StoryPrompt.system(StoryBundle(firstStory, profile, emptyList(), first))
            assertTrue("Context too long for ${profile.id}", context.length <= StoryPrompt.MAX_SYSTEM_CHARS)
            assertTrue(context.contains(profile.name))
            assertTrue(context.contains(profile.role))
        }
    }
}
