package dev.datastar.kotlin.sdk.testcases

import dev.datastar.kotlin.sdk.Event
import dev.datastar.kotlin.sdk.EventsWrapper
import org.junit.jupiter.api.DynamicTest.dynamicTest

data class TestCase(
    val name: String,
    val input: String,
    val output: String,
    val expectedInput: EventsWrapper,
)

val patchElement =
    TestCase(
        name = "single patch element",
        input = """
        {
          "events": [
            {
              "type": "patchElements",
              "elements": "<div>Merge</div>"
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchElements",
                            elements = "<div>Merge</div>",
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-elements
    |data: elements <div>Merge</div>
    |
    |
            """.trimMargin(),
    )

val sendTwoEvents =
    TestCase(
        name = "send two events",
        input = """
        {
          "events": [
            {
              "type": "patchElements",
              "elements": "<div>Merge</div>"
            },
            {
              "type": "patchElements",
              "elements": "<div>Merge 2</div>"
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchElements",
                            elements = "<div>Merge</div>",
                        ),
                        Event(
                            type = "patchElements",
                            elements = "<div>Merge 2</div>",
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-elements
    |data: elements <div>Merge</div>
    |
    |event: datastar-patch-elements
    |data: elements <div>Merge 2</div>
    |
    |
            """.trimMargin(),
    )

val patchElementsWithDefaults =
    TestCase(
        name = "patch elements with defaults",
        input = """
        {
          "events": [
            {
              "type": "patchElements",
              "elements": "<div>Merge</div>",
              "mode": "outer",
              "useViewTransition": false
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchElements",
                            elements = "<div>Merge</div>",
                            mode = "outer",
                            useViewTransition = false,
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-elements
    |data: elements <div>Merge</div>
    |
    |
            """.trimMargin(),
    )

val patchElementWithAllOptions =
    TestCase(
        name = "patch element with all options",
        input = """
        {
          "events": [
            {
              "type": "patchElements",
              "elements": "<div>Merge</div>",
              "eventId": "event1",
              "retryDuration": 2000,
              "selector": "div",
              "mode": "append",
              "useViewTransition": true
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchElements",
                            elements = "<div>Merge</div>",
                            eventId = "event1",
                            retryDuration = 2000,
                            selector = "div",
                            mode = "append",
                            useViewTransition = true,
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-elements
    |id: event1
    |retry: 2000
    |data: selector div
    |data: mode append
    |data: useViewTransition true
    |data: elements <div>Merge</div>
    |
    |
            """.trimMargin(),
    )

val patchElementsWithMultilineElements =
    TestCase(
        name = "patch elements with multiline elements",
        input = """
        {
          "events": [
            {
              "type": "patchElements",
              "elements": "<div>\n  <span>Merge</span>\n</div>"
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchElements",
                            elements = "<div>\n  <span>Merge</span>\n</div>",
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-elements
    |data: elements <div>
    |data: elements   <span>Merge</span>
    |data: elements </div>
    |
    |
            """.trimMargin(),
    )

val patchElementsWithoutDefaults =
    TestCase(
        name = "patch elements without defaults",
        input = """
        {
          "events": [
            {
              "type": "patchElements",
              "elements": "<div>Merge</div>"
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchElements",
                            elements = "<div>Merge</div>",
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-elements
    |data: elements <div>Merge</div>
    |
    |
            """.trimMargin(),
    )

val removeElementsWithAllOptions =
    TestCase(
        name = "remove elements with all options",
        input = """
        {
          "events": [
            {
              "type": "patchElements",
              "selector": "#target",
              "eventId": "event1",
              "mode": "remove",
              "retryDuration": 2000,
              "useViewTransition": true
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchElements",
                            selector = "#target",
                            eventId = "event1",
                            mode = "remove",
                            retryDuration = 2000,
                            useViewTransition = true,
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-elements
    |id: event1
    |retry: 2000
    |data: selector #target
    |data: mode remove
    |data: useViewTransition true
    |
    |
            """.trimMargin(),
    )

