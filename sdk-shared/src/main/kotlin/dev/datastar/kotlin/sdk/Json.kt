package dev.datastar.kotlin.sdk

/**
 * A type alias for a function that unmarshalls a JSON string into an object of type `T`.
 */
typealias JsonUnmarshaller<T> = (String) -> T

/**
 * A type alias for a function that serializes an object of type `T` into a JSON string.
 */
typealias JsonMarshaller<T> = (T) -> String
