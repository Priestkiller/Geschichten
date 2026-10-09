package dev.vincent.geschichten

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import androidx.compose.ui.test.onRoot
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Uses the real Activity, ViewModel and SQLite repository, without downloading/loading a model. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MainActivityLaunchTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun firstLaunchOpensIntegratedSetupAndCreatesAStory() {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithTag("character_runa").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("characters_screen").assertIsDisplayed()
        compose.onNodeWithTag("character_runa").assertIsDisplayed()
        val firstLaunch = File("build/ui-screenshots/07-erster-app-start.png")
        firstLaunch.parentFile?.mkdirs()
        compose.onRoot().captureRoboImage(firstLaunch.absolutePath)

        compose.onNodeWithTag("model_status").performClick()
        compose.onNodeWithTag("setup_screen").assertIsDisplayed()
        compose.onNodeWithTag("download_model").assertIsDisplayed()
        // No click on download_model: network and native inference are outside this test.
        compose.onNodeWithTag("back_button").performClick()
        compose.onNodeWithTag("characters_screen").assertIsDisplayed()

        compose.onNodeWithTag("character_runa").performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithTag("chat_screen").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("chat_screen").assertIsDisplayed()
        compose.onNodeWithTag("chat_input").assertIsDisplayed()
    }
}
