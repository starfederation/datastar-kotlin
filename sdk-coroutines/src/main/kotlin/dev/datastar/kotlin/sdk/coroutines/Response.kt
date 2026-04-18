package dev.datastar.kotlin.sdk.coroutines

import dev.datastar.kotlin.sdk.EventType

/**
 * A minimal suspend representation of a Response.
 * Common interface for the Datastar coroutines SDK with which adapters must comply.
 *
 * Every method is `suspend` to accommodate frameworks whose underlying emit primitives
 * are themselves suspending.
 */
interface Response {
    suspend fun sendConnectionHeaders(
        status: Int,
        headers: Map<String, List<String>>,
    )

    suspend fun write(text: String)

    suspend fun flush()
}

suspend fun Response.writeRetry(retry: Long) = write("retry: $retry\n")

suspend fun Response.writeData(data: String) = write("data: $data\n")

suspend fun Response.writeEvent(event: EventType) = write("event: ${event.value}\n")

suspend fun Response.writeId(id: String) = write("id: $id\n")
