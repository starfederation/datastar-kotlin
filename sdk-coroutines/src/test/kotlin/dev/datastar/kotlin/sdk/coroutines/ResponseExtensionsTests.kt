package dev.datastar.kotlin.sdk.coroutines

import dev.datastar.kotlin.sdk.EventType
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ResponseExtensionsTests {
    @Test
    fun `writeRetry emits retry line`() =
        runTest {
            val response = TestResponse()
            response.writeRetry(2000)
            response.output shouldBe "retry: 2000\n"
        }

    @Test
    fun `writeData emits data line`() =
        runTest {
            val response = TestResponse()
            response.writeData("elements <div/>")
            response.output shouldBe "data: elements <div/>\n"
        }

    @Test
    fun `writeEvent emits event line with type wire token`() =
        runTest {
            val response = TestResponse()
            response.writeEvent(EventType.PatchElements)
            response.output shouldBe "event: datastar-patch-elements\n"
        }

    @Test
    fun `writeId emits id line`() =
        runTest {
            val response = TestResponse()
            response.writeId("abc")
            response.output shouldBe "id: abc\n"
        }
}
