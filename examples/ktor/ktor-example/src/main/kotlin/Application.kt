package dev.datastar.kotlin

import dev.datastar.kotlin.sdk.Response
import dev.datastar.kotlin.sdk.ServerSentEventGenerator
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.Application
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.Writer

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureRouting()
}

val counter = MutableStateFlow(0)

val counterPage = object {}.javaClass.classLoader.getResourceAsStream(
    "counter.html"
)?.readBytes()!!

fun Application.configureRouting() {
    routing {

        get("/") {
            call.response.headers.append("Content-Type", "text/html")
            call.response.status(OK)
            call.respondBytes(counterPage)
        }

        get("/counter") {
            call.respondTextWriter(
                status = OK,
                contentType = ContentType.Text.EventStream,
            ) {

                val response = adaptResponse(this)
                val generator = ServerSentEventGenerator(response)

                counter.collect { event ->
                    generator.patchElements("""<span id="counter">${event}</span>""")

                    if (event == 3) {
                        generator.executeScript("""alert('Thanks for trying Datastar!')""")
                    }
                }

            }
        }

        post("/increment") {
            counter.value++
            call.response.status(NoContent)
        }

        post("/decrement") {
            counter.value--
            call.response.status(NoContent)
        }

    }

}

private fun adaptResponse(writer: Writer): Response =
    object : Response {
        override fun sendConnectionHeaders(
            status: Int,
            headers: Map<String, List<String>>,
        ) {
            // connection is already set up when used
        }

        override fun write(text: String) {
            writer.write(text)
        }

        override fun flush() {
            writer.flush()
        }
    }

