package dev.datastar.kotlin.sdk

/**
 * Reads signals from a `StarRequest` object and unmarshalls them into the specified type.
 *
 * This function determines if the `StarRequest` is a GET request or not. If it is, it reads
 * the signal from the request parameter "datastar". Otherwise, it reads the signal from
 * the request body. The signal is then converted into the desired type using the provided
 * `unmarshaller` function.
 *
 * @param T The type into which the signal will be unmarshalled.
 * @param request The `StarRequest` object containing the signal data.
 * @param unmarshaller A function used to unmarshal the signal data into the target type `T`.
 * @return The signal unmarshalled into the specified type `T`.
 */
inline fun <reified T> readSignals(
    request: Request,
    unmarshaller: JsonUnmarshaller<T>,
): T {
    if (request.isGet()) {
        return unmarshaller(request.readParam("datastar"))
    }
    return unmarshaller(request.bodyString())
}

/**
 * A type alias for a function that unmarshalls a JSON string into an object of type `T`.
 *
 * @param T The target type into which the JSON string will be converted.
 */
typealias JsonUnmarshaller<T> = (String) -> T

/**
 * A type alias for a function that serializes an object of type `T` into a JSON string.
 *
 * @param T The type of object that will be serialized into a JSON string.
 */
typealias JsonMarshaller<T> = (T) -> String
