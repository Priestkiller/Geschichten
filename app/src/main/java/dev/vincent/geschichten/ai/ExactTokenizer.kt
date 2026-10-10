package dev.vincent.geschichten.ai

import android.content.Context
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.text.Normalizer

/** Tokenizer assets extracted from the exact pinned LiteRT and Ollama GGUF model files.
 * No weights, approximate word counters, or second inference call. */
internal class ExactTokenizer(context: Context, modelId: String) : AutoCloseable {
    private val specification = when(modelId) {
        "ollama-gemma4-12b" -> Triple("gemma4-12b-ollama.gguf", "7cbf6f9caccbb910e619152e5c825d9dfc8e5b677fb39e9e4f19a642c9b58a3b", 0)
        "gemma-4-e2b" -> Triple("gemma4.model", "e594c8a90eb08d8bda498ff4747977dc827ae0c3c56b5c0d41a605a22d02ef03", 1)
        "qwen2.5-1.5b" -> Triple("qwen25.gguf", "69970130e011e3069e39e9ac036575948262cf9b6a046a610bb5aae1a8736e61", 1)
        "qwen3-0.6b" -> Triple("qwen06.gguf", "79a87fd4710737eefeff82a41208cbf086dc9a5ed92c5d896edbd9e4caa32ab1", 0)
        else -> error("Für diese KI fehlt der genaue Tokenizer.")
    }
    private var handle: Long
    init {
        val data = context.assets.open("tokenizers/${specification.first}").use { it.readBytes() }
        val hash=MessageDigest.getInstance("SHA-256").digest(data).joinToString("") { "%02x".format(it) }
        check(hash == specification.second) { "Die Tokenizer-Datei wurde verändert." }
        val file=File(context.cacheDir,specification.first)
        file.writeBytes(data)
        handle=ExactTokenizerNative.create(file.absolutePath.toByteArray(UTF_8),modelId == "gemma-4-e2b")
    }
    fun count(rendered: String): Int {
        // The two embedded HF JSONs explicitly specify NFC before ByteLevel BPE.
        // SentencePiece uses the original model's own compiled normalizer.
        val normalized=if(specification.first.endsWith(".gguf") && specification.first != "gemma4-12b-ollama.gguf") Normalizer.normalize(rendered,Normalizer.Form.NFC) else rendered
        return ExactTokenizerNative.encode(handle,normalized.toByteArray(UTF_8)).size + specification.third
    }
    override fun close() { if(handle != 0L) { ExactTokenizerNative.destroy(handle);handle=0 } }
}
internal object ExactTokenizerNative {
    init { System.loadLibrary("geschichten_gguf") }
    @JvmStatic external fun create(path: ByteArray, sentencePiece: Boolean): Long
    @JvmStatic external fun encode(handle: Long, text: ByteArray): IntArray
    @JvmStatic external fun destroy(handle: Long)
}
