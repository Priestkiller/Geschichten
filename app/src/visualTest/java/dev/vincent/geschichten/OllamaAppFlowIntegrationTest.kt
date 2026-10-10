package dev.vincent.geschichten

import android.app.Application
import android.os.Looper
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import dev.vincent.geschichten.ai.*
import dev.vincent.geschichten.data.*
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class OllamaAppFlowIntegrationTest {
    private fun fresh(): Application = ApplicationProvider.getApplicationContext<Application>().also {
        it.deleteDatabase("geschichten.db")
        it.getSharedPreferences("preferences",0).edit().clear().commit()
        it.getSharedPreferences("model_selection",0).edit().clear().commit()
    }
    private fun await(seconds: Int = 15, condition: ()->Boolean) {
        val deadline=System.nanoTime()+seconds*1_000_000_000L
        while(!condition()) { check(System.nanoTime()<deadline) {"App flow timeout"}; shadowOf(Looper.getMainLooper()).idle();Thread.sleep(10) }
    }
    @Test fun failedConnectionAndSwitchingBackKeepStoryDraftAndStoredSettings() {
        val app=fresh()
        val story=StoryRepository(app).use {it.createStory("hedda")}
        val preferences=app.getSharedPreferences("preferences",0)
        preferences.edit().putBoolean("ollama_enabled",true).putString("ollama_address","http://127.0.0.1:1")
            .putString("ollama_model","geschichten-gemma4-12b-test").putString("draft_${story.id}","Mein Entwurf bleibt.").commit()
        val vm=AppViewModel(app);val store=ViewModelStore().also {it.put("server-settings",vm)}
        try {
            await { vm.state.value.stories.size==1 && !vm.state.value.modelBusy && vm.state.value.model.stage==ModelStage.ERROR }
            assertTrue(vm.state.value.server.enabled)
            assertEquals("http://127.0.0.1:1",vm.state.value.server.address)
            vm.openStory(story.id);await {vm.state.value.current?.story?.id==story.id}
            assertEquals("Mein Entwurf bleibt.",vm.state.value.draft)
            vm.setServerEnabled(false);await {!vm.state.value.modelBusy && !vm.state.value.server.enabled}
            assertFalse(preferences.getBoolean("ollama_enabled",true))
            assertEquals("http://127.0.0.1:1",preferences.getString("ollama_address",null))
            assertEquals("Mein Entwurf bleibt.",vm.state.value.draft)
            StoryRepository(app).use {assertEquals(1,it.bundle(story.id)!!.messages.size);assertEquals(90,it.characters().size)}
        } finally {store.clear()}
    }
    @Test fun realLanReplyUsesConfirmedAppFactsAndPersistsOnlyVisibleAnswer() {
        assumeTrue("Explicit local Ollama validation only",System.getProperty("ollamaLive")=="true")
        val app=fresh()
        val story=StoryRepository(app).use { repository ->
            val created=repository.createStory("hedda")
            repository.appendMessage(created.id,ChatRole.USER,"Ich heiße Alva. Mein Bruder Leif hat dir die Kette gegeben.")
            repository.appendMessage(created.id,ChatRole.CHARACTER,"„Ich höre zu.“")
            created
        }
        val output=File("build/test-output/ollama-live-app")
        val backend=DesktopOllamaInference("http://192.168.178.73:11434",File(checkNotNull(System.getProperty("ollamaTokenizerExecutable"))),
            File("src/main/assets/tokenizers/gemma4-12b-ollama.gguf"),output)
        runBlocking {backend.connect()}
        val vm=AppViewModel(app,backend);val store=ViewModelStore().also {it.put("live-server",vm)}
        try {
            await {vm.state.value.stories.size==1};vm.openStory(story.id);await {vm.state.value.current?.story?.id==story.id}
            vm.changeDraft("Wie heiße ich?")
            await { vm.sendMessage(); vm.state.value.busy }
            await(600) {!vm.state.value.busy}
            assertTrue(vm.state.value.notice.orEmpty(),vm.state.value.draft.isEmpty())
            assertEquals("",vm.state.value.partialReply)
            StoryRepository(app).use { repository ->
                val saved=repository.bundle(story.id)!!
                assertEquals(5,saved.messages.size)
                val reply=saved.messages.last()
                assertEquals(ChatRole.CHARACTER,reply.role)
                assertTrue(reply.text.contains("Alva"))
                assertFalse(reply.text.contains("<|channel>"))
                assertFalse(reply.text.contains("<think>"))
                assertTrue(saved.memory.current.any {it.field=="identity" && it.value=="Alva"})
                File(output,"persisted-story.json").writeText(com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(saved))
            }
        } finally {store.clear()}
    }
}
