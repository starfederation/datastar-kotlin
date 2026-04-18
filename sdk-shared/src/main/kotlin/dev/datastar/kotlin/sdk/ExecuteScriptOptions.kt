package dev.datastar.kotlin.sdk

/**
 * Options for executing a client-side script.
 *
 * Each entry in [attributes] is inserted verbatim into the emitted `<script>` opening tag,
 * separated by a single space. Entries MUST be well-formed HTML attribute fragments
 * (e.g. `type="module"`, `data-foo="bar"`). The SDK does not escape or validate them —
 * tag-breaking input is the caller's responsibility.
 */
data class ExecuteScriptOptions(
    val autoRemove: Boolean = true,
    val attributes: List<String> = emptyList(),
    val eventId: String? = null,
    val retryDuration: Long = DEFAULT_RETRY_DURATION,
)
