package dev.vincent.geschichten.ai

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException
import java.io.ByteArrayOutputStream
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** One request at a time; cancellation closes even a socket waiting for the first thought. */
class OllamaTransport {
    private class ServerResponseException(message: String) : IOException(message)
    private val active = AtomicReference<HttpURLConnection?>(null)
    suspend fun json(address: String, path: String, body: JsonObject? = null): JsonObject = exchange(address, path, body, 15_000) { connection ->
        val bytes = connection.inputStream.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val size = input.read(buffer)
                if (size < 0) break
                require(output.size() + size <= 2_000_000) { "Die Serverbeschreibung ist zu groß." }
                output.write(buffer, 0, size)
            }
            output.toByteArray()
        }
        JsonParser.parseString(bytes.toString(Charsets.UTF_8)).asJsonObject
    }
    suspend fun generate(address: String, request: JsonObject, expectedTokens: Int, onContent: (String) -> Unit): String =
        exchange(address, "/api/chat", request, 600_000) { connection ->
            val reply = OllamaReply(expectedTokens)
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                var frames = 0
                while (!reply.isDone) {
                    currentCoroutineContext().ensureActive()
                    val line = boundedLine(reader) ?: break
                    require(++frames <= 16_384) { "Die Serverantwort ist zu lang." }
                    if (line.isNotBlank()) reply.accept(line, onContent)
                }
            }
            reply.finish()
        }

    private suspend fun <T> exchange(address: String, path: String, body: JsonObject?, timeout: Int,
                                     read: suspend (HttpURLConnection) -> T): T = withContext(Dispatchers.IO) {
        val connection = URL(address + path).openConnection() as HttpURLConnection
        check(active.compareAndSet(null, connection)) { "Es läuft bereits eine Anfrage an den Server." }
        val job = currentCoroutineContext()[Job]
        // An independent completion handler registered on a child job fires immediately
        // on cancellation, while this IO coroutine is still blocked in socket.read().
        val interrupt = Job(job)
        val cancellation = interrupt.invokeOnCompletion { if (interrupt.isCancelled) connection.disconnect() }
        try {
            currentCoroutineContext().ensureActive()
            connection.connectTimeout = 8_000
            connection.readTimeout = timeout
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "application/json, application/x-ndjson")
            if (body != null) {
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                val bytes = body.toString().toByteArray(Charsets.UTF_8)
                connection.setFixedLengthStreamingMode(bytes.size)
                connection.outputStream.use { it.write(bytes) }
            }
            if (connection.responseCode !in 200..299) {
                throw ServerResponseException("Der Ollama-Server meldet HTTP ${connection.responseCode}. Prüfe die Adresse und den Modellnamen.")
            }
            val result = read(connection)
            currentCoroutineContext().ensureActive()
            result
        } catch (error: IOException) {
            currentCoroutineContext().ensureActive()
            if (error is ServerResponseException) throw error
            throw IOException("Der PC ist nicht erreichbar oder die Verbindung wurde unterbrochen. Prüfe Ollama und die WLAN-Verbindung.", error)
        } finally {
            cancellation.dispose()
            interrupt.complete()
            active.compareAndSet(connection, null)
            connection.disconnect()
        }
    }
    fun cancel() { active.get()?.disconnect() }

    private fun boundedLine(reader: BufferedReader): String? {
        val line = StringBuilder()
        while (true) {
            val next = reader.read()
            if (next < 0) return if (line.isEmpty()) null else line.toString()
            if (next == '\n'.code) return line.toString().removeSuffix("\r")
            require(line.length < 262_144) { "Die Serverantwort ist beschädigt oder zu groß." }
            line.append(next.toChar())
        }
    }
}
