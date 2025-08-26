package dev.datastar.kotlin.sdk

/**
 * A minimal representation of a Request.
 * Common interface for the Datastar SDK with which adapters must comply.
 */
interface Request {
    fun bodyString(): String

    fun isGet(): Boolean

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
     *
     * This function is used to transmit a custom event with a specific type, a list of data lines,
     * and optional settings. The event is formatted according to the SSE protocol,
     * ensuring compatibility with SSE-capable clients.
     *
     * @param eventType The type of event to be sent, represented by the `EventType` enum.
     * @param dataLines A list of strings representing the data to be included in the event.
     *                  Each string corresponds to a single "data:" line in the event.
     *                  Multiple lines of data will result in multiple "data:" fields in the event.
     * @param options   Additional options for sending the event, encapsulated in the `SendEventOptions` object.
     *                  These options include fields like `eventId` for identifying the event
     *                  and `retryDuration` for specifying the reconnection time in case of a failure.
     */
    fun send(
        eventType: EventType,
        dataLines: List<String>,
        options: SendEventOptions = SendEventOptions(),
    )

    /**
     * Updates or modifies specified elements on the client-side based on the provided parameters.
     *
     * This function handles patching elements by specifying their content and additional options
     * such as the selector, patch mode, and view transition configurations. It enables dynamic
     * updates to elements through server-sent events while allowing optional retry configurations.
     *
     * @param elements The content or structure of the elements to be patched, represented as a string.
     *                 Can be omitted in case of removal if a selector is provided.
     * @param options  Configuration options for patching elements, encapsulated in a `PatchElementsOptions` object.
     *                 These include the element selector, patch mode, view transition usage, event ID, and retry duration.
     */
    fun patchElements(
        elements: String?,
        options: PatchElementsOptions = PatchElementsOptions(),
    )

    /**
     * Patches the signals on the client-side based on the provided parameters.
     *
     * This function modifies the state or configuration of signals using the provided
     * JSON-formatted string, along with optional patch settings. It enables dynamic
     * behavior updates through server-sent events and supports conditional operations.
     *
     * @param signals A JSON-formatted string representing the signals to be patched.
     *                This string contains the necessary data and configurations for the signals.
     * @param options Configuration options for patching signals, encapsulated in a `PatchSignalsOptions` object.
     *                These include conditions such as whether to patch only if signals are missing, the event ID,
     *                and the retry duration for re-establishing the signals.
     */
    fun patchSignals(
        signals: String,
        options: PatchSignalsOptions = PatchSignalsOptions(),
    )

    /**
     * Executes a client-side script on the connected client.
     *
     * This function is used to send JavaScript code to the client for execution.
     * The script can include optional configuration parameters such as auto-removal,
     * attributes, an event ID for identification, and a retry duration for handling execution failures.
     *
     * @param script The JavaScript code to be executed on the client-side.
     * @param options Optional configuration parameters for script execution, encapsulated
     *                in an `ExecuteScriptOptions` object. This includes:
     *                - `autoRemove`: Whether the script should be automatically removed after execution.
     *                - `attributes`: A list of custom attributes associated with the script execution.
     *                - `eventId`: Optionally specifies an event ID for the script.
     *                - `retryDuration`: Duration (in milliseconds) to specify a retry interval in case of failure.
     */
    fun executeScript(
        script: String,
        options: ExecuteScriptOptions = ExecuteScriptOptions(),
    )
}

enum class EventType(
    val value: String,
) {
    PatchElements("datastar-patch-elements"),
    PatchSignals("datastar-patch-signals"),
}

data class SendEventOptions(
    val eventId: String? = null,
    val retryDuration: Long = DEFAULT_RETRY_DURATION,
)

const val DEFAULT_RETRY_DURATION = 1000L
val DEFAULT_MODE = ElementPatchMode.Outer

data class PatchElementsOptions(
    val selector: String? = null,
    val mode: ElementPatchMode = DEFAULT_MODE,
    val useViewTransition: Boolean = false,
    val eventId: String? = null,
    val retryDuration: Long = DEFAULT_RETRY_DURATION,
)

enum class ElementPatchMode(
    val value: String,
) {
    Outer("outer"),
    Inner("inner"),
    Replace("replace"),
    Prepend("prepend"),
    Append("append"),
    Before("before"),
    After("after"),
    Remove(
        "remove",
    ), ;

    companion object {
        operator fun invoke(value: String?) = entries.firstOrNull { it.value == value } ?: DEFAULT_MODE
    }
}

data class PatchSignalsOptions(
    val onlyIfMissing: Boolean = false,
    val eventId: String? = null,
    val retryDuration: Long = DEFAULT_RETRY_DURATION,
)

data class ExecuteScriptOptions(
    val autoRemove: Boolean = true,
    val attributes: List<String> = emptyList(),
    val eventId: String? = null,
    val retryDuration: Long = DEFAULT_RETRY_DURATION,
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
