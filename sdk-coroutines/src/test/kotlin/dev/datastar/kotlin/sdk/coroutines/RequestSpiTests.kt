package dev.datastar.kotlin.sdk.coroutines

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RequestSpiTests {
    @Test
    fun `Request Method enum entries cover all HTTP verbs`() {
        Request.Method.entries shouldBe
            listOf(
                Request.Method.GET,
                Request.Method.POST,
                Request.Method.PUT,
                Request.Method.PATCH,
                Request.Method.DELETE,
            )
    }

    @Test
    fun `Request suspend accessors are reachable through an adapter`() =
        runTest {
            val request =
                object : Request {
                    override suspend fun bodyString() = "body"

                    override suspend fun method() = Request.Method.POST

                    override suspend fun readParam(string: String) = "param=$string"
                }

            request.bodyString() shouldBe "body"
            request.method() shouldBe Request.Method.POST
            request.readParam("datastar") shouldBe "param=datastar"
        }
}
