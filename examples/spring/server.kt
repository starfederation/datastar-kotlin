import dev.datastar.kotlin.sdk.Response
import dev.datastar.kotlin.sdk.ServerSentEventGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter

///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21
//KOTLIN 2.2.0
//DEPS dev.data-star.kotlin:kotlin-sdk:1.0.0-RC1
//DEPS org.springframework.boot:spring-boot-starter-web:3.5.5
//DEPS org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2

fun main() {
    SpringApplication.run(CounterApp::class.java)
}

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(CounterController::class)
open class CounterApp

@RestController
class CounterController {

    private val counter = MutableStateFlow(0)

    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun index() = StreamingResponseBody { stream ->
        File("../front/counter.html").inputStream().use { inputStream ->
            inputStream.transferTo(stream)
        }
    }

    @GetMapping("/counter", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun counter() = StreamingResponseBody { stream ->
        runBlocking {
            val response = adapterResponse(stream)
            val generator = ServerSentEventGenerator(response)
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
