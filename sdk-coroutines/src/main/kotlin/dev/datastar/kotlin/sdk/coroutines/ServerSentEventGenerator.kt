package dev.datastar.kotlin.sdk.coroutines

import dev.datastar.kotlin.sdk.DEFAULT_MODE
import dev.datastar.kotlin.sdk.DEFAULT_NAMESPACE
import dev.datastar.kotlin.sdk.DEFAULT_RETRY_DURATION
import dev.datastar.kotlin.sdk.ElementPatchMode
import dev.datastar.kotlin.sdk.EventType
import dev.datastar.kotlin.sdk.ExecuteScriptOptions
import dev.datastar.kotlin.sdk.PatchElementsOptions
import dev.datastar.kotlin.sdk.PatchSignalsOptions
import dev.datastar.kotlin.sdk.SendEventOptions

/**
 * Suspend interface for generating and sending Server-Sent Events (SSE) to a client.
 *
 * Mirrors the concepts of the blocking `ServerSentEventGenerator` but every public method
 * is `suspend` so adapters can wire directly to framework-native suspend emit primitives.
 */
interface ServerSentEventGenerator {
    companion object {
        suspend operator fun invoke(response: Response): ServerSentEventGenerator {
            response.sendConnectionHeaders(
                200,
                mapOf(
                    "Content-Type" to listOf("text/event-stream"),
                    "Connection" to listOf("keep-alive"),
                    "Cache-Control" to listOf("no-cache"),
                ),
            )
            response.flush()
            return ServerSentEventGeneratorBase(response)
        }
    }

    /**
     * Sends a server-sent event (SSE) to a connected client.
     * Lowest-level API that complies with the Datastar ADR.
     */
    suspend fun send(
        eventType: EventType,
        dataLines: List<String>,
        options: SendEventOptions = SendEventOptions(),
    )

    /**
     * Updates or modifies specified elements on the client-side based on the provided parameters.
     */
    suspend fun patchElements(
        elements: String? = null,
        options: PatchElementsOptions = PatchElementsOptions(),
    )

    /**
     * Patches the signals on the client-side based on the provided parameters.
     */
    suspend fun patchSignals(
        signals: String,
        options: PatchSignalsOptions = PatchSignalsOptions(),
    )

    /**
     * Executes a client-side script on the connected client.
     */
    suspend fun executeScript(
        script: String,
        options: ExecuteScriptOptions = ExecuteScriptOptions(),
    )
}

suspend fun ServerSentEventGenerator.redirect(url: String) =
    executeScript(
        script = "setTimeout(() => window.location = '$url')",
    )

private class ServerSentEventGeneratorBase(
    private val response: Response,
) : ServerSentEventGenerator {
    override suspend fun send(
        eventType: EventType,
        dataLines: List<String>,
        options: SendEventOptions,
    ) {
        response.writeEvent(eventType)
        options.eventId?.let { response.writeId(it) }
        options.retryDuration.let { if (it != DEFAULT_RETRY_DURATION) response.writeRetry(it) }
        for (dataLine in dataLines) {
            response.writeData(dataLine)
        }
        response.write("\n")
        response.flush()
    }

    override suspend fun patchElements(
        elements: String?,
        options: PatchElementsOptions,
    ) = send(
        eventType = EventType.PatchElements,
        dataLines =
            buildList {
                options.selector?.let { add("selector $it") }
                options.mode.let { if (it != DEFAULT_MODE) add("mode ${it.value}") }
                options.useViewTransition.let { if (it) add("useViewTransition ${true}") }
                options.namespace.let { if (it != DEFAULT_NAMESPACE) add("namespace ${it.value}") }
                (elements ?: "")
                    .lineSequence()
                    .filter { it.isNotBlank() }
                    .forEach { line ->
                        add("elements $line")
                    }
            },
        options =
            SendEventOptions(
                eventId = options.eventId,
                retryDuration = options.retryDuration,
            ),
    )

    override suspend fun patchSignals(
        signals: String,
        options: PatchSignalsOptions,
    ) = send(
        eventType = EventType.PatchSignals,
        dataLines =
            buildList {
                options.onlyIfMissing.let { if (it) add("onlyIfMissing ${true}") }
                signals.lineSequence().forEach { line -> add("signals $line") }
            },
        options =
            SendEventOptions(
                eventId = options.eventId,
                retryDuration = options.retryDuration,
            ),
    )

    override suspend fun executeScript(
        script: String,
        options: ExecuteScriptOptions,
    ) = patchElements(
        elements = "<script${attributes(options.attributes)}${autoRemove(options.autoRemove)}>$script</script>",
        options =
            PatchElementsOptions(
                eventId = options.eventId,
                retryDuration = options.retryDuration,
                mode = ElementPatchMode.Append,
                selector = "body",
            ),
    )

    private fun attributes(attributes: List<String>) =
        if (attributes.isEmpty()) {
            ""
        } else {
            attributes.joinToString(" ", prefix = " ")
        }

    private fun autoRemove(autoRemove: Boolean) = if (autoRemove) " data-effect=\"el.remove()\"" else ""
}
