package dev.vincent.geschichten.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import dev.vincent.geschichten.data.*
import dev.vincent.geschichten.memory.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MemoryUiTest {
    @get:Rule val compose=createComposeRule()
    private val character=BuiltinCharacters.profiles.first {it.id=="mira"}
    private val story=Story(id="synthetic-memory-story",characterId=character.id,title="Synthetische Gedächtnisprüfung")
    private val source=ChatMessage(id="synthetic-source",storyId=story.id,role=ChatRole.USER,text="Der Schlüssel ist silbern, nicht bronzen.")
    private val fact=StateFact("synthetic-fact",story.id,MemoryRules.identity(story.id,EntityKind.ITEM,"Schlüssel"),"color","silbern",sourceId=source.id,sourceText=source.text)
    private fun state()=AppUiState(screen=AppScreen.MEMORIES,characters=listOf(character),stories=listOf(story),current=StoryBundle(story,character,listOf(source),emptyList(),MemorySnapshot(facts=listOf(fact),knowledge=listOf(Knowledge(fact.id,"character",source.id,0)))))
    private fun output(name:String)=File("build/ui-screenshots/gedaechtnis/$name.png").apply {parentFile.mkdirs()}.absolutePath
    @Test fun readableStateProvenanceAndEditorWorkWithoutDisplayingIds(){
        val state=mutableStateOf(state());var saved:String?=null
        val actions=object:AppActions by ScreenshotNoOpActions {
            override fun editStateFact(storyId:String,factId:String,value:String,pinned:Boolean,exclude:Boolean,known:Boolean?){saved=value}
        }
        compose.setContent {StoryApp(state.value,actions)}
        compose.onNodeWithTag("memories_screen").performScrollToNode(hasTestTag("state_memory_${fact.id}"))
        compose.onNodeWithText("Schlüssel · Farbe").assertIsDisplayed();compose.onNodeWithText("silbern").assertIsDisplayed()
        compose.onNodeWithText(fact.id).assertDoesNotExist();compose.onRoot().captureRoboImage(output("01-zustand"))
        compose.onNodeWithTag("state_memory_${fact.id}").performClick()
        compose.onNodeWithTag("state_memory_editor").assertIsDisplayed();compose.onNodeWithText("Herkunft").assertIsDisplayed()
        compose.onNodeWithTag("state_memory_value").performTextReplacement("blau")
        compose.waitForIdle();compose.onNodeWithTag("state_memory_value").assertTextContains("blau")
        compose.onRoot().captureRoboImage(output("02-korrektur"))
        compose.onNodeWithTag("save_state_memory").performClick();assertEquals("blau",saved)
    }
    @Test fun excludedAndHistoricalFactsAreAvailableOnDemand(){
        val past=fact.copy(id="historical",status=FactStatus.HISTORICAL,value="bronzen")
        val b=state().current!!;compose.setContent {StoryApp(state().copy(current=b.copy(memory=b.memory.copy(facts=listOf(fact,past)))),ScreenshotNoOpActions)}
        compose.onNodeWithText("bronzen").assertDoesNotExist()
        compose.onNodeWithTag("memories_screen").performScrollToNode(hasText("Historische / ausgeschlossene Angaben ansehen"))
        compose.onNodeWithText("Historische / ausgeschlossene Angaben ansehen").performClick()
        compose.onNodeWithTag("memories_screen").performScrollToNode(hasTestTag("state_memory_historical"))
        compose.onNodeWithText("bronzen").assertIsDisplayed();compose.onRoot().captureRoboImage(output("03-historie"))
    }
    @Test fun manualCreationIsReachableAndKeepsErrorsVisible(){
        val state=mutableStateOf(state());var saved:String?=null
        val actions=object:AppActions by ScreenshotNoOpActions {
            override fun addStateFact(storyId:String,kind:EntityKind,name:String,field:String,value:String,known:Boolean){saved="$name:$field:$value";state.value=state.value.copy(notice="Testfehler: nicht gespeichert.")}
        }
        compose.setContent {StoryApp(state.value,actions)}
        compose.onNodeWithTag("memories_screen").performScrollToNode(hasText("Zustand festhalten"))
        compose.onNodeWithText("Zustand festhalten").performClick()
        compose.onNodeWithTag("state_memory_creation").assertIsDisplayed()
        compose.onNodeWithText("Gegenstand: Träger").performClick()
        compose.onNodeWithText("Gegenstand: Ablage (auf, in, unter …)").performClick()
        compose.onNodeWithTag("new_memory_name").performTextReplacement("Schlüssel")
        compose.onNodeWithTag("new_memory_value").performTextReplacement("in der Truhe")
        compose.onNodeWithTag("save_new_memory").performClick()
        assertEquals("Schlüssel:placement:in der Truhe",saved)
        compose.onNodeWithTag("state_memory_creation").assertIsDisplayed()
        compose.onRoot().captureRoboImage(output("04-neuer-zustand"))
    }
}
