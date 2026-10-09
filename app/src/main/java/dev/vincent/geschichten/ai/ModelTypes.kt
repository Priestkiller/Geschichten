package dev.vincent.geschichten.ai

enum class ModelStage { MISSING, DOWNLOADING, VERIFYING, DOWNLOADED, LOADING, READY, GENERATING, ERROR }

data class ModelState(
    val modelId: String = LocalModelCatalog.DEFAULT_ID,
    val downloadedModelIds: Set<String> = emptySet(),
    val stage: ModelStage = ModelStage.MISSING,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0,
    val totalBytes: Long = 0,
    val detail: String = "Die KI wird einmal auf deinem Handy eingerichtet.",
    val deviceName: String = "Dein Handy",
    val cpuThreads: Int = 4,
    val optimizing: Boolean = false,
    val performanceDetail: String? = null,
    val firstTextMs: Long? = null,
)

data class ModelMessage(val user: Boolean, val text: String)
