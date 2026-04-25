# Implementation Plan: Interop Probe Conformance Suite

**Branch**: `029-interop-probe-conformance-suite` | **Date**: 2026-04-24 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/029-interop-probe-conformance-suite/spec.md`

## Summary

Upgrade `:interop-probe-app` from a manual validation client into a repeatable conformance suite. Keep the existing manual diagnostics, add a conformance runner and result model, cover compatibility/authorization/spoof/invocation/task/artifact/revoke/malformed/version cases, and export shareable reports while preserving strict dependency isolation from `:app`.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain
**Primary Dependencies**: Android Gradle Plugin 8.9.3, Jetpack Compose Material 3, Kotlin coroutines and Flow, Android `ContentResolver`, shared Hub Interop modules from `027`, Mobile Claw host behavior from `028` only through public IPC
**Storage**: In-memory conformance run state for the first suite; optional report share/export text generated on demand
**Testing**: `:interop-probe-app:testDebugUnitTest`, focused unit tests for conformance runner, report formatting, compatibility handling, client fakes, and UI state reduction
**Target Platform**: Separate Android probe app installed alongside a Hub Interop host
**Project Type**: Android app module used as an external conformance client
**Performance Goals**: Manual actions remain responsive; conformance matrix remains bounded and suitable for iterative local device testing
**Constraints**: Must not depend on `:app`, must use only public contract modules for protocol behavior, must handle negative statuses as first-class outcomes, must remain a validation tool rather than a product shell
**Scale/Scope**: One manual mode, one conformance runner, one report model, `generate.reply` and bounded `calendar.read` validation, status/version/spoof/task/artifact diagnostics

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: Pass. The probe validates local Android interop paths and does not require remote infrastructure.
- **Persona and Memory Are Separate Systems**: Pass. The probe does not access Mobile Claw persona or memory internals.
- **Safety Gates Override Convenience**: Pass. Negative authorization, denial, downgrade, and malformed outcomes are first-class conformance results.
- **Adapter-Based Capability Integration**: Pass. The probe consumes only public contract modules and Android IPC helpers.
- **Privacy, Scope, and Audit by Default**: Pass. The probe validates caller, grant, task, artifact, and compatibility semantics without bypassing host governance.

**Post-Design Re-Check**: Pass. The design keeps conformance isolated from host implementation while covering trust and lifecycle behavior.

## Project Structure

### Documentation (this feature)

```text
specs/029-interop-probe-conformance-suite/
├── plan.md
├── spec.md
└── tasks.md
```

### Source Code (repository root)

```text
interop-probe-app/
└── src/
    ├── main/java/com/mobileclaw/interop/probe/
    │   ├── client/
    │   ├── model/
    │   └── ui/
    └── test/java/com/mobileclaw/interop/probe/

hub-interop-contract-core/
hub-interop-android-contract/
```

**Structure Decision**: Add conformance models, runner, report formatting, and UI state inside `:interop-probe-app`. Continue to rely on the existing probe client abstraction and fake client tests.

## Complexity Tracking

No constitution violations are anticipated. The added conformance runner is justified by the public protocol's need for repeatable external validation.
