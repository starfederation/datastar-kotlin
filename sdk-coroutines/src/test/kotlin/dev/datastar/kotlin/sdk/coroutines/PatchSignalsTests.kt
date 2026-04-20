package dev.datastar.kotlin.sdk.coroutines

import dev.datastar.kotlin.sdk.PatchSignalsOptions
import dev.datastar.kotlin.sdk.testfixtures.testCases
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class PatchSignalsTests {
    @TestFactory
    fun `patchSignals matches shared wire-format fixtures`() =
        testCases
            .filter { case -> case.expectedInput.events.all { it.type == "patchSignals" } }
            .map { case ->
                dynamicTest(case.name) {
                    runTest {
                        val response = TestResponse()
                        val generator = ServerSentEventGenerator(response)

                        handleEvents(generator, case.expectedInput.events)

                        response.output shouldBe case.output
                        response.flushedCount shouldBe 1 + case.expectedInput.events.size
                    }
                }
            }

    @TestFactory
    fun `patchSignals with direct calls`() =
        listOf(
            dynamicTest("default call emits single signals line") {
                runTest {
                    val response = TestResponse()
                    val generator = ServerSentEventGenerator(response)

                    generator.patchSignals(signals = """{"x":1}""")

                    response.output shouldBe
                        """
                        event: datastar-patch-signals
                        data: signals {"x":1}


                        """.trimIndent()
                }
            },
            dynamicTest("onlyIfMissing emits its data line") {
                runTest {
                    val response = TestResponse()
                    val generator = ServerSentEventGenerator(response)

                    generator.patchSignals(
                        signals = """{"x":1}""",
                        options = PatchSignalsOptions(onlyIfMissing = true),
                    )

                    response.output shouldBe
                        """
                        event: datastar-patch-signals
                        data: onlyIfMissing true
                        data: signals {"x":1}


                        """.trimIndent()
                }
            },
        )
}
