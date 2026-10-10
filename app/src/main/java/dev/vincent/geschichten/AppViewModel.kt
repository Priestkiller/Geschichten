package dev.vincent.geschichten

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.vincent.geschichten.ai.LocalModelEngine
import dev.vincent.geschichten.ai.LocalModelCatalog
import dev.vincent.geschichten.ai.ModelStage
import dev.vincent.geschichten.ai.StoryPrompt
import dev.vincent.geschichten.memory.*
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import dev.vincent.geschichten.ai.StoryGeneration
import dev.vincent.geschichten.ai.StoryReplyValidation
import dev.vincent.geschichten.ai.TeamEnginePort
import dev.vincent.geschichten.ai.OllamaSettings
import dev.vincent.geschichten.ai.OllamaProtocol
import dev.vincent.geschichten.ai.OllamaStoryEngine
import dev.vincent.geschichten.data.CharacterProfile
import dev.vincent.geschichten.data.ChatRole
import dev.vincent.geschichten.data.MemoryEntry
import dev.vincent.geschichten.data.MemoryKind
import dev.vincent.geschichten.data.StoryBundle
import dev.vincent.geschichten.data.StoryRepository
import dev.vincent.geschichten.ui.AppActions
import dev.vincent.geschichten.ui.AppScreen
import dev.vincent.geschichten.ui.AppUiState
import dev.vincent.geschichten.updates.AppUpdater
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StoryExport(val fileName: String, val text: String)

/** Coordinates real on-device inference with local persistence and the native UI. */
class AppViewModel @JvmOverloads constructor(application: Application, private val suppliedInference: StoryGeneration? = null, suppliedTeamPort: TeamPort? = null) : AndroidViewModel(application), AppActions {
    private val repository = StoryRepository(application)
    private val engine = LocalModelEngine(application)
    private val preferences = application.getSharedPreferences("preferences", 0)
    private var serverSettings = OllamaSettings(
        enabled = suppliedInference == null && preferences.getBoolean("ollama_enabled", false),
        address = preferences.getString("ollama_address", null) ?: OllamaSettings().address,
        model = preferences.getString("ollama_model", null) ?: OllamaSettings().model,
    )
    private val serverEngine = OllamaStoryEngine(application, serverSettings)
    private val inference: StoryGeneration get() = suppliedInference ?: if (serverSettings.enabled) serverEngine else engine
    private val helperEngine = LocalModelEngine(application,"helper_model_selection","huihui-qwen3-4b")
    private val nativeTeamPort = TeamEnginePort(engine,helperEngine)
    private val teamPort:TeamPort = suppliedTeamPort ?: nativeTeamPort
    private val updater = AppUpdater(application, BuildConfig.UPDATE_REPOSITORY, BuildConfig.UPDATE_INCLUDE_PRERELEASE)
    private val semanticSearch = SemanticSearch(application)
    private var indexWork: Job? = null
    private var searchDownloadWork: Job? = null
    private val _state = MutableStateFlow(
        AppUiState(model = inference.state.value, adultThemes = preferences.getBoolean("adult_themes", false), updates = updater.state.value,
            factsAnswers = preferences.getBoolean("facts_answers", false),teamEnabled=preferences.getBoolean("team_enabled",false),helperModel=helperEngine.state.value,server=serverSettings),
    )
    val state = _state.asStateFlow()
    private val exportRequests = Channel<StoryExport>(Channel.BUFFERED)
    val exports = exportRequests.receiveAsFlow()
    private val installRequests = Channel<File>(Channel.BUFFERED)
    val installations = installRequests.receiveAsFlow()
    private var pendingExport: StoryExport? = null
    private var generation: Job? = null
    private var modelWork: Job? = null
    private var navigationWork: Job? = null
    private var updateWork: Job? = null
    private val drafts = mutableMapOf<String, String>()
    private var historyRevision = 0L
    private val generationRevision = AtomicLong(0)

