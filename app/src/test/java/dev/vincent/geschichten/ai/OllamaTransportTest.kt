package dev.vincent.geschichten.ai

import com.google.gson.JsonParser
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class OllamaTransportTest {
    private class TestServer : AutoCloseable {
        val listener = ServerSocket().apply { bind(InetSocketAddress("127.0.0.1",0)) }
        val port get() = listener.localPort
        private val executor = Executors.newCachedThreadPool { task -> Thread(task).apply {isDaemon=true} }
        private val sockets = ConcurrentHashMap.newKeySet<Socket>()
        private val routes = mutableMapOf<String,(Exchange)->Unit>()
        fun createContext(path: String, handler: (Exchange)->Unit) { routes[path]=handler }
        fun start() { executor.submit {
            while (!listener.isClosed) {
                val socket = try {listener.accept()} catch (_: Exception) {break}
                sockets.add(socket)
                executor.submit {
                    try { val exchange=Exchange(socket); checkNotNull(routes[exchange.path])(exchange) }
                    finally { socket.close(); sockets.remove(socket) }
                }
            }
        } }
        override fun close() { listener.close(); sockets.forEach {it.close()};executor.shutdownNow() }
    }
    private class Exchange(private val socket: Socket) {
        private val input = socket.getInputStream().buffered()
        private fun line(): String = buildString { while(true) {val next=input.read();if(next<0 || next==10)break;append(next.toChar())} }.removeSuffix("\r")
        val path: String
        val requestBody: ByteArrayInputStream
        val responseBody get() = socket.getOutputStream()
        val responseHeaders = Headers()
        class Headers {
            val entries = mutableListOf<Pair<String,String>>()
            fun add(name:String,value:String) {entries.add(name to value)}
        }
        init {
            socket.soTimeout=5000
            path=line().split(' ')[1]
            var length=0
            while(true) {val header=line();if(header.isEmpty())break;if(header.startsWith("Content-Length:",true))length=header.substringAfter(':').trim().toInt()}
            val body=ByteArray(length)
            var used=0
            while(used<length) {val size=input.read(body,used,length-used);check(size>0);used+=size}
            requestBody=ByteArrayInputStream(body)
        }
        fun sendResponseHeaders(status:Int,length:Long) {
            val header=buildString {
                append("HTTP/1.1 $status Test\r\nConnection: close\r\nContent-Type: application/x-ndjson\r\n")
                if(length>0)append("Content-Length: $length\r\n")
                responseHeaders.entries.forEach {(name,value)->append("$name: $value\r\n")}
                append("\r\n")
            }
            responseBody.write(header.toByteArray(Charsets.US_ASCII));responseBody.flush()
        }
        fun close() {socket.close()}
    }
    private fun server(block: (TestServer,String) -> Unit) {
        val server = TestServer()
        try { block(server,"http://127.0.0.1:${server.port}") }
        finally { server.close() }
    }
    @Test fun streamsTheRealUtf8HttpProtocolAndStopsAtDone() = server { server,address ->
        server.createContext("/api/chat") { exchange ->
            val sent = JsonParser.parseString(exchange.requestBody.bufferedReader().use {it.readText()}).asJsonObject
            check(!sent["think"].asBoolean)
            exchange.sendResponseHeaders(200,0)
            exchange.responseBody.use { output ->
                output.write("""{"message":{"role":"assistant","thinking":"geheim"},"done":false}
{"message":{"role":"assistant","content":"Grüße von Leif."},"done":false}
{"message":{"role":"assistant","content":""},"done":true,"done_reason":"stop","prompt_eval_count":20}
""".toByteArray(Charsets.UTF_8))
                output.flush()
            }
        }
        server.start()
        val visible = StringBuilder()
        val result = runBlocking { OllamaTransport().generate(address,OllamaProtocol.request("test","Profil",listOf(ModelMessage(true,"Frage"))),20,visible::append) }
        assertEquals("Grüße von Leif.",result)
        assertEquals(result,visible.toString())
    }
    @Test fun cancellationUnblocksTheSocketDuringThinking() = server { server,address ->
        val thinking = CountDownLatch(1)
        val release = CountDownLatch(1)
        val completed = CountDownLatch(1)
        server.createContext("/api/chat") { exchange ->
            exchange.requestBody.use { it.readBytes() }
            exchange.sendResponseHeaders(200,0)
            exchange.responseBody.use { output ->
                output.write("""{"message":{"role":"assistant","thinking":"noch keine Antwort"},"done":false}
""".toByteArray())
                output.flush(); thinking.countDown(); release.await(8,TimeUnit.SECONDS)
            }
        }
        server.start()
        runBlocking {
            val request = launch(Dispatchers.IO) {
                try { OllamaTransport().generate(address,OllamaProtocol.request("test","Profil",listOf(ModelMessage(true,"Frage"))),20) {} }
                finally { completed.countDown() }
            }
            assertTrue(thinking.await(4,TimeUnit.SECONDS))
            val cancel = launch(Dispatchers.IO) { request.cancel() }
            val prompt = completed.await(2,TimeUnit.SECONDS)
            release.countDown()
            cancel.join(); request.join()
            assertTrue("Cancellation waited for server data",prompt)
        }
    }
    @Test fun rejectsRedirectsAndPrematureEof() = server { server,address ->
        server.createContext("/redirect") { exchange -> exchange.responseHeaders.add("Location","/api/chat");exchange.sendResponseHeaders(302,-1);exchange.close() }
        server.createContext("/api/chat") { exchange ->
            exchange.requestBody.use {it.readBytes()}
            val text = """{"message":{"role":"assistant","content":"Nur ein Teil"},"done":false}
""".toByteArray()
            exchange.sendResponseHeaders(200,text.size.toLong());exchange.responseBody.use {it.write(text)}
        }
        server.start()
        val redirect=assertThrows(Exception::class.java) { runBlocking { OllamaTransport().json(address,"/redirect") } }
        assertTrue(redirect.message.orEmpty().contains("HTTP 302"))
        assertThrows(IllegalStateException::class.java) { runBlocking { OllamaTransport().generate(address,OllamaProtocol.request("test","Profil",listOf(ModelMessage(true,"Frage"))),20) {} } }
    }
}
