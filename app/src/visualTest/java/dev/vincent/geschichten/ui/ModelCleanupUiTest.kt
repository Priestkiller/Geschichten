package dev.vincent.geschichten.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import dev.vincent.geschichten.ai.LocalModelCatalog
import dev.vincent.geschichten.ai.ModelStage
import dev.vincent.geschichten.ai.ModelState
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
class ModelCleanupUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun keepAndDeleteAreSeparateExplicitChoicesAndShowTheOldModelAndSize() {
        val old = LocalModelCatalog.models.first()
        val state = mutableStateOf(AppUiState(screen = AppScreen.SETUP, modelCleanupId = old.id,
            model = ModelState(modelId = "qwen3-0.6b", stage = ModelStage.READY)))
        var kept = 0; var deleted = 0
        val actions = object : AppActions by ScreenshotNoOpActions {
            override fun keepPreviousModel() { kept++; state.value = state.value.copy(modelCleanupId = null) }
            override fun deletePreviousModel() { deleted++; state.value = state.value.copy(modelCleanupId = null) }
        }
        compose.setContent { StoryApp(state.value, actions) }
        compose.onNodeWithText("Bisherige KI behalten?").assertIsDisplayed()
        val target = File("build/ui-screenshots/continuity-0.7.2/model-cleanup.png").apply { parentFile!!.mkdirs() }
        compose.onNodeWithTag("model_cleanup_dialog").captureRoboImage(target.absolutePath)
        compose.onNodeWithTag("keep_previous_model").performClick()
        assertEquals(1, kept); assertEquals(0, deleted)
        compose.onNodeWithTag("model_cleanup_dialog").assertDoesNotExist()
        compose.runOnIdle { state.value = state.value.copy(modelCleanupId = old.id) }
        compose.onNodeWithTag("delete_previous_model").performClick()
        assertEquals(1, kept); assertEquals(1, deleted)
    }

    @Test fun modelDeletionIsDisabledWhileAnotherModelOperationIsRunning() {
        compose.setContent { StoryApp(AppUiState(screen = AppScreen.SETUP, modelCleanupId = LocalModelCatalog.models.first().id,
            modelBusy = true), ScreenshotNoOpActions) }
        compose.onNodeWithTag("delete_previous_model").assertIsNotEnabled()
        compose.onNodeWithTag("keep_previous_model").assertIsNotEnabled()
    }
}
