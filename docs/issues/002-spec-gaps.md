# Issue: Remaining Spec Gaps After Spec1 + Spec2

**Area**: cross-cutting
**Severity**: Medium — mostly runtime/backend stubs, not blocking UI validation
**Specs affected**: spec1, spec2

---

## Spec1 Gaps

### GAP-S1-01: `SessionCancelled` event is defined but never emitted or handled

`RuntimeSessionEvent.SessionCancelled` exists in the sealed interface but `RuntimeSessionOrchestrator` never emits it and `AgentWorkspaceViewModel` silently drops it (`is RuntimeSessionEvent.SessionCancelled -> Unit`). The spec edge case "what happens when a session is cancelled while a capability call is in progress?" is unaddressed.

**Spec reference**: spec1 edge cases, spec2 FR-004 (CANCELLED terminal outcome).

### GAP-S1-02: Streamed content overwritten at completion

In `AgentWorkspaceViewModel.finalizeAssistantTurn`, the assistant turn's content is replaced with `event.outcome.outputText` from `SessionCompleted`. The chunked content built up during `CapabilityOutputChunk` events is discarded and replaced by the final output string from the provider. If these differ (e.g. the provider normalizes whitespace or trims the output), the visible text will visibly jump at the end of generation.

**Spec reference**: spec1 FR-005 (streamed output visible while in progress), SC-002.

### GAP-S1-03: `CapabilityFailed` event is silently dropped

`AgentWorkspaceViewModel` drops `CapabilityFailed` (`is RuntimeSessionEvent.CapabilityFailed -> Unit`). The UI only reacts to `SessionFailed`. If a capability fails and `finishSession` is not called in the orchestrator (or is called after a delay), the UI stays in `STREAMING` indefinitely.

**Spec reference**: spec1 FR-008.

### GAP-S1-04: Model import is in the ViewModel but not in spec1

`onImportModel(Uri)` and `ModelImportResult` are implemented in the ViewModel and wired to a file picker in the screen. Neither spec1 nor spec2 mentions model import. This is forward-looking work that has landed in the v0 shell without a spec backing it.

---

## Spec2 Gaps

### GAP-S2-01: `NoOpRuntimeContextLoader` does nothing

`contextLoader.load(request)` is called in the orchestrator but returns an empty/stub payload. The spec explicitly reserves this as an integration point (FR-007), which is acceptable for v0, but the stub should prevent the context field from being passed to the provider with stale or missing data in later milestones. There is no contract yet for what `contextPayload` looks like or how the provider should use it.

### GAP-S2-02: `DefaultRuntimeExecutionGate` always approves

The gate always returns `ALLOW`. The `awaitingInput` field in `RuntimeStatusSummary` is set based on whether `"approval"` appears as a substring in the gate details string — a fragile string-match heuristic. When a real gate is introduced, this check will need to be replaced with a structured signal.

**Spec reference**: spec2 FR-007, edge case "what if approval is required".

### GAP-S2-03: Sessions accumulate in `RuntimeSessionRegistry` indefinitely

`RuntimeSessionRegistry` stores every session in a `MutableStateFlow<Map<String, ExecutionSession>>` with no eviction. In extended use the map grows without bound. There is no `cancelSession` method and no expiry mechanism.

**Spec reference**: spec2 FR-008 (one session must not corrupt another — memory growth is a related risk).

### GAP-S2-04: `CapabilityProviderRegistry.getProvider(plan)` can throw

`getProvider(plan)` resolves the provider by iterating registered providers and finding the first that `supports(plan)`. If no provider matches, the behavior is undefined (runtime crash or exception). There is no fallback, no graceful DENIED outcome, and no user-facing message.

**Spec reference**: spec2 FR-004 (DENIED terminal outcome should be reachable for unsupported capabilities).

---

## Shared / Structural Observations

### OBS-01: Dual state sync for stage label and context summary

`AgentWorkspaceUiState` holds both:
- `stageLabel: String` and `contextSummary: String` (plain strings, updated directly)
- `runtimeStatus: RuntimeStatusUiModel` (structured, updated from `StatusSummaryUpdated`)

The ViewModel updates both independently in multiple places. If a code path updates one but not the other, the `ContextWindowCard` (which reads `runtimeStatus`) and any legacy reader of `stageLabel`/`contextSummary` will diverge. Consider consolidating to a single source.

### OBS-02: `finalizeAssistantTurn` sets `isTerminal = true` unconditionally

After any successful completion, `runtimeStatus.isTerminal` is set to `true`. But the runtime may continue to be used in the same session for further turns. The `isTerminal` flag should reflect the session's terminal state, not the most recent turn's state.
