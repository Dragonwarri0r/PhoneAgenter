# Implementation Plan: Mobile Claw Trusted Interop Host

**Branch**: `028-mobileclaw-trusted-interop-host` | **Date**: 2026-04-24 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/028-mobileclaw-trusted-interop-host/spec.md`

## Summary

Harden Mobile Claw's `HubInteropProvider` and runtime interop services so external calls use host-attested caller identity, governed authorization lifecycle, owner-checked task/artifact access, and the first real Android read capability exposed through interop: bounded `calendar.read`. Reuse the existing runtime, governance, policy, calendar provider, audit, and control-center paths instead of creating a second execution stack.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain
**Primary Dependencies**: Android Gradle Plugin 8.9.3, Jetpack Compose Material 3, Hilt, Kotlin coroutines and Flow, Room, Android `ContentProvider`, `PackageManager`, `SigningInfo`, `CalendarContract`, shared Hub Interop modules from `027`, existing runtime/session/capability/policy/governance/provider layers
**Storage**: Existing Room/DataStore-backed governance and audit state; add interop authorization/task/artifact records locally if durable lifecycle is implemented. If `MemoryDatabase` schema changes, bump `MemoryDatabase.version` in the same patch.
**Testing**: `:app:testDebugUnitTest`, `:app:compileDebugKotlin`, focused host tests under `app/src/test/java/com/mobileclaw/app/runtime/interop/`, and existing governance/provider tests where affected
**Target Platform**: Android Mobile Claw host app implementing Hub Interop baseline transport
**Project Type**: Android app host implementation
**Performance Goals**: Caller identity resolution and ownership checks should add negligible overhead to provider calls; task/artifact lookup must remain bounded and safe for polling
**Constraints**: Local-first, authorization-by-default, no trust in claimed caller payloads, no side-effect capability exposure, bounded calendar reads only, audit and scope preservation by default
**Scale/Scope**: One trusted provider boundary, one authorization lifecycle, durable or explicitly lifecycle-safe task/artifact records, `generate.reply`, and bounded `calendar.read`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: Pass. Interop stays device-local and user-governed.
- **Persona and Memory Are Separate Systems**: Pass. Calendar read and interop records do not alter persona semantics or collapse memory into prompt state.
- **Safety Gates Override Convenience**: Pass. Authorization and policy outcomes remain explicit and block execution when needed.
- **Adapter-Based Capability Integration**: Pass. External calls adapt into the existing runtime/capability spine.
- **Privacy, Scope, and Audit by Default**: Pass. Host-attested caller identity, scope grants, ownership checks, and audit are central requirements.

**Post-Design Re-Check**: Pass. The plan preserves existing governance and runtime paths while hardening the public boundary.

## Project Structure

### Documentation (this feature)

```text
specs/028-mobileclaw-trusted-interop-host/
├── plan.md
├── spec.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
└── src/
    ├── main/java/com/mobileclaw/app/runtime/interop/
    ├── main/java/com/mobileclaw/app/runtime/governance/
    ├── main/java/com/mobileclaw/app/runtime/memory/
    ├── main/java/com/mobileclaw/app/runtime/capability/
    ├── main/java/com/mobileclaw/app/runtime/provider/
    ├── main/java/com/mobileclaw/app/runtime/session/
    ├── main/java/com/mobileclaw/app/ui/agentworkspace/
    └── test/java/com/mobileclaw/app/runtime/interop/

hub-interop-contract-core/
hub-interop-android-contract/
```

**Structure Decision**: Keep host-specific trust, persistence, runtime, and UI code inside `:app`. Continue to consume public protocol shapes from the shared Hub Interop modules.

## Complexity Tracking

No constitution violations are anticipated. If durable interop records require new Room entities, the complexity is justified by the public protocol commitment around task/artifact handles.
