package dev.datastar.kotlin.sdk

val DEFAULT_MODE = ElementPatchMode.Outer
val DEFAULT_NAMESPACE = ElementNamespace.Html

data class PatchElementsOptions(
    val selector: String? = null,
    val mode: ElementPatchMode = DEFAULT_MODE,
    val useViewTransition: Boolean = false,
    val namespace: ElementNamespace = DEFAULT_NAMESPACE,
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

enum class ElementNamespace(
    val value: String,
) {
    Html("html"),
    Svg("svg"),
    MathMl("mathml"),
    ;

    companion object {
        operator fun invoke(value: String?) = entries.firstOrNull { it.value == value } ?: DEFAULT_NAMESPACE
    }
}

data class PatchSignalsOptions(
    val onlyIfMissing: Boolean = false,
    val eventId: String? = null,
    val retryDuration: Long = DEFAULT_RETRY_DURATION,
)
