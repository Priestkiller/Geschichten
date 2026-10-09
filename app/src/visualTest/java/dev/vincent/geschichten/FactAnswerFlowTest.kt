package dev.vincent.geschichten

import android.app.Application
import android.os.Looper
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import dev.vincent.geschichten.ai.*
import dev.vincent.geschichten.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class FactAnswerFlowTest {
    private class Backend:StoryGeneration {
        override val state=MutableStateFlow(ModelState(stage=ModelStage.READY,modelId="huihui-qwen3-4b"))
        val inputs=CopyOnWriteArrayList<String>()
        override suspend fun countPrompt(system:String,history:List<ModelMessage>)=(system.length+history.sumOf {it.text.length})/4
        override suspend fun generate(systemPrompt:String,history:List<ModelMessage>,recordTiming:Boolean,onToken:(String)->Unit):String {
            inputs+=systemPrompt+history.joinToString("\n"){it.text}
            return "*Mira lächelt kurz.* „Ich höre zu.“"
        }
        override fun cancelGeneration() {}
        override suspend fun clearConversationCache() {}
        override fun close() {}
    }
    private fun await(test:()->Boolean) {
        val end=System.nanoTime()+15_000_000_000
        while(!test()){check(System.nanoTime()<end){"ViewModel timeout"};shadowOf(Looper.getMainLooper()).idle();Thread.sleep(10)}
    }
    @Test fun actualSendRoutesOnlyEnabledMemoryQuestionsAndKeepsChatsAndModelSelectionOnRestart() {
        val app=ApplicationProvider.getApplicationContext<Application>();app.deleteDatabase("geschichten.db")
        val prefs=app.getSharedPreferences("preferences",0);prefs.edit().clear().commit()
        val selection=app.getSharedPreferences("model_selection",0);selection.edit().putString("model_id","huihui-qwen3-4b").commit()
        val selectedBefore=selection.all
        val s=StoryRepository(app).use {r->
            val story=r.createStory("mira");val m=r.appendMessage(story.id,ChatRole.USER,"Ich heiße Rian. Der Ring gehört mir. Du hältst den Ring.")
            r.commitReply(story.id,r.bundle(story.id)!!.memory.version,m.id,m.id,"„Verstanden.“");story
        }
        val backend=Backend();val vm=AppViewModel(app,backend);val store=ViewModelStore().also {it.put("test",vm)}
        try {
            await {vm.state.value.stories.isNotEmpty()};assertFalse(vm.state.value.factsAnswers)
            vm.openStory(s.id);await {vm.state.value.current?.story?.id==s.id}
            vm.setFactsAnswers(true)
            fun send(text:String) {
                val n=vm.state.value.current!!.messages.size
                // Opening refresh can still own navigationWork after publishing current.
                await {if(!vm.state.value.busy && vm.state.value.current!!.messages.size==n){vm.changeDraft(text);vm.sendMessage()};vm.state.value.busy || vm.state.value.current!!.messages.size>n}
                await {!vm.state.value.busy && vm.state.value.current!!.messages.size==n+2}
            }
            send("Wem gehört der Ring?")
            assertEquals("„Der Ring gehört dir.“",vm.state.value.current!!.messages.last().text);assertTrue(backend.inputs.isEmpty())
            send("Ich winke dir zu.");assertEquals(1,backend.inputs.size)
            vm.setFactsAnswers(false);send("Wem gehört der Ring?");assertEquals(2,backend.inputs.size)
            assertTrue(backend.inputs.last().contains("Ring · Eigentümer: Rian"))
            vm.setFactsAnswers(true);assertEquals(selectedBefore,selection.all)
        }finally{store.clear()}
        val saved=StoryRepository(app).use {it.bundle(s.id)!!}
        val restarted=AppViewModel(app,Backend());val next=ViewModelStore().also {it.put("restart",restarted)}
        try {await {restarted.state.value.stories.isNotEmpty()};assertTrue(restarted.state.value.factsAnswers);restarted.openStory(s.id);await {restarted.state.value.current?.story?.id==s.id};assertEquals(saved.messages,restarted.state.value.current!!.messages);assertEquals(selectedBefore,selection.all)}finally{next.clear()}
    }
}
