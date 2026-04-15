# Review: Spec3 / Spec4 / Spec5 Code Review + Layout Follow-up

**Date**: 2026-04-09
**Covers**: spec3 (persona-memory-fabric), spec4 (safe-execution-policy), spec5 (android-capability-bridge)
**Also tracks**: layout issue from [001](001-layout-and-keyboard.md), spec gaps from [002](002-spec-gaps.md)

---

## Layout Issue 001 — Follow-up

### Fixed ✅

The layout was substantially reworked:
- Root switched from `Column` to `Box` — conversation fills full size.
- Top panel group (ModelHealthCard, ContextWindowCard, QuickActionStrip) is now overlaid and collapsible via `AnimatedVisibility`.
- `ComposerDock` pinned to `Alignment.BottomCenter`.
- `WorkspaceFeedbackHost` overlaid via `Box` alignment and offset above the composer.
- `LaunchedEffect(imeVisible)` auto-collapses panels when keyboard opens.
- `CollapsedPanelSummary` stub shown when panels are collapsed.

### Still Open

**Layout-01A: Panels do not re-expand when keyboard dismisses.**
`LaunchedEffect(imeVisible)` sets `panelsExpanded = false` when keyboard opens, but there is no corresponding re-expand when `imeVisible` becomes `false`. After the user closes the keyboard, panels remain collapsed and the user must manually tap the expand button.

```kotlin
LaunchedEffect(imeVisible) {
    if (imeVisible && panelsExpanded) {
        panelsExpanded = false
    }
    // Missing: if (!imeVisible && ...) panelsExpanded = true
}
```

**Layout-01B: First-frame padding flash.**
`topContentPadding` and `bottomContentPadding` are computed from `onSizeChanged` callbacks, which fire after the first composition. On frame zero, both values are 0dp, so the conversation card initially spans the full box before snapping to its correct position. Visible as a brief flash on cold start or rotation.

**Layout-01C: AndroidManifest still missing `windowSoftInputMode`.**
The activity has no `windowSoftInputMode` declared. With `enableEdgeToEdge()`, the system relies on inset APIs. Without `adjustResize`, some OEM devices may not deliver correct IME insets, making the `imeVisible` detection unreliable.

**Layout-01D: `WorkspacePlaceholderCard` title wastes vertical space.**
The card always renders a "Conversation" text label (from `R.string.workspace_conversation`) before its body content, consuming ~40–48dp. When panels are collapsed to give the conversation maximum room, this title label is redundant — the card shape already implies the conversation region.

---

## Spec3 — Persona and Scoped Memory Fabric

### Compliance: ✅ All FRs covered

| FR | Status | Notes |
|---|---|---|
| FR-001 | ✅ | `PersonaProfile` separate entity, separate repo |
| FR-002 | ✅ | `MemoryLifecycle`: DURABLE / WORKING / EPHEMERAL |
| FR-003 | ✅ | `MemoryScope`: GLOBAL / APP_SCOPED / CONTACT_SCOPED / DEVICE_SCOPED |
| FR-004 | ✅ | `MemoryExposurePolicy`: PRIVATE / SHAREABLE_SUMMARY / SHAREABLE_FULL |
| FR-005 | ✅ | `MemorySyncPolicy`: LOCAL_ONLY / SUMMARY_SYNC_READY / FULL_SYNC_READY |
| FR-006 | ✅ | provenance, confidence, timestamps, sourceType in `MemoryItem` |
| FR-007 | ✅ | `MemoryWritebackService` defaults to APP_SCOPED + PRIVATE |
| FR-008 | ✅ | `RetrievalQuery` filters by lifecycle, scope, exposure, relevance |
| FR-009 | ✅ | `ContextInspectorSheet` allows pin, promote, demote edit |
| FR-010 | ✅ | promote/demote/expire preserve provenance (no delete) |
| FR-011 | ✅ | `ActiveContextSummary` hides private evidence, shows safe summary |

### Issues

