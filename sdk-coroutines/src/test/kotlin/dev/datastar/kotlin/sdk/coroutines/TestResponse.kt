package dev.datastar.kotlin.sdk.coroutines

class TestResponse : Response {
    var status: Int? = null
    val headers: MutableMap<String, List<String>> = mutableMapOf()
    var output: String = ""
    var flushedCount: Int = 0

    override suspend fun sendConnectionHeaders(
        status: Int,
        headers: Map<String, List<String>>,
    ) {
        this.status = status
        this.headers += headers
    }

    override suspend fun write(text: String) {
        output += text
    }

    override suspend fun flush() {
        flushedCount++
    }
}
