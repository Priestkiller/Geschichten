package dev.vincent.geschichten.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
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
class ModelSelectionUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun allSixModelsCanBeSelectedWithoutAutomaticallyDownloading() {
        val state = mutableStateOf(AppUiState(screen = AppScreen.SETUP))
        var downloads = 0
        val actions = object : AppActions by ScreenshotNoOpActions {
            override fun selectModel(id: String) {
                val spec = LocalModelCatalog.find(id)!!
                state.value = state.value.copy(model = ModelState(modelId = id, totalBytes = spec.bytes))
            }
            override fun downloadModel() { downloads++ }
        }
        compose.setContent { StoryApp(state.value, actions) }
        val output = File("build/ui-screenshots/models-0.7.1").apply { mkdirs() }
        for (spec in LocalModelCatalog.models) {
            compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("select_model_${spec.id}"))
            compose.onNodeWithTag("select_model_${spec.id}").performClick().assertIsSelected()
            compose.waitForIdle()
            compose.onRoot().captureRoboImage(File(output, "${spec.id}.png").absolutePath)
            assertEquals(spec.id, state.value.model.modelId)
            assertEquals(0, downloads)
            compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("selected_model_name"))
            compose.onNodeWithTag("selected_model_name").assertTextEquals("Ausgewählt: ${spec.name}")
        }
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("download_model"))
        compose.onNodeWithTag("download_model").performClick()
        assertEquals(1, downloads)
    }

    @Test fun selectionIsDisabledDuringDownloadAndGeneration() {
        val state = mutableStateOf(AppUiState(screen = AppScreen.SETUP, modelBusy = true, model = ModelState(stage = ModelStage.DOWNLOADING)))
        compose.setContent { StoryApp(state.value, ScreenshotNoOpActions) }
        val spec = LocalModelCatalog.models.last()
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("select_model_${spec.id}"))
        compose.onNodeWithTag("select_model_${spec.id}").assertIsNotEnabled()
        compose.runOnIdle { state.value = state.value.copy(modelBusy = false, busy = true, model = ModelState(stage = ModelStage.GENERATING)) }
        compose.onNodeWithTag("select_model_${spec.id}").assertIsNotEnabled()
    }

    @Test
    fun largerTextKeepsTheLastModelSelectableAndUpdatesReachable() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.3f)) {
                StoryApp(AppUiState(screen = AppScreen.SETUP), ScreenshotNoOpActions)
            }
        }
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("select_model_gemma3-davidau-4b"))
        compose.onNodeWithTag("select_model_gemma3-davidau-4b").assertIsDisplayed()
        compose.onRoot().captureRoboImage(File("build/ui-screenshots/models-0.7.1/large-text.png").absolutePath)
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("app_updates"))
        compose.onNodeWithTag("app_updates").assertIsDisplayed()
    }
}
