import dev.datastar.kotlin.sdk.Response
import dev.datastar.kotlin.sdk.ServerSentEventGenerator
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.io.Writer

///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21
//KOTLIN 2.2.0
//DEPS dev.cloudgt.datastar:kotlin-sdk:0.1.0
//DEPS io.ktor:ktor-server-core-jvm:3.2.3
//DEPS io.ktor:ktor-server-netty-jvm:3.2.3


fun main() {
    server().run {
        println("Let's go counting star... http://localhost:8080")
        start(wait = true)
    }
}

fun server(
    counter: MutableStateFlow<Int> = MutableStateFlow(0),
) = embeddedServer(Netty, port = 8080) {

    val counterPage = File(
        "../front/counter.html"
    ).readBytes()

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