package dev.vincent.geschichten.ai

import dev.vincent.geschichten.memory.HybridRecall
import java.nio.charset.StandardCharsets.UTF_8

object EmbeddingModel {
    const val ID = "embeddinggemma-300m-q8"
    const val REVISION = "0f741b5a6585bd53aeb15cd1372c56f2a0f65e12"
    const val FILE_NAME = "embeddinggemma-300M-Q8_0.gguf"
    const val BYTES = 333_590_944L
    const val SHA256 = "b5ce9d77a3fc4b3b39ccb5643c36777911cc4eb46a66962eadfa3f5f60490d63"
    const val URL = "https://huggingface.co/ggml-org/embeddinggemma-300M-GGUF/resolve/$REVISION/$FILE_NAME?download=true"
    const val SPACE = "$ID:$SHA256:${HybridRecall.PREPROCESSING}:${HybridRecall.INDEX_VERSION}:768"
    fun document(text: String) = "title: none | text: $text"
    fun query(text: String) = "task: search result | query: $text"
}

internal class NativeEmbeddingEncoder(path: String) : AutoCloseable {
    private var handle=EmbeddingNative.create(path.toByteArray(UTF_8))
    fun encode(text: String): FloatArray = EmbeddingNative.encode(handle,text.toByteArray(UTF_8))
    @Synchronized fun cancel() {if(handle!=0L)EmbeddingNative.cancel(handle)}
    @Synchronized override fun close() {if(handle!=0L){EmbeddingNative.destroy(handle);handle=0}}
}
internal object EmbeddingNative {
    init {System.loadLibrary("geschichten_gguf")}
    @JvmStatic external fun create(path: ByteArray): Long
    @JvmStatic external fun encode(handle: Long,input: ByteArray): FloatArray
    @JvmStatic external fun cancel(handle: Long)
    @JvmStatic external fun destroy(handle: Long)
}
