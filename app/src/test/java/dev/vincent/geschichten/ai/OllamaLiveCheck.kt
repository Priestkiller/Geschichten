package dev.vincent.geschichten.ai

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/** Optional Windows harness: same protocol/transport, same vocab code as the Android JNI. */
class DesktopOllamaInference(private val address: String, private val executable: File, private val asset: File, private val output: File) : StoryGeneration {
    override val state = MutableStateFlow(ModelState(stage=ModelStage.READY,server=true))
    override val contextTokens = OllamaProtocol.CONTEXT
    override val outputReserve = OllamaProtocol.RESERVE
    private val transport = OllamaTransport()
    private val counts = ConcurrentHashMap<String,Int>()
    suspend fun connect() {
        transport.json(address,"/api/version")
        OllamaProtocol.requireSupported(transport.json(address,"/api/show",JsonObject().apply {addProperty("model",OllamaSettings().model)}))
    }
    override suspend fun countPrompt(system: String, history: List<ModelMessage>): Int = withContext(Dispatchers.IO) {
        val rendered = OllamaProtocol.rendered(system,history)
        counts[rendered] ?: run {
            output.mkdirs()
            val prompt = File(output,"tokenizer-input.txt").apply {writeText(rendered,Charsets.UTF_8)}
            val process = ProcessBuilder(executable.absolutePath,"-m",asset.absolutePath,"-f",prompt.absolutePath,"--ids","--no-bos","--no-escape")
                .redirectError(File(output,"tokenizer.log")).start()
            val ids = process.inputStream.bufferedReader(Charsets.UTF_8).readText()
            check(process.waitFor()==0) { "Native tokenizer failed: ${File(output,"tokenizer.log").readText().takeLast(1000)}" }
            val count = JsonParser.parseString(ids.trim()).asJsonArray.size()
            counts[rendered]=count
            count
        }
    }
    override suspend fun generate(systemPrompt: String, history: List<ModelMessage>, recordTiming: Boolean, onToken: (String)->Unit): String {
        val request = OllamaProtocol.request(OllamaSettings().model,systemPrompt,history)
        val expected = countPrompt(systemPrompt,history)
        File(output,"request.json").writeText(GsonBuilder().setPrettyPrinting().create().toJson(request),Charsets.UTF_8)
        val start = System.nanoTime()
        val result = transport.generate(address,request,expected,onToken)
        File(output,"result.json").writeText(GsonBuilder().setPrettyPrinting().create().toJson(mapOf("expectedPromptTokens" to expected,"reserve" to outputReserve,"seconds" to (System.nanoTime()-start)/1e9,"content" to result,"thinkingStored" to false)),Charsets.UTF_8)
        return result
    }
    override fun cancelGeneration() = transport.cancel()
    override suspend fun clearConversationCache() = Unit
    override fun close() = transport.cancel()
}

object OllamaLiveCheck {
    @JvmStatic fun main(args: Array<String>) = runBlocking {
        val root = File(args[0])
        val output = File(args[2])
        val fixture = JsonParser.parseString(File(root,"docs/validation/server-2026-10-09/quality/qa-new-gift.json").readText()).asJsonObject["request"].asJsonObject["messages"].asJsonArray
        val system = fixture[0].asJsonObject["content"].asString
        val history = fixture.drop(1).map { message -> val row=message.asJsonObject; ModelMessage(row["role"].asString=="user",row["content"].asString) }
        val backend = DesktopOllamaInference("http://192.168.178.73:11434",File(args[1]),File(root,"app/src/main/assets/tokenizers/gemma4-12b-ollama.gguf"),output)
        backend.connect()
        val count = backend.countPrompt(system,history)
        println("Native tokenizer input=$count reserve=${backend.outputReserve}")
        val answer = backend.generate(system,history,true) {}
        println("Complete LAN response received (${answer.length} visible characters)")
    }
}
