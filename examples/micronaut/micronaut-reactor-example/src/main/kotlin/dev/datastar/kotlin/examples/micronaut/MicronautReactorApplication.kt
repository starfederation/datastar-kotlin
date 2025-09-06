package dev.datastar.kotlin.examples.micronaut

import dev.datastar.kotlin.sdk.Response
import dev.datastar.kotlin.sdk.ServerSentEventGenerator
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.Status
import io.micronaut.runtime.Micronaut.run
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.reactor.flux
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Flux

fun main(args: Array<String>) {
    run(*args)
}

@Controller
class CounterController {

    private val counter = MutableStateFlow(0)

    @Get
    @Produces(MediaType.TEXT_HTML)
    fun index(): Flux<String> = flux {
        this.javaClass.classLoader
            .getResource("counter.html")
            ?.openStream()
            ?.reader()
            ?.useLines { lines ->
                for (line in lines) {
                    send(line)
                }
            }
    }


    @Get("/counter")
    @Produces(MediaType.TEXT_EVENT_STREAM)
    @Status(HttpStatus.OK)
    fun counter(): Flux<String> = flux {
        val response = MicronautResponse { s: String ->
            runBlocking {
                send(s)
            }
        }

        val generator = ServerSentEventGenerator(response)

        counter.collect { event ->
            generator.patchElements("""<span id="counter">${event}</span>""")

            if (event == 3) {
                generator.executeScript("""alert('Thanks for trying Datastar!')""")
            }
        }
    }

    @Post("/increment")
    @Status(HttpStatus.NO_CONTENT)
    fun increment() {
        counter.value++
    }

    @Post("/decrement")
    @Status(HttpStatus.NO_CONTENT)
    fun decrement() {
        counter.value--
    }

}

class MicronautResponse(
    private val sender: (String) -> Unit,
) : Response {
    override fun flush() = Unit

    override fun sendConnectionHeaders(
        status: Int,
        headers: Map<String, List<String>>
    ) = Unit

    override fun write(text: String) = sender(text)
}