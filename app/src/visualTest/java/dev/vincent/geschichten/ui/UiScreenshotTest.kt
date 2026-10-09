package dev.vincent.geschichten.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import com.github.takahirom.roborazzi.captureRoboImage
import dev.vincent.geschichten.ai.ModelArtifact
import dev.vincent.geschichten.ai.ModelStage
import dev.vincent.geschichten.ai.ModelState
import dev.vincent.geschichten.data.BuiltinCharacters
import dev.vincent.geschichten.data.CharacterProfile
import dev.vincent.geschichten.data.ChatMessage
import dev.vincent.geschichten.data.ChatRole
import dev.vincent.geschichten.data.MemoryEntry
import dev.vincent.geschichten.data.Story
import dev.vincent.geschichten.data.StoryBundle
import java.io.File
import java.time.Instant
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the actual production Compose UI with deterministic, authored test fixtures.
 *
 * These screenshots validate layout only. The transcript below is deliberately written
 * for visual QA; it is not an AI-generated conversation, remembered user history, evidence
 * of a completed model download, or a measurement of inference on a physical Galaxy S24.
 * This opt-in visualTest source directory is not packaged into the application. No model,
 * repository, download, or network service is constructed by this test.
 *
 * Run with the build's visual-test opt-in and Roborazzi recording enabled. PNGs are saved
 * in app/build/ui-screenshots when Gradle uses the app module as the test working directory.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UiScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun figuresGallery() {
        render(ScreenshotFixtures.baseState.copy(screen = AppScreen.CHARACTERS))
        compose.onNodeWithTag("characters_screen").assertIsDisplayed()
        compose.onNodeWithTag("character_runa").assertIsDisplayed()
        capture("01-figuren.png")
    }

    @Test
    fun expandedGalleryReachesTheNewCreatureCharacters() {
        render(ScreenshotFixtures.baseState.copy(screen = AppScreen.CHARACTERS))
        compose.onNodeWithTag("characters_screen")
            .performScrollToNode(hasTestTag("character_vaelgor"))
        compose.onNodeWithTag("character_vaelgor").assertIsDisplayed()
        capture("08-neue-figuren.png")
        compose.onNodeWithTag("characters_screen")
            .performScrollToNode(hasTestTag("character_aruun"))
        compose.onNodeWithTag("character_aruun").assertIsDisplayed()
    }

    @Test
    fun creatureFilterShowsTheNewGroupAndRemainsScrollable() {
        render(ScreenshotFixtures.baseState.copy(screen = AppScreen.CHARACTERS))
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange))
            .performScrollToNode(hasTestTag("genre_Kreaturen"))
        compose.onNodeWithTag("genre_Kreaturen").performClick().assertIsSelected()
        compose.onNodeWithTag("character_vaelgor").assertIsDisplayed()
        capture("10-kreaturenfilter.png")
        compose.onNodeWithTag("characters_screen")
            .performScrollToNode(hasTestTag("character_aruun"))
        compose.onNodeWithTag("character_aruun").assertIsDisplayed()
    }

    @Test
    fun newWorldFiltersReachBothEndsOfEveryNewCategory() {
        render(ScreenshotFixtures.baseState.copy(screen = AppScreen.CHARACTERS))
        for ((genre, first, last) in listOf(
            Triple("Mittelerde", "caerion", "berenor"),
            Triple("Blade Runner", "soren_vale", "daren_moss"),
            Triple("Cyberpunk 2077", "kira_rook", "ari_maddox"),
            Triple("Monster", "morga", "throgg"),
        )) {
            compose.onNodeWithTag("characters_screen")
                .performScrollToNode(hasTestTag("character_search"))
            compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange))
                .performScrollToNode(hasTestTag("genre_$genre"))
            compose.onNodeWithTag("genre_$genre").performClick().assertIsSelected()
            compose.onNodeWithTag("characters_screen").performScrollToNode(hasTestTag("character_$first"))
            compose.onNodeWithTag("character_$first").assertIsDisplayed()
            capture("world-${first}-filter.png")
            compose.onNodeWithTag("characters_screen").performScrollToNode(hasTestTag("character_$last"))
            compose.onNodeWithTag("character_$last").assertIsDisplayed()
        }
    }

    @Test
    fun authoredChatFixture() {
        render(
            ScreenshotFixtures.baseState.copy(
                screen = AppScreen.CHAT,
                current = ScreenshotFixtures.runaStory,
            ),
        )
        compose.onNodeWithTag("chat_screen").assertIsDisplayed()
        compose.onNodeWithTag("chat_input").assertIsDisplayed()
        capture("02-geschichte.png")
    }

    @Test
    fun editableStartingMemories() {
        render(
            ScreenshotFixtures.baseState.copy(
                screen = AppScreen.MEMORIES,
                current = ScreenshotFixtures.runaStory,
            ),
        )
        compose.onNodeWithTag("memories_screen").assertIsDisplayed()
        compose.onNodeWithTag("memory_fixture_memory_0").assertIsDisplayed()
        capture("03-erinnerungen.png")
    }

    @Test
    fun firstRunSetup() {
        render(
            ScreenshotFixtures.baseState.copy(
                screen = AppScreen.SETUP,
                current = null,
                model = ModelState(stage = ModelStage.MISSING, totalBytes = ModelArtifact.BYTES),
            ),
        )
        compose.onNodeWithTag("setup_screen").assertIsDisplayed()
        compose.onNodeWithTag("download_model").assertIsDisplayed()
        capture("04-einrichtung.png")
    }

    @Test
    fun characterEditor() {
        render(
            ScreenshotFixtures.baseState.copy(
                screen = AppScreen.EDITOR,
                editingCharacter = ScreenshotFixtures.runa,
            ),
        )
        compose.onNodeWithTag("character_editor").assertIsDisplayed()
        compose.onNodeWithTag("save_character").assertIsDisplayed()
        capture("05-figureneditor.png")
    }

    @Test
    fun editorCanChooseAndSaveTheTwentiethPortrait() {
        var saved: CharacterProfile? = null
        val actions = object : AppActions by ScreenshotNoOpActions {
            override fun saveCharacter(character: CharacterProfile) { saved = character }
        }
        render(
            ScreenshotFixtures.baseState.copy(
                screen = AppScreen.EDITOR,
                editingCharacter = ScreenshotFixtures.runa,
            ),
            actions,
        )
        val last = BuiltinCharacters.profiles.last()
        compose.onNodeWithTag("editor_portrait_choices")
            .performScrollToIndex(BuiltinCharacters.profiles.lastIndex)
        compose.onNodeWithTag("editor_portrait_${last.avatarKey}")
            .assertIsDisplayed().performClick().assertIsSelected()
        capture("09-portraetauswahl.png")
        compose.onNodeWithTag("save_character").performClick()
        compose.runOnIdle {
            assertEquals(ScreenshotFixtures.runa.id, saved?.id)
            assertEquals(last.avatarKey, saved?.avatarKey)
        }
    }

    @Test
    fun savedStories() {
        render(ScreenshotFixtures.baseState.copy(screen = AppScreen.STORIES))
        compose.onNodeWithTag("stories_screen").assertIsDisplayed()
        compose.onNodeWithTag("story_fixture_story_runa").assertIsDisplayed()
        capture("06-geschichtenliste.png")
    }

    private fun render(state: AppUiState, actions: AppActions = ScreenshotNoOpActions) {
        compose.setContent { StoryApp(state, actions) }
        compose.waitForIdle()
    }

    private fun capture(name: String) {
        val directory = File("build/ui-screenshots")
        check(directory.isDirectory || directory.mkdirs()) {
            "Could not create screenshot output directory: ${directory.absolutePath}"
        }
        compose.onRoot().captureRoboImage(File(directory, name).absolutePath)
    }
}