    init {
        viewModelScope.launch {helperEngine.state.collect {s ->_state.update {it.copy(helperModel=s)}}}
        viewModelScope.launch {semanticSearch.state.collect {s -> _state.update {it.copy(semanticSearch=s)}}}
        listOfNotNull(engine, serverEngine, suppliedInference).distinct().forEach { provider -> viewModelScope.launch { provider.state.collect { model ->
            if (inference !== provider) return@collect
            _state.update { it.copy(model = model) }
            val previous = preferences.getString("pending_model_cleanup", null)
            if (!model.server && model.stage == ModelStage.READY && previous != model.modelId && previous in model.downloadedModelIds) {
                _state.update { it.copy(modelCleanupId = previous) }
            }
        } } }
        viewModelScope.launch { updater.state.collect { updates -> _state.update { it.copy(updates = updates) } } }
        // Recover staged APK metadata locally; never check GitHub automatically.
        viewModelScope.launch { updater.restoreDownloadedUpdate() }
        viewModelScope.launch {
            try {
                refresh()
            } catch (error: Exception) {
                showNotice(error.message ?: "Die gespeicherten Geschichten konnten nicht geöffnet werden.")
            }
        }
        if (serverSettings.enabled) modelAction { serverEngine.connect(serverSettings) }
        else if (engine.state.value.stage == ModelStage.DOWNLOADED) loadModel()
    }

    override fun navigate(screen: AppScreen) {
        _state.update { current ->
            current.copy(screen = if (screen == AppScreen.CHAT && current.current == null) AppScreen.STORIES else screen)
        }
    }

    override fun selectCharacter(character: CharacterProfile) = openCharacter(character, forceNew = false)

    override fun startNewStory(character: CharacterProfile) = openCharacter(character, forceNew = true)

