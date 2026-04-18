package dev.datastar.kotlin.sdk.coroutines

import dev.datastar.kotlin.sdk.JsonMarshaller
import dev.datastar.kotlin.sdk.JsonUnmarshaller
import dev.datastar.kotlin.sdk.testfixtures.EventsWrapper
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeStringUtf8
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("integration")
class IntegrationTests {
    @Test
    fun `Run Datastar golden suite`() {
        val server = embeddedServer(Netty, port = TEST_PORT, module = Application::testModule).start(wait = false)
        var process: Process? = null
        try {
            println("Starting Datastar golden test suite")
            val testSuiteVersion = findTestSuiteVersion()
            println("Using Datastar test suite version $testSuiteVersion")
            process =
                ProcessBuilder(
                    "go",
                    "run",
                    "github.com/starfederation/datastar/sdk/tests/cmd/datastar-sdk-tests@$testSuiteVersion",
                ).redirectErrorStream(true)
                    .inheritIO()
                    .start()

            val exitCode = process.waitFor()
            withClue("Integration tests failed") {
                exitCode shouldBe 0
            }
        } finally {
            process?.destroy()
            server.stop(1_000L, 5_000L)
        }
    }

    private fun findTestSuiteVersion(): String =
        System.getProperty("datastar.test-suite.version")
            ?: throw Exception("datastar.test-suite.version system property not set")
}

private const val TEST_PORT = 7331

private val jsonUnmarshaller: JsonUnmarshaller<EventsWrapper> = {
    Json.decodeFromString<EventsWrapper>(it)
}

private val jsonMarshaller: JsonMarshaller<JsonObject> = {
    Json.encodeToString(it)
}

private fun Application.testModule() {
    routing {
        route("/test") {
            handle {
                val request = adaptRequest(call)
                val signals = readSignals<EventsWrapper>(request, jsonUnmarshaller)

                call.respondBytesWriter(contentType = ContentType.Text.EventStream) {
                    val response = adaptResponse(this)
                    val generator = ServerSentEventGenerator(response)
                    handleEvents(generator, signals.events, jsonMarshaller)
                }
            }
        }
    }
}

private fun adaptRequest(call: ApplicationCall): Request =
    object : Request {
        override suspend fun bodyString(): String = call.receiveText()

        override suspend fun method(): Request.Method = Request.Method.valueOf(call.request.local.method.value)

        override suspend fun readParam(string: String): String = call.request.queryParameters[string] ?: ""
    }

private fun adaptResponse(channel: ByteWriteChannel): Response =
    object : Response {
        override suspend fun sendConnectionHeaders(
            status: Int,
            headers: Map<String, List<String>>,
        ) {
            // Ktor's respondBytesWriter already set status and Content-Type upfront.
        }

        override suspend fun write(text: String) {
            channel.writeStringUtf8(text)
        }

        override suspend fun flush() {
            channel.flush()
        }
    }
