# Implementation Plan: Structured Action Payloads

**Branch**: `008-structured-action-payloads` | **Date**: 2026-04-09 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/008-structured-action-payloads/spec.md`

## Summary

Add a structured action normalization layer between runtime planning and provider execution for the first set of high-value tool actions: `message.send`, `calendar.write`, and `external.share`.
This milestone will keep the existing canonical `RuntimeRequest` intact, but introduce a normalized payload contract that extracts execution-safe fields, marks payload completeness explicitly, and makes those fields visible in preview, approval, runtime status, and audit surfaces.

The result should move these actions away from raw prompt passthrough and toward a more reliable, explainable execution contract without breaking current text-only generation flows.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain  
**Primary Dependencies**: Android Gradle Plugin 8.8.x, Jetpack Compose Material 3, Hilt, Kotlin coroutines and Flow, existing runtime/session, runtime/provider, runtime/policy, runtime/capability, and runtime/intent layers  
**Storage**: Existing Room-backed policy/audit persistence plus in-memory structured payload normalization and preview state during runtime execution  
**Testing**: Build, lint, and manual quickstart validation using workspace-triggered requests and external handoff requests that map to message, calendar, and share actions  
**Target Platform**: Android 12+ phones and emulators, single-user local-first runtime  
**Project Type**: Single-module mobile app (Android)  
**Performance Goals**: Structured normalization should remain lightweight enough to happen inline before provider execution without noticeably slowing normal runtime submission  
**Constraints**: The first slice must stay bounded to three action types; raw user text remains visible for explainability but should no longer be the primary execution contract for supported structured actions; incomplete payloads must fail or pause safely instead of silently executing; provider-specific Android `Intent` details must stay out of the normalization layer  
**Scale/Scope**: One normalization layer, three structured payload types, one completeness/evidence model, one preview/explainability slice, and provider updates for the supported action set

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: PASS. Normalization and preview remain fully local and reinforce user visibility before high-impact actions.
- **Persona and Memory Are Separate Systems**: PASS. This feature changes action execution contracts, not persona/memory ownership.
- **Safety Gates Override Convenience**: PASS. Explicit payload completeness improves the ability to preview, confirm, or deny unsafe actions rather than auto-executing brittle raw text.
- **Adapter-Based Capability Integration**: PASS. Structured payloads become a common runtime contract above provider adapters rather than provider-specific logic.
- **Privacy, Scope, and Audit by Default**: PASS. Structured fields and completeness state increase explainability and can be audited without requiring raw data leakage.

Post-design expectation: still passes, provided structured payload extraction remains inside the runtime contract layer, incomplete payloads never silently bypass safety gates, and provider adapters consume normalized fields instead of re-parsing raw text independently.

## Project Structure

### Documentation (this feature)

```text
specs/008-structured-action-payloads/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── structured-action-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
└── src/
    └── main/
        ├── java/com/mobileclaw/app/
        │   ├── di/
        │   │   └── AppModule.kt
        │   ├── runtime/
        │   │   ├── action/
        │   │   │   ├── StructuredActionType.kt
        │   │   │   ├── PayloadCompletenessState.kt
        │   │   │   ├── PayloadFieldEvidence.kt
        │   │   │   ├── StructuredActionPayload.kt
        │   │   │   ├── ActionNormalizationResult.kt
        │   │   │   ├── StructuredActionNormalizer.kt
        │   │   │   └── StructuredExecutionPreview.kt
        │   │   ├── intent/
        │   │   │   └── RuntimeIntentHeuristics.kt
        │   │   ├── policy/
        │   │   │   ├── RiskClassifier.kt
        │   │   │   ├── PolicyEngine.kt
        │   │   │   └── AuditRepository.kt
        │   │   ├── provider/
        │   │   │   ├── CapabilityProvider.kt
        │   │   │   ├── AndroidIntentCapabilityProvider.kt
        │   │   │   └── CapabilityProviderRegistry.kt
        │   │   ├── session/
        │   │   │   ├── RuntimeRequest.kt
        │   │   │   ├── RuntimePlan.kt
        │   │   │   ├── RuntimeSessionEvent.kt
        │   │   │   └── RuntimeSessionOrchestrator.kt
        │   │   └── strings/
        │   │       └── AppStrings.kt
        │   └── ui/
        │       └── agentworkspace/
        │           ├── AgentWorkspaceViewModel.kt
        │           ├── model/
        │           │   ├── ApprovalUiModel.kt
        │           │   └── RuntimeStatusUiModel.kt
        │           └── components/
        │               ├── ApprovalSheet.kt
        │               ├── ContextWindowCard.kt
        │               └── InlineFailureBanner.kt
        └── res/
            ├── values/
            │   └── strings.xml
            └── values-zh/
                └── strings.xml
```

**Structure Decision**: Add a dedicated `runtime/action` package to hold structured payload types, completeness/evidence metadata, and the normalization service. The session/orchestration layer should ask this service to produce a normalized payload before policy and provider execution continue. UI and policy layers should consume the resulting preview/completeness state, while Android intent/share providers should consume typed payload fields instead of reparsing natural language.

## Complexity Tracking

No constitution violations are expected for this feature.