val removeElementsWithDefaults =
    TestCase(
        name = "remove elements with defaults",
        input = """
        {
          "events": [
            {
              "type": "patchElements",
              "selector": "#target",
              "mode": "remove",
              "useViewTransition": false
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchElements",
                            selector = "#target",
                            mode = "remove",
                            useViewTransition = false,
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-elements
    |data: selector #target
    |data: mode remove
    |
    |
            """.trimMargin(),
    )

val removeElementsWithoutDefaults =
    TestCase(
        name = "remove elements without defaults",
        input = """
        {
          "events": [
            {
              "type": "patchElements",
              "selector": "#target",
              "mode": "remove"
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchElements",
                            selector = "#target",
                            mode = "remove",
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-elements
    |data: selector #target
    |data: mode remove
    |
    |
            """.trimMargin(),
    )

val patchSignalsWithDefaults =
    TestCase(
        name = "patch signals with defaults",
        input = """
        {
          "events": [
            {
              "type": "patchSignals",
              "signals-raw": "{\"one\":1,\"two\":2}",
              "onlyIfMissing": false
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchSignals",
                            signalsRaw = "{\"one\":1,\"two\":2}",
                            onlyIfMissing = false,
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-signals
    |data: signals {"one":1,"two":2}
    |
    |
            """.trimMargin(),
    )

val patchSignalsWithAllOptions =
    TestCase(
        name = "patch signals with all options",
        input = """
        {
          "events": [
            {
              "type": "patchSignals",
              "signals-raw": "{\"one\":1,\"two\":2}",
              "eventId": "event1",
              "retryDuration": 2000,
              "onlyIfMissing": true
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchSignals",
                            signalsRaw = "{\"one\":1,\"two\":2}",
                            eventId = "event1",
                            retryDuration = 2000,
                            onlyIfMissing = true,
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-signals
    |id: event1
    |retry: 2000
    |data: onlyIfMissing true
    |data: signals {"one":1,"two":2}
    |
    |
            """.trimMargin(),
    )

val patchSignalsWithMultilineJson =
    TestCase(
        name = "patch signals with multiline json",
        input = """
        {
          "events": [
            {
              "type": "patchSignals",
              "signals-raw": "{\n\"one\": \"first signal\",\n\"two\":  \n\"second signal\"}"
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchSignals",
                            signalsRaw = "{\n\"one\": \"first signal\",\n\"two\":  \n\"second signal\"}",
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-signals
    |data: signals {
    |data: signals "one": "first signal",
    |data: signals "two":  
    |data: signals "second signal"}
    |
    |
            """.trimMargin(),
    )

val patchSignalsWithMultilineSignals =
    TestCase(
        name = "patch signals with multiline signals",
        input = """
        {
          "events": [
            {
              "type": "patchSignals",
              "signals-raw": "{\"one\":\"first\\n signal\",\"two\":\"second signal\"}"
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchSignals",
                            signalsRaw = "{\"one\":\"first\\n signal\",\"two\":\"second signal\"}",
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-signals
    |data: signals {"one":"first\n signal","two":"second signal"}
    |
    |
            """.trimMargin(),
    )

val patchSignalsWithoutDefaults =
    TestCase(
        name = "patch signals without defaults",
        input = """
        {
          "events": [
            {
              "type": "patchSignals",
              "signals-raw": "{\"one\":1,\"two\":2}"
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchSignals",
                            signalsRaw = "{\"one\":1,\"two\":2}",
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-signals
    |data: signals {"one":1,"two":2}
    |
    |
            """.trimMargin(),
    )

val removeSignalsWithAllOptions =
    TestCase(
        name = "remove signals with all options",
        input = """
        {
          "events": [
            {
              "type": "patchSignals",
              "signals-raw": "{\"one\":null,\"two\":{\"alpha\":null}}",
              "eventId": "event1",
              "retryDuration": 2000
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchSignals",
                            signalsRaw = "{\"one\":null,\"two\":{\"alpha\":null}}",
                            eventId = "event1",
                            retryDuration = 2000,
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-signals
    |id: event1
    |retry: 2000
    |data: signals {"one":null,"two":{"alpha":null}}
    |
    |
            """.trimMargin(),
    )

