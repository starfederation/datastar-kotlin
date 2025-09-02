package dev.datastar.kotlin

import dev.datastar.kotlin.sdk.Response
import dev.datastar.kotlin.sdk.ServerSentEventGenerator
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.StreamingOutput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.Writer

@Path("/")
class CounterApp {

    private val counter: MutableStateFlow<Int> = MutableStateFlow(0)

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun index(): String {
        val file = File(
            "../../../../../front/counter.html"
        )
        println(file.absolutePath)
        return file.readText()
    }

    @GET
    @Path("/counter")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    fun counter() = StreamingOutput { output ->
        runBlocking {
            val writer = output.writer()
            val generator = ServerSentEventGenerator(adaptResponse(writer))

            counter.collect { event ->
                generator.patchElements("""<span id="counter">${event}</span>""")

                if (event == 3) {
                    generator.executeScript("""alert('Thanks for trying Datastar!')""")
                }
            }
        }
    }

    private fun adaptResponse(writer: Writer): Response = object : Response {
        override fun sendConnectionHeaders(status: Int, headers: Map<String, List<String>>) {
            // connection is already set up by JAX-RS
        }

        override fun write(text: String) {
            writer.write(text)
        }

        override fun flush() {
            writer.flush()
        }
    }

    @POST
    @Path("/increment")
    fun increment() {
        counter.value++
    }

    @POST
    @Path("/decrement")
    fun decrement() {
        counter.value--
    }
}