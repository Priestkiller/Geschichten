package dev.vincent.geschichten

import android.app.Application
import android.os.Looper
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import dev.vincent.geschichten.ai.*
import dev.vincent.geschichten.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ChatContextIntegrationTest {
    private class Inference : StoryGeneration {
        override val state = MutableStateFlow(ModelState(stage = ModelStage.READY))
        val inputs = CopyOnWriteArrayList<Pair<String, List<ModelMessage>>>()
        var block = false
        var failAtLimit = false
        var replyOverride: String? = null
        var release: CompletableDeferred<Unit>? = null
        val entered = CompletableDeferred<Unit>()
        var lateCallback: ((String) -> Unit)? = null
        override suspend fun generate(systemPrompt: String, history: List<ModelMessage>, recordTiming: Boolean, onToken: (String) -> Unit): String {
            inputs += systemPrompt to history
            lateCallback = onToken
            if (block) { entered.complete(Unit); release?.await() ?: awaitCancellation() }
            if (failAtLimit) { onToken("UNVOLLSTAENDIGER_TEXT"); StoryOutputLimit.requireCompleted(512,512) }
            replyOverride?.let {return it}
            return if (systemPrompt.startsWith("Du pflegst")) "Rian besitzt den Schlüssel. Seine linke Hand ist verletzt. Mira und Rian sind im Turmzimmer."
            else if(systemPrompt.contains("Ablage: in der Truhe")) "*Mira nickt.* „Der Schlüssel liegt in der Truhe.“"
            else "*Mira blickt zu Rian.* „Rian hat den Schlüssel.“"
        }
        override suspend fun countPrompt(system: String, history: List<ModelMessage>): Int = (system.length+history.sumOf {it.text.length})/4 + 50
        override fun cancelGeneration() {}
        override suspend fun clearConversationCache() {}
        override fun close() {}
    }
    private fun await(condition: () -> Boolean) {
        val deadline = System.nanoTime() + 15_000_000_000L
        while (!condition()) { check(System.nanoTime() < deadline) { "ViewModel timeout" }; shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(10) }
    }
    private fun fresh(): Application = ApplicationProvider.getApplicationContext<Application>().also {
        it.deleteDatabase("geschichten.db"); it.getSharedPreferences("preferences", 0).edit().clear().commit()
        it.getSharedPreferences("model_selection",0).edit().clear().commit()
    }

    @Test fun earlyFactsAreSavedAndActuallyUsedWithoutAnExtraModelCall() {
        val app = fresh(); val backend = Inference()
        val story = StoryRepository(app).use { r ->
            val s = r.createStory("mira")
            r.appendMessage(s.id, ChatRole.USER, "Mira gibt Rian den Schlüssel. Rian trägt ihn jetzt.")
            r.appendMessage(s.id, ChatRole.CHARACTER, "„Du hast den Schlüssel, Rian.“")
            repeat(2) { r.appendMessage(s.id, ChatRole.USER, "Eine Wandfrage. ".repeat(45)); r.appendMessage(s.id, ChatRole.CHARACTER, "*Mira betrachtet die Wand.* ".repeat(50)) }
            s
        }
        val vm = AppViewModel(app, backend); val store = ViewModelStore().also { it.put("audit", vm) }
        try {
            await { vm.state.value.stories.isNotEmpty() }; vm.openStory(story.id); await { vm.state.value.current?.story?.id == story.id }
            await { vm.changeDraft("Wer hat den Schlüssel?"); vm.sendMessage(); backend.inputs.size >= 1 }
            await { !vm.state.value.busy }
            assertEquals(1,backend.inputs.size)
            assertTrue(backend.inputs[0].first.contains("Träger: Rian"))
            assertEquals(1, backend.inputs[0].second.count { it.user && it.text.endsWith("NÄCHSTE NUTZERNACHRICHT:\nWer hat den Schlüssel?") })
            StoryRepository(app).use { r ->
                assertEquals(9,r.bundle(story.id)!!.messages.size)
                assertTrue(r.bundle(story.id)!!.memory.current.any {it.field=="holder" && it.value=="Rian"})
            }
        } finally { store.clear() }
    }

    @Test fun legacySummaryCannotOverrideLaterStateAndDoesNotBlockTheNextReply() {
        val app = fresh(); val backend = Inference()
        val story = StoryRepository(app).use { r ->
            val s = r.createStory("mira")
            repeat(3) {
                r.appendMessage(s.id, ChatRole.USER, "ALTE_RUNDE_${it + 1}")
                r.appendMessage(s.id, ChatRole.CHARACTER, "„Diese Runde ist abgeschlossen.“")
            }
            r.updateSummary(s.id, "Rian besitzt den Schlüssel. Mira ist im Turmzimmer.")
            r.appendMessage(s.id, ChatRole.USER, "NEUE_RUNDE_4: Rian legt den Schlüssel in die Truhe.")
            r.appendMessage(s.id, ChatRole.CHARACTER, "*Mira betrachtet die Wand.* ".repeat(120))
            s
        }
        app.getSharedPreferences("preferences",0).edit().putInt("summary_turns_${story.id}",3).commit()
        val vm = AppViewModel(app,backend); val store = ViewModelStore().also { it.put("audit",vm) }
        try {
            await {vm.state.value.stories.isNotEmpty()}; vm.openStory(story.id); await {vm.state.value.current?.story?.id==story.id}
            await {vm.changeDraft("Wer hat den Schlüssel? " + "Weitere Frage. ".repeat(40));vm.sendMessage();backend.inputs.size>=1}
            await {!vm.state.value.busy}
            assertEquals(1,backend.inputs.size)
            assertTrue(backend.inputs[0].first.contains("in der Truhe"))
            assertFalse(backend.inputs[0].first.contains("Rian besitzt den Schlüssel. Mira ist im Turmzimmer."))
            StoryRepository(app).use {r -> assertTrue(r.bundle(story.id)!!.memory.current.any {it.field=="placement" && it.value=="in der Truhe"})}
            assertEquals(3,app.getSharedPreferences("preferences",0).getInt("summary_turns_${story.id}",0))
        } finally {store.clear()}
    }

    @Test fun duplicateSendAndChatSwitchAreBlockedAndCancelRestoresDraftWithoutSavingPartialReply() {
        val app = fresh(); val backend = Inference().apply { block = true }
        val stories = StoryRepository(app).use { listOf(it.createStory("mira"), it.createStory("grask")) }
        val vm = AppViewModel(app, backend); val store = ViewModelStore().also { it.put("audit", vm) }
        try {
            await { vm.state.value.stories.size == 2 }; vm.openStory(stories[0].id); await { vm.state.value.current?.story?.id == stories[0].id }
            await { vm.changeDraft("Ich heiße Rian."); vm.sendMessage(); vm.sendMessage(); backend.entered.isCompleted }
            vm.openStory(stories[1].id); assertEquals(stories[0].id, vm.state.value.current?.story?.id)
            assertEquals(1, backend.inputs.size)
            val cancelledCallback = backend.lateCallback
            vm.stopGeneration(); await { !vm.state.value.busy }
            assertEquals("Ich heiße Rian.", vm.state.value.draft)
            StoryRepository(app).use { assertEquals(1, it.bundle(stories[0].id)!!.messages.size) }
            vm.openStory(stories[1].id); await { vm.state.value.current?.story?.id == stories[1].id }
            // Deliberately late producer event: the ViewModel boundary must also reject it.
            cancelledCallback?.invoke("VERSPEAETETE_ANTWORT_A")
            assertEquals("", vm.state.value.partialReply)
            StoryRepository(app).use { assertEquals(1, it.bundle(stories[1].id)!!.messages.size) }
            await { vm.changeDraft("Neue Nachricht in B."); vm.sendMessage(); backend.inputs.size == 2 }
            cancelledCallback?.invoke("ALTE_ANTWORT_WAEHREND_NEUER_GENERIERUNG")
            assertEquals("",vm.state.value.partialReply)
            vm.stopGeneration(); await { !vm.state.value.busy }
        } finally { store.clear() }
    }

    @Test fun outputCapFailureDoesNotEnterTranscriptOrSummaryAndRestoresTheExactInput() {
        val app=fresh();val backend=Inference().apply {failAtLimit=true}
        val story=StoryRepository(app).use {it.createStory("mira")}
        val vm=AppViewModel(app,backend);val store=ViewModelStore().also {it.put("audit",vm)}
        try {
            await {vm.state.value.stories.isNotEmpty()};vm.openStory(story.id);await {vm.state.value.current?.story?.id==story.id}
            await {vm.changeDraft("Rian hat den Schlüssel.");vm.sendMessage();backend.inputs.isNotEmpty()}
            await {!vm.state.value.busy}
            assertEquals("Rian hat den Schlüssel.",vm.state.value.draft);assertEquals("",vm.state.value.partialReply)
            StoryRepository(app).use {r->assertEquals(1,r.bundle(story.id)!!.messages.size);assertEquals("",r.bundle(story.id)!!.story.summary)}
            assertTrue(vm.state.value.notice.orEmpty().contains("Ausgabelimit"))
        } finally {store.clear()}
    }

    @Test fun pendingInputSurvivesViewModelRecreationAndDifferentChatsStayIsolated() {
        val app = fresh(); val pair = StoryRepository(app).use { r ->
            val a=r.createStory("mira");val b=r.createStory("grask")
            r.appendMessage(a.id,ChatRole.USER,"Ich bin Rian, meine linke Hand ist verletzt.")
            r.appendMessage(a.id,ChatRole.CHARACTER,"„Ich merke es mir.“")
            r.appendMessage(a.id,ChatRole.USER,"Nach Prozessabbruch noch unbeantwortet."); listOf(a,b)
        }
        val vm=AppViewModel(app,Inference()); val store=ViewModelStore().also {it.put("audit",vm)}
        try {
            await {vm.state.value.stories.size==2};vm.openStory(pair[0].id);await {vm.state.value.current?.story?.id==pair[0].id}
            assertEquals("Nach Prozessabbruch noch unbeantwortet.",vm.state.value.draft)
            assertEquals(3,vm.state.value.current!!.messages.size)
            await {vm.openStory(pair[1].id);vm.state.value.current?.story?.id==pair[1].id}
            assertEquals("",vm.state.value.draft);assertFalse(StoryPrompt.history(vm.state.value.current!!).any {"Rian" in it.text})
        } finally {store.clear()}
    }

    @Test fun immediateUserFactsAndOldOriginalArePassedToTheActualGenerationBoundary() {
        val app=fresh();val backend=Inference().apply {replyOverride="*Mira nickt.* „Morgenstern.“"}
        val story=StoryRepository(app).use {r ->
            val s=r.createStory("mira")
            r.appendMessage(s.id,ChatRole.USER,"Das Losungswort am Brunnen lautet Morgenstern.")
            r.appendMessage(s.id,ChatRole.CHARACTER,"„Verstanden.“")
            repeat(12){i ->r.appendMessage(s.id,ChatRole.USER,"Ein Vorgang $i. ".repeat(70));r.appendMessage(s.id,ChatRole.CHARACTER,"*Mira betrachtet Wand $i.* ".repeat(70))}
            s
        }
        val vm=AppViewModel(app,backend);val store=ViewModelStore().also {it.put("audit",vm)}
        try {
            await {vm.state.value.stories.isNotEmpty()};vm.openStory(story.id);await {vm.state.value.current?.story?.id==story.id}
            await {vm.changeDraft("Der Schlüssel ist blau. Wie lautet das Losungswort am Brunnen?");vm.sendMessage();backend.inputs.isNotEmpty()}
            await {!vm.state.value.busy}
            val input=backend.inputs.single()
            assertTrue(input.first.contains("Farbe: blau"));assertTrue(input.first.contains("Originalstelle"));assertTrue(input.first.contains("Das Losungswort am Brunnen lautet Morgenstern."))
            assertTrue(input.second.any {it.text.contains("Wand 11")})
            StoryRepository(app).use {r ->assertTrue(r.bundle(story.id)!!.memory.current.any {it.field=="color" && it.value=="blau"})}
        } finally {store.clear()}
    }

    @Test fun savedManualCorrectionSurvivesGenerationFailure() {
        val app=fresh();val backend=Inference().apply {failAtLimit=true}
        val story=StoryRepository(app).use {r ->r.createStory("mira").also {r.addStateFact(it.id,dev.vincent.geschichten.memory.EntityKind.ITEM,"Schlüssel","color","blau",true)}}
        val vm=AppViewModel(app,backend);val store=ViewModelStore().also {it.put("audit",vm)}
        try {
            await {vm.state.value.stories.isNotEmpty()};vm.openStory(story.id);await {vm.state.value.current?.story?.id==story.id}
            await {vm.changeDraft("Wir sind nun im Hof.");vm.sendMessage();backend.inputs.isNotEmpty()};await {!vm.state.value.busy}
            assertTrue(backend.inputs.single().first.contains("Farbe: blau"))
            StoryRepository(app).use {r ->val b=r.bundle(story.id)!!;assertTrue(b.memory.current.any {it.manual && it.value=="blau"});assertFalse(b.memory.current.any {it.value=="Hof"});assertEquals(1,b.messages.size)}
            assertEquals("Wir sind nun im Hof.",vm.state.value.draft)
        } finally {store.clear()}
    }

    @Test fun lateAnswerCannotReplaceCorrectionMadeAfterPromptPlanning() {
        val app=fresh();val backend=Inference().apply {block=true;release=CompletableDeferred();replyOverride="*Mira nickt.* „Verstanden.“"}
        val story=StoryRepository(app).use {r ->r.createStory("mira").also {r.addStateFact(it.id,dev.vincent.geschichten.memory.EntityKind.ITEM,"Schlüssel","color","blau",true)}}
        val vm=AppViewModel(app,backend);val store=ViewModelStore().also {it.put("audit",vm)}
        try {
            await {vm.state.value.stories.isNotEmpty()};vm.openStory(story.id);await {vm.state.value.current?.story?.id==story.id}
            await {vm.changeDraft("Was machen wir jetzt?");vm.sendMessage();backend.entered.isCompleted}
            StoryRepository(app).use {r ->val f=r.bundle(story.id)!!.memory.current.first {it.field=="color"};r.editStateFact(story.id,f.id,"grün",true)}
            backend.release!!.complete(Unit);await {!vm.state.value.busy}
            StoryRepository(app).use {r ->val b=r.bundle(story.id)!!;assertTrue(b.memory.current.any {it.manual && it.value=="grün"});assertEquals(1,b.messages.size)}
            assertTrue(vm.state.value.notice.orEmpty().contains("inzwischen geändert"))
        } finally {store.clear()}
    }
}
