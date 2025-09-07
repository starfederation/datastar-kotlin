package dev.datastar.kotlin.examples.javalin

import dev.datastar.kotlin.sdk.Response
import dev.datastar.kotlin.sdk.ServerSentEventGenerator
import io.javalin.Javalin
import io.javalin.http.ContentType
import io.javalin.http.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.eclipse.jetty.http.HttpStatus


fun main() {
    server().start(8080)
}

private fun server(counter: MutableStateFlow<Int> = MutableStateFlow(0)): Javalin = Javalin.create()
    .get("/") { context ->
        context
            .status(HttpStatus.OK_200)
            .contentType(ContentType.TEXT_HTML)

        object {}.javaClass.classLoader.getResource("counter.html")?.openStream()?.let {
            context.result(it)
        }
    }
    .get("/counter") { context ->

        val response = response(context)
        val generator = ServerSentEventGenerator(response)
        generator.patchElements(
            """<span id="counter">${counter.value}</span>"""
        )
        runBlocking {
            counter.collect { event ->
                generator.patchElements(
                    """<span id="counter">${event}</span>"""
                )
                if (event == 3) {
                    generator.executeScript("""alert('Thanks for trying Datastar!')""")
                }
            }
        }
    }
    .post("/increment") { context ->
        counter.value++
        context.status(HttpStatus.NO_CONTENT_204)
    }
    .post("/decrement") {
        counter.value--
        it.status(HttpStatus.NO_CONTENT_204)
    }

private fun response(context: Context): Response = object : Response {
    private val outputStream = context.res().outputStream
    override fun sendConnectionHeaders(
        status: Int,
        headers: Map<String, List<String>>
    ) {
        context.res().status = status
        for ((key, values) in headers) {
            context.res().setHeader(key, values.joinToString(","))
        }
    }

    override fun write(text: String) {
        outputStream.write(text.toByteArray())
    }

    override fun flush() {
        outputStream.flush()
    }
}