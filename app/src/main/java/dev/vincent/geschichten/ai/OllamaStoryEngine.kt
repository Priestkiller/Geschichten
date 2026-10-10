package dev.vincent.geschichten.ai

import android.content.Context
import com.google.gson.JsonObject
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class OllamaStoryEngine(context: Context, initial: OllamaSettings) : StoryGeneration {
    private val context = context.applicationContext
    private val operation = Mutex()
    private val transport = OllamaTransport()
    private val closed = AtomicBoolean(false)
    private var settings = initial
    private var tokenizer: ExactTokenizer? = null
    private val _state = MutableStateFlow(initialState())
    override val state = _state.asStateFlow()
    override val contextTokens = OllamaProtocol.CONTEXT
    override val outputReserve = OllamaProtocol.RESERVE

    private fun initialState() = ModelState(modelId = settings.model, server = true,
        detail = "Verbinde die App mit Ollama auf deinem PC.", deviceName = "Dein PC")

    suspend fun connect(configuration: OllamaSettings) = withContext(Dispatchers.IO) {
        operation.withLock {
            check(!closed.get())
            settings = configuration.copy(address = OllamaProtocol.address(configuration.address), model = OllamaProtocol.model(configuration.model))
            tokenizer?.close(); tokenizer = null
            _state.value = initialState().copy(stage = ModelStage.LOADING, detail = "Ollama und das ausgewählte Modell werden geprüft …")
            try {
                transport.json(settings.address, "/api/version")
                OllamaProtocol.requireSupported(transport.json(settings.address, "/api/show", JsonObject().apply { addProperty("model", settings.model) }))
                tokenizer = ExactTokenizer(context, OllamaProtocol.TOKENIZER_ID)
                _state.value = initialState().copy(stage = ModelStage.READY, detail = "Mit deinem PC verbunden · ${settings.model}")
            } catch (cancelled: CancellationException) {
                _state.value = initialState()
                throw cancelled
            } catch (error: Exception) {
                _state.value = initialState().copy(stage = ModelStage.ERROR, detail = error.message.orEmpty())
                throw error
            }
        }
    }

    override suspend fun countPrompt(system: String, history: List<ModelMessage>): Int = withContext(Dispatchers.IO) {
        operation.withLock {
            check(!closed.get())
            checkNotNull(tokenizer) { "Verbinde zuerst den Ollama-Server." }.count(OllamaProtocol.rendered(system, history))
        }
    }

    override suspend fun generate(systemPrompt: String, history: List<ModelMessage>, recordTiming: Boolean, onToken: (String) -> Unit): String =
        withContext(Dispatchers.IO) {
            operation.withLock {
                check(!closed.get())
                val expected = checkNotNull(tokenizer) { "Verbinde zuerst den Ollama-Server." }.count(OllamaProtocol.rendered(systemPrompt, history))
                require(expected + outputReserve <= contextTokens) { "Gespräch und Antwortreserve passen nicht in das Kontextfenster. Deine Nachricht bleibt als Entwurf erhalten." }
                val ready = _state.value.copy(stage = ModelStage.READY)
                val started = System.nanoTime()
                var first = true
                _state.value = ready.copy(stage = ModelStage.GENERATING, detail = "Dein PC bereitet die Antwort vor …")
                try {
                    val result = transport.generate(settings.address, OllamaProtocol.request(settings.model, systemPrompt, history), expected) { text ->
                        if (first) {
                            first = false
                            _state.value = _state.value.copy(detail = "Deine Figur schreibt auf dem PC …",
                                firstTextMs = if (recordTiming) (System.nanoTime() - started) / 1_000_000 else ready.firstTextMs)
                        }
                        onToken(text)
                    }
                    _state.value = _state.value.copy(stage = ModelStage.READY, detail = "Mit deinem PC verbunden · ${settings.model}")
                    result
                } catch (cancelled: CancellationException) {
                    _state.value = ready.copy(detail = "Antwort angehalten. Du kannst weiterschreiben.")
                    throw cancelled
                } catch (error: Exception) {
                    _state.value = ready.copy(stage = ModelStage.ERROR, detail = error.message.orEmpty())
                    throw error
                }
            }
        }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        operation.withLock { tokenizer?.close(); tokenizer = null; _state.value = initialState() }
    }
    override fun cancelGeneration() = transport.cancel()
    override suspend fun clearConversationCache() = Unit // Every request carries its complete planned history.
    override fun close() {
        if (closed.compareAndSet(false, true)) {
            transport.cancel()
            CoroutineScope(Dispatchers.IO).launch { operation.withLock { tokenizer?.close(); tokenizer = null } }
        }
    }
}
