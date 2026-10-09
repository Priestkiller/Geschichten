package dev.vincent.geschichten.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import dev.vincent.geschichten.ai.*
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
class ModelPerformanceUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun s24UltraCanStartItsOwnTimingAndShowsRealTimingFieldsWithoutStoryOutput() {
        var started = 0
        val actions = object : AppActions by ScreenshotNoOpActions { override fun optimizeModel() { started++ } }
        compose.setContent { StoryApp(AppUiState(screen = AppScreen.SETUP, model = ModelState(stage = ModelStage.READY,
            deviceName = "Galaxy S24 Ultra", cpuThreads = 6, firstTextMs = 1234,
            performanceDetail = "Testdaten zur Darstellung: 4 Threads: 3,2 s · 6 Threads: 2,1 s. Gespeichert: 6 Threads.")), actions) }
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("optimize_model"))
        compose.onNodeWithTag("optimize_model").assertIsEnabled().performClick()
        assertEquals(1, started)
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("model_performance"))
        compose.onNodeWithTag("performance_result").assertExists()
        val target = File("build/ui-screenshots/performance-0.7.3/measurement-card.png").apply { parentFile!!.mkdirs() }
        compose.onNodeWithTag("model_performance").captureRoboImage(target.absolutePath)
    }
    @Test fun timingIsDisabledDuringAReplyOrWithoutALoadedModel() {
        compose.setContent { StoryApp(AppUiState(screen = AppScreen.SETUP, busy = true,
            model = ModelState(stage = ModelStage.READY, deviceName = "Galaxy S24")), ScreenshotNoOpActions) }
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("optimize_model"))
        compose.onNodeWithTag("optimize_model").assertIsNotEnabled()
    }
    @Test fun runningMeasurementOffersCancellationAndBlocksModelSwitching() {
        var cancelled = 0
        val actions = object : AppActions by ScreenshotNoOpActions { override fun cancelModelOptimization() { cancelled++ } }
        compose.setContent { StoryApp(AppUiState(screen = AppScreen.SETUP, modelBusy = true,
            model = ModelState(stage = ModelStage.LOADING, optimizing = true, deviceName = "Galaxy S24")), actions) }
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("cancel_model_optimization"))
        compose.onNodeWithTag("cancel_model_optimization").performClick()
        assertEquals(1, cancelled)
        compose.onNodeWithTag("optimize_model").assertDoesNotExist()
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("select_model_qwen3-0.6b"))
        compose.onNodeWithTag("select_model_qwen3-0.6b").assertIsNotEnabled()
    }
}
