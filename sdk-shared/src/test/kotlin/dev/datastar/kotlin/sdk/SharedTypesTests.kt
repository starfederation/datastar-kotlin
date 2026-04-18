package dev.datastar.kotlin.sdk

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SharedTypesTests {
    @Test
    fun `EventType values map to datastar wire tokens`() {
        EventType.PatchElements.value shouldBe "datastar-patch-elements"
        EventType.PatchSignals.value shouldBe "datastar-patch-signals"
    }

    @Test
    fun `ElementPatchMode invoke returns matching entry for known value`() {
        ElementPatchMode("outer") shouldBe ElementPatchMode.Outer
        ElementPatchMode("append") shouldBe ElementPatchMode.Append
        ElementPatchMode("remove") shouldBe ElementPatchMode.Remove
    }

    @Test
    fun `ElementPatchMode invoke falls back to default for null value`() {
        ElementPatchMode(null) shouldBe DEFAULT_MODE
    }

    @Test
    fun `ElementPatchMode invoke falls back to default for unknown value`() {
        ElementPatchMode("not-a-mode") shouldBe DEFAULT_MODE
    }

    @Test
    fun `ElementNamespace invoke returns matching entry for known value`() {
        ElementNamespace("html") shouldBe ElementNamespace.Html
        ElementNamespace("svg") shouldBe ElementNamespace.Svg
        ElementNamespace("mathml") shouldBe ElementNamespace.MathMl
    }

    @Test
    fun `ElementNamespace invoke falls back to default for null value`() {
        ElementNamespace(null) shouldBe DEFAULT_NAMESPACE
    }

    @Test
    fun `ElementNamespace invoke falls back to default for unknown value`() {
        ElementNamespace("not-a-namespace") shouldBe DEFAULT_NAMESPACE
    }

    @Test
    fun `DEFAULT_RETRY_DURATION is 1000 millis`() {
        DEFAULT_RETRY_DURATION shouldBe 1000L
    }

    @Test
    fun `DEFAULT_MODE is Outer`() {
        DEFAULT_MODE shouldBe ElementPatchMode.Outer
    }

    @Test
    fun `DEFAULT_NAMESPACE is Html`() {
        DEFAULT_NAMESPACE shouldBe ElementNamespace.Html
    }

    @Test
    fun `SendEventOptions defaults and copy`() {
        val defaults = SendEventOptions()
        defaults.eventId shouldBe null
        defaults.retryDuration shouldBe DEFAULT_RETRY_DURATION
        val modified = defaults.copy(eventId = "e", retryDuration = 5000)
        modified.eventId shouldBe "e"
        modified.retryDuration shouldBe 5000
    }

    @Test
    fun `PatchElementsOptions defaults and copy`() {
        val defaults = PatchElementsOptions()
        defaults.selector shouldBe null
        defaults.mode shouldBe DEFAULT_MODE
        defaults.useViewTransition shouldBe false
        defaults.namespace shouldBe DEFAULT_NAMESPACE
        defaults.eventId shouldBe null
        defaults.retryDuration shouldBe DEFAULT_RETRY_DURATION
        val modified =
            defaults.copy(
                selector = "#x",
                mode = ElementPatchMode.Append,
                useViewTransition = true,
                namespace = ElementNamespace.Svg,
                eventId = "e",
                retryDuration = 2000,
            )
        modified.selector shouldBe "#x"
        modified.mode shouldBe ElementPatchMode.Append
    }

    @Test
    fun `PatchSignalsOptions defaults and copy`() {
        val defaults = PatchSignalsOptions()
        defaults.onlyIfMissing shouldBe false
        defaults.eventId shouldBe null
        defaults.retryDuration shouldBe DEFAULT_RETRY_DURATION
        val modified = defaults.copy(onlyIfMissing = true, eventId = "e", retryDuration = 2000)
        modified.onlyIfMissing shouldBe true
    }

    @Test
    fun `ExecuteScriptOptions defaults and copy`() {
        val defaults = ExecuteScriptOptions()
        defaults.autoRemove shouldBe true
        defaults.attributes shouldBe emptyList()
        defaults.eventId shouldBe null
        defaults.retryDuration shouldBe DEFAULT_RETRY_DURATION
        val modified =
            defaults.copy(
                autoRemove = false,
                attributes = listOf("""type="module""""),
                eventId = "e",
                retryDuration = 2000,
            )
        modified.autoRemove shouldBe false
        modified.attributes shouldBe listOf("""type="module"""")
    }
}
