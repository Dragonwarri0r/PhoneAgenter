# Implementation Plan: Android Capability Bridge

**Branch**: `005-android-capability-bridge` | **Date**: 2026-04-08 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/005-android-capability-bridge/spec.md`

## Summary

Add the first Android-facing capability bridge on top of the existing runtime pipeline.
This milestone introduces a normalized capability registry, AppFunctions-first provider descriptors, ordered fallback routing, caller verification, and normalized invocation results.

Because the current project compiles against Android SDK 35, the implementation should not hard-depend on Android 16 AppFunctions framework types at compile time.
Instead, `005` should establish an AppFunctions-first adapter boundary and deliver a working bridge flow using in-app descriptors plus approved fallback providers.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain  
**Primary Dependencies**: Android Gradle Plugin 8.8.x, Jetpack Compose Material 3, Hilt, Kotlin coroutines and Flow, Room, existing runtime/provider/policy layers  
**Storage**: In-memory registry plus Room-backed audit/policy state already added in `004`; no durable capability sync layer in this milestone  
**Testing**: Build, lint, and manual quickstart validation using seeded AppFunctions-style registrations and fallback routing  
**Target Platform**: Android 12+ phones and emulators, single-user local-first runtime  
**Project Type**: Single-module mobile app (Android)  
**Performance Goals**: Capability routing should resolve quickly enough to preserve the current runtime UX, while provider failures should surface without hanging the orchestration loop  
**Constraints**: AppFunctions is preferred but cannot be assumed available at compile time on SDK 35; Accessibility remains reserved only; caller trust must be enforced before restricted capability execution; bridge contracts must stay stable even when the provider path changes  
**Scale/Scope**: One normalized capability registry, one AppFunctions-first bridge abstraction, one ordered fallback router, one caller verification layer, and one workspace-visible route explanation path

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: PASS. Routing and caller verification remain local and operate entirely on-device.
- **Persona and Memory Are Separate Systems**: PASS. `005` consumes runtime context but does not reshape persona or memory modeling.
- **Safety Gates Override Convenience**: PASS. Bridge routing happens under the existing `004` policy pipeline rather than bypassing it.
- **Adapter-Based Capability Integration**: PASS. This is the primary purpose of the feature and must remain adapter-first.
- **Privacy, Scope, and Audit by Default**: PASS. Provider routing will emit normalized failures and continue to respect policy/audit boundaries.

Post-design expectation: still passes, provided AppFunctions remains an adapter boundary, caller trust is enforced before execution, and fallback routing stays explicit and auditable.

## Project Structure

### Documentation (this feature)

```text
specs/005-android-capability-bridge/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── capability-bridge-contract.md
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
        │   │   ├── capability/
        │   │   │   ├── CapabilityRegistration.kt
        │   │   │   ├── ProviderDescriptor.kt
        │   │   │   ├── CallerIdentity.kt
        │   │   │   ├── InvocationResult.kt
        │   │   │   ├── CapabilityRegistry.kt
        │   │   │   ├── CapabilityRouter.kt
        │   │   │   ├── CallerVerifier.kt
        │   │   │   ├── AppFunctionBridge.kt
        │   │   │   ├── IntentFallbackBridge.kt
        │   │   │   └── ShareFallbackBridge.kt
        │   │   ├── provider/
        │   │   │   └── CapabilityProviderRegistry.kt
        │   │   ├── session/
        │   │   │   ├── RuntimeRequest.kt
        │   │   │   └── RuntimeSessionOrchestrator.kt
        │   │   └── strings/
        │   │       └── AppStrings.kt
        │   └── ui/
        │       └── agentworkspace/
        │           ├── AgentWorkspaceViewModel.kt
        │           ├── model/
        │           │   └── RuntimeStatusUiModel.kt
        │           └── components/
        │               └── ContextWindowCard.kt
        └── res/
            ├── values/
            │   └── strings.xml
            └── values-zh/
                └── strings.xml
```

**Structure Decision**: Introduce a dedicated `runtime/capability` package for Android bridge abstractions and keep the existing provider layer as the execution backend that receives already-routed plans. AppFunctions support should be modeled as a first-class provider type through descriptors and adapter interfaces without introducing a compile-time dependency on Android 16-only APIs.

## Complexity Tracking

No constitution violations are expected for this feature.