**Spec3-01: `mustRedactEvidence` is always `true` in `ExportDecisionService`.**
`evaluateRedactionPolicy()` always sets `mustRedactEvidence = true` regardless of exposure policy. This means even `SHAREABLE_FULL` items will have their evidence references stripped in exports, contradicting FR-004 and FR-011. The policy check for `SHAREABLE_FULL` correctly sets `canExportFull = true` but the redaction flag overrides this in practice.

**Spec3-02: Extension compatibility checked but never used during execution.**
`ExportDecisionService.extensionCompatibilities()` verifies schema version and required fields against registered extensions, but the orchestrator and retrieval pipeline never call this check before serving context to providers. Schema incompatibility would silently pass through.

**Spec3-03: `MemoryRetrievalService` score is hardcoded without tie-breaking.**
When two items share an identical score (e.g. two pinned DURABLE GLOBAL items with similar freshness), the ordering is undefined (depends on DB insertion order). For a small fixture set this is not visible, but with real memory this can cause non-deterministic context selection.

---

## Spec4 — Safe Execution Policy and Approval Flow

### Compliance: ✅ All FRs covered

| FR | Status | Notes |
|---|---|---|
| FR-001 | ✅ | `RiskClassifier` → LOW/MEDIUM/HIGH/BLOCKED |
| FR-002 | ✅ | `PolicyEngine` separate from `RiskClassifier` |
| FR-003 | ✅ | AUTO_EXECUTE / PREVIEW_FIRST / REQUIRE_CONFIRMATION / DENY |
| FR-004 | ✅ | `ActionScope` hard-confirm list: MESSAGE_SEND, CALENDAR_WRITE, EXTERNAL_SHARE, UI_ACT, SENSITIVE_WRITE |
| FR-005 | ✅ | BLOCKED_RULE denies regardless of classifier |
| FR-006 | ✅ | `ApprovalRequest` with previewPayload + summary |
| FR-007 | ✅ | APPROVED / REJECTED / ABANDONED outcomes |
| FR-008 | ✅ | `AuditRepository` with 7 structured event types |
| FR-009 | ✅ | `AuditFormatter` with human-readable rationale |
| FR-010 | ✅ | `ApprovalSheet` with clear approve/reject/cancel hierarchy |
| FR-011 | ✅ | `WorkspaceFeedbackHost` for lightweight outcome toasts |
| FR-012 | ✅ | `ContextWindowCard` shows audit events — no log-only path |
| FR-013 | ✅ | `values/strings.xml` + `values-zh/strings.xml` both exist |
| FR-014 | ✅ | `AppStrings.localeTag()` reads device locale |

### Issues

**Spec4-01: `RiskClassifier` and `ActionScope.infer()` rely on bracket markers that real users will never type.**

`ActionScope.infer()` classifies user input by checking for literal strings like `[blocked]`, `[message]`, `[calendar]`, `[share]`, `[ui]`, `[write]`. `RiskClassifier.classify()` similarly checks for `[blocked]`, `[high]`, `[medium]`, `[approval]`.

In production, no user will type these markers. Every real user message will:
- Be classified as REPLY_GENERATE scope (→ AUTO_EXECUTE)
- Receive LOW risk classification

This means the entire policy and approval pipeline is effectively inactive for real user input. The REQUIRE_CONFIRMATION path, HARD_CONFIRM rules, and denial logic will never trigger unless the planner (`DefaultRuntimePlanner`) can infer a non-generate capability from the text — which also uses the same bracket markers.

**Spec4-02: `PendingApprovalCoordinator` has no timeout.**
`awaitOutcome()` awaits a `CompletableDeferred` indefinitely. If the app is backgrounded while an approval is pending and the OS kills the process later, the coordinator state is lost with no recovery path. If the session's coroutine is cancelled (e.g. `streamJob.cancel()` on a new send), the `finally` block in `awaitOutcome` correctly removes the entry — but the session ends with no outcome recorded in audit. The approval request in the DB will remain PENDING with no corresponding outcome row.

