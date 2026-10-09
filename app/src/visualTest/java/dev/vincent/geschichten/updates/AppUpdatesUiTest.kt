package dev.vincent.geschichten.updates

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.github.takahirom.roborazzi.captureRoboImage
import dev.vincent.geschichten.ai.ModelStage
import dev.vincent.geschichten.ai.ModelState
import dev.vincent.geschichten.data.CharacterProfile
import dev.vincent.geschichten.data.MemoryEntry
import dev.vincent.geschichten.ui.AppActions
import dev.vincent.geschichten.ui.AppScreen
import dev.vincent.geschichten.ui.AppUiState
import dev.vincent.geschichten.ui.StoryApp
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Real Compose UI with authored update states; no GitHub request or installation is made. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppUpdatesUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun updateButtonInvokesCheckOnlyAfterTapAndRendersNatively() {
        val actions = RecordingActions()
        render(
            AppUpdateState(
                repository = "Priestkiller/Geschichten", installedVersion = "0.2.0", installedVersionCode = 2,
                stage = UpdateStage.IDLE, detail = "Updates werden nur gesucht, wenn du den Button antippst.",
            ), actions,
        )
        assertEquals(0, actions.checks)
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("check_app_updates"))
        compose.onNodeWithTag("check_app_updates").assertIsDisplayed().performClick()
        compose.waitForIdle()
        assertEquals(1, actions.checks)
        assertEquals(0, actions.installs)
        val output = File("build/ui-screenshots").apply { mkdirs() }
        compose.onRoot().captureRoboImage(File(output, "07-app-updates.png").absolutePath)
    }

    @Test fun missingRepositoryDoesNotPretendToBeCurrentOrStartCheck() {
        val actions = RecordingActions()
        render(AppUpdateState(), actions)
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("check_app_updates"))
        compose.onNodeWithTag("check_app_updates").assertIsNotEnabled()
        assertEquals(0, actions.checks)
    }

    @Test fun verifiedUpdateRequiresSeparateExplicitInstallTap() {
        val actions = RecordingActions()
        val candidate = AvailableUpdate(
            repository = "example/Geschichten", versionCode = 3, versionName = "test-fixture",
            prerelease = true, releaseTag = "test-fixture", changelog = "Authored test fixture; not a published release.",
            minSdk = 31, assetName = "test-fixture.apk", downloadUrl = "https://github.com/example/Geschichten/releases/download/test-fixture/test-fixture.apk",
            byteCount = 123456, sha256 = "a".repeat(64),
        )
        render(AppUpdateState(repository = candidate.repository, stage = UpdateStage.READY_TO_INSTALL, available = candidate, detail = "Test: geprüfter Download."), actions)
        assertEquals(0, actions.installs)
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("install_app_update"))
        compose.onNodeWithTag("install_app_update").assertIsDisplayed().performClick()
        compose.waitForIdle()
        assertEquals(1, actions.installs)
        assertEquals(0, actions.checks)
    }

    private fun render(updates: AppUpdateState, actions: AppActions) {
        compose.setContent { StoryApp(AppUiState(screen = AppScreen.SETUP, model = ModelState(stage = ModelStage.READY), updates = updates), actions) }
        compose.waitForIdle()
    }
}

private class RecordingActions : AppActions {
    var checks = 0
    var installs = 0
    override fun checkForUpdates() { checks++ }
    override fun installAppUpdate() { installs++ }
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
