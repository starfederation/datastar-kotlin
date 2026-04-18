package dev.datastar.kotlin.sdk.testfixtures

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
    val namespace: String? = null,
)