**Spec4-03: AWAITING_APPROVAL screen state not reflected in ContextWindowCard stage label.**
The `RuntimeStatusSummary` maps `AWAITING_APPROVAL` stage type to the label "Awaiting approval" in `runtimeSummaryForStage`, but `RuntimeStageType.AWAITING_APPROVAL` is defined and used in the orchestrator. When the session enters approval wait, `StatusSummaryUpdated` should emit with `isBusy=true` and `awaitingInput=true`. The `awaitingInput` field in `RuntimeStatusSummary` is only set when `"approval"` appears as a substring in the details string — a string-match heuristic that is fragile and will break if the details text changes.

---

## Spec5 — Android Capability Bridge

### Compliance: ✅ All FRs covered structurally

| FR | Status | Notes |
|---|---|---|
| FR-001 | ✅ | `CapabilityRegistry` centralizes all registrations |
| FR-002 | ✅ | `AppFunctionBridge` is primary type (seeded stub) |
| FR-003 | ✅ | Ordered fallback: AppFunctions → Intent → Share (Accessibility reserved) |
| FR-004 | ✅ | `ProviderDescriptor` with capability, scopes, risk, confirmation policy |
| FR-005 | ✅ | `CallerVerifier` with package + SHA-256 signing check |
| FR-006 | ✅ | `CapabilityAvailabilityState` changes don't affect contract shape |
| FR-007 | ✅ | `InvocationResult` normalizes success/failure from any provider |
| FR-008 | ✅ | `CapabilityRouter.route()` returns `failureReason` when no provider found |

### Issues

**Spec5-01: `SeededAppFunctionBridge` uses `[mock]` marker — same problem as Spec4-01.**
`SeededAppFunctionBridge.discoverProviders()` checks if user input contains `[mock]` to return available mock providers. Without this marker, it returns UNAVAILABLE. No real AppFunctions SDK is integrated. For real usage, the bridge always falls through to `IntentFallbackBridge`.

**Spec5-02: `CallerVerifier` only trusts own package and a hardcoded alias list.**
`CallerVerifier` marks a caller as trusted if `originApp` is in `trustedAliases` (currently only `"self"`) or equals `BuildConfig.APPLICATION_ID`. Cross-app callers that should be trusted (e.g. a companion sync app) have no provisioning path — there is no runtime trust delegation or certificate pinning for external callers.

**Spec5-03: `CapabilityProviderRegistry.getProvider(plan)` can still return null silently.**
`getProvider(plan)` returns the first provider that `supports(plan)`, or null. The caller in `RuntimeSessionOrchestrator` must handle the null case. If it does not, a runtime NPE occurs. The orchestrator wraps the provider call in error handling (per the agent summary), but this path deserves an explicit test.

---

## Old Issues from 002 — Status Update

| Issue | Status |
|---|---|
| GAP-S1-01: `SessionCancelled` never emitted | Likely still open — needs verification |
| GAP-S1-02: Streamed content overwritten at completion | Likely still open |
| GAP-S1-03: `CapabilityFailed` dropped | Now handled — ViewModel processes new event set |
| GAP-S2-01: `NoOpRuntimeContextLoader` is no-op | **Fixed** — replaced by `PersonaMemoryContextLoader` |
| GAP-S2-02: Gate always approves | Partially improved — `PolicyEngine` now has full logic; gate DENY path exists |
| GAP-S2-03: Sessions accumulate in registry | Still open — no eviction or TTL |
| GAP-S2-04: `getProvider(plan)` can crash | Now returns null with explanation via `CapabilityRouter` |
| OBS-01: Dual state sync (stageLabel + runtimeStatus) | **Fixed** — `AgentWorkspaceUiState` no longer has separate `stageLabel`/`contextSummary` fields |
| OBS-02: `isTerminal` set unconditionally | Open — `RuntimeStatusUiModel.isTerminal` still set to `true` after each completion |
