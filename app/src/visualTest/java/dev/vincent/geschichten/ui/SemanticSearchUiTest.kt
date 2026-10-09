package dev.vincent.geschichten.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import dev.vincent.geschichten.memory.SemanticSearchState
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SemanticSearchUiTest {
    @get:Rule val compose=createComposeRule()
    @Test fun testOptionIsExplicitOffByDefaultAndDoesNotSelectATextModel() {
        var enabled:Boolean?=null;var download=false;var selected:String?=null
        val actions=object:AppActions by ScreenshotNoOpActions {
            override fun setSemanticSearch(value:Boolean){enabled=value}
            override fun downloadSearchModel(){download=true}
            override fun selectModel(id:String){selected=id}
        }
        compose.setContent {StoryApp(AppUiState(screen=AppScreen.SETUP),actions)}
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasTestTag("semantic_search_switch"))
        compose.onNodeWithTag("semantic_search_switch").assertIsOff().performClick()
        assertEquals(true,enabled);assertNull(selected)
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasText("Suchmodell herunterladen"))
        compose.onNodeWithText("Suchmodell herunterladen").performClick();assertTrue(download)
        compose.onRoot().captureRoboImage(File("build/ui-screenshots/semantic-search.png").apply {parentFile?.mkdirs()}.absolutePath)
    }
    @Test fun indexingStatusAndCancelAreVisibleWithoutDiscardingTheCurrentChat() {
        var stopped=false
        val actions=object:AppActions by ScreenshotNoOpActions {override fun stopSearchWork(){stopped=true}}
        compose.setContent {StoryApp(AppUiState(screen=AppScreen.SETUP,semanticSearch=SemanticSearchState(enabled=true,ready=true,busy=true,indexed=16,total=42,detail="Erinnerungen vorbereiten …")),actions)}
        compose.onNodeWithTag("setup_screen").performScrollToNode(hasText("Anhalten"))
        compose.onNodeWithText("16 von 42 Abschnitten dieser Geschichte vorbereitet.").assertIsDisplayed()
        compose.onNodeWithText("Anhalten").performClick();assertTrue(stopped)
    }
}
