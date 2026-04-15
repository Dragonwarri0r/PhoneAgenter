# Implementation Plan: Local Runtime Session Pipeline

**Branch**: `002-runtime-session-pipeline` | **Date**: 2026-04-08 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-runtime-session-pipeline/spec.md`

## Summary

Build a unified local runtime session pipeline inside the existing Android app so every accepted request runs through the same execution-session contract, emits ordered stage updates, and completes through a stable outcome model whether the request is pure generation or uses a provider.

The implementation will introduce a new runtime session layer under `app`, keep provider execution behind pluggable adapters, and migrate the `001` workspace from direct `LocalChatGateway` control to a `RuntimeSessionFacade` that exposes ordered session updates and compact status summaries.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain  
**Primary Dependencies**: Android Gradle Plugin 8.8.x, Jetpack Compose Material 3, Lifecycle ViewModel, Hilt, Kotlin coroutines and Flow  
**Storage**: In-memory session registry for `v0`, existing DataStore remains limited to lightweight workspace preferences  
**Testing**: JUnit4 for runtime state transitions and provider substitution, AndroidX instrumentation only where integration verification is needed  
**Target Platform**: Android 12+ phones and emulators, local-first single-device runtime  
**Project Type**: Single-module mobile app (Android)  
**Performance Goals**: Session acceptance and first visible stage update should occur within one user interaction loop; UI should receive compact stage/status updates quickly enough to keep the workspace understandable  
**Constraints**: Local-first, single-user, no cloud orchestration, must preserve stable top-level session contract, must reserve future hooks for memory/persona/policy/capability routing without implementing them fully in this milestone  
**Scale/Scope**: One active user workspace path, multiple concurrent runtime sessions supported in-process, one unified session contract used for both generation-only and provider-backed execution

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: PASS. The session pipeline is entirely local and does not depend on remote control planes.
- **Persona and Memory Are Separate Systems**: PASS. This milestone adds explicit placeholder hooks for future retrieval without collapsing them into current request prompts.
- **Safety Gates Override Convenience**: PASS. Approval and denial are modeled as lifecycle stages and outcomes, even though richer policy logic is deferred to `004`.
- **Adapter-Based Capability Integration**: PASS. Provider execution is normalized behind a stable runtime contract and does not make Android bridges the core protocol.
- **Privacy, Scope, and Audit by Default**: PASS. Session state remains structured and observable, while future audit and policy metadata are reserved as first-class fields.

Post-design re-check expectation: still passes, provided provider adapters remain replaceable and session summaries stay user-safe rather than log-shaped.

## Project Structure

### Documentation (this feature)

```text
specs/002-runtime-session-pipeline/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── runtime-session-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
└── src/
    └── main/
        └── java/com/mobileclaw/app/
            ├── di/
            │   └── AppModule.kt
            ├── runtime/
            │   ├── localchat/
            │   │   └── LocalChatGateway.kt
            │   ├── provider/
            │   │   ├── CapabilityProvider.kt
            │   │   ├── CapabilityProviderRegistry.kt
            │   │   └── LocalGenerationProvider.kt
            │   └── session/
            │       ├── ExecutionSession.kt
            │       ├── RuntimeRequest.kt
            │       ├── RuntimeSessionEvent.kt
            │       ├── RuntimeSessionFacade.kt
            │       ├── RuntimeSessionOrchestrator.kt
            │       ├── RuntimeSessionRegistry.kt
            │       └── RuntimeStatusSummary.kt
            └── ui/
                └── agentworkspace/
                    ├── AgentWorkspaceViewModel.kt
                    └── model/
                        └── RuntimeStatusUiModel.kt
```

**Structure Decision**: Keep `002` inside the existing Android app and add a dedicated `runtime/session` and `runtime/provider` layer. This lets the milestone replace the direct UI-to-gateway path introduced in `001` without forcing a separate module split before the session contract stabilizes.

## Complexity Tracking

No constitution violations are expected for this feature.
