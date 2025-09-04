package dev.datastar.kotlin.examples.httpserver

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import dev.datastar.kotlin.sdk.Response
import dev.datastar.kotlin.sdk.ServerSentEventGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.util.concurrent.Executors


fun main(): Unit = server().run {
    start()
    println("Let's go counting star... http://localhost:${address.port}")
}

fun server(
    counter: MutableStateFlow<Int> = MutableStateFlow(0),
): HttpServer = HttpServer.create(
    InetSocketAddress(8080),
    0
).apply {

    executor = Executors.newVirtualThreadPerTaskExecutor()

    val counterPage = object {}.javaClass.classLoader.getResource(
        "counter.html"
    )!!.readBytes()

    createContext("/") { exchange ->
        exchange.responseHeaders.add("Content-Type", "text/html")
        exchange.sendResponseHeaders(200, counterPage.size.toLong())
        exchange.responseBody.use { os ->
            os.write(counterPage)
        }
        exchange.close()
    }

    sseContext(path = "/counter") {
        runBlocking {
            counter.collect { event ->
                this@sseContext.patchElements(
                    """<span id="counter">${event}</span>"""
                )
                if (event == 3) {
                    this@sseContext.executeScript("""alert('Thanks for trying Datastar!')""")
                }
            }
        }
    }

    createContext("/increment") { exchange ->
        counter.value++
        exchange.sendResponseHeaders(204, -1)
        exchange.close()
    }

    createContext("/decrement") { exchange ->
        counter.value--
        exchange.sendResponseHeaders(204, -1)
        exchange.close()
    }

}

private fun HttpServer.sseContext(
    path: String,
    sender: ServerSentEventGenerator.() -> Unit
) {
    createContext(path) { exchange ->
        try {
            val generator = ServerSentEventGenerator(adaptResponse(exchange))
            sender(generator)
        } finally {
            exchange.close()
        }
    }
}

private fun adaptResponse(exchange: HttpExchange): Response =
    object : Response {
        override fun sendConnectionHeaders(
            status: Int,
            headers: Map<String, List<String>>,
        ) {
            exchange.responseHeaders.putAll(headers)
            exchange.sendResponseHeaders(status, 0)
        }

        override fun write(text: String) {
            exchange.responseBody.write(text.toByteArray())
        }

        override fun flush() {
            exchange.responseBody.flush()
        }
    }