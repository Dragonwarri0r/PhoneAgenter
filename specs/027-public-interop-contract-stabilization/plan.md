# Implementation Plan: Public Hub Interop Contract Stabilization

**Branch**: `027-public-interop-contract-stabilization` | **Date**: 2026-04-24 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/027-public-interop-contract-stabilization/spec.md`

## Summary

Stabilize the public Hub Interop contract modules delivered by `024` so callers, Mobile Claw host code, and the probe app can depend on a precise public API before trusted-host and conformance work begins. The implementation focuses on method/status taxonomy, descriptor v1 fields, compatibility and unknown-field behavior, Android Bundle codec roundtrips, docs alignment, and `024/025` task checklist reconciliation.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain
**Primary Dependencies**: Android Gradle Plugin 8.9.3, Kotlin/JVM library module, Android library module, Android `Bundle` APIs, existing `:hub-interop-contract-core` and `:hub-interop-android-contract` modules
**Storage**: None; public contract stabilization is code and documentation only
**Testing**: `:hub-interop-contract-core:test`, `:hub-interop-android-contract:testDebugUnitTest`, focused JVM/Android unit tests for validators, compatibility, status mapping, and Bundle codecs
**Target Platform**: Android Hub Interop `v1` public contract consumed by Mobile Claw and external apps
**Project Type**: Android multi-module repository with shared protocol libraries
**Performance Goals**: Compatibility evaluation and descriptor validation remain constant-time for current v1 descriptor sizes; Bundle codec roundtrips remain lightweight enough for ContentResolver call setup
**Constraints**: Contract modules must remain host-agnostic, local-first, locale-aware for user-facing explanations, and independent of Mobile Claw runtime internals
**Scale/Scope**: Two public contract modules, public docs, and task checklist alignment for `024/025`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: Pass. The feature defines local Android integration semantics and does not add remote infrastructure.
- **Persona and Memory Are Separate Systems**: Pass. No persona or memory behavior is exposed or merged into the public contract.
- **Safety Gates Override Convenience**: Pass. The status taxonomy preserves authorization, approval, policy, and failure distinctions rather than collapsing them.
- **Adapter-Based Capability Integration**: Pass. The contract stays adapter-neutral while preserving the Android binding layer.
- **Privacy, Scope, and Audit by Default**: Pass. Descriptors expose scope and lifecycle semantics without leaking host internals.

**Post-Design Re-Check**: Pass. The plan keeps protocol code separated from host implementation while strengthening governance-facing outcomes.

## Project Structure

### Documentation (this feature)

```text
specs/027-public-interop-contract-stabilization/
├── plan.md
├── spec.md
└── tasks.md
```

### Source Code (repository root)

```text
hub-interop-contract-core/
└── src/
    ├── main/kotlin/com/mobileclaw/interop/contract/
    └── test/kotlin/com/mobileclaw/interop/contract/

hub-interop-android-contract/
└── src/
    ├── main/java/com/mobileclaw/interop/android/
    ├── main/java/com/mobileclaw/interop/android/bundle/
    ├── main/java/com/mobileclaw/interop/android/call/
    └── test/java/com/mobileclaw/interop/android/

docs/
└── hub-interop-*.md

specs/
├── 024-shared-interop-contract/
└── 025-mobileclaw-interop-host/
```

**Structure Decision**: Keep all protocol changes in shared contract modules and docs. Do not modify `:app` host behavior except where compile adoption requires consuming renamed or expanded public fields.

## Complexity Tracking

No constitution violations or exception tracking are anticipated for this feature.
