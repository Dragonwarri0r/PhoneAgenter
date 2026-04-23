# Implementation Plan: Capability Inference and Read Tools

**Branch**: `023-capability-inference-read-tools` | **Date**: 2026-04-23 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/023-capability-inference-read-tools/spec.md`

## Summary

Introduce a conservative capability-selection layer for freeform workspace requests so the main conversation surface no longer defaults every request to reply generation. Build explicit read-tool execution as a first-class runtime path, with `calendar.read` as the first complete capability. Reuse the existing tool contract, provider routing, approval/audit, and extension registration architecture so future first-party and external-app read capabilities can plug in without adding new core-specific logic.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain  
**Primary Dependencies**: Android Gradle Plugin 8.8.x, Jetpack Compose Material 3, Hilt, Kotlin coroutines and Flow, Room, existing runtime capability/policy/extension/session layers  
**Storage**: Reuse local Room/DataStore-backed runtime state and audit history; no remote storage  
**Testing**: Gradle compile validation plus focused runtime unit tests under `app/src/test/java/com/mobileclaw/app/runtime/`  
**Target Platform**: Android mobile app, single-user local-first device runtime  
**Project Type**: Mobile app  
**Performance Goals**: Capability selection should add negligible perceived overhead to the existing request pipeline; explicit read lookups must stay bounded enough for conversational display in a single request flow  
**Constraints**: Local-first, explicit permission awareness, conservative escalation, stable auditability, separation of explicit read execution from passive context ingestion, no new remote dependency  
**Scale/Scope**: One reusable capability-selection slice for workspace freeform input, one fully executable explicit read capability (`calendar.read`), and one extensible discovery surface for future read capabilities

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: Pass. Capability selection and read execution stay on-device and permission-aware.
- **Persona and Memory Are Separate Systems**: Pass. Explicit read-tool execution remains distinct from passive memory and system-context ingestion.
- **Safety Gates Override Convenience**: Pass. Freeform inference stays conservative, falls back to reply generation when uncertain, and preserves existing policy control for higher-risk actions.
- **Adapter-Based Capability Integration**: Pass. The design extends the common capability and extension surface instead of adding app-specific execution branches.
- **Privacy, Scope, and Audit by Default**: Pass. Read access remains bounded, explainable, and aligned to stable tool identity and audit paths.

**Post-Design Re-Check**: Pass. The generated research, contracts, and data model preserve the same gates and do not introduce a competing execution stack.

## Project Structure

### Documentation (this feature)

```text
specs/023-capability-inference-read-tools/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── calendar-read-contract.md
│   ├── read-tool-provider-contract.md
│   └── workspace-capability-selection-contract.md
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
│   │   │   │   ├── action/
│   │   │   │   ├── capability/
│   │   │   │   ├── extension/
│   │   │   │   ├── intent/
│   │   │   │   ├── policy/
│   │   │   │   ├── provider/
│   │   │   │   ├── session/
│   │   │   │   ├── strings/
│   │   │   │   └── systemsource/
│   │   │   └── ui/agentworkspace/
│   │   └── res/
│   └── test/java/com/mobileclaw/app/runtime/
│       ├── intent/
│       ├── localchat/
│       └── policy/
docs/
specs/
```

**Structure Decision**: Keep this feature inside the existing single Android app. Runtime selection logic belongs in `runtime/session`, `runtime/intent`, and `runtime/capability`; explicit read-provider execution belongs in `runtime/provider` and `runtime/systemsource`; workspace-visible behavior belongs in `ui/agentworkspace`; dependency wiring remains in `di`.

## Complexity Tracking

No constitution violations or exception tracking are anticipated for this feature.
