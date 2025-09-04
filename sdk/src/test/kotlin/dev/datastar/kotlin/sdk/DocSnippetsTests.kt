package dev.datastar.kotlin.sdk

import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver
import java.time.LocalDateTime

const val ONE_SECOND = 1L

@ExtendWith(ResponseParameterResolver::class)
class DocSnippetsTests {
    @Test
    fun `Getting started`(response: TestResponse) {
        // SNIPPET BEGIN
        // title=Getting Started
        // page=https://data-star.dev/guide/getting_started
        val generator = ServerSentEventGenerator(response)

        generator.patchElements(
            elements = """<div id="hal">I’m sorry, Dave. I’m afraid I can’t do that.</div>""",
        )

        Thread.sleep(ONE_SECOND)

        generator.patchElements(
            elements = """<div id="hal">Waiting for an order...</div>""",
        )
        // SNIPPET END

        // Assertion
        response.output shouldBe
            """
            event: datastar-patch-elements
            data: elements <div id="hal">I’m sorry, Dave. I’m afraid I can’t do that.</div>

            event: datastar-patch-elements
            data: elements <div id="hal">Waiting for an order...</div>


            """.trimIndent()
    }

    @Test
    fun `Patching Signals`(response: TestResponse) {
        // SNIPPET BEGIN
        // title=Patching Signals
        // page=https://data-star.dev/guide/reactive_signals#patching-signals
        val generator = ServerSentEventGenerator(response)

        generator.patchSignals(
            signals = """{"hal": "Affirmative, Dave. I read you."}""",
        )

        Thread.sleep(ONE_SECOND)

        generator.patchSignals(
            signals = """{"hal": "..."}""",
        )
        // SNIPPET END

        // Assertion
        response.output shouldBe
            """
            event: datastar-patch-signals
            data: signals {"hal": "Affirmative, Dave. I read you."}
            
            event: datastar-patch-signals
            data: signals {"hal": "..."}


            """.trimIndent()
    }

    @Test
    fun `Reading Signals`() {
        // SNIPPET BEGIN
        // title=Reading Signals
        // page=https://data-star.dev/guide/backend_requests#reading-signals

        // This example uses kotlinx.serialization to deserialize the signal JSON.
        @Serializable
        data class Signals(
            val foo: String,
        )

        val jsonUnmarshaller: JsonUnmarshaller<Signals> = { json -> Json.decodeFromString(json) }

        val request: Request =
            postRequest(
                body =
                    """
                    {
                     "foo": "bar"
                    }
                    """.trimIndent(),
            )

        val signals = readSignals(request, jsonUnmarshaller)
        // SNIPPET END

        // Assertion
        signals shouldBe
            Signals(
                foo = "bar",
            )
    }

    @Test
    fun `SSE Events`(response: TestResponse) {
        // SNIPPET BEGIN
        // title=SSE Events
        // page=https://data-star.dev/guide/backend_requests#sse-events
        val generator = ServerSentEventGenerator(response)

        generator.patchElements(
            elements = """<div id="question">What do you put in a toaster?</div>""",
        )

        generator.patchSignals(
            signals = """{response: '', answer: 'bread'}""",
        )
        // SNIPPET END

        // Assertion
        response.output shouldBe
            """
            event: datastar-patch-elements
            data: elements <div id="question">What do you put in a toaster?</div>
            
            event: datastar-patch-signals
            data: signals {response: '', answer: 'bread'}


            """.trimIndent()
    }

    @Test
    fun `Backend Actions`(response: TestResponse) {
        // SNIPPET BEGIN
        // title=Backend Actions
        // page=https://data-star.dev/guide/backend_requests#backend-actions
        val generator = ServerSentEventGenerator(response)

        generator.patchElements(
            elements = """<div id="question">...</div>""",
        )

        generator.patchElements(
            elements = """<div id="instructions">...</div>""",
        )

        generator.patchSignals(
            signals = """{answer: '...', prize: '...'}""",
        )
        // SNIPPET END

        // Assertion
        response.output shouldBe
            """
            event: datastar-patch-elements
            data: elements <div id="question">...</div>
            
            event: datastar-patch-elements
            data: elements <div id="instructions">...</div>
            
            event: datastar-patch-signals
            data: signals {answer: '...', prize: '...'}


            """.trimIndent()
    }

