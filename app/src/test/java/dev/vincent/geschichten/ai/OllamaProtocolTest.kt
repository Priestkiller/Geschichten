package dev.vincent.geschichten.ai

import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test

class OllamaProtocolTest {
    private fun final(tokens: Int = 20, reason: String = "stop") = """{"message":{"role":"assistant","content":""},"done":true,"done_reason":"$reason","prompt_eval_count":$tokens}"""
    @Test fun keepsRoleBoundariesAndSetsTheExplicitRuntimeBudget() {
        val request = OllamaProtocol.request("test", "Profil", listOf(ModelMessage(true,"Frage"),ModelMessage(false,"Antwort"),ModelMessage(true,"Korrektur")))
        val messages = request["messages"].asJsonArray
        assertEquals(listOf("system","user","assistant","user"), messages.map { it.asJsonObject["role"].asString })
        assertEquals("Korrektur",messages[3].asJsonObject["content"].asString)
        assertFalse(request["think"].asBoolean)
        assertEquals(1024,request["options"].asJsonObject["num_predict"].asInt)
        assertEquals(8192,request["options"].asJsonObject["num_ctx"].asInt)
        assertFalse(request.toString().contains("thinking"))
    }
    @Test fun rendersTheOllamaGemma4TextFormatIncludingBosAndEmptyThoughtBlock() {
        val history = listOf(ModelMessage(true,"\u00a0Frage\u0085"),ModelMessage(false," Eins <|channel>geheim<channel|> "),ModelMessage(false,"Zwei"),ModelMessage(true,"Weiter"))
        assertEquals("<bos><|turn>system\nProfil<turn|>\n<|turn>user\nFrage<turn|>\n<|turn>model\nEinsZwei<turn|>\n<|turn>user\nWeiter<turn|>\n<|turn>model\n<|channel>thought\n<channel|>",OllamaProtocol.rendered(" Profil ",history))
    }
    @Test fun showsAndReturnsOnlyContentEvenWhenThinkingAndContentShareAFrame() {
        val visible = StringBuilder()
        val reply = OllamaReply(20)
        reply.accept("""{"message":{"role":"assistant","thinking":"Vertrauliche Analyse","content":"Die Kette "},"done":false}""",visible::append)
        reply.accept("""{"message":{"role":"assistant","content":"kam von Leif."},"done":false}""",visible::append)
        reply.accept(final(),visible::append)
        assertEquals("Die Kette kam von Leif.",reply.finish())
        assertEquals(reply.finish(),visible.toString())
    }
    @Test fun refusesPartialStreamsThinkingOnlyAndTokenLimitBeforeCommit() {
        val partial = OllamaReply(20)
        partial.accept("""{"message":{"role":"assistant","content":"Unvollständig"},"done":false}""") {}
        assertThrows(IllegalStateException::class.java) { partial.finish() }
        val thinkingOnly = OllamaReply(20)
        thinkingOnly.accept(final()) {}
        assertThrows(IllegalStateException::class.java) { thinkingOnly.finish() }
        assertThrows(IllegalArgumentException::class.java) { OllamaReply(20).accept(final(reason="length")) {} }
    }
    @Test fun refusesInputTruncationAndInsufficientOutputReserve() {
        assertThrows(IllegalArgumentException::class.java) { OllamaReply(20).accept(final(19)) {} }
        assertThrows(IllegalArgumentException::class.java) { OllamaReply(7169).accept(final(7169)) {} }
    }
    @Test fun refusesWrongRolesToolsMalformedDataAndServerErrors() {
        for (frame in listOf("invalid", """{"error":"missing model"}""", """{"message":{"role":"user","content":"bad"}}""", """{"message":{"role":"assistant","tool_calls":[{}]}}""")) {
            assertThrows(Exception::class.java) { OllamaReply(20).accept(frame) {} }
        }
    }
    @Test fun onlyAllowsLanHttpOrHttpsWithoutCredentialsPathsOrQueries() {
        assertEquals("http://192.168.178.73:11434",OllamaProtocol.address(" http://192.168.178.73:11434/ "))
        assertEquals("https://example.com",OllamaProtocol.address("https://example.com"))
        for (value in listOf("http://example.com", "http://8.8.8.8", "http://192.168.178.73@8.8.8.8", "http://192.168.178.73/api/chat", "http://192.168.178.73?token=test", "file:///private", "http://192.168.178.999")) {
            assertThrows(Exception::class.java) { OllamaProtocol.address(value) }
        }
    }
    @Test fun pinsTheTokenizerToTheActualTestedWeightsAndRenderer() {
        val show = JsonParser.parseString("""{"model_info":{"general.architecture":"gemma4"},"capabilities":["completion","thinking"],"modelfile":"FROM /models/sha256-${OllamaProtocol.WEIGHTS}\nRENDERER gemma4\n"}""").asJsonObject
        OllamaProtocol.requireSupported(show)
        show.addProperty("modelfile","FROM /other/sha256-000\nRENDERER gemma4\n")
        assertThrows(IllegalArgumentException::class.java) { OllamaProtocol.requireSupported(show) }
    }
}
