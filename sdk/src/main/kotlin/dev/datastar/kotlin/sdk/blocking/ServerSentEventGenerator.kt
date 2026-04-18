package dev.datastar.kotlin.sdk.blocking

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
 * A minimal representation of a Request.
 * Common interface for the Datastar SDK with which adapters must comply.
 */
interface Request {
    enum class Method {
        GET,
        POST,
        PUT,
        PATCH,
        DELETE,
    }

    fun bodyString(): String

    fun method(): Method

    /**
     * Returns the query-parameter value as the adapter reads it from the underlying HTTP request.
     *
     * **Contract**: the returned string MUST already be URL-decoded. The SDK passes it straight
     * to the `JsonUnmarshaller` and does not decode itself. Most web frameworks decode query
     * parameters by default, so adapters typically satisfy this contract without extra work.
     */
    fun readParam(string: String): String
}

/**
 * A minimal representation of a Response.
 * Common interface for the Datastar SDK with which adapters must comply.
 */
interface Response {
    fun sendConnectionHeaders(
        status: Int,
        headers: Map<String, List<String>>,
    )

    /**
     * Writes a string to the response output stream.
     */
    fun write(text: String)

    /**
     * Flushes the response output stream.
     */
    fun flush()
}

/**
 * Write the SSE retry line to the response output stream.
 */
fun Response.writeRetry(retry: Long) = write("retry: $retry\n")

/**
 * Write an SSE data line to the response output stream.
 */
fun Response.writeData(data: String) = write("data: $data\n")

/**
 * Write the SSE event type line to the response output stream.
 */
fun Response.writeEvent(event: EventType) = write("event: ${event.value}\n")

/**
 * Write the SSE event ID line to the response output stream.
 */
fun Response.writeId(id: String) = write("id: $id\n")

/**
 * Interface for generating and sending Server-Sent Events (SSE) to a client.
 *
 * This interface provides methods to send custom SSE messages, patch DOM elements,
 * transmit signals, and execute scripts on the client-side. It ensures compatibility
 * with the SSE protocol through HTTP headers and data stream formatting.
 *
 * The `ServerSentEventGenerator` can be instantiated using the `invoke` function
 * with an implementation of the `StarResponse` interface.
 *
 */
interface ServerSentEventGenerator {
    companion object {
        operator fun invoke(response: Response): ServerSentEventGenerator = ServerSentEventGeneratorBase(response)
    }

    /**
     * Sends a server-sent event (SSE) to a connected client.
     * It's the lowest-level API that complies with the Datastar ADR.
     */
    fun send(
        eventType: EventType,
        dataLines: List<String>,
        options: SendEventOptions = SendEventOptions(),
    )

    /**
     * Updates or modifies specified elements on the client-side based on the provided parameters.
     */
    fun patchElements(
        elements: String? = null,
        options: PatchElementsOptions = PatchElementsOptions(),
    )

    /**
     * Patches the signals on the client-side based on the provided parameters.
     */
    fun patchSignals(
        signals: String,
        options: PatchSignalsOptions = PatchSignalsOptions(),
    )

    /**
     * Executes a client-side script on the connected client.
     */
    fun executeScript(
        script: String,
        options: ExecuteScriptOptions = ExecuteScriptOptions(),
    )
}

fun ServerSentEventGenerator.redirect(url: String) =
    executeScript(
        script = "setTimeout(() => window.location = '$url')",
    )

private class ServerSentEventGeneratorBase(
    private val response: Response,
) : ServerSentEventGenerator {
    init {
        response.sendConnectionHeaders(
            200,
            mapOf(
                "Content-Type" to listOf("text/event-stream"),
                "Connection" to listOf("keep-alive"),
                "Cache-Control" to listOf("no-cache"),
            ),
        )
        response.flush()
    }

    override fun send(
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

    override fun patchElements(
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

    override fun patchSignals(
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

    override fun executeScript(
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
