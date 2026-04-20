package dev.datastar.kotlin.sdk.coroutines

import dev.datastar.kotlin.sdk.JsonUnmarshaller

/**
 * Reads signals from a suspend `Request` and unmarshalls them into the specified type.
 *
 * For `GET` and `DELETE` requests, reads from the `datastar` query parameter.
 * Otherwise reads from the request body. The signal is then converted into the desired type
 * using the provided `unmarshaller` function.
 */
suspend inline fun <reified T> readSignals(
    request: Request,
    unmarshaller: JsonUnmarshaller<T>,
): T {
    if (request.method() in setOf(Request.Method.GET, Request.Method.DELETE)) {
        return unmarshaller(request.readParam("datastar"))
    }
    return unmarshaller(request.bodyString())
}
