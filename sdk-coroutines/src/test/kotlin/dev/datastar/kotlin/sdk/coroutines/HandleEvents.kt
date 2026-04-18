package dev.datastar.kotlin.sdk.coroutines

import dev.datastar.kotlin.sdk.ElementNamespace
import dev.datastar.kotlin.sdk.ElementPatchMode
import dev.datastar.kotlin.sdk.ExecuteScriptOptions
import dev.datastar.kotlin.sdk.JsonMarshaller
import dev.datastar.kotlin.sdk.PatchElementsOptions
import dev.datastar.kotlin.sdk.PatchSignalsOptions
import dev.datastar.kotlin.sdk.testfixtures.Event
import kotlinx.serialization.json.JsonObject

suspend fun handleEvents(
    generator: ServerSentEventGenerator,
    events: List<Event>,
    jsonMarshaller: JsonMarshaller<JsonObject> = { "{\"obj\": \"$it\"}" },
) {
    for (event in events) {
        when (event.type) {
            "patchElements" -> {
                generator.patchElements(
                    elements = event.elements,
                    options =
                        PatchElementsOptions(
                            selector = event.selector,
                            mode = ElementPatchMode(event.mode),
                            useViewTransition = event.useViewTransition ?: false,
                            namespace = ElementNamespace(event.namespace),
                            eventId = event.eventId,
                            retryDuration = event.retryDuration ?: 1000L,
                        ),
                )
            }

            "patchSignals" -> {
                generator.patchSignals(
                    signals = event.signals?.let(jsonMarshaller::invoke) ?: event.signalsRaw!!,
                    options =
                        PatchSignalsOptions(
                            eventId = event.eventId,
                            retryDuration = event.retryDuration ?: 1000L,
                            onlyIfMissing = event.onlyIfMissing ?: false,
                        ),
                )
            }

            "executeScript" -> {
                generator.executeScript(
                    script = event.script!!,
                    options =
                        ExecuteScriptOptions(
                            autoRemove = event.autoRemove ?: true,
                            attributes = event.attributes?.map { (k, v) -> "$k=\"$v\"" } ?: emptyList(),
                            eventId = event.eventId,
                            retryDuration = event.retryDuration ?: 1000L,
                        ),
                )
            }

            else -> throw IllegalArgumentException("Unknown event type: ${event.type}")
        }
    }
}
