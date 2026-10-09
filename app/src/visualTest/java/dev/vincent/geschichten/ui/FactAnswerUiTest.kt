package dev.vincent.geschichten.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FactAnswerUiTest {
    @get:Rule val compose=createComposeRule()
    @Test fun optionalModeIsOffAndItsScopeIsVisible() {
        var enabled:Boolean?=null
        val actions=object:AppActions by ScreenshotNoOpActions {override fun setFactsAnswers(value:Boolean){enabled=value}}
        compose.setContent {StoryApp(AppUiState(screen=AppScreen.SETUP),actions)}
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("facts_answer_switch"))
        compose.onNodeWithTag("facts_answer_switch").assertIsOff().performClick();assertEquals(true,enabled)
        compose.onNodeWithText("Kurze Faktenantworten").assertIsDisplayed()
        compose.onRoot().captureRoboImage(File("build/ui-screenshots/facts-answer.png").apply {parentFile!!.mkdirs()}.absolutePath)
    }
}
