package dev.vincent.geschichten.ai

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.URI

data class OllamaSettings(
    val enabled: Boolean = false,
    val address: String = "http://192.168.178.73:11434",
    val model: String = "geschichten-gemma4-12b-test",
)

/** Text-only Gemma 4 protocol. Thought fields are never returned to the caller. */
object OllamaProtocol {
    const val CONTEXT = 8192
    const val RESERVE = 1024
    const val TOKENIZER_ID = "ollama-gemma4-12b"
    const val WEIGHTS = "bb722270d54346adc198f213851dbe0207e87c3ecf2a0aff4d92262726215391"

    fun address(value: String): String {
        val uri = try { URI(value.trim()) } catch (_: Exception) { error("Bitte gib eine gültige Serveradresse ein.") }
        require(uri.scheme in setOf("http", "https") && uri.host != null && uri.userInfo == null &&
            uri.rawQuery == null && uri.rawFragment == null && uri.path.orEmpty() in setOf("", "/") &&
            (uri.port == -1 || uri.port in 1..65535)) { "Verwende eine Serveradresse wie http://192.168.178.73:11434 ohne weiteren Pfad." }
        if (uri.scheme == "http") {
            val parts = uri.host.split('.').map { it.toIntOrNull() }
            require(parts.size == 4 && parts.all { it != null && it in 0..255 } &&
                (parts[0] == 10 || parts[0] == 192 && parts[1] == 168 || parts[0] == 172 && parts[1]!! in 16..31 || parts[0] == 127)) {
                "Für HTTP verwende die lokale IP-Adresse deines PCs. Andere Server benötigen HTTPS."
            }
        }
        return uri.toASCIIString().removeSuffix("/")
    }

    fun model(value: String): String = value.trim().also {
        require(it.isNotEmpty() && it.length <= 200 && it.none { c -> c.isWhitespace() || c.isISOControl() }) { "Bitte gib den Ollama-Modellnamen ein." }
    }

    fun requireSupported(show: JsonObject) {
        require(show["model_info"]?.asJsonObject?.get("general.architecture")?.asString == "gemma4" &&
            show["capabilities"]?.asJsonArray?.any { it.asString == "thinking" } == true &&
            show["modelfile"]?.asString?.lineSequence()?.any {
                it.startsWith("FROM ") && it.trim().endsWith("sha256-$WEIGHTS")
            } == true && show["modelfile"]?.asString?.lineSequence()?.any { it.trim() == "RENDERER gemma4" } == true) {
            "Diese Anbindung unterstützt das auf diesem PC geprüfte Gemma 4 12B Q4_K_M. Wähle geschichten-gemma4-12b-test."
        }
    }

    fun request(model: String, system: String, history: List<ModelMessage>): JsonObject {
        require(history.lastOrNull()?.user == true) { "Es fehlt die letzte Nutzernachricht." }
        val messages = JsonArray().apply {
            add(JsonObject().apply { addProperty("role", "system"); addProperty("content", system) })
            history.forEach { message -> add(JsonObject().apply {
                addProperty("role", if (message.user) "user" else "assistant")
                addProperty("content", message.text)
            }) }
        }
        return JsonObject().apply {
            addProperty("model", model)
            add("messages", messages)
            addProperty("think", false)
            addProperty("stream", true)
            addProperty("keep_alive", "10m")
            add("options", JsonObject().apply {
                addProperty("num_ctx", CONTEXT); addProperty("num_predict", RESERVE); addProperty("seed", 42)
                addProperty("temperature", 1.0); addProperty("top_k", 64); addProperty("top_p", 0.95)
                addProperty("draft_num_predict", 3)
            })
        }
    }

    // Mirrors the tested 12B renderer with its empty thought block in normal mode.
    fun rendered(system: String, history: List<ModelMessage>): String = buildString {
        append("<bos><|turn>system\n").append(goTrim(system)).append("<turn|>\n")
        history.forEachIndexed { index, message ->
            if (message.user || index == 0 || history[index - 1].user) append("<|turn>${if(message.user) "user" else "model"}\n")
            append(if (message.user) goTrim(message.text) else goTrim(stripThinking(message.text)))
            if (message.user || history.getOrNull(index + 1)?.user != false) append("<turn|>\n")
        }
        append("<|turn>model\n<|channel>thought\n<channel|>")
    }

    private fun goTrim(text: String) = text.trim { it in '\t'..'\r' || it == ' ' || it in "\u0085\u00a0\u1680\u2028\u2029\u202f\u205f\u3000" || it in '\u2000'..'\u200a' }
    private fun stripThinking(text: String): String {
        var remaining = text
        return buildString {
            while (true) {
                val start = remaining.indexOf("<|channel>")
                if (start < 0) { append(remaining); break }
                append(remaining.substring(0, start))
                val end = remaining.indexOf("<channel|>", start)
                if (end < 0) break
                remaining = remaining.substring(end + "<channel|>".length)
            }
        }
    }
}

/** A reply is usable only after a complete stop and a matching, untruncated input budget. */
class OllamaReply(private val expectedPromptTokens: Int) {
    private val answer = StringBuilder()
    private var done = false
    val isDone: Boolean get() = done
    fun accept(line: String, onContent: (String) -> Unit) {
        check(!done) { "Der Server hat nach dem Antwortende weitere Daten gesendet." }
        val chunk = try { JsonParser.parseString(line).asJsonObject } catch (_: Exception) { error("Die Serverantwort ist beschädigt.") }
        if (chunk.has("error")) error("Ollama konnte die Antwort nicht erstellen. Prüfe den Server und das ausgewählte Modell.")
        val message = chunk["message"]?.asJsonObject
        require(message == null || message["role"]?.asString == "assistant") { "Der Server hat eine unerwartete Nachrichtenrolle gesendet." }
        require(message?.get("tool_calls")?.asJsonArray?.isEmpty != false) { "Die Figur hat einen Werkzeugaufruf statt einer Geschichte erzeugt." }
        val content = message?.get("content")?.asString.orEmpty()
        require(answer.length + content.length <= 64_000) { "Die Serverantwort ist zu lang." }
        if (content.isNotEmpty()) { answer.append(content); onContent(content) }
        if (chunk["done"]?.asBoolean == true) {
            require(chunk["done_reason"]?.asString == "stop") { "Die Antwort wurde nicht vollständig beendet. Deine Nachricht bleibt als Entwurf erhalten." }
            require(chunk["prompt_eval_count"]?.asInt == expectedPromptTokens && expectedPromptTokens + OllamaProtocol.RESERVE <= OllamaProtocol.CONTEXT) {
                "Der Server hat den Gesprächskontext anders verarbeitet oder gekürzt. Die Antwort wurde nicht gespeichert."
            }
            done = true
        }
    }
    fun finish(): String {
        check(done) { "Die Verbindung ist vor dem vollständigen Antwortende abgebrochen. Deine Nachricht bleibt als Entwurf erhalten." }
        return answer.toString().trim().also { check(it.isNotEmpty()) { "Der Server hat keine sichtbare Antwort erzeugt." } }
    }
}
