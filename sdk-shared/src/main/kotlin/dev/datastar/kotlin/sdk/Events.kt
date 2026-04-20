package dev.datastar.kotlin.sdk

enum class EventType(
    val value: String,
) {
    PatchElements("datastar-patch-elements"),
    PatchSignals("datastar-patch-signals"),
}

const val DEFAULT_RETRY_DURATION = 1000L

data class SendEventOptions(
    val eventId: String? = null,
    val retryDuration: Long = DEFAULT_RETRY_DURATION,
)
