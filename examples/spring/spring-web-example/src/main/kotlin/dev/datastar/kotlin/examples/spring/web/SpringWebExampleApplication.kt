package dev.datastar.kotlin.examples.spring.web

import dev.datastar.kotlin.sdk.Response
import dev.datastar.kotlin.sdk.ServerSentEventGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.OutputStream
import java.io.OutputStreamWriter

@SpringBootApplication
class SpringWebExampleApplication

fun main(args: Array<String>) {
    runApplication<SpringWebExampleApplication>(*args)
}

@RestController
class CounterController {

    private val counter = MutableStateFlow(0)

    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun index() = StreamingResponseBody { stream ->
        this.javaClass.classLoader.getResource("counter.html")?.openStream().use { inputStream ->
            inputStream?.transferTo(stream)
        }
    }

    @GetMapping("/counter", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun counter() = StreamingResponseBody { stream ->
        val response = adapterResponse(stream)
        val generator = ServerSentEventGenerator(response)
        runBlocking {
            counter.collect { event ->
                generator.patchElements("""<span id="counter">${event}</span>""")

                if (event == 3) {
                    generator.executeScript("""alert('Thanks for trying Datastar!')""")
                }
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

    private fun adapterResponse(stream: OutputStream): Response = object : Response {
        private val writer = OutputStreamWriter(stream)

        override fun sendConnectionHeaders(
            status: Int,
            headers: Map<String, List<String>>
        ) = Unit

        override fun write(text: String) = writer.write(text)

        override fun flush() = writer.flush()

    }
}