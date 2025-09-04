package dev.datastar.kotlin.sdk

import kotlin.properties.Delegates

class TestResponse : Response {
    var status by Delegates.notNull<Int>()
    var headers: MutableMap<String, List<String>> = mutableMapOf()
    var output: String = ""
    var flushedCount = 0

    override fun sendConnectionHeaders(
        status: Int,
        headers: Map<String, List<String>>,
    ) {
        this.status = status
        this.headers += headers
    }

    override fun write(text: String) {
        output += text
    }

    override fun flush() {
        flushedCount++
    }
}
