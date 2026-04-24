# Implementation Plan: Hub Interop Probe App

**Branch**: `026-interop-probe-app` | **Date**: 2026-04-23 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/026-interop-probe-app/spec.md`

## Summary

Build a separate Android app that validates the shared Hub Interop contract against Mobile Claw as an external consumer. The probe app must depend only on the shared public contract modules from `024`, never on `:app` internals, and it must exercise real discovery, authorization, invocation, task, artifact, and compatibility paths against the Mobile Claw host implementation from `025`. The probe app is a protocol verifier first, not a second Mobile Claw client product.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain
**Primary Dependencies**: Android Gradle Plugin 8.9.3, Jetpack Compose Material 3, Hilt or lightweight DI as needed, Kotlin coroutines and Flow, shared modules from `024`, Android `ContentResolver` APIs, optional AppFunctions caller helpers from the shared Android contract
**Storage**: No durable protocol storage required for the first slice; keep validation state in memory unless a local session summary becomes necessary
**Testing**: Gradle compile validation plus focused unit coverage for probe client helpers, compatibility handling, and UI state reduction
**Target Platform**: Separate Android probe app installed alongside Mobile Claw on the same device
**Project Type**: Mobile app module used as an external protocol consumer
**Performance Goals**: Discovery and validation actions should complete quickly enough for iterative manual testing; polling and compatibility diagnostics should remain bounded and understandable
**Constraints**: Must not depend on `:app`, must use only the shared public contract modules, must surface explicit rejection or incompatibility states, and must stay simple enough to remain a verification tool rather than a full product shell
**Scale/Scope**: One standalone probe app, one discovery flow, one authorization plus invocation flow, one task or artifact continuation flow, and one explicit contract-drift visibility surface

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: Pass. The probe app validates local device interop paths and does not require remote infrastructure.
- **Persona and Memory Are Separate Systems**: Pass. The probe app consumes only the public interop contract and does not depend on Mobile Claw memory or persona internals.
- **Safety Gates Override Convenience**: Pass. The probe app explicitly exercises authorization-required, rejection, and downgrade paths instead of assuming happy-path access.
- **Adapter-Based Capability Integration**: Pass. The app is intentionally built only on the shared contract and Android binding layer to prove that the protocol is externally consumable.
- **Privacy, Scope, and Audit by Default**: Pass. The probe app validates explicit caller, grant, compatibility, task, and artifact semantics rather than bypassing them.

**Post-Design Re-Check**: Pass. The probe app remains isolated from host internals while still covering the governed protocol paths that matter.

## Project Structure

### Documentation (this feature)

```text
specs/026-interop-probe-app/
├── plan.md
└── tasks.md
```

### Source Code (repository root)

```text
interop-probe-app/
├── build.gradle.kts
└── src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   ├── java/com/mobileclaw/interop/probe/
    │   │   ├── client/
    │   │   ├── model/
    │   │   └── ui/
    │   └── res/
    └── test/java/com/mobileclaw/interop/probe/

hub-interop-contract-core/
hub-interop-android-contract/
```

**Structure Decision**: Add a dedicated Android app module that depends only on the shared interop modules, with a small Compose-based validation UI and reusable client helpers for discovery, authorization, invocation, task, and compatibility flows.

## Complexity Tracking

No constitution violations or exception tracking are anticipated for this feature.
