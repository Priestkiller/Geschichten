package dev.vincent.geschichten.ui

import dev.vincent.geschichten.ai.ModelState
import dev.vincent.geschichten.data.*
import dev.vincent.geschichten.updates.AppUpdateState

enum class AppScreen { CHARACTERS, STORIES, MEMORIES, CHAT, EDITOR, SETUP }

data class AppUiState(
    val screen: AppScreen = AppScreen.CHARACTERS,
    val characters: List<CharacterProfile> = emptyList(),
    val stories: List<Story> = emptyList(),
    val current: StoryBundle? = null,
    val model: ModelState = ModelState(),
    val modelBusy: Boolean = false,
    val modelCleanupId: String? = null,
    val draft: String = "",
    val partialReply: String = "",
    val busy: Boolean = false,
    val notice: String? = null,
    val editingCharacter: CharacterProfile? = null,
    val memoryBusy: Boolean = false,
    val historyBusy: Boolean = false,
    val adultThemes: Boolean = false,
    val updates: AppUpdateState = AppUpdateState(),
    val semanticSearch: dev.vincent.geschichten.memory.SemanticSearchState = dev.vincent.geschichten.memory.SemanticSearchState(),
    val factsAnswers: Boolean = false,
    val teamEnabled: Boolean = false,
    val helperModel: ModelState = ModelState(),
    val teamStatus: String = "",
    val server: dev.vincent.geschichten.ai.OllamaSettings = dev.vincent.geschichten.ai.OllamaSettings(),
)

interface AppActions {
    fun setServerEnabled(enabled: Boolean) {}
    fun connectServer(address: String, model: String) {}
    fun setTeamEnabled(enabled: Boolean) {}
    fun selectHelperModel(id: String) {}
    fun downloadHelperModel() {}
    fun cancelHelperDownload() {}
    fun setFactsAnswers(enabled: Boolean) {}
    fun setSemanticSearch(enabled: Boolean) {}
    fun downloadSearchModel() {}
    fun prepareSearchIndex(rebuild: Boolean = false) {}
    fun stopSearchWork() {}
    fun navigate(screen: AppScreen)
    fun selectCharacter(character: CharacterProfile)
    fun openStory(storyId: String)
    fun clearHistory() {}
    fun startNewStory(character: CharacterProfile)
    fun createCharacter()
    fun editCharacter(character: CharacterProfile)
    fun saveCharacter(character: CharacterProfile)
    fun changeDraft(value: String)
    fun sendMessage()
    fun stopGeneration()
    fun saveMemory(memory: MemoryEntry)
    fun editStateFact(storyId: String, factId: String, value: String, pinned: Boolean, exclude: Boolean = false, known: Boolean? = null) {}
    fun processMemoryBatch() {}
    fun addStateFact(storyId: String, kind: dev.vincent.geschichten.memory.EntityKind, name: String, field: String, value: String, known: Boolean) {}
    fun deleteMemory(memory: MemoryEntry)
    fun downloadModel()
    fun cancelDownload()
    fun loadModel()
    fun optimizeModel() {}
    fun cancelModelOptimization() {}
    fun selectModel(id: String) {}
    fun requestModelDeletion(id: String) {}
    fun keepPreviousModel() {}
    fun deletePreviousModel() {}
    fun dismissNotice()
    fun exportCurrentStory()
    fun setAdultThemes(enabled: Boolean)
    // Defaults keep preview fixtures compatible; the real ViewModel implements all.
    fun saveUpdateRepository(value: String) {}
    fun setIncludeTestUpdates(enabled: Boolean) {}
    fun checkForUpdates() {}
    fun downloadAppUpdate() {}
    fun cancelAppUpdate() {}
    fun installAppUpdate() {}
}
