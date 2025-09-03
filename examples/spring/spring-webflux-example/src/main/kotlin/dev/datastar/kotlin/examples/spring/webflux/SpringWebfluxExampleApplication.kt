package dev.datastar.kotlin.examples.spring.webflux

import dev.datastar.kotlin.sdk.Response
import dev.datastar.kotlin.sdk.ServerSentEventGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactor.flux
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@SpringBootApplication
class SpringWebfluxExampleApplication

fun main(args: Array<String>) {
    runApplication<SpringWebfluxExampleApplication>(*args)
}


@RestController
class CounterController {

    private val counter = MutableStateFlow(0)

    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseStatus(HttpStatus.OK)
    suspend fun index() = flow {
        this.javaClass.classLoader.getResource("counter.html")
            ?.openStream()
            ?.use {
                emit(it.readAllBytes())
            }
    }

    @GetMapping("/counter")
    fun counter(response: ServerHttpResponse) = datastarFlux(response) {

        counter.collect { event ->
            patchElements("""<span id="counter">${event}</span>""")

            if (event == 3) {
                executeScript("""alert('Thanks for trying Datastar!')""")
            }
        }

    }


    @PostMapping("/increment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun increment() {
        counter.value++
    }

    @PostMapping("/decrement")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun decrement() {
        counter.value--
    }

}

fun datastarFlux(
    response: ServerHttpResponse,
    sender: suspend ServerSentEventGenerator.() -> Unit
): Flux<DataBuffer> {
    return flux {
        val generator = ServerSentEventGenerator(
            SpringReactiveResponse(
                response,
                send = ::send
            )
        )
        sender(generator)
    }
}

class SpringReactiveResponse(
    private val response: ServerHttpResponse,
    private val send: suspend (DataBuffer) -> Unit
) : Response {

    private val factory = response.bufferFactory()

    override fun sendConnectionHeaders(
        status: Int,
        headers: Map<String, List<String>>
    ) {
        response.statusCode = HttpStatus.valueOf(status)
        response.headers.putAll(headers)
    }

    override fun write(text: String) {
        runBlocking(Dispatchers.IO) {
            send(factory.wrap(text.toByteArray()))
        }
    }

    override fun flush() = Unit
}