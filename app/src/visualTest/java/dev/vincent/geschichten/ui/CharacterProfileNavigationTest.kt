package dev.vincent.geschichten.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.github.takahirom.roborazzi.captureRoboImage
import dev.vincent.geschichten.data.*
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Production UI, installed profiles and authored openings; no network or model inference. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CharacterProfileNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h780dp-xxhdpi")
    fun allFourTabsNavigateAndSettingsRetainsItsReturnTargetAtNarrowPhoneWidth() {
        val state = mutableStateOf(AppUiState(characters = BuiltinCharacters.profiles))
        val actions = RecordingActions { state.value = state.value.copy(screen = it) }
        compose.setContent { StoryApp(state.value, actions) }
        val tabs = listOf(
            Triple("tab_characters", "Figuren", AppScreen.CHARACTERS),
            Triple("tab_stories", "Verlauf", AppScreen.STORIES),
            Triple("tab_memories", "Gedächtnis", AppScreen.MEMORIES),
            Triple("tab_setup", "Einstellungen", AppScreen.SETUP),
        )
        for ((tag, label, screen) in tabs) {
            for ((otherTag, otherLabel) in tabs) compose.onNodeWithTag(otherTag).assertIsDisplayed().assert(hasText(otherLabel))
            compose.onNodeWithTag(tag).performClick().assertIsSelected()
            compose.runOnIdle { assertEquals(screen, state.value.screen) }
            capture("tabs-${screen.name.lowercase()}.png")
        }
        compose.onNodeWithTag("back_button").performClick()
        compose.onNodeWithTag("tab_memories").assertIsSelected()
        compose.onNodeWithTag("tab_characters").performClick()
        compose.onNodeWithTag("tab_setup").performClick()
        compose.onNodeWithTag("back_button").performClick()
        compose.onNodeWithTag("tab_characters").assertIsSelected()
        assertEquals(0, actions.selected)
        assertEquals(0, actions.started)
    }

    @Test
    fun allNinetyGalleryProfilesExposeIdentityTraitsAndFullPersonalityWithoutStoryIntroduction() {
        val actions = RecordingActions()
        compose.setContent { StoryApp(AppUiState(characters = BuiltinCharacters.profiles), actions) }
        for (profile in BuiltinCharacters.profiles) {
            compose.onNodeWithTag("characters_screen").performScrollToNode(hasTestTag("profile_character_${profile.id}"))
            compose.onNodeWithTag("profile_character_${profile.id}").performClick()
            compose.onNodeWithTag("personality_dialog").assertIsDisplayed()
            val parts = profile.personality.split("\n\n", limit = 2)
            compose.onNodeWithTag("personality_content").performScrollToNode(hasTestTag("character_identity_text"))
            compose.onNodeWithTag("character_identity_text").assertTextEquals(parts.first())
            compose.onNodeWithTag("personality_content").performScrollToNode(hasTestTag("character_personality_text"))
            compose.onNodeWithTag("character_personality_text").assertTextEquals(parts.last())
            capture("all-90/profile-${profile.id}.png")
            compose.onNodeWithTag("profile_introduction").assertDoesNotExist()
            compose.onNodeWithTag("personality_content").performScrollToNode(hasTestTag("profile_end"))
            compose.onNodeWithTag("profile_end").assertIsDisplayed()
            if (profile.id in setOf("runa", "aelwyn", "johanna", "sana", "vaelgor", "linnet", "riven", "kira_rook", "varkesha")) {
                capture("introduction-end-${profile.id}.png")
            }
            compose.onNodeWithTag("close_personality").assertIsDisplayed().performClick()
            compose.onNodeWithTag("personality_dialog").assertDoesNotExist()
        }
        assertEquals(0, actions.selected)
        assertEquals(0, actions.started)
        assertEquals(0, actions.edited)
    }

    @Test
    @Config(sdk = [35], qualifiers = "w852dp-h393dp-xxhdpi")
    fun chatPersonalityUsesTheInstalledUserVariantAndClosesInLandscape() {
        val character = BuiltinCharacters.profiles.first().copy(
            name = "Meine eigene Runa", personality = "Meine Persönlichkeit. ".repeat(25) + "Vollständiges Ende.",
            openingMessage = "*Mein eigener Hintergrund.*\n\n**„Mein eigener Dialog.“**", custom = true,
        )
        val story = Story(id = "own-story", characterId = character.id, title = "Eigene Geschichte")
        val archived = ChatMessage(storyId = story.id, role = ChatRole.CHARACTER, text = "Unveränderter alter Dialog.")
        val bundle = StoryBundle(story, character, listOf(archived), emptyList())
        val actions = RecordingActions()
        compose.setContent { StoryApp(AppUiState(screen = AppScreen.CHAT, characters = listOf(character), current = bundle), actions) }
        compose.onNodeWithTag("chat_menu").performClick()
        compose.onNodeWithTag("chat_personality").performClick()
        compose.onNodeWithTag("personality_content").performScrollToNode(hasTestTag("character_identity_text"))
        compose.onNodeWithTag("character_identity_text").assertTextEquals(character.personality)
        capture("own-personality-landscape.png")
        compose.onNodeWithTag("profile_introduction").assertDoesNotExist()
        compose.onNodeWithTag("close_personality").performClick()
        compose.onNodeWithTag("chat_screen").assertIsDisplayed()
        compose.onNodeWithTag("chat_messages").performScrollToNode(hasTestTag("character_message"))
        compose.onNodeWithText(archived.text).assertIsDisplayed()
        assertEquals(0, actions.started)
        assertTrue(actions.navigated.isEmpty())
    }

    @Test
    fun allNinetyFreshChatsShowTheAuthoredBeginningAndKeepTheirEntireEntry() {
        val state = mutableStateOf(AppUiState(screen = AppScreen.CHAT, characters = BuiltinCharacters.profiles))
        compose.setContent { StoryApp(state.value, RecordingActions()) }
        for (profile in BuiltinCharacters.profiles) {
            val story = Story(id = "fresh-${profile.id}", characterId = profile.id, title = profile.storyTitle,
                startContext = CharacterIntroductions.contextFor(profile))
            val opening = ChatMessage(storyId = story.id, role = ChatRole.CHARACTER, text = profile.openingMessage)
            compose.runOnIdle { state.value = state.value.copy(current = StoryBundle(story, profile, listOf(opening), emptyList())) }
            compose.onNodeWithTag("chat_introduction_label").assertIsDisplayed()
            compose.onNodeWithText(storyText(profile.openingMessage).text).assertExists()
            capture("all-90/start-${profile.id}.png")
            compose.onNodeWithTag("chat_messages").performScrollToIndex(2)
            compose.onNodeWithTag("chat_end").assertIsDisplayed()
            capture("all-90/end-${profile.id}.png")
        }
    }

    @Test
    fun playedConversationOpensAtTheLatestMessageInsteadOfTheLongIntroduction() {
        val profile = BuiltinCharacters.profiles.first()
        val story = Story(id = "played", characterId = profile.id, title = profile.storyTitle)
        val messages = listOf(ChatMessage(storyId = story.id, role = ChatRole.CHARACTER, text = profile.openingMessage)) +
            (1..20).map { ChatMessage(storyId = story.id, role = if (it % 2 == 1) ChatRole.USER else ChatRole.CHARACTER,
                text = "Gespeicherte Nachricht $it. " + "Weiterer Verlauf. ".repeat(8)) }
        compose.setContent { StoryApp(AppUiState(screen = AppScreen.CHAT,
            current = StoryBundle(story, profile, messages, emptyList())), RecordingActions()) }
        compose.onNodeWithText(messages.last().text).assertIsDisplayed()
        compose.onNodeWithTag("chat_introduction_label").assertDoesNotExist()
        capture("played-chat-latest.png")
        assertEquals(21, messages.size)
    }

    @Test
    fun allPrologueMarkupProducesItalicNarrationAndBoldDialogueWithoutLeakingMarkers() {
        for (profile in BuiltinCharacters.profiles) {
            val formatted = storyText(profile.openingMessage)
            assertFalse("No visible markup in ${profile.id}", formatted.text.contains('*'))
            val narration = formatted.spanStyles.filter { it.item.fontStyle == FontStyle.Italic }
            assertTrue("Past and conflict styled for ${profile.id}", narration.size >= 2)
            assertTrue(narration.take(2).all { formatted.text.substring(it.start, it.end).length > 200 })
            val dialogues = Regex("„[^“]+“").findAll(formatted.text).toList()
            assertTrue(dialogues.isNotEmpty())
            for (dialogue in dialogues) {
                assertTrue("Entire dialogue is bold in ${profile.id}", formatted.spanStyles.any {
                    it.item.fontWeight == FontWeight.SemiBold && it.start <= dialogue.range.first && it.end > dialogue.range.last
                })
            }
        }
    }

    private fun capture(name: String) {
        val target = File("build/ui-screenshots/profiles/$name")
        target.parentFile!!.mkdirs()
        val dialog = compose.onAllNodesWithTag("personality_dialog").fetchSemanticsNodes()
        if (dialog.isNotEmpty()) compose.onNodeWithTag("personality_dialog").captureRoboImage(target.absolutePath)
        else compose.onRoot().captureRoboImage(target.absolutePath)
    }
}

private class RecordingActions(private val onNavigate: (AppScreen) -> Unit = {}) : AppActions {
    var selected = 0
    var started = 0
    var edited = 0
    val navigated = mutableListOf<AppScreen>()
    override fun navigate(screen: AppScreen) { navigated += screen; onNavigate(screen) }
    override fun selectCharacter(character: CharacterProfile) { selected++ }
    override fun startNewStory(character: CharacterProfile) { started++ }
    override fun editCharacter(character: CharacterProfile) { edited++ }
    override fun openStory(storyId: String) = Unit
    override fun createCharacter() = Unit
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
