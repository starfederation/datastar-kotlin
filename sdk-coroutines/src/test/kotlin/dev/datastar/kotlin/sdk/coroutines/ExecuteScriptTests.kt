package dev.datastar.kotlin.sdk.coroutines

import dev.datastar.kotlin.sdk.ExecuteScriptOptions
import dev.datastar.kotlin.sdk.testfixtures.testCases
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class ExecuteScriptTests {
    @TestFactory
    fun `executeScript matches shared wire-format fixtures`() =
        testCases
            .filter { case -> case.expectedInput.events.all { it.type == "executeScript" } }
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
    fun `executeScript edge cases`() =
        listOf(
            dynamicTest("autoRemove false omits the data-effect attribute") {
                runTest {
                    val response = TestResponse()
                    val generator = ServerSentEventGenerator(response)

                    generator.executeScript(
                        script = "alert('x')",
                        options = ExecuteScriptOptions(autoRemove = false),
                    )

                    response.output shouldBe
                        """
                        event: datastar-patch-elements
                        data: selector body
                        data: mode append
                        data: elements <script>alert('x')</script>


                        """.trimIndent()
                }
            },
            dynamicTest("redirect helper emits setTimeout wrapper script") {
                runTest {
                    val response = TestResponse()
                    val generator = ServerSentEventGenerator(response)

                    generator.redirect("/guide")

                    response.output shouldBe
                        """
                        event: datastar-patch-elements
                        data: selector body
                        data: mode append
                        data: elements <script data-effect="el.remove()">setTimeout(() => window.location = '/guide')</script>


                        """.trimIndent()
                }
            },
        )
}
