package dev.vincent.geschichten.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import dev.vincent.geschichten.ai.ModelState
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TeamUiTest {
    @get:Rule val compose=createComposeRule()
    @Test fun defaultOffSeparatePickerAndSameFileWarningAreUnderstandable() {
        var enabled:Boolean?=null;var chosen:String?=null
        val actions=object:AppActions by ScreenshotNoOpActions {override fun setTeamEnabled(value:Boolean){enabled=value};override fun selectHelperModel(id:String){chosen=id}}
        compose.setContent {StoryApp(AppUiState(screen=AppScreen.SETUP,model=ModelState(modelId="gemma-4-e2b"),helperModel=ModelState(modelId="gemma-4-e2b")),actions)}
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("team_switch"))
        compose.onNodeWithTag("team_switch").assertIsOff().performClick();assertEquals(true,enabled)
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("helper_self_check"));compose.onNodeWithTag("helper_self_check").assertExists()
        compose.onNodeWithTag("helper_picker").performClick();compose.onNodeWithTag("helper_huihui-qwen3-4b").performClick();assertEquals("huihui-qwen3-4b",chosen)
        compose.onRoot().captureRoboImage(File("build/ui-screenshots/model-team.png").apply {parentFile!!.mkdirs()}.absolutePath)
    }
}