val removeSignalsWithDefaults =
    TestCase(
        name = "remove signals with defaults",
        input = """
        {
          "events": [
            {
              "type": "patchSignals",
              "signals-raw": "{\"one\":null}"
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "patchSignals",
                            signalsRaw = "{\"one\":null}",
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-signals
    |data: signals {"one":null}
    |
    |
            """.trimMargin(),
    )

val executeScriptWithDefaults =
    TestCase(
        name = "execute script with defaults",
        input = """
        {
          "events": [
            {
              "type": "executeScript",
              "script": "console.log('hello');",
              "autoRemove": true
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "executeScript",
                            script = "console.log('hello');",
                            autoRemove = true,
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-elements
    |data: selector body
    |data: mode append
    |data: elements <script data-effect="el.remove()">console.log('hello');</script>
    |
    |
            """.trimMargin(),
    )

val executeScriptWithAllOptions =
    TestCase(
        name = "execute script with all options",
        input = """
        {
          "events": [
            {
              "type": "executeScript",
              "script": "console.log('hello');",
              "eventId": "event1",
              "retryDuration": 2000,
              "attributes": {
                "type": "text/javascript",
                "blocking": "false"
              },
              "autoRemove": false
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "executeScript",
                            script = "console.log('hello');",
                            eventId = "event1",
                            retryDuration = 2000,
                            attributes =
                                mapOf(
                                    "type" to "text/javascript",
                                    "blocking" to "false",
                                ),
                            autoRemove = false,
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-elements
    |id: event1
    |retry: 2000
    |data: selector body
    |data: mode append
    |data: elements <script type="text/javascript" blocking="false">console.log('hello');</script>
    |
    |
            """.trimMargin(),
    )

val executeScriptWithMultilineScript =
    TestCase(
        name = "execute script with multiline script",
        input = """
        {
          "events": [
            {
              "type": "executeScript",
              "script": "if (true) {\n  console.log('hello');\n}"
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "executeScript",
                            script = "if (true) {\n  console.log('hello');\n}",
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-elements
    |data: selector body
    |data: mode append
    |data: elements <script data-effect="el.remove()">if (true) {
    |data: elements   console.log('hello');
    |data: elements }</script>
    |
    |
            """.trimMargin(),
    )

val executeScriptWithoutDefaults =
    TestCase(
        name = "execute script without defaults",
        input = """
        {
          "events": [
            {
              "type": "executeScript",
              "script": "console.log('hello');"
            }
          ]
        }
    """,
        expectedInput =
            EventsWrapper(
                events =
                    listOf(
                        Event(
                            type = "executeScript",
                            script = "console.log('hello');",
                        ),
                    ),
            ),
        output =
            """
    |event: datastar-patch-elements
    |data: selector body
    |data: mode append
    |data: elements <script data-effect="el.remove()">console.log('hello');</script>
    |
    |
            """.trimMargin(),
    )

val testCases =
    listOf(
        patchElement,
        sendTwoEvents,
        patchElementsWithDefaults,
        patchElementWithAllOptions,
        patchElementsWithMultilineElements,
        patchElementsWithoutDefaults,
        removeElementsWithAllOptions,
        removeElementsWithDefaults,
        removeElementsWithoutDefaults,
        patchSignalsWithDefaults,
        patchSignalsWithAllOptions,
        patchSignalsWithMultilineJson,
        patchSignalsWithMultilineSignals,
        patchSignalsWithoutDefaults,
        removeSignalsWithAllOptions,
        removeSignalsWithDefaults,
        executeScriptWithDefaults,
        executeScriptWithAllOptions,
        executeScriptWithMultilineScript,
        executeScriptWithoutDefaults,
    )

fun datastarTest(function: (case: TestCase) -> Unit) =
    testCases.map { case ->
        dynamicTest(case.name) {
            function(case)
        }
    }
