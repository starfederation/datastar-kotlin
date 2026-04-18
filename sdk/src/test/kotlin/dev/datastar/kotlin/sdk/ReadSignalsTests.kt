package dev.datastar.kotlin.sdk

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ReadSignalsTests {
    val passthrough: JsonUnmarshaller<String> = { it }

    @Test
    fun `reads param from GET request`() {
        val request =
            createRequest(
                method = Request.Method.GET,
                param = "param",
            )

        val signals = readSignals(request, passthrough)

        signals shouldBe "param"
    }

    @Test
    fun `reads param from DELETE request`() {
        val request =
            createRequest(
                method = Request.Method.DELETE,
                param = "param",
            )

        val signals = readSignals(request, passthrough)

        signals shouldBe "param"
    }

    @Test
    fun `reads body from POST request`() {
        val request =
            createRequest(
                method = Request.Method.POST,
                body = "body",
            )

        val signals = readSignals(request, passthrough)

        signals shouldBe "body"
    }

    private fun createRequest(
        method: Request.Method,
        body: String = "",
        param: String = "",
    ): Request =
        object : Request {
            override fun bodyString(): String = body

            override fun method(): Request.Method = method

            override fun readParam(string: String): String = param
        }
}
