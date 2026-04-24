# Implementation Plan: Mobile Claw Hub Interop Host Implementation

**Branch**: `025-mobileclaw-interop-host` | **Date**: 2026-04-23 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/025-mobileclaw-interop-host/spec.md`

## Summary

Implement Mobile Claw as one governed host/provider of the shared Hub Interop contract delivered by `024`. Add a shared-contract-aware Android host boundary around the existing runtime so external apps can discover Mobile Claw, request authorization, invoke governed capabilities, and follow task or artifact handles through public protocol semantics. Reuse existing runtime/session, policy, governance, approval, and AppFunctions infrastructure instead of creating a second execution stack, while keeping `ACTION_SEND` as a compatibility ingress rather than the main interop contract.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain
**Primary Dependencies**: Android Gradle Plugin 8.9.3, Jetpack Compose Material 3, Hilt, Kotlin coroutines and Flow, Room, AndroidX AppFunctions, shared modules from `024`, existing runtime/session/capability/policy/governance layers
**Storage**: Reuse existing Room/DataStore governance, approval, audit, and runtime state; add runtime-local interop task or artifact records only where needed locally
**Testing**: Gradle compile validation plus focused unit coverage under `app/src/test/java/com/mobileclaw/app/runtime/interop/` and existing policy tests
**Target Platform**: Android Mobile Claw host app implementing the shared public interop contract
**Project Type**: Mobile app host implementation
**Performance Goals**: Discovery and governed invocation setup should remain lightweight enough for external callers; task polling and artifact lookups should stay bounded and explainable
**Constraints**: Local-first, authorization-by-default, explicit compatibility signaling, no bypass around approval/audit/governance, share ingress retained only as compatibility path
**Scale/Scope**: One meaningful discovery surface, one governed capability invocation path, one authorization flow, one connected-app management slice, and one task or artifact continuation slice

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: Pass. Interop stays device-local and governed by in-app authorization and control-center surfaces.
- **Persona and Memory Are Separate Systems**: Pass. The host surface reuses runtime capabilities without exposing persona or memory internals as raw public implementation details.
- **Safety Gates Override Convenience**: Pass. External interop always routes through caller verification, grant checks, approvals, and explicit compatibility outcomes.
- **Adapter-Based Capability Integration**: Pass. The host implementation consumes the shared contract from `024` and adapts existing AppFunctions and provider paths instead of inventing a host-only transport vocabulary.
- **Privacy, Scope, and Audit by Default**: Pass. Caller identity, scope intent, authorization state, and audit outcomes remain attached from interop ingress through runtime execution and control-center visibility.

**Post-Design Re-Check**: Pass. The host plan preserves a single governed runtime path while adding an explicit public boundary for external callers.

## Project Structure

### Documentation (this feature)

```text
specs/025-mobileclaw-interop-host/
├── plan.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
├── src/
│   ├── main/
│   │   ├── java/com/mobileclaw/app/
│   │   │   ├── di/
│   │   │   ├── runtime/
│   │   │   │   ├── appfunctions/
│   │   │   │   ├── capability/
│   │   │   │   ├── governance/
│   │   │   │   ├── ingress/
│   │   │   │   ├── interop/
│   │   │   │   ├── policy/
│   │   │   │   └── session/
│   │   │   └── ui/agentworkspace/
│   │   └── res/
│   └── test/java/com/mobileclaw/app/runtime/
│       ├── interop/
│       └── policy/

hub-interop-contract-core/
hub-interop-android-contract/
```

**Structure Decision**: Keep Mobile Claw as a consumer and implementer of the shared contract by adding a dedicated `runtime/interop` host boundary inside `:app`, while reusing existing runtime/governance/UI layers and the shared modules from `024`.

## Complexity Tracking

No constitution violations or exception tracking are anticipated for this feature.
