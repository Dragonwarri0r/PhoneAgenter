# Review: Spec6–12 Code Review + Prior Issues Follow-up

**Date**: 2026-04-09
**Covers**: spec6 (sync-extension-hooks), spec7 (external-runtime-entry), spec8 (structured-action-payloads), spec9 (permission-governance-center), spec10 (system-source-ingestion), spec11 (portability-bundles), spec12 (real-appfunctions-integration)
**Also tracks**: layout issues from [001](001-layout-and-keyboard.md), prior gaps from [002](002-spec-gaps.md) and [003](003-spec345-review.md)

---

## Prior Issues — Status Update

### Layout issues (from 001 + 003)

| Issue | Status |
|---|---|
| 01A: Panels don't re-expand on keyboard dismiss | **Fixed** — `autoCollapsedForIme` flag added; panels auto-restore when IME hides |
| 01B: First-frame padding flash | **Partially mitigated** — fallback constants (64dp header, 84dp composer) reduce the flash but `onSizeChanged` still fires after first frame |
| 01C: Manifest missing `windowSoftInputMode` | **Fixed** — `android:windowSoftInputMode="adjustResize"` now set |
| 01D: "Conversation" card title wastes space | **Fixed** — `WorkspacePlaceholderCard` only renders title when `title.isNotBlank()`; now receives empty string |

Panel group also now has `heightIn(max = maxPanelHeight)` + `verticalScroll` — prevents overflow on small screens. Layout issues are largely resolved.

### Spec gaps (from 002 + 003)

| Issue | Status |
|---|---|
| Spec4-01: RiskClassifier used bracket markers | **Fixed** — now uses `RuntimeIntentHeuristics` with natural language signal matching |
| Spec5-01: AppFunctionBridge was seeded stub | **Fixed** — `RealAppFunctionBridge` with `AppFunctionManager` availability probing |
| Approval timeout missing | **Fixed** — `PendingApprovalCoordinator.awaitOutcome()` now has 120s timeout, returns `ABANDONED` |
| GAP-S1-03: CapabilityFailed dropped | **Fixed** in updated ViewModel |
| GAP-S2-01: NoOpContextLoader | **Fixed** — `PersonaMemoryContextLoader` |
| Spec3-01: `mustRedactEvidence` always true | Still open — see below |

---

## Spec6 — Sync Extension Hooks

### Compliance: ✅ All FRs covered

Memory items carry full exposure/sync/origin/version metadata. `ExportDecisionService`, `ExportBundle`, and `ExtensionRegistration` provide the redaction-aware export and extension-ready contracts. Local-only defaults apply throughout v0. No breaking schema assumptions found.

### Issue

**Spec6-01: `mustRedactEvidence = true` always set (carries over from Spec3-01).**
`ExportDecisionService.evaluateRedactionPolicy()` sets `mustRedactEvidence = true` regardless of exposure policy. Even `SHAREABLE_FULL` items have their evidence references stripped in export bundles. This contradicts FR-005 (redaction-aware, not unconditional redaction) and FR-002/FR-004 (SHAREABLE_FULL should permit full export). The `PortabilityBundleSheet` would always show evidence as redacted even when the policy permits sharing it.

---

## Spec7 — External Runtime Entry

### Compliance: ✅ All FRs covered

`ExternalHandoffParser` validates and normalizes incoming `ACTION_SEND` intents. `ExternalRuntimeRequestMapper` converts to `RuntimeRequest` with `RuntimeSourceMetadata`. `MainActivity` handles both `onCreate` and `onNewIntent`. The `ACTIVITY_SHARE` intent-filter is declared in `AndroidManifest.xml`. External requests route through the full context/policy/capability/audit pipeline unchanged.

### Issues

**Spec7-01: Spec FR-010 partially met — no explicit "handoff received" surface.**
The spec requires the user to see that an incoming external handoff was received as a new or resumed agent session. The ViewModel observes `ExternalHandoffCoordinator.pendingEvent` and maps it to a `RuntimeRequest`, but there is no explicit UI banner, session attribution label, or workspace notification confirming the handoff source. The audit trail records it, but the user sees no direct acknowledgment that a handoff triggered the current session. The `runtimeStatus.sourceLabel` field exists but it is unclear whether it is surfaced prominently enough to satisfy FR-010.

