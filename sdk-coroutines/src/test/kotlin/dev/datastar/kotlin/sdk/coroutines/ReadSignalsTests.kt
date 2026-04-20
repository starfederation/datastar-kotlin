package dev.datastar.kotlin.sdk.coroutines

import dev.datastar.kotlin.sdk.JsonUnmarshaller
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ReadSignalsTests {
    private val passthrough: JsonUnmarshaller<String> = { it }

    @Test
    fun `reads param from GET request`() =
        runTest {
            val request = testRequest(method = Request.Method.GET, param = "param")

            val signals = readSignals(request, passthrough)

            signals shouldBe "param"
        }

    @Test
    fun `reads param from DELETE request`() =
        runTest {
            val request = testRequest(method = Request.Method.DELETE, param = "param")

            val signals = readSignals(request, passthrough)

            signals shouldBe "param"
        }

    @Test
    fun `does not URL-decode the datastar param - adapter delivers decoded value`() =
        runTest {
            val captured = mutableListOf<String>()
            val capturing: JsonUnmarshaller<String> = {
                captured += it
                it
            }
            val request =
                testRequest(
                    method = Request.Method.GET,
                    param = "%7B%22a%22%3A1%7D",
                )

            readSignals(request, capturing)

            captured shouldBe listOf("%7B%22a%22%3A1%7D")
        }

    @Test
    fun `reads body from POST request`() =
        runTest {
            val request = testRequest(method = Request.Method.POST, body = "body")

            val signals = readSignals(request, passthrough)

            signals shouldBe "body"
        }

    @Test
    fun `reads body from PUT request`() =
        runTest {
            val request = testRequest(method = Request.Method.PUT, body = "body")

            val signals = readSignals(request, passthrough)

            signals shouldBe "body"
        }

    @Test
    fun `reads body from PATCH request`() =
        runTest {
            val request = testRequest(method = Request.Method.PATCH, body = "body")

            val signals = readSignals(request, passthrough)

            signals shouldBe "body"
        }

    private fun testRequest(
        method: Request.Method,
        body: String = "",
        param: String = "",
    ): Request =
        object : Request {
            override suspend fun bodyString(): String = body

            override suspend fun method(): Request.Method = method

            override suspend fun readParam(string: String): String = param
        }
}
