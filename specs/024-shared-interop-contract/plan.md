# Implementation Plan: Shared Hub Interop Contract And Android Binding

**Branch**: `024-shared-interop-contract` | **Date**: 2026-04-23 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/024-shared-interop-contract/spec.md`

## Summary

Establish the Hub Interop protocol as a reusable public contract that is isolated from any single host implementation. Deliver two shared modules: a host-agnostic protocol core for public entities and compatibility semantics, plus an Android binding layer for authorities, URIs, method families, Bundle codecs, and caller helpers. Mobile Claw will consume these modules as one implementation, and later external consumers and the probe app will use the same public surface instead of copying constants or depending on `:app` internals.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain
**Primary Dependencies**: Android Gradle Plugin 8.9.3, Kotlin/JVM and Android library modules, AndroidX core `Bundle`/`Uri` APIs, existing Mobile Claw runtime modules as consumers
**Storage**: None for the shared contract modules; public contract remains in code only
**Testing**: Gradle module compilation plus focused JVM and Android unit coverage for shared entities, compatibility evaluation, and Android binding helpers
**Target Platform**: Android `v1` shared contract consumed by Mobile Claw and external Android apps
**Project Type**: Mobile app repository with shared library modules
**Performance Goals**: Public contract lookup and request assembly should add negligible overhead to discovery or invocation setup; compatibility evaluation should stay constant-time for supported version slices
**Constraints**: Contract must stay host-agnostic, locale-aware for user-visible explanations, baseline-transport-capable without AppFunctions, and narrow enough to avoid leaking host-only runtime or governance internals
**Scale/Scope**: Two reusable modules, one host compile-time adoption path in `:app`, and the minimum Android-facing contract needed for discovery, authorization, invocation, task, and artifact semantics

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: Pass. The contract defines on-device integration semantics and does not require remote infrastructure.
- **Persona and Memory Are Separate Systems**: Pass. The shared public contract exposes neutral resource and artifact semantics without coupling to persona or memory internals.
- **Safety Gates Override Convenience**: Pass. Authorization, compatibility, and governed-task semantics remain first-class protocol concepts rather than optional host extras.
- **Adapter-Based Capability Integration**: Pass. The contract is explicitly host-agnostic and designed for multiple Android adapters and future platforms.
- **Privacy, Scope, and Audit by Default**: Pass. Public contract entities preserve explicit scope, grant, task, and compatibility semantics instead of allowing opaque unrestricted calls.

**Post-Design Re-Check**: Pass. The planned module split and Android binding helpers preserve the same governance and host-isolation guarantees.

## Project Structure

### Documentation (this feature)

```text
specs/024-shared-interop-contract/
├── plan.md
└── tasks.md
```

### Source Code (repository root)

```text
hub-interop-contract-core/
├── build.gradle.kts
└── src/
    ├── main/kotlin/com/mobileclaw/interop/contract/
    └── test/kotlin/com/mobileclaw/interop/contract/

hub-interop-android-contract/
├── build.gradle.kts
└── src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   ├── java/com/mobileclaw/interop/android/
    │   └── res/
    └── test/java/com/mobileclaw/interop/android/

app/
├── build.gradle.kts
└── src/main/java/com/mobileclaw/app/runtime/
    ├── appfunctions/
    └── ingress/
```

**Structure Decision**: Add one pure/shared protocol module and one Android binding module, then make `:app` consume them as a client of the public contract instead of remaining the source of truth for public interop identifiers.

## Complexity Tracking

No constitution violations or exception tracking are anticipated for this feature.
