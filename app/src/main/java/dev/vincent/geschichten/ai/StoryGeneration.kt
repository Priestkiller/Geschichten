package dev.vincent.geschichten.ai

import kotlinx.coroutines.flow.StateFlow

/** Small inference boundary for deterministic persistence/concurrency tests without model weights. */
interface StoryGeneration : AutoCloseable {
    val state: StateFlow<ModelState>
    val contextTokens: Int get() = 4096
    val outputReserve: Int get() = 512
    suspend fun countPrompt(system: String, history: List<ModelMessage>): Int =
        error("Diese Laufzeit bietet noch keinen genauen Tokenizer an.")
    suspend fun generate(systemPrompt: String, history: List<ModelMessage>, recordTiming: Boolean = true,
                         onToken: (String) -> Unit): String
    fun cancelGeneration()
    suspend fun clearConversationCache()
}
