package dev.datastar.kotlin.sdk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class EventsWrapper(
    val events: List<Event>,
)

@Serializable
data class Event(
    val type: String,
    val elements: String? = null,
    val script: String? = null,
    val eventId: String? = null,
    val retryDuration: Long? = null,
    val attributes: Map<String, String>? = null,
    val autoRemove: Boolean? = null,
    val mode: String? = null,
    val useViewTransition: Boolean? = null,
    val selector: String? = null,
    @SerialName("signals-raw")
    val signalsRaw: String? = null,
    val signals: JsonObject? = null,
    val onlyIfMissing: Boolean? = null,
)

fun handleEvents(
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
                            eventId = event.eventId,
                            retryDuration = event.retryDuration ?: 1000L,
                        ),
                )
            }

            "patchSignals" -> {
                generator.patchSignals(
                    signals =
                        if (event.signals != null) {
                            jsonMarshaller.invoke(event.signals)
                        } else {
                            event.signalsRaw!!
                        },
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
