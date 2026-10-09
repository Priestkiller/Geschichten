package dev.vincent.geschichten.ai

import java.nio.charset.StandardCharsets.UTF_8

/** Native handles are owned by LocalModelEngine's operation lock. Only cancel is concurrent. */
internal class GgufEngine(path: String, contextTokens: Int, templateFallback: String?, threads: Int = 4) : AutoCloseable {
    private var handle = GgufNative.create(path.toByteArray(UTF_8), contextTokens, threads, templateFallback.orEmpty().toByteArray(UTF_8))
    fun clearContext() = GgufNative.clearContext(handle)
    fun resetCancellation() = GgufNative.resetCancellation(handle)
    fun cancel() = GgufNative.cancel(handle)
    fun countPrompt(system: String, history: List<ModelMessage>): Int = GgufNative.countPrompt(handle,
        arrayOf("system") + history.map { if(it.user) "user" else "assistant" },
        arrayOf(system.toByteArray(UTF_8)) + history.map { it.text.toByteArray(UTF_8) })
    fun generate(system: String, history: List<ModelMessage>, maxTokens: Int, allowTokenLimit: Boolean = false, onToken: (String) -> Unit): String {
        val roles = arrayOf("system") + history.map { if (it.user) "user" else "assistant" }
        val texts = arrayOf(system.toByteArray(UTF_8)) + history.map { it.text.toByteArray(UTF_8) }
        val callback = object : GgufTokenCallback {
            override fun onChunk(bytes: ByteArray) { onToken(String(bytes, UTF_8)) }
        }
        return String(GgufNative.generate(handle, roles, texts, maxTokens, allowTokenLimit, callback), UTF_8).trim()
    }
    override fun close() {
        val value = handle
        if (value != 0L) {
            handle = 0
            GgufNative.destroy(value)
        }
    }
}

internal interface GgufTokenCallback { fun onChunk(bytes: ByteArray) }

internal object GgufNative {
    init { System.loadLibrary("geschichten_gguf") }
    @JvmStatic external fun create(path: ByteArray, contextTokens: Int, threads: Int, templateFallback: ByteArray): Long
    @JvmStatic external fun generate(handle: Long, roles: Array<String>, texts: Array<ByteArray>, maxTokens: Int, allowTokenLimit: Boolean, callback: GgufTokenCallback): ByteArray
    @JvmStatic external fun resetCancellation(handle: Long)
    @JvmStatic external fun cancel(handle: Long)
    @JvmStatic external fun destroy(handle: Long)
    @JvmStatic external fun clearContext(handle: Long)
    @JvmStatic external fun cacheStats(handle: Long): LongArray
    @JvmStatic external fun countPrompt(handle: Long, roles: Array<String>, texts: Array<ByteArray>): Int
}