    @Test
    fun `How to load more list items`(response: TestResponse) {
        for (i in 1..5) {
            val request: Request = getRequest(query = """datastar={"offset":$i}""")

            // SNIPPET BEGIN
            // title=Load More List Items
            // page=https://data-star.dev/how_tos/load_more_list_items
            @Serializable
            data class OffsetSignals(
                val offset: Int,
            )

            val signals =
                readSignals(
                    request,
                    { json: String -> Json.decodeFromString<OffsetSignals>(json) },
                )

            val max = 5
            val limit = 1
            val offset = signals.offset

            val generator = ServerSentEventGenerator(response)

            if (offset < max) {
                val newOffset = offset + limit

                generator.patchElements(
                    elements = "<div>Item $newOffset</div>",
                    options =
                        PatchElementsOptions(
                            selector = "#list",
                            mode = ElementPatchMode.Append,
                        ),
                )

                if (newOffset < max) {
                    generator.patchSignals(
                        signals = """{"offset": $newOffset}""",
                    )
                } else {
                    generator.patchElements(
                        options =
                            PatchElementsOptions(
                                selector = "#load-more",
                                mode = ElementPatchMode.Remove,
                            ),
                    )
                }
            }
            // SNIPPET END
        }

        // Assertion
        response.output shouldBe
            """
            event: datastar-patch-elements
            data: selector #list
            data: mode append
            data: elements <div>Item 2</div>
            
            event: datastar-patch-signals
            data: signals {"offset": 2}
            
            event: datastar-patch-elements
            data: selector #list
            data: mode append
            data: elements <div>Item 3</div>
            
            event: datastar-patch-signals
            data: signals {"offset": 3}
            
            event: datastar-patch-elements
            data: selector #list
            data: mode append
            data: elements <div>Item 4</div>
            
            event: datastar-patch-signals
            data: signals {"offset": 4}
            
            event: datastar-patch-elements
            data: selector #list
            data: mode append
            data: elements <div>Item 5</div>
            
            event: datastar-patch-elements
            data: selector #load-more
            data: mode remove


            """.trimIndent()
    }

    @Test
    fun `How to poll the backend at regular intervals`(response: TestResponse) {
        while (currentTimes.isNotEmpty()) {
            // SNIPPET BEGIN
            // title=Poll the Backend at Regular Intervals
            // page=https://data-star.dev/how_tos/poll_the_backend_at_regular_intervals
            val now: LocalDateTime = currentTime()

            val generator = ServerSentEventGenerator(response)

            generator.patchElements(
                elements =
                    """
                     <div id="time" data-on-interval__duration.5s="@get('/endpoint')">
                        $now
                    </div>
                    """.trimIndent(),
            )
            // SNIPPET END
        }

        // Assertion
        response.output shouldBe
            """
            event: datastar-patch-elements
            data: elements  <div id="time" data-on-interval__duration.5s="@get('/endpoint')">
            data: elements     2023-01-01T00:00:35
            data: elements </div>
            
            event: datastar-patch-elements
            data: elements  <div id="time" data-on-interval__duration.5s="@get('/endpoint')">
            data: elements     2023-01-01T00:00:45
            data: elements </div>
            
            event: datastar-patch-elements
            data: elements  <div id="time" data-on-interval__duration.5s="@get('/endpoint')">
            data: elements     2023-01-01T00:00:55
            data: elements </div>
            
            event: datastar-patch-elements
            data: elements  <div id="time" data-on-interval__duration.5s="@get('/endpoint')">
            data: elements     2023-01-01T00:01:05
            data: elements </div>


            """.trimIndent()
    }

    @Test
    fun `How to poll the backend at regular intervals with increasing frequency`(response: TestResponse) {
        while (currentTimes.isNotEmpty()) {
            // SNIPPET BEGIN
            // title=Poll the Backend at Regular Intervals with Increasing Frequency
            // page=https://data-star.dev/how_tos/poll_the_backend_at_regular_intervals
            val now: LocalDateTime = currentTime()
            val currentSeconds = now.second
            val duration = if (currentSeconds < 50) 5 else 1

            val generator = ServerSentEventGenerator(response)

            generator.patchElements(
                elements =
                    """
                     <div id="time" data-on-interval__duration.${duration}s="@get('/endpoint')">
                        $now
                    </div>
                    """.trimIndent(),
            )
            // SNIPPET END
        }

        // Assertion
        response.output shouldBe
            """
            event: datastar-patch-elements
            data: elements  <div id="time" data-on-interval__duration.5s="@get('/endpoint')">
            data: elements     2023-01-01T00:00:35
            data: elements </div>
            
            event: datastar-patch-elements
            data: elements  <div id="time" data-on-interval__duration.5s="@get('/endpoint')">
            data: elements     2023-01-01T00:00:45
            data: elements </div>
            
            event: datastar-patch-elements
            data: elements  <div id="time" data-on-interval__duration.1s="@get('/endpoint')">
            data: elements     2023-01-01T00:00:55
            data: elements </div>
            
            event: datastar-patch-elements
            data: elements  <div id="time" data-on-interval__duration.5s="@get('/endpoint')">
            data: elements     2023-01-01T00:01:05
            data: elements </div>


            """.trimIndent()
    }

