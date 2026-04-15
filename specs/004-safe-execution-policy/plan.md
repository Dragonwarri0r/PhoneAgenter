# Implementation Plan: Safe Execution Policy and Approval Flow

**Branch**: `004-safe-execution-policy` | **Date**: 2026-04-08 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/004-safe-execution-policy/spec.md`

## Summary

Introduce the first real safety and approval layer for the local runtime by adding structured risk assessment, final policy resolution, approval requests, approval outcomes, and audit records.

This milestone should extend the `002` runtime session pipeline and the `003` context-aware runtime instead of bypassing them.
Risk classification remains advisory.
Policy becomes the final authority for whether a request auto-executes, pauses for confirmation, or is denied.
As a cross-cutting requirement, user-facing policy and approval surfaces should support English and Simplified Chinese through device-locale-based selection.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain  
**Primary Dependencies**: Android Gradle Plugin 8.8.x, Jetpack Compose Material 3, Hilt, Kotlin coroutines and Flow, Preferences DataStore, Room  
**Storage**: Room for risk assessments, policy decisions, approval requests, approval outcomes, and audit events; existing DataStore and Room layers from `003` remain in use  
**Testing**: JUnit4 for policy rules, risk normalization, and audit persistence; AndroidX instrumentation only where approval UI and locale-sensitive behavior need validation  
**Target Platform**: Android 12+ phones and emulators, single-user local-first runtime  
**Project Type**: Single-module mobile app (Android)  
**Performance Goals**: Policy resolution should happen inside the runtime execution loop quickly enough that low-risk actions still feel immediate, while approval requests should appear with a clear preview before execution starts  
**Constraints**: Local-first, single-user, policy overrides classifier optimism, hard-confirm and deny rules are deterministic, approval UI must remain lightweight, and user-facing strings must localize between English and Simplified Chinese based on device language  
**Scale/Scope**: One local risk pipeline, one approval sheet/dialog surface, one audit persistence layer, one hard-confirm rule set, and localized approval/execution messaging reused across current and upcoming runtime surfaces

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: PASS. Risk, policy, approval, and audit are fully local and designed around explicit user control.
- **Persona and Memory Are Separate Systems**: PASS. This milestone reads runtime context but does not collapse persona or memory into policy state.
- **Safety Gates Override Convenience**: PASS. This feature operationalizes the constitution by separating classification from final authorization.
- **Adapter-Based Capability Integration**: PASS. Approval gates sit in the common runtime session pipeline and do not create provider-specific safety paths.
- **Privacy, Scope, and Audit by Default**: PASS. Structured audit records, approval requests, and localized explanations improve traceability without weakening scope/privacy defaults.

Post-design re-check expectation: still passes, provided hard-confirm rules remain deterministic, audit records are stored locally, and approval surfaces do not bypass policy for convenience.

## Project Structure

### Documentation (this feature)

```text
specs/004-safe-execution-policy/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── safe-execution-contract.md
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
        │   │   ├── policy/
        │   │   │   ├── RiskAssessment.kt
        │   │   │   ├── RiskClassifier.kt
        │   │   │   ├── PolicyDecision.kt
        │   │   │   ├── PolicyEngine.kt
        │   │   │   ├── ApprovalRequest.kt
        │   │   │   ├── ApprovalRepository.kt
        │   │   │   ├── AuditEvent.kt
        │   │   │   └── AuditRepository.kt
        │   │   ├── session/
        │   │   │   ├── RuntimeRequest.kt
        │   │   │   ├── RuntimeSessionEvent.kt
        │   │   │   └── RuntimeSessionOrchestrator.kt
        │   │   └── strings/
        │   │       └── AppStrings.kt
        │   └── ui/
        │       └── agentworkspace/
        │           ├── AgentWorkspaceScreen.kt
        │           ├── AgentWorkspaceViewModel.kt
        │           ├── model/
        │           │   ├── ApprovalUiModel.kt
        │           │   └── AuditUiModel.kt
        │           └── components/
        │               ├── ApprovalSheet.kt
        │               ├── InlineFailureBanner.kt
        │               └── WorkspaceFeedbackHost.kt
        └── res/
            ├── values/
            │   └── strings.xml
            └── values-zh/
                └── strings.xml
```

**Structure Decision**: Keep risk, policy, approval, and audit logic in a dedicated runtime policy package and integrate it into the established runtime session orchestration flow. Reuse the workspace as the first approval surface. Treat localization as a shared platform concern through string resources plus a non-Compose string resolver so `004` and later milestones can share bilingual runtime messaging.

## Complexity Tracking

No constitution violations are expected for this feature.
