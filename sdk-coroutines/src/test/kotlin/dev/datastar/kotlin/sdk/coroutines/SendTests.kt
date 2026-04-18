package dev.datastar.kotlin.sdk.coroutines

import dev.datastar.kotlin.sdk.EventType
import dev.datastar.kotlin.sdk.SendEventOptions
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SendTests {
    @Test
    fun `invoke sends connection headers and flushes`() =
        runTest {
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

    @Test
    fun `send emits event with single data line and trailing newline`() =
        runTest {
            val response = TestResponse()
            val generator = ServerSentEventGenerator(response)

            generator.send(EventType.PatchElements, listOf("elements <div/>"))

            response.output shouldBe
                """
                event: datastar-patch-elements
                data: elements <div/>


                """.trimIndent()
            response.flushedCount shouldBe 2
        }

    @Test
    fun `send emits id line when eventId provided`() =
        runTest {
            val response = TestResponse()
            val generator = ServerSentEventGenerator(response)

            generator.send(
                EventType.PatchElements,
                listOf("data-only"),
                SendEventOptions(eventId = "e1"),
            )

            response.output shouldBe
                """
                event: datastar-patch-elements
                id: e1
                data: data-only


                """.trimIndent()
        }

    @Test
    fun `send emits retry line when retryDuration is non-default`() =
        runTest {
            val response = TestResponse()
            val generator = ServerSentEventGenerator(response)

            generator.send(
                EventType.PatchSignals,
                listOf("signals {}"),
                SendEventOptions(retryDuration = 2000),
            )

            response.output shouldBe
                """
                event: datastar-patch-signals
                retry: 2000
                data: signals {}


                """.trimIndent()
        }

    @Test
    fun `send omits retry line when retryDuration equals default`() =
        runTest {
            val response = TestResponse()
            val generator = ServerSentEventGenerator(response)

            generator.send(
                EventType.PatchElements,
                listOf("data"),
                SendEventOptions(retryDuration = 1000),
            )

            response.output shouldBe
                """
                event: datastar-patch-elements
                data: data


                """.trimIndent()
        }

    @Test
    fun `send emits multiple data lines in order`() =
        runTest {
            val response = TestResponse()
            val generator = ServerSentEventGenerator(response)

            generator.send(
                EventType.PatchElements,
                listOf("selector #x", "elements <span/>"),
            )

            response.output shouldBe
                """
                event: datastar-patch-elements
                data: selector #x
                data: elements <span/>


                """.trimIndent()
        }

    @Test
    fun `send with empty data lines still emits trailing newline and flush`() =
        runTest {
            val response = TestResponse()
            val generator = ServerSentEventGenerator(response)

            generator.send(EventType.PatchElements, emptyList())

            response.output shouldBe
                """
                event: datastar-patch-elements


                """.trimIndent()
            response.flushedCount shouldBe 2
        }
}