    @Test
    fun `How to redirect page from the backend`(response: TestResponse) {
        // SNIPPET BEGIN
        // title=How to Redirect Page from the Backend
        // page=https://data-star.dev/how_tos/redirect_the_page_from_the_backend
        val generator = ServerSentEventGenerator(response)

        generator.patchElements(
            elements =
                """
                <div id="indicator">Redirecting in 3 seconds...</div>
                """.trimIndent(),
        )

        Thread.sleep(3 * ONE_SECOND)

        generator.executeScript(
            script = "window.location.href = '/success'",
        )
        // SNIPPET END

        // Assertion
        response.output shouldBe
            """
            event: datastar-patch-elements
            data: elements <div id="indicator">Redirecting in 3 seconds...</div>
            
            event: datastar-patch-elements
            data: selector body
            data: mode append
            data: elements <script data-effect="el.remove()">window.location.href = '/success'</script>


            """.trimIndent()
    }

    @Test
    fun `How to redirect page from the backend with timeout`(response: TestResponse) {
        // SNIPPET BEGIN
        // title=How to Redirect Page from the Backend with Timeout
        // page=https://data-star.dev/how_tos/redirect_the_page_from_the_backend
        val generator = ServerSentEventGenerator(response)

        generator.patchElements(
            elements =
                """
                <div id="indicator">Redirecting in 3 seconds...</div>
                """.trimIndent(),
        )

        Thread.sleep(3 * ONE_SECOND)

        generator.executeScript(
            script = "setTimeout(() => window.location = '/guide')",
        )
        // SNIPPET END

        // Assertion
        response.output shouldBe
            """
            event: datastar-patch-elements
            data: elements <div id="indicator">Redirecting in 3 seconds...</div>
            
            event: datastar-patch-elements
            data: selector body
            data: mode append
            data: elements <script data-effect="el.remove()">setTimeout(() => window.location = '/guide')</script>


            """.trimIndent()
    }

    @Test
    fun `How to redirect page from the backend with helper`(response: TestResponse) {
        // SNIPPET BEGIN
        // title=How to Redirect Page from the Backend with Helper
        // page=https://data-star.dev/how_tos/redirect_the_page_from_the_backend
        val generator = ServerSentEventGenerator(response)

        generator.patchElements(
            elements =
                """
                <div id="indicator">Redirecting in 3 seconds...</div>
                """.trimIndent(),
        )

        Thread.sleep(3 * ONE_SECOND)

        generator.redirect("/guide")
        // SNIPPET END

        // Assertion
        response.output shouldBe
            """
            event: datastar-patch-elements
            data: elements <div id="indicator">Redirecting in 3 seconds...</div>
            
            event: datastar-patch-elements
            data: selector body
            data: mode append
            data: elements <script data-effect="el.remove()">setTimeout(() => window.location = '/guide')</script>


            """.trimIndent()
    }

    private fun postRequest(body: String): Request =
        object : Request {
            override fun bodyString() = body

            override fun isGet(): Boolean = false

            override fun readParam(string: String) = ""
        }

    private fun getRequest(query: String): Request =
        object : Request {
            override fun bodyString() = ""

            override fun isGet(): Boolean = true

            override fun readParam(string: String) = query.split("=")[1]
        }

    private val currentTimes =
        mutableListOf(
            "2023-01-01T00:00:35",
            "2023-01-01T00:00:45",
            "2023-01-01T00:00:55",
            "2023-01-01T00:01:05",
        )

    private fun currentTime(): LocalDateTime {
        val now = currentTimes.removeFirst()
        return LocalDateTime.parse(now)
    }
}

class ResponseParameterResolver : TypeBasedParameterResolver<TestResponse>() {
    override fun resolveParameter(
        parameterContext: ParameterContext?,
        extensionContext: ExtensionContext?,
    ): TestResponse = TestResponse()
}
