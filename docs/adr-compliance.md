# ADR Compliance Analysis

This document is the living record of how the Datastar Kotlin SDK maps onto the
[official Datastar SDK ADR](https://github.com/starfederation/datastar/blob/develop/sdk/ADR.md).

It exists so that:

1. Any future compliance review can resume from the current state without
   re-deriving intent.
2. Every intentional deviation from the ADR is captured next to its rationale.

## Pinning

- **ADR revision analyzed**: Datastar repo commit
  [`ecb1d4c4043524c1c5c58681c8337ded544f7a3a`](https://github.com/starfederation/datastar/blob/ecb1d4c4043524c1c5c58681c8337ded544f7a3a/sdk/ADR.md)
  (corresponds to release
  [`v1.0.0`](https://github.com/starfederation/datastar/releases/tag/v1.0.0)).
- **SDK version analyzed**: `1.0.0-RC4` (from `sdk/gradle.properties`).
- **Source of truth for the pinned version**: the property
  `datastar.test-suite.version` in `sdk/gradle.properties`. Update that
  property first when re-pinning — it drives the integration-test suite.

When the pinned commit changes, re-run this analysis and bump the revision
above (and the matching line in `CHANGELOG.md`).

## Methodology

The analysis walks the ADR top-to-bottom and, for each normative statement,
records:

| Column | Meaning |
|--------|---------|
| **ADR requirement** | Verbatim or paraphrased rule from the ADR, keyed by its section. |
| **Kotlin impl** | File and symbol implementing it (`path:line` style where useful). |
| **Status** | `OK` (matches) or `DIFFER` (intentionally different — see rationale). |
| **Notes / rationale** | Why we chose a different path; or the fixture that proves compliance. |

The normative-statement extraction treats **MUST / Required** as binding,
**SHOULD** as binding unless a rationale is recorded, and **MAY** as informational.

### How to re-run the analysis

1. Re-pin the commit (see *Pinning* above) and pull the ADR at that commit.
2. For each section below, re-confirm the *Kotlin impl* pointers still exist
   and still behave as described. Prefer exercising the behavior through the
   test suite over reading the code only — tests in `sdk/src/test/kotlin/...`
   are the executable spec.
3. Run the official test suite: `./gradlew integrationTest`. Any new ADR rule
   that is unchecked by the suite should be called out explicitly in the
   matrix, even if the code appears compliant.
4. For every new `DIFFER` finding, write the rationale here before shipping.
   If a decision is still open, record it as a `DIFFER` with the rationale
   `"open — pending decision"` rather than leaving it unmarked.

## Compliance matrix

### 1. `ServerSentEventGenerator` — construction

| ADR requirement | Kotlin impl | Status | Notes / rationale |
|---|---|---|---|
| Constructor MUST accept HTTP Request and Response objects. | `ServerSentEventGenerator(response: Response)` in `ServerSentEventGenerator.kt`. | **DIFFER** | Request is *not* passed to the generator. Reading incoming signals is a separate concern handled by `readSignals(request, unmarshaller)`. This keeps the generator pure-output and framework-agnostic: the same generator can be built on top of any `Response` adapter without the SDK owning a request type. |
| Response MUST set `Cache-Control: no-cache`, `Content-Type: text/event-stream`, `Connection: keep-alive`. | `ServerSentEventGeneratorBase.init` in `ServerSentEventGenerator.kt`. | **OK** | All three headers set via `sendConnectionHeaders(200, …)` on construction. |
| SHOULD flush response immediately. | Same `init` block calls `response.flush()`. | **OK** | |
| SHOULD ensure ordered delivery (e.g. mutex). | Not implemented. | **DIFFER** | Ordering is the caller's responsibility. Rationale: the SDK does not own the underlying `Response` adapter and cannot reason about framework-specific locking semantics (blocking vs suspending, per-request vs per-connection scope). A naive internal mutex could deadlock a framework that already serializes writes, or worse, create false confidence that concurrent sends are safe when they aren't. We keep the generator single-writer by contract and let callers enforce it where they already know the concurrency model. |

### 2. `ServerSentEventGenerator.send`

| ADR requirement | Kotlin impl | Status | Notes / rationale |
|---|---|---|---|
| Accept `eventType`, `dataLines`, optional `eventId` + `retryDuration`. | `send(eventType, dataLines, options)` with `SendEventOptions(eventId, retryDuration)`. | **OK** | |
| Write order: `event:`, `id:` (if set), `retry:` (if not default `1000`), `data:` per line, trailing blank line. | `ServerSentEventGeneratorBase.send`. | **OK** | `retry:` is only written when `retryDuration != DEFAULT_RETRY_DURATION`. |
| SHOULD flush after each event. | `response.flush()` at end of `send`. | **OK** | |
| Errors MUST be returned/thrown per language conventions. | Implicit: any I/O error from `Response.write` / `flush` propagates. | **DIFFER** | No SDK-specific error hierarchy. Rationale: the underlying `Response` adapter owns I/O semantics, and the SDK itself has no failure modes of its own to report (no parsing, no validation). Wrapping adapter exceptions would only hide useful framework diagnostics and add surface area. We will only introduce a custom exception type when a concrete caller need surfaces — not preemptively. |

### 3. `ServerSentEventGenerator.PatchElements`

| ADR requirement | Kotlin impl | Status | Notes / rationale |
|---|---|---|---|
| Parameters: optional `elements`, `selector`, `mode`, `useViewTransition`, `namespace`, `eventId`, `retryDuration`. | `patchElements(elements, PatchElementsOptions(...))`. | **OK** | `namespace` was added in `1.0.0-RC4` (see CHANGELOG "Unreleased"). |
| `ElementPatchMode` enum: `outer` (default), `inner`, `replace`, `prepend`, `append`, `before`, `after`, `remove`. | `enum class ElementPatchMode`. | **OK** | |
| `namespace` enum: `html` (default), `svg`, `mathml`. | `enum class ElementNamespace`. | **OK** | |
| Emit `selector` only if provided. | `options.selector?.let { add("selector $it") }`. | **OK** | |
| Emit `mode` only if not `outer`. | `if (it != DEFAULT_MODE) add("mode ${it.value}")`. | **OK** | |
| Emit `useViewTransition true` only if `true`. | Guarded by `if (it) add(...)`. | **OK** | |
| Emit `namespace` only if not `html`. | Guarded by `if (it != DEFAULT_NAMESPACE) add(...)`. | **OK** | |
| One `elements` line per line of HTML. | `elements.lineSequence().filter { it.isNotBlank() }.forEach { add("elements $line") }`. | **DIFFER** | **Blank lines in the caller-supplied HTML are stripped.** The ADR does not require this. Rationale: whitespace-only input lines are typically formatting artifacts from multi-line Kotlin string literals and emitting them as empty `data: elements ` lines is noise. Validated against the integration-test suite at the pinned version. If the suite starts asserting blank-line preservation, revisit. |
| With `mode = remove`, `elements` MAY be omitted. | `elements: String? = null` and the builder tolerates `null` by using `""`. | **OK** | |

### 4. `ServerSentEventGenerator.PatchSignals`

| ADR requirement | Kotlin impl | Status | Notes / rationale |
|---|---|---|---|
| Parameters: `signals` (required), `onlyIfMissing`, `eventId`, `retryDuration`. | `patchSignals(signals, PatchSignalsOptions(...))`. | **OK** | |
| Emit `onlyIfMissing true` only if `true`. | Guarded. | **OK** | |
| One `signals` line per line of JSON input. | `signals.lineSequence().forEach { add("signals $line") }`. | **OK** | No blank-line filter here (unlike `PatchElements`). Intentional: JSON never contains semantically-empty lines, and the ADR examples show verbatim per-line emission. |

### 5. `ServerSentEventGenerator.ExecuteScript`

| ADR requirement | Kotlin impl | Status | Notes / rationale |
|---|---|---|---|
| Parameters: `script`, `autoRemove` (default `true`), `attributes`, `eventId`, `retryDuration`. | `executeScript(script, ExecuteScriptOptions(...))`. | **OK** | |
| MUST emit via `datastar-patch-elements` with a `<script>` tag. | Implementation delegates to `patchElements` with `mode=Append`, `selector="body"`. | **OK** | |
| If `autoRemove`, `<script>` MUST carry `data-effect="el.remove()"`. | `autoRemove(options.autoRemove)` appends the attribute. | **OK** | |
| Custom `attributes` MUST be added to the `<script>` tag. | `attributes(options.attributes)` joins with spaces. | **OK** | Entries are interpolated verbatim; the caveat that each entry must be a well-formed HTML attribute fragment is documented on `ExecuteScriptOptions` KDoc. |

### 6. `ReadSignals`

| ADR requirement | Kotlin impl | Status | Notes / rationale |
|---|---|---|---|
| Parse incoming HTTP request into a target object. | `readSignals<T>(request, unmarshaller)` in `ReadSignals.kt`. | **OK** | |
| `GET` → decode query param `datastar` as URL-encoded JSON. | `request.readParam("datastar")` then unmarshal. | **DIFFER** | URL decoding is delegated to the `Request` adapter. Rationale: every target framework already URL-decodes query params; re-decoding in the SDK risks double-decoding. The contract is made explicit on the `Request.readParam` KDoc ("returned string MUST already be URL-decoded") and verified by `ReadSignalsTests."does not URL-decode the datastar param"`. |
| `DELETE` → same as `GET`. | Same branch, `DELETE` included in the method set. | **OK** | Added alongside `Request.method()` (see CHANGELOG "Unreleased"). |
| `POST` / `PUT` / `PATCH` → parse request body as JSON. | Falls through to `unmarshaller(request.bodyString())`. | **OK** | |
| MUST return error for invalid JSON. | Error propagation is delegated to the user-supplied `JsonUnmarshaller<T>`. | **DIFFER** | No SDK-owned JSON parser — callers plug in `kotlinx.serialization`, Jackson, Moshi, etc. Whatever exception the parser throws reaches the caller. Rationale: "no dependencies" is a stated design goal (see `README.md`). |

### 7. Naming conventions

The ADR mandates Go-style names but permits language-specific conventions.
The Kotlin SDK uses lowerCamelCase for functions (`patchElements`,
`patchSignals`, `executeScript`, `readSignals`) and UpperCamelCase for types —
this is the idiomatic Kotlin mapping of the ADR names and is allowed by the
ADR's explicit note.
