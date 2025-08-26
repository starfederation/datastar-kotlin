package dev.datastar.kotlin.sdk

import dev.datastar.kotlin.sdk.testcases.datastarTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.properties.Delegates

class SendEventTests {
    @TestFactory
    fun `generator send success headers on creation`() =
        datastarTest { _ ->
            val response = TestResponse()

            ServerSentEventGenerator(response)

            response.status shouldBe 200
            response.headers shouldBe
                mapOf(
                    "Content-Type" to listOf("text/event-stream"),
                    "Connection" to listOf("keep-alive"),
                    "Cache-Control" to listOf("no-cache"),
                )
            response.flushedCount shouldBe 1
        }

    @TestFactory
    fun `generator emits events`() =
        datastarTest { case ->
            val response = TestResponse()
            val generator =
                ServerSentEventGenerator(
                    response,
                )

            handleEvents(generator, case.expectedInput.events)

            response.output shouldBe case.output
            response.flushedCount shouldBe 1 + case.expectedInput.events.size
        }

    @Test
    fun `Check defaults for send event options`() {
        val response = TestResponse()
        val generator = ServerSentEventGenerator(response)

        generator.send(
            eventType = EventType.PatchElements,
            dataLines = listOf("test"),
        )

        response.output shouldBe
            """
            event: datastar-patch-elements
            data: test
            
            
            """.trimIndent()
    }

    @Test
    fun `Check defaults for patch elements options`() {
        val response = TestResponse()
        val generator = ServerSentEventGenerator(response)

        generator.patchElements(
            elements = "<div>Merge</div>",
        )

        response.output shouldBe
            """
            event: datastar-patch-elements
            data: elements <div>Merge</div>
            
            
            """.trimIndent()
    }

    @Test
    fun `Check defaults for patch signals options`() {
        val response = TestResponse()
        val generator = ServerSentEventGenerator(response)

        generator.patchSignals(
            signals =
                """
                {
                  "one":1,
                  "two":2
                }
                """.trimIndent(),
        )

        response.output shouldBe
            """
            event: datastar-patch-signals
            data: signals {
            data: signals   "one":1,
            data: signals   "two":2
            data: signals }
            
            
            """.trimIndent()
    }

    @Test
    fun `Check default for executing script`() {
        val response = TestResponse()
        val generator = ServerSentEventGenerator(response)

        generator.executeScript(
            script = "alert('Hello World!')",
        )

        response.output shouldBe
            """
            event: datastar-patch-elements
            data: selector body
            data: mode append
            data: elements <script data-effect="el.remove()">alert('Hello World!')</script>
            
            
            """.trimIndent()
    }

    @Test
    fun `Empty elements are ignored`() {
        val response = TestResponse()
        val generator = ServerSentEventGenerator(response)

        generator.patchElements(elements = "")

        response.output shouldBe
            """
            event: datastar-patch-elements
            
            
            """.trimIndent()
    }
}

class TestResponse : Response {
    var status by Delegates.notNull<Int>()
    var headers: MutableMap<String, List<String>> = mutableMapOf()
    var output: String = ""
    var flushedCount = 0

    override fun sendConnectionHeaders(
        status: Int,
        headers: Map<String, List<String>>,
    ) {
        this.status = status
        this.headers += headers
    }

    override fun write(text: String) {
        output += text
    }

    override fun flush() {
        flushedCount++
    }
}
