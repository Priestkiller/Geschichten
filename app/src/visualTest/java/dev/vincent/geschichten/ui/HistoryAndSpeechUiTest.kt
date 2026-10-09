package dev.vincent.geschichten.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import dev.vincent.geschichten.data.*
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HistoryAndSpeechUiTest {
    @get:Rule val compose = createComposeRule()
    private val grask = BuiltinCharacters.profiles.single { it.id == "grask" }
    private val story = Story(id = "history-test", characterId = grask.id, title = grask.storyTitle)

    @Test fun clearHistoryRequiresConfirmationAndCancellationPreservesTheConversation() {
        val state = mutableStateOf(AppUiState(screen = AppScreen.STORIES, characters = listOf(grask), stories = listOf(story)))
        var deleted = 0
        val actions = object : AppActions by ScreenshotNoOpActions {
            override fun clearHistory() { deleted++; state.value = state.value.copy(stories = emptyList(), current = null) }
        }
        compose.setContent { StoryApp(state.value, actions) }
        compose.onNodeWithTag("clear_history").assertIsEnabled().performClick()
        compose.onNodeWithText("Verlauf leeren?").assertIsDisplayed()
        compose.onNodeWithTag("confirm_clear_history").captureRoboImage(output("clear-confirmation-button.png"))
        compose.onNodeWithTag("cancel_clear_history").performClick()
        compose.onNodeWithTag("story_${story.id}").assertExists()
        assertEquals(0, deleted)
        compose.onNodeWithTag("clear_history").performClick()
        compose.onNodeWithTag("confirm_clear_history").performClick()
        assertEquals(1, deleted)
        compose.onNodeWithTag("story_${story.id}").assertDoesNotExist()
        compose.onNodeWithTag("clear_history").assertIsNotEnabled()
        compose.onNodeWithText("Jede Geschichte beginnt mit dir").assertIsDisplayed()
        compose.onRoot().captureRoboImage(output("history-empty.png"))
    }

    @Test fun clearingIsDisabledWhileAnAnswerIsBeingGeneratedOrDeletionIsRunning() {
        val state = mutableStateOf(AppUiState(screen = AppScreen.STORIES, characters = listOf(grask), stories = listOf(story), busy = true))
        compose.setContent { StoryApp(state.value, ScreenshotNoOpActions) }
        compose.onNodeWithTag("clear_history").assertIsNotEnabled()
        compose.runOnIdle { state.value = state.value.copy(busy = false, historyBusy = true) }
        compose.onNodeWithTag("clear_history").assertIsNotEnabled()
        compose.onNodeWithTag("story_${story.id}").assertIsNotEnabled()
    }

    @Test fun characterSpeechIsReadableInARealChatBubbleWithoutMarkdownMarkers() {
        // Authored visual fixture; the separate native probe measures actual model output.
        val reply = "Grask deutet auf das Register hinter dem Gitter.\n\n„Dort. Lies die Namen der letzten Käufer. Einen davon suche ich.“\n\nEr wartet, ohne den Blick von den vergilbten Seiten zu nehmen."
        val message = ChatMessage(storyId = story.id, role = ChatRole.CHARACTER, text = reply)
        compose.setContent { StoryApp(AppUiState(screen = AppScreen.CHAT, characters = listOf(grask),
            current = StoryBundle(story, grask, listOf(message), emptyList())), ScreenshotNoOpActions) }
        compose.onNodeWithText(reply).assertIsDisplayed()
        compose.onRoot().captureRoboImage(output("chat-speech.png"))
    }

    private fun output(name: String): String = File("build/ui-screenshots/continuity-0.7.2/$name").apply { parentFile!!.mkdirs() }.absolutePath
}
