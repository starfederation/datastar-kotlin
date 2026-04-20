package dev.datastar.kotlin.sdk.coroutines

import dev.datastar.kotlin.sdk.ElementNamespace
import dev.datastar.kotlin.sdk.ElementPatchMode
import dev.datastar.kotlin.sdk.PatchElementsOptions
import dev.datastar.kotlin.sdk.testfixtures.testCases
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class PatchElementsTests {
    @TestFactory
    fun `patchElements matches shared wire-format fixtures`() =
        testCases
            .filter { case -> case.expectedInput.events.all { it.type == "patchElements" } }
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
    fun `patchElements with defaults from direct call`() =
        listOf(
            dynamicTest("default call emits minimal frame") {
                runTest {
                    val response = TestResponse()
                    val generator = ServerSentEventGenerator(response)

                    generator.patchElements(elements = "<div>Merge</div>")

                    response.output shouldBe
                        """
                        event: datastar-patch-elements
                        data: elements <div>Merge</div>


                        """.trimIndent()
                }
            },
            dynamicTest("Svg namespace is emitted explicitly") {
                runTest {
                    val response = TestResponse()
                    val generator = ServerSentEventGenerator(response)

                    generator.patchElements(
                        elements = "<circle/>",
                        options = PatchElementsOptions(namespace = ElementNamespace.Svg),
                    )

                    response.output shouldBe
                        """
                        event: datastar-patch-elements
                        data: namespace svg
                        data: elements <circle/>


                        """.trimIndent()
                }
            },
            dynamicTest("empty elements produce no elements data lines") {
                runTest {
                    val response = TestResponse()
                    val generator = ServerSentEventGenerator(response)

                    generator.patchElements(elements = "")

                    response.output shouldBe
                        """
                        event: datastar-patch-elements


                        """.trimIndent()
                }
            },
            dynamicTest("remove mode with selector and no elements omits elements parameter") {
                runTest {
                    val response = TestResponse()
                    val generator = ServerSentEventGenerator(response)

                    generator.patchElements(
                        options =
                            PatchElementsOptions(
                                selector = "#target",
                                mode = ElementPatchMode.Remove,
                            ),
                    )

                    response.output shouldBe
                        """
                        event: datastar-patch-elements
                        data: selector #target
                        data: mode remove


                        """.trimIndent()
                }
            },
        )
}