    private fun openCharacter(character: CharacterProfile, forceNew: Boolean) {
        if (!canChangeStory() || navigationWork?.isActive == true) return
        navigationWork = viewModelScope.launch {
            try {
                val storyId = withContext(Dispatchers.IO) {
                    val existing = if (forceNew) null else repository.stories().firstOrNull { it.characterId == character.id }
                    (existing ?: repository.createStory(character.id)).id
                }
                selectStory(storyId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                showNotice(error.message ?: "Die Geschichte konnte nicht geöffnet werden.")
            }
        }
    }

    override fun openStory(storyId: String) {
        if (!canChangeStory() || navigationWork?.isActive == true) return
        navigationWork = viewModelScope.launch {
            try {
                selectStory(storyId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                showNotice(error.message ?: "Die Geschichte konnte nicht geöffnet werden.")
            }
        }
    }

    override fun clearHistory() {
        val snapshot = _state.value
        if (snapshot.busy || snapshot.modelBusy || snapshot.historyBusy || navigationWork?.isActive == true) return
        historyRevision++
        _state.update { it.copy(historyBusy = true) }
        navigationWork = viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.clearHistory()
                    val editor = preferences.edit()
                    preferences.all.keys.filter { it.startsWith("draft_") || it.startsWith("summary_turns_") }
                        .forEach { editor.remove(it) }
                    editor.apply()
                }
                inference.clearConversationCache()
                drafts.clear()
                pendingExport = null
                _state.update { it.copy(current = null, stories = emptyList(), draft = "", partialReply = "") }
                refresh()
                showNotice("Der Verlauf wurde geleert.")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                showNotice(error.message ?: "Der Verlauf konnte nicht geleert werden.")
            } finally {
                _state.update { it.copy(historyBusy = false) }
            }
        }
    }

    private suspend fun selectStory(storyId: String) {
        val previous = _state.value
        previous.current?.let { drafts[it.story.id] = previous.draft }
        var recoveredDraft: String? = null
        val bundle = withContext(Dispatchers.IO) {
            val saved = repository.bundle(storyId) ?: error("Diese Geschichte wurde nicht gefunden.")
            // A process can be killed during native inference. Restore its unanswered input
            // to the editor, instead of sending a second USER turn or inventing a reply.
            saved.messages.lastOrNull()?.takeIf { it.role == ChatRole.USER }?.let { pending ->
                check(preferences.edit().putString(draftKey(storyId), pending.text).commit()) {
                    "Deine Nachricht bleibt im Verlauf gesichert. Bitte prüfe den freien Speicher und öffne die Geschichte erneut."
                }
                recoveredDraft = pending.text
                repository.deleteLastUserMessageIfUnanswered(storyId)
            }
            repository.backfillMemory(storyId)
            repository.bundle(storyId) ?: error("Diese Geschichte wurde nicht gefunden.")
        }
        if (recoveredDraft != null) drafts[storyId] = recoveredDraft.orEmpty()
        _state.update {
            it.copy(screen = AppScreen.CHAT, current = bundle,
                draft = preferences.getString(draftKey(storyId), drafts[storyId].orEmpty()).orEmpty(), partialReply = "")
        }
        refresh()
        if (recoveredDraft != null) showNotice("Die letzte Antwort wurde unterbrochen. Deine Nachricht steht wieder im Eingabefeld.")
    }

    private fun canChangeStory(): Boolean {
        if (_state.value.historyBusy) return false
        if (!_state.value.busy) return true
        showNotice("Halte die laufende Antwort an, bevor du die Geschichte wechselst.")
        return false
    }

    override fun createCharacter() {
        if (canChangeStory()) _state.update { it.copy(screen = AppScreen.EDITOR, editingCharacter = null) }
    }

    override fun editCharacter(character: CharacterProfile) {
        if (canChangeStory()) _state.update { it.copy(screen = AppScreen.EDITOR, editingCharacter = character) }
    }

    override fun saveCharacter(character: CharacterProfile) {
        if (!canChangeStory()) return
        if (character.name.isBlank() || character.personality.isBlank() || character.scenario.isBlank()) {
            showNotice("Name, Persönlichkeit und Ausgangsszene brauchen einen Text.")
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { repository.upsertCharacter(character) }
                refresh()
                _state.update { it.copy(screen = AppScreen.CHARACTERS, editingCharacter = null) }
                showNotice("${character.name} ist gespeichert.")
            } catch (error: Exception) {
                showNotice(error.message ?: "Die Figur konnte nicht gespeichert werden.")
            }
        }
    }

    override fun changeDraft(value: String) {
        val snapshot = _state.value
        if (!snapshot.busy && !snapshot.historyBusy && value.length <= StoryPrompt.MAX_USER_MESSAGE_CHARS) {
            _state.update { it.copy(draft = value) }
            snapshot.current?.let { preferences.edit().putString(draftKey(it.story.id), value).apply() }
        }
    }

    override fun sendMessage() {
        val snapshot = _state.value
        val bundle = snapshot.current ?: return
        val input = snapshot.draft.trim()
        if (snapshot.busy || snapshot.memoryBusy || snapshot.historyBusy || snapshot.modelBusy || modelWork?.isActive == true || navigationWork?.isActive == true || input.isBlank()) return
        if (input.length > StoryPrompt.MAX_USER_MESSAGE_CHARS) {
            showNotice("Bitte kürze deine Nachricht auf höchstens ${StoryPrompt.MAX_USER_MESSAGE_CHARS} Zeichen.")
            return
        }
        if (snapshot.model.stage != ModelStage.READY) {
            navigate(AppScreen.SETUP)
            showNotice("Richte zuerst deine KI ein. Deine Nachricht bleibt im Eingabefeld.")
            return
        }
        val requestRevision = generationRevision.incrementAndGet()
        _state.update { it.copy(busy = true, memoryBusy = false, partialReply = "", notice = null) }
        generation = viewModelScope.launch {
            var userAppended = false
            var replyCommitted = false
            var teamUsed = false
            try {
                var promptBundle = withContext(Dispatchers.IO) {
                    // Preserve the exact send durably before replacing a recovered pending
                    // turn. This also covers a previous recovery interrupted by low disk space.
                    check(preferences.edit().putString(draftKey(bundle.story.id), input).commit()) {
                        "Deine Nachricht konnte noch nicht gespeichert werden. Bitte prüfe den freien Speicher."
                    }
                    repository.deleteLastUserMessageIfUnanswered(bundle.story.id)
                    repository.appendMessage(bundle.story.id, ChatRole.USER, input)
                    userAppended = true
                    preferences.edit().remove(draftKey(bundle.story.id)).apply()
                    repository.bundle(bundle.story.id) ?: error("Die Geschichte fehlt.")
                }
                drafts[bundle.story.id] = ""
                _state.update { it.copy(current = promptBundle, draft = "") }
                withContext(Dispatchers.IO) { repository.backfillMemory(bundle.story.id) }
                promptBundle = withContext(Dispatchers.IO) { repository.bundle(bundle.story.id) } ?: error("Die Geschichte fehlt.")
                val pending=promptBundle.messages.last()
                val provisional=promptBundle.copy(memory=MemoryRules.preview(promptBundle,pending))
                // Stop background index decoding before retrieval/text generation.
                indexWork?.cancel();semanticSearch.cancel();indexWork?.join()
                val factAnswer=if(snapshot.factsAnswers) FactAnswers.resolve(provisional) else null
                teamUsed=snapshot.teamEnabled && !snapshot.model.server && factAnswer==null
                if(teamUsed && inference===engine)engine.unload()
                val recalled=if(factAnswer==null) semanticSearch.search(provisional) else null
                val outcome=if(teamUsed)TeamRunner.run(provisional,"$requestRevision-${pending.id}",snapshot.adultThemes,recalled ?: ArchiveRecall.search(provisional,pending.text),teamPort,status={s ->
                    _state.update {if(generationRevision.get()==requestRevision && it.current?.story?.id==bundle.story.id)it.copy(teamStatus=s) else it}
                }) else null
                val plan=if(factAnswer!=null || outcome!=null) null else if(recalled==null) MemoryPrompt.plan(provisional,snapshot.adultThemes,context=inference.contextTokens,reserve=inference.outputReserve,count=inference::countPrompt)
                    else MemoryPrompt.planWithRecall(provisional,snapshot.adultThemes,recalled,context=inference.contextTokens,reserve=inference.outputReserve,count=inference::countPrompt)
                val reply = factAnswer?.text ?: outcome?.reply ?: inference.generate(
                    systemPrompt = checkNotNull(plan).system,
                    history = plan.history,
                    onToken = { chunk -> _state.update {
                        if (generationRevision.get() == requestRevision && it.busy && it.current?.story?.id == bundle.story.id)
                            it.copy(partialReply = it.partialReply + chunk) else it
                    } },
                )
                StoryReplyValidation.requireStoryReply(reply, input)
                val finalSources=outcome?.recall ?: plan?.recalledSources.orEmpty()
                MemoryReplyGuard.validate(provisional,reply,finalSources)
                withContext(Dispatchers.IO) {
                    currentCoroutineContext().ensureActive()
                    check(generationRevision.get() == requestRevision) { "Dieser Sendeversuch wurde angehalten." }
                    repository.commitReply(bundle.story.id,promptBundle.memory.version,pending.id,"$requestRevision-${pending.id}",reply,finalSources,outcome?.work,outcome?.proposals.orEmpty())
                    replyCommitted = true
                }
                refresh()
                _state.update { it.copy(partialReply = "") }
                outcome?.notice?.let {showNotice(it)}

            } catch (cancelled: CancellationException) {
                withContext(NonCancellable) {
                    if (!replyCommitted && userAppended) recoverSafely(bundle.story.id, input)
                    showNotice(if (replyCommitted) "Antwort gespeichert. Das Aktualisieren der Erinnerung wurde angehalten."
                        else "Antwort angehalten. Deine Nachricht steht wieder im Eingabefeld.")
                }
                throw cancelled
            } catch (error: Exception) {
                withContext(NonCancellable) {
                    if (!replyCommitted && userAppended) recoverSafely(bundle.story.id, input)
                    showNotice(if (replyCommitted) "Antwort gespeichert. Die Zusammenfassung konnte noch nicht aktualisiert werden."
                        else error.message ?: "Die Antwort konnte nicht erstellt werden. Bitte versuche es erneut.")
                }
            } finally {
                if(teamUsed && inference===engine) {
                    withContext(NonCancellable) {nativeTeamPort.release()}
                    if(currentCoroutineContext().isActive)runCatching {nativeTeamPort.restoreNarrator()}
                }
                _state.update { it.copy(busy = false, memoryBusy = false, partialReply = "",teamStatus="") }
                scheduleSearchIndex()
            }
        }
    }

    private suspend fun recoverSafely(storyId: String, text: String) {
        // Keep the DB copy unless its replacement draft is already on disk. An
        // interruption during recovery must not lose a previously sent message.
        runCatching {
            withContext(Dispatchers.IO) {
                if (preferences.edit().putString(draftKey(storyId), text).commit()) {
                    repository.deleteLastUserMessageIfUnanswered(storyId)
                }
            }
            refresh()
        }
        drafts[storyId] = text
        _state.update { if (it.current?.story?.id == storyId) it.copy(draft = text) else it }
    }

    override fun stopGeneration() {
        generationRevision.incrementAndGet()
        semanticSearch.cancel()
        inference.cancelGeneration()
        nativeTeamPort.cancel()
        generation?.cancel(CancellationException("Vom Nutzer angehalten."))
    }

    override fun saveMemory(memory: MemoryEntry) {
        if (!canEditMemories(memory.storyId)) return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.upsertMemory(memory)

                }
                refresh()
                showNotice("Erinnerung gespeichert.")
            } catch (error: Exception) {
                showNotice(error.message ?: "Die Erinnerung konnte nicht gespeichert werden.")
            }
        }
    }

    override fun deleteMemory(memory: MemoryEntry) {
        if (!canEditMemories(memory.storyId)) return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteMemory(memory.id)

                }
                refresh()
                showNotice("Erinnerung gelöscht.")
            } catch (error: Exception) {
                showNotice(error.message ?: "Die Erinnerung konnte nicht gelöscht werden.")
            }
        }
    }

    override fun editStateFact(storyId: String, factId: String, value: String, pinned: Boolean, exclude: Boolean, known: Boolean?) {
        if(!canEditMemories(storyId)) return
        viewModelScope.launch {
            try { withContext(Dispatchers.IO) { repository.editStateFact(storyId,factId,value,pinned,exclude,known) };refresh();showNotice(if(exclude) "Vom Gedächtnis ausgeschlossen. Chatnachrichten bleiben erhalten." else "Gedächtnis aktualisiert.") }
            catch(e:Exception){showNotice(e.message ?: "Änderung nicht gespeichert.")}
        }
    }
    override fun processMemoryBatch() {
        val story=_state.value.current?.story?.id ?: return
        if(!canEditMemories(story)) return
        viewModelScope.launch {
            _state.update { it.copy(memoryBusy=true) }
            try { withContext(Dispatchers.IO) {repository.backfillMemory(story,8)};refresh() }
            catch(e:Exception){showNotice(e.message ?: "Auswertung fehlgeschlagen.")}
            finally {_state.update {it.copy(memoryBusy=false)}}
        }
    }

    override fun addStateFact(storyId: String, kind: EntityKind, name: String, field: String, value: String, known: Boolean) {
        if(!canEditMemories(storyId)) return
        viewModelScope.launch {
            try {withContext(Dispatchers.IO){repository.addStateFact(storyId,kind,name,field,value,known)};refresh();showNotice("Neuer Zustand gespeichert.")}
            catch(e:Exception){showNotice(e.message ?: "Zustand nicht gespeichert.")}
        }
    }

    private fun canEditMemories(storyId: String): Boolean {
        if (_state.value.historyBusy || _state.value.memoryBusy) return false
        if (_state.value.current?.story?.id != storyId) return false
        if (!_state.value.busy) return true
        showNotice("Warte auf die Antwort oder halte sie an, bevor du Erinnerungen änderst.")
        return false
    }

    override fun downloadModel() = modelAction {
        if (serverSettings.enabled) { serverEngine.connect(serverSettings); return@modelAction }
        engine.downloadModel()
        engine.loadModel()
    }

    override fun cancelDownload() {
        engine.cancelDownload()
        modelWork?.cancel()
    }

    override fun loadModel() = modelAction { if (serverSettings.enabled) serverEngine.connect(serverSettings) else engine.loadModel() }

    override fun optimizeModel() {
        if (serverSettings.enabled) return
        if (_state.value.historyBusy || navigationWork?.isActive == true) return
        modelAction { engine.optimizeForDevice() }
    }

    override fun cancelModelOptimization() {
        if (!_state.value.model.optimizing) return
        engine.cancelGeneration()
        modelWork?.cancel()
    }

    override fun selectModel(id: String) {
        if (id == engine.state.value.modelId && !serverSettings.enabled) return
        if (_state.value.busy) {
            showNotice("Halte die laufende Antwort an, bevor du die KI wechselst.")
            return
        }
        modelAction {
            if (serverSettings.enabled) {
                saveServerSelection(serverSettings.copy(enabled = false))
                serverEngine.disconnect()
            }
            val previous = engine.state.value.modelId.takeIf { it in engine.state.value.downloadedModelIds }
            engine.selectModel(id)
            if (previous != null) preferences.edit().putString("pending_model_cleanup", previous).apply()
            if (preferences.getString("pending_model_cleanup", null) == id) preferences.edit().remove("pending_model_cleanup").apply()
            if (engine.state.value.stage == ModelStage.DOWNLOADED) engine.loadModel()
            showNotice("${LocalModelCatalog.find(id)?.name.orEmpty()} ausgewählt.")
        }
    }

    override fun requestModelDeletion(id: String) {
        if(_state.value.teamEnabled && id==helperEngine.state.value.modelId){showNotice("Diese Datei wird als Fakten-KI verwendet. Schalte das Team aus oder wähle zuerst einen anderen Helfer.");return}
        val snapshot = _state.value
        if (snapshot.busy || snapshot.modelBusy || modelWork?.isActive == true || id == snapshot.model.modelId || id !in snapshot.model.downloadedModelIds) return
        _state.update { it.copy(modelCleanupId = id) }
    }

    override fun keepPreviousModel() {
        if (_state.value.modelBusy) return
        val id = _state.value.modelCleanupId ?: return
        if (preferences.getString("pending_model_cleanup", null) == id) preferences.edit().remove("pending_model_cleanup").apply()
        _state.update { it.copy(modelCleanupId = null) }
    }

    override fun deletePreviousModel() {
        val id = _state.value.modelCleanupId ?: return
        if(_state.value.teamEnabled && id==helperEngine.state.value.modelId){showNotice("Diese Datei wird als Fakten-KI verwendet. Schalte das Team aus oder wähle zuerst einen anderen Helfer.");return}
        modelAction {
            engine.deleteStoredModel(id)
            if (preferences.getString("pending_model_cleanup", null) == id) preferences.edit().remove("pending_model_cleanup").apply()
            _state.update { it.copy(modelCleanupId = null) }
            showNotice("${LocalModelCatalog.find(id)?.name.orEmpty()} gelöscht. Du kannst es später erneut herunterladen.")
        }
    }

    private fun modelAction(block: suspend () -> Unit) {
        if (modelWork?.isActive == true || _state.value.busy) return
        _state.update { it.copy(modelBusy = true) }
        modelWork = viewModelScope.launch {
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                showNotice(error.message ?: "Die KI konnte nicht eingerichtet werden.")
            } finally {
                _state.update { it.copy(modelBusy = false) }
            }
        }
    }

    private fun saveServerSelection(settings: OllamaSettings) {
        check(preferences.edit().putBoolean("ollama_enabled", settings.enabled).putString("ollama_address", settings.address)
            .putString("ollama_model", settings.model).commit()) { "Die Server-Einstellungen konnten nicht gespeichert werden." }
        serverSettings = settings
        _state.update { it.copy(server = settings, model = inference.state.value, modelCleanupId = null) }
    }

    override fun setServerEnabled(enabled: Boolean) = modelAction {
        saveServerSelection(serverSettings.copy(enabled = enabled))
        if (enabled) {
            engine.unload()
            serverEngine.connect(serverSettings)
        } else {
            serverEngine.disconnect()
            if (engine.state.value.stage == ModelStage.DOWNLOADED) engine.loadModel()
        }
    }

    override fun connectServer(address: String, model: String) = modelAction {
        val settings = OllamaSettings(true, OllamaProtocol.address(address), OllamaProtocol.model(model))
        engine.unload()
        saveServerSelection(settings)
        serverEngine.connect(settings)
    }

    override fun setAdultThemes(enabled: Boolean) {
        preferences.edit().putBoolean("adult_themes", enabled).apply()
        _state.update { it.copy(adultThemes = enabled) }
        showNotice(if (enabled) "Erwachsene Themen aktiviert. Intime Szenen werden ausgeblendet." else "Erwachsene Themen deaktiviert.")
    }

    override fun saveUpdateRepository(value: String) = updateAction {
        updater.configure(value, updater.state.value.includePrerelease)
    }

    override fun setIncludeTestUpdates(enabled: Boolean) = updateAction {
        updater.setIncludePrerelease(enabled)
    }

    override fun checkForUpdates() = updateAction { updater.checkForUpdates() }
    override fun downloadAppUpdate() = updateAction { updater.downloadUpdate() }

    override fun cancelAppUpdate() {
        updater.cancel()
        updateWork?.cancel()
    }

    override fun installAppUpdate() {
        if (_state.value.busy) {
            showNotice("Beende die laufende Antwort oder halte sie an, bevor du die App aktualisierst.")
            return
        }
        updateAction {
            val apk = updater.prepareInstall()
            if (_state.value.busy) {
                showNotice("Das Update ist bereit. Tippe nach der laufenden Antwort erneut auf „Update installieren“.")
            } else {
                installRequests.send(apk)
            }
        }
    }

    private fun updateAction(block: suspend () -> Unit) {
        if (updateWork?.isActive == true) return
        updateWork = viewModelScope.launch {
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                showNotice(error.message ?: "Der App-Update-Vorgang konnte nicht abgeschlossen werden.")
            }
        }
    }

    fun updateInstallerOpened() = updater.installerOpened()
    fun updateInstallPermissionRequired() = updater.installPermissionRequired()
    fun updateInstallerUnavailable() = updater.installerUnavailable()
    fun updateInstallerReturned() = updateAction { updater.installerReturned() }

    override fun dismissNotice() { _state.update { it.copy(notice = null) } }

    private fun showNotice(message: String) { _state.update { it.copy(notice = message) } }

    override fun exportCurrentStory() {
        val storyId = _state.value.current?.story?.id ?: return
        if (pendingExport != null) return
        viewModelScope.launch {
            try {
                val bundle = withContext(Dispatchers.IO) { repository.bundle(storyId) } ?: return@launch
                val safeName = bundle.story.title.replace(Regex("[^\\p{L}\\p{N}_-]+"), "_").take(70).ifBlank { "Geschichte" }
                val payload = StoryExport("$safeName.txt", exportText(bundle))
                pendingExport = payload
                exportRequests.send(payload)
            } catch (error: Exception) {
                pendingExport = null
                showNotice(error.message ?: "Die Geschichte konnte nicht exportiert werden.")
            }
        }
    }

    /** Called with an Android document URI selected explicitly by the user. No broad storage permission. */
    fun completeExport(uri: Uri?) {
        val payload = pendingExport
        pendingExport = null
        if (uri == null || payload == null) return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val output = getApplication<Application>().contentResolver.openOutputStream(uri, "wt")
                        ?: error("Die ausgewählte Datei konnte nicht geöffnet werden.")
                    output.bufferedWriter(Charsets.UTF_8).use { it.write(payload.text) }
                }
                showNotice("Geschichte als Textdatei gespeichert.")
            } catch (error: Exception) {
                showNotice(error.message ?: "Die Exportdatei konnte nicht gespeichert werden.")
            }
        }
    }

    fun exportUnavailable() {
        pendingExport = null
        showNotice("Auf diesem Gerät ist gerade keine Dateiauswahl verfügbar.")
    }

    private suspend fun refresh() {
        val revision = historyRevision
        val currentId = _state.value.current?.story?.id
        val result = withContext(Dispatchers.IO) {
            Triple(repository.characters(), repository.stories(), currentId?.let(repository::bundle))
        }
        if (revision != historyRevision) return
        _state.update {
            it.copy(characters = result.first, stories = result.second,
                current = if (it.current?.story?.id == currentId) result.third else it.current)
        }
        scheduleSearchIndex()
    }

    override fun setTeamEnabled(enabled: Boolean) {
        if (serverSettings.enabled) return
        if(_state.value.busy || _state.value.modelBusy)return
        if(preferences.edit().putBoolean("team_enabled",enabled).commit())_state.update {it.copy(teamEnabled=enabled)}
    }
    override fun selectHelperModel(id: String) {
        if(id !in setOf("huihui-qwen3-4b","gemma-4-e2b",_state.value.model.modelId))return
        modelAction {helperEngine.selectModel(id)}
    }
    override fun downloadHelperModel() = modelAction {
        if (serverSettings.enabled) return@modelAction
        indexWork?.cancel();semanticSearch.cancel();indexWork?.join()
        engine.unload()
        try {helperEngine.downloadModel()}finally{if(currentCoroutineContext().isActive)runCatching {engine.loadModel()}}
    }
    override fun cancelHelperDownload() {helperEngine.cancelDownload();modelWork?.cancel()}

    override fun setFactsAnswers(enabled: Boolean) {
        if(_state.value.busy)return
        if(preferences.edit().putBoolean("facts_answers",enabled).commit())_state.update {it.copy(factsAnswers=enabled)}
        else showNotice("Die Einstellung konnte nicht gespeichert werden.")
    }

    override fun setSemanticSearch(enabled: Boolean) {
        try {
            semanticSearch.setEnabled(enabled)
            if(enabled)scheduleSearchIndex() else indexWork?.cancel()
        }catch(e: Exception){showNotice(e.message ?: "Die Suchoption konnte nicht gespeichert werden.")}
    }
    override fun downloadSearchModel() {
        if(searchDownloadWork?.isActive==true || _state.value.busy)return
        indexWork?.cancel();semanticSearch.cancel()
        searchDownloadWork=viewModelScope.launch {
            try {indexWork?.join();semanticSearch.download();searchDownloadWork=null;scheduleSearchIndex()}
            catch(cancelled: CancellationException){throw cancelled}
            catch(e: Exception){showNotice(e.message ?: "Das Suchmodell konnte nicht heruntergeladen werden.")}
        }
    }
    override fun prepareSearchIndex(rebuild: Boolean) {
        if(_state.value.busy || searchDownloadWork?.isActive==true)return
        val bundle=_state.value.current ?: return
        indexWork?.cancel();semanticSearch.cancel()
        val previous=indexWork
        indexWork=viewModelScope.launch {
            previous?.join()
            if(rebuild)semanticSearch.rebuild(bundle.story.id)
            while(_state.value.current?.story?.id==bundle.story.id && semanticSearch.state.value.enabled && !_state.value.busy) {
                if(!semanticSearch.indexBatch(bundle))break
                kotlinx.coroutines.delay(400)
            }
        }
    }
    override fun stopSearchWork() {indexWork?.cancel();searchDownloadWork?.cancel();semanticSearch.cancel()}
    private fun scheduleSearchIndex() {
        if(!semanticSearch.state.value.enabled || !semanticSearch.state.value.ready || _state.value.busy || searchDownloadWork?.isActive==true)return
        // Refreshes occur after story changes, completed answers and manual corrections.
        // Cancel the captured version so a late worker cannot replace a newer correction.
        prepareSearchIndex(false)
    }

    override fun onCleared() {
        semanticSearch.cancel()
        engine.close()
        serverEngine.close()
        helperEngine.close()
        if (inference !== engine) inference.close()
        updater.close()
        exportRequests.close()
        installRequests.close()
        // Cancellation recovery still owns database writes. Wait for the ViewModel's
        // children on a separate scope before closing SQLite; never block the main thread.
        val outstandingWork = viewModelScope.coroutineContext[Job]
        outstandingWork?.cancel()
        CoroutineScope(Dispatchers.IO).launch {
            outstandingWork?.join()
            semanticSearch.close()
            repository.close()
        }
        super.onCleared()
    }

    companion object {
        private fun draftKey(storyId: String) = "draft_$storyId"

        private fun exportText(bundle: StoryBundle): String = buildString {
            appendLine(bundle.story.title)
            appendLine("Mit ${bundle.character.name} · Geschichten")
            appendLine("Exportiert am ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(Date())}")
            appendLine()
            appendLine("VERLAUF")
            bundle.messages.forEach { message ->
                appendLine()
                appendLine(if (message.role == ChatRole.USER) "Du:" else "${bundle.character.name}:")
                appendLine(message.text)
            }
            appendLine()
            appendLine("ERINNERUNGEN")
            bundle.memories.forEach { memory ->
                appendLine()
                appendLine("${memory.kind.title}${if (memory.pinned) " · Angeheftet" else ""}")
                appendLine(memory.text)
            }
            appendLine()
            appendLine("STRUKTURIERTES GEDÄCHTNIS")
            bundle.memory.facts.forEach {fact ->
                appendLine()
                appendLine("${fact.entity.name} · ${fact.label} · ${when(fact.status) {FactStatus.CURRENT -> "Aktuell";FactStatus.HISTORICAL -> "Historisch";FactStatus.UNCERTAIN -> "Unsicher";FactStatus.EXCLUDED -> "Ausgeschlossen"}}${if(fact.pinned) " · Angeheftet" else ""}")
                appendLine(fact.value)
                appendLine("Quelle: ${fact.sourceText}")
                appendLine(if(bundle.memory.knowledge.any {it.factId==fact.id && it.knower=="character"}) "${bundle.character.name} kennt diese Angabe." else "${bundle.character.name} kennt diese Angabe nicht.")
            }
        }
    }
}