/** Authored, fixed test content used exclusively to exercise real layouts. */
private object ScreenshotFixtures {
    private val timestamp = Instant.parse("2026-10-03T18:22:00Z").toEpochMilli()
    val runa = BuiltinCharacters.profiles.first { it.id == "runa" }

    private val stories = BuiltinCharacters.profiles.mapIndexed { index, character ->
        Story(
            id = "fixture_story_${character.id}",
            characterId = character.id,
            title = character.storyTitle,
            summary = character.scenario,
            createdAt = timestamp - index * 60_000L,
            updatedAt = timestamp - index * 60_000L,
        )
    }

    private val runaStoryData = stories.first { it.characterId == runa.id }

    // This small dialogue is a hand-authored visual fixture, never a production fallback.
    private val dialogue = listOf(
        ChatMessage(
            id = "fixture_user_message",
            storyId = runaStoryData.id,
            role = ChatRole.USER,
            text = "Ich ziehe die Tür hinter mir zu. „Vincent. Ich habe mich im Sturm verirrt.“",
            createdAt = timestamp,
        ),
        ChatMessage(
            id = "fixture_character_message",
            storyId = runaStoryData.id,
            role = ChatRole.CHARACTER,
            text = "*Runa nimmt die Hand vom Dolch und deutet auf den freien Stuhl.*\n\n" +
                "„Dann haben wir wenigstens denselben Feind. Setz dich. " +
                "Weißt du etwas über das Kloster am Pass?“",
            createdAt = timestamp + 1,
        ),
    )

    val runaStory = StoryBundle(
        story = runaStoryData,
        character = runa,
        messages = dialogue,
        memories = BuiltinCharacters.initialMemories(runa, runaStoryData.id, timestamp)
            .mapIndexed { index, entry -> entry.copy(id = "fixture_memory_$index") },
    )

    val baseState = AppUiState(
        characters = BuiltinCharacters.profiles,
        stories = stories,
        model = ModelState(stage = ModelStage.READY),
    )
}

/** Prevents visual fixtures from writing data, requesting a download, or generating text. */
internal object ScreenshotNoOpActions : AppActions {
    override fun navigate(screen: AppScreen) = Unit
    override fun selectCharacter(character: CharacterProfile) = Unit
    override fun openStory(storyId: String) = Unit
    override fun startNewStory(character: CharacterProfile) = Unit
    override fun createCharacter() = Unit
    override fun editCharacter(character: CharacterProfile) = Unit
    override fun saveCharacter(character: CharacterProfile) = Unit
    override fun changeDraft(value: String) = Unit
    override fun sendMessage() = Unit
    override fun stopGeneration() = Unit
    override fun saveMemory(memory: MemoryEntry) = Unit
    override fun deleteMemory(memory: MemoryEntry) = Unit
    override fun downloadModel() = Unit
    override fun cancelDownload() = Unit
    override fun loadModel() = Unit
    override fun dismissNotice() = Unit
    override fun exportCurrentStory() = Unit
    override fun setAdultThemes(enabled: Boolean) = Unit
}