**Spec7-02: `ExternalHandoffCoordinator` holds only one pending event.**
A second external share arriving before the first is consumed silently overwrites the first. Rapid shares from the same or different sources will lose the earlier request with no error or feedback to the user.

---

## Spec8 — Structured Action Payloads

### Compliance: ✅ All FRs covered

`StructuredActionNormalizer` handles `message.send`, `calendar.write`, and `external.share` with field extraction, `PayloadCompletenessState` (COMPLETE / PARTIAL / INSUFFICIENT), and `StructuredExecutionPreview`. The `RiskClassifier` consumes completeness state (INSUFFICIENT → HIGH, PARTIAL → MEDIUM). Provider construction remains in `AndroidIntentCapabilityProvider`. The normalization layer is decoupled from routing.

### Issues

**Spec8-01: Regex extraction is bilingual but confidence scores are uniform across languages.**
The normalizer extracts recipient and body hints with the same confidence values (0.62 for recipient, 0.78 for body) regardless of whether the input was English or Chinese. Chinese extraction patterns are structurally different and may produce more partial matches, but completeness classification won't distinguish this.

**Spec8-02: Structured payload passing to `RiskClassifier` depends on orchestrator sequencing.**
The `RiskClassifier` checks `structuredPayload?.completenessState` to adjust risk level, but the structured payload is produced by `StructuredActionNormalizer` during orchestrator execution. If the orchestrator calls the classifier before normalization completes, or if normalization is skipped for a capability type, the classifier falls back to signal-only assessment without the completeness signal. The orchestrator ordering needs to guarantee normalization precedes classification.

---

## Spec9 — Permission Governance Center

### Compliance: ✅ All FRs covered

`GovernanceRepository`, `DefaultGovernanceRepository`, `CallerGovernanceRecord`, `ScopeGrantRecord` provide persistent caller trust and per-scope grant management. `CallerVerifier` consults `GovernanceRepository` for overrides. `GovernanceCenterSheet` provides the user surface. English + Chinese supported. Local-first.

### Issues

**Spec9-01: `GovernanceActivityItem.scopeLabel` is never populated.**
`DefaultGovernanceRepository` builds activity items from audit events (`APPROVAL_REQUESTED`, `APPROVAL_RESOLVED`, `EXECUTION_DENIED`) but always leaves `scopeLabel` as empty string. `GovernanceCenterSheet` renders the scope label as part of the activity row. The column is always blank.

**Spec9-02: No cascading cleanup when a caller is no longer needed.**
`ScopeGrantRecord` rows are linked to callers by `callerId` but there is no `ON DELETE CASCADE` or manual cleanup. If a caller record is ever removed or replaced, orphaned scope grant records accumulate in the database.

**Spec9-03: `GovernanceCenterSheet` doesn't debounce rapid trust mode changes.**
Each tap on a trust mode chip immediately calls `onUpdateTrust`. If a user taps quickly between options, multiple `updateTrustMode` calls fire in rapid succession against the database. No debounce or optimistic-update guard is in place.

---

## Spec10 — System Source Ingestion

### Compliance: ✅ All FRs covered

`AndroidSystemSourceRepository` checks `READ_CONTACTS` + `READ_CALENDAR` permissions. `SystemSourceIngestionService` ingests up to 3 contacts and 3 calendar events per request, stores as `WORKING` / `SYSTEM_SOURCE` `MemoryItem` rows, with 7-day / 2-day expiry. `ContextWindowCard` receives `onRequestSystemPermissions` callback, surfacing permission state. `PersonaMemoryContextLoader` includes system-source records in context assembly.

### Issues

