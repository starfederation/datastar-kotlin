package dev.datastar.kotlin.sdk

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.net.URLDecoder

@Tag("integration")
class IntegrationTests {
    @Test
    fun `Run Datastar golden suite`() {
        var process: Process? = null
        val server = server()
        try {
            println("Starting test server on port ${server.address.port}")
            server.start()

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
            server.stop(0)
        }
    }

    private fun findTestSuiteVersion(): String =
        System.getProperty("datastar.test-suite.version")
            ?: throw Exception("datastar.test-suite.version system property not set")
}

val jsonUnmarshaller: JsonUnmarshaller<EventsWrapper> = {
    Json.decodeFromString<EventsWrapper>(it)
}

val jsonMarshaller: JsonMarshaller<JsonObject> = {
    Json.encodeToString(it)
}

fun server(): HttpServer =
    HttpServer.create(
        InetSocketAddress(7331),
        0,
        "/test",
        { exchange ->

            val request = adaptRequest(exchange)

            val response = adaptResponse(exchange)

            val signals = readSignals<EventsWrapper>(request, jsonUnmarshaller)

            val generator = ServerSentEventGenerator(response)

            handleEvents(generator, signals.events, jsonMarshaller)

            exchange.close()
        },
    )

private fun adaptResponse(exchange: HttpExchange): Response =
    object : Response {
        override fun sendConnectionHeaders(
            status: Int,
            headers: Map<String, List<String>>,
        ) {
            exchange.responseHeaders.putAll(headers)
            exchange.sendResponseHeaders(status, 0)
        }

        override fun write(text: String) {
            exchange.responseBody.write(text.toByteArray())
        }

        override fun flush() {
            exchange.responseBody.flush()
        }
    }

private fun adaptRequest(exchange: HttpExchange): Request =
    object : Request {
        override fun bodyString() = exchange.requestBody.use { it.readAllBytes().decodeToString() }

        override fun isGet() = exchange.requestMethod == "GET"

        override fun readParam(string: String) =
            exchange.requestURI.query
                ?.let { URLDecoder.decode(it, Charsets.UTF_8) }
                ?.split("&")
                ?.find { it.startsWith("$string=") }
                ?.substringAfter("=")!!
    }
