package dev.datastar.kotlin.sdk.coroutines

/**
 * A minimal suspend representation of a Request.
 * Common interface for the Datastar coroutines SDK with which adapters must comply.
 *
 * Every method is `suspend` to accommodate frameworks whose underlying accessors
 * may themselves be suspending.
 */
interface Request {
    enum class Method {
        GET,
        POST,
        PUT,
        PATCH,
        DELETE,
    }

    suspend fun bodyString(): String

    suspend fun method(): Method

    /**
     * Returns the query-parameter value as the adapter reads it from the underlying HTTP request.
     *
     * **Contract**: the returned string MUST already be URL-decoded.
     */
    suspend fun readParam(string: String): String
}