**Spec10-01: Stopword filter is English-only.**
`SystemSourceIngestionService` filters query tokens by removing common English stopwords ("send", "message", "share", "calendar", "event", "text", "write", "post"). Chinese equivalents (发消息, 分享, 日历, 活动) are not filtered. A Chinese-language input like "帮我发消息给小明" will produce noisy tokens including "帮我", "发消息", "给" that may not match contact names correctly.

**Spec10-02: Calendar ingestion auto-triggers on `calendar.write` without user awareness.**
`SystemSourceIngestionService.shouldCheckCalendar()` returns `true` whenever `capabilityId == "calendar.write"`. The user has no indication that their calendar is being queried as part of every calendar-write request. This is a privacy transparency gap.

**Spec10-03: No deduplication guard in ingestion.**
Each call to `ingestForRequest` may upsert the same contact or calendar record multiple times (e.g. across multiple requests in the same session). The upsert operation in `ScopedMemoryRepository` updates `updatedAtEpochMillis` and `logicalVersion` on every call, creating unnecessary churn on stable data.

---

## Spec11 — Portability Bundles

### Compliance: ✅ All FRs covered

`PortabilityBundleFormatter`, `PortabilityBundlePreview`, `PortabilityBundleShareService`, `PortabilityBundleSheet` provide the full preview + share flow. Redaction-aware export, mode switching, extension compatibility, and Android share intent are all implemented. English + Chinese supported.

### Issues

**Spec11-01: `FLAG_ACTIVITY_NEW_TASK` added twice in `PortabilityBundleShareService`.**
The share intent builder calls `.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)` in two separate places. Redundant but harmless; duplicates can confuse future readers.

**Spec11-02: `canShare` validation is caller-enforced, not enforced in the share service.**
`PortabilityBundleShareService.shareBundle()` checks `preview.canShare` before proceeding, but this flag is computed by `PortabilityBundleFormatter` from upstream policy. If a caller somehow passes a preview with `canShare = true` for a private record (e.g. through a stale preview object), the service will share it. A defense-in-depth re-check against the record's exposure policy at share time would be safer.

---

## Spec12 — Real AppFunctions Integration

### Compliance: ✅ All FRs covered

`RealAppFunctionBridge` probes `AppFunctionManager` availability (API ≥ 36, service registered). `MobileClawAppFunctions` exposes two `@AppFunction` methods: `draftReply` and `exportPortableSummary`. KSP generates registry metadata (confirmed by build output). Ordered fallback to `IntentFallbackBridge` / `ShareFallbackBridge` is preserved. English + Chinese supported.

### Issues

**Spec12-01: `MobileClawAppFunctions.draftReply` has no timeout.**
The method streams a local model response and collects it via `.toList()`. If the local model hangs or is very slow, the AppFunction call blocks indefinitely. External callers invoking via `AppFunctionManager` will wait without any cancellation signal, potentially causing ANR-like behavior in the calling app.

**Spec12-02: `AppFunctionExposureCatalog` maps only two capabilities.**
`"generate.reply"` → `"draftReply"` and `"external.share"` → `"exportPortableSummary"` are the only entries. `"message.send"`, `"calendar.write"`, `"ui.act"`, and `"sensitive.write"` have no AppFunction exposure paths, so `RealAppFunctionBridge.discoverProviders()` will find no match for these capabilities and fall through to intent or share fallbacks. This is acceptable for v0 but should be explicit in the catalog.

---

## Cross-Cutting

**Cross-01: Debug bracket markers still recognized in production code.**
`RuntimeIntentHeuristics.infer()` still recognizes `[blocked]`, `[calendar]`, `[share]`, `[message]`, `[write]`, `[ui]` with 0.99 confidence. These are test helpers that leak into the production binary. A real user who types `[message] send Alice hello` would trigger `message.send` + HARD_CONFIRM with near-certainty. The markers should either be removed from the production path or gated behind a debug build flag.

**Cross-02: Tests cover two areas, rest is untested.**
`RuntimeIntentHeuristicsTest` and `PendingApprovalCoordinatorTest` are the only test files. The policy engine, risk classifier, governance repository, memory retrieval scoring, structured action normalizer, and ingress pipeline have no test coverage.
