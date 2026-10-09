package dev.vincent.geschichten

import android.app.Application
import android.os.Looper
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.google.gson.*
import dev.vincent.geschichten.ai.*
import dev.vincent.geschichten.data.*
import dev.vincent.geschichten.memory.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class TeamFlowTest {
    private class Narrator:StoryGeneration {
        override val state=MutableStateFlow(ModelState(stage=ModelStage.READY))
        override suspend fun generate(systemPrompt:String,history:List<ModelMessage>,recordTiming:Boolean,onToken:(String)->Unit):String=error("Team must use its separate port")
        override fun cancelGeneration(){}
        override suspend fun clearConversationCache(){}
        override fun close(){}
    }
    private class Port:TeamPort {
        var role=TeamRole.HELPER;var entered=false;val release=CompletableDeferred<Unit>()
        override suspend fun activate(role:TeamRole){this.role=role}
        override suspend fun count(system:String,history:List<ModelMessage>)=100
        override suspend fun generate(system:String,history:List<ModelMessage>,onToken:(String)->Unit):String {
            if(role==TeamRole.NARRATOR){onToken("UNGEPRUEFT_UND_NICHT_ANZUZEIGEN");return "„Der Ring gehört dir.“"}
            val w=JsonParser.parseString(history.last().text.substringAfter("DATEN:\n").substringBefore("\nANTWORT (Daten):")).asJsonObject
            val facts=system.contains("Faktenauswertung.")
            if(!facts){entered=true;release.await()}
            return JsonObject().apply {add("story",w["story"]);add("request",w["request"]);add("version",w["version"]);if(facts){add("facts",JsonArray());add("unknown",JsonArray())}else{addProperty("verdict","clear");add("issues",JsonArray())}}.toString()
        }
    }
    private fun await(test:()->Boolean) {
        val end=System.nanoTime()+20_000_000_000
        while(!test()){check(System.nanoTime()<end){"ViewModel timeout"};shadowOf(Looper.getMainLooper()).idle();Thread.sleep(10)}
    }
    @Test fun actualTeamDraftIsInvisibleAndCancellationCannotCommitItOrUndoSavedCorrection() {
        val app=ApplicationProvider.getApplicationContext<Application>();app.deleteDatabase("geschichten.db")
        app.getSharedPreferences("preferences",0).edit().clear().commit()
        val s=StoryRepository(app).use {r ->val story=r.createStory("mira");val m=r.appendMessage(story.id,ChatRole.USER,"Ich heiße Rian. Der Ring gehört mir.");r.commitReply(story.id,r.bundle(story.id)!!.memory.version,m.id,m.id,"„Verstanden.“");story}
        val port=Port();val vm=AppViewModel(app,Narrator(),port);val store=ViewModelStore().also {it.put("team",vm)}
        try {
            await {vm.state.value.stories.isNotEmpty()};assertFalse(vm.state.value.teamEnabled)
            vm.openStory(s.id);await {vm.state.value.current?.story?.id==s.id};vm.setTeamEnabled(true)
            await {if(!vm.state.value.busy){vm.changeDraft("Wem gehört der Ring?");vm.sendMessage()};port.entered}
            assertEquals("",vm.state.value.partialReply);assertTrue(vm.state.value.teamStatus.contains("prüft"))
            StoryRepository(app).use {r ->
                assertFalse(r.bundle(s.id)!!.messages.any {it.text.contains("UNGEPRUEFT") || it.text=="„Der Ring gehört dir.“"})
                val old=r.bundle(s.id)!!.memory.current.single {it.field=="owner"};r.editStateFact(s.id,old.id,"Uta",false,false,true)
            }
            vm.stopGeneration();await {!vm.state.value.busy};port.release.complete(Unit)
            assertEquals("Wem gehört der Ring?",vm.state.value.draft)
            StoryRepository(app).use {r ->assertTrue(r.bundle(s.id)!!.memory.current.any {it.manual && it.value=="Uta"});assertTrue(r.bundle(s.id)!!.messages.none {it.text=="„Der Ring gehört dir.“"})}
        }finally{store.clear()}
    }
}
