package dev.vincent.geschichten.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import dev.vincent.geschichten.ai.*
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OllamaSetupUiTest {
    @get:Rule val compose=createComposeRule()
    @Test fun serverSettingsAreVisibleAndConnectUsesTheEnteredValues() {
        var received: Pair<String,String>?=null
        val settings=OllamaSettings(enabled=true)
        val state=mutableStateOf(AppUiState(screen=AppScreen.SETUP,server=settings,model=ModelState(server=true,stage=ModelStage.READY,
            modelId=settings.model,detail="Mit deinem PC verbunden · ${settings.model}")))
        val actions=object:AppActions by ScreenshotNoOpActions {
            override fun connectServer(address:String,model:String) {received=address to model}
        }
        compose.setContent {StoryApp(state.value,actions)}
        compose.onNodeWithTag("ollama_address").assertIsDisplayed()
        compose.onNodeWithTag("ollama_model").assertIsDisplayed()
        compose.onNodeWithTag("ollama_address").performTextReplacement("http://192.168.178.20:11434")
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("ollama_connect"))
        compose.onNodeWithTag("ollama_connect").performClick()
        assertEquals("http://192.168.178.20:11434" to settings.model,received)
        compose.onNodeWithTag("setup_screen").performScrollToIndex(0)
        val output=File("build/ui-screenshots/ollama").apply {mkdirs()}
        compose.onRoot().captureRoboImage(File(output,"server-settings.png").absolutePath)
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("ollama_status"))
        compose.onNodeWithTag("ollama_status").assertTextContains("Mit deinem PC verbunden", substring=true)
    }
    @Test fun changingServerIsDisabledWhileAReplyIsBeingGenerated() {
        compose.setContent {StoryApp(AppUiState(screen=AppScreen.SETUP,busy=true,server=OllamaSettings(enabled=true),
            model=ModelState(server=true,stage=ModelStage.GENERATING)),ScreenshotNoOpActions)}
        compose.onNodeWithTag("ollama_enabled").assertIsNotEnabled()
        compose.onNodeWithTag("ollama_address").assertIsNotEnabled()
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("ollama_connect"))
        compose.onNodeWithTag("ollama_connect").assertIsNotEnabled()
    }
}
