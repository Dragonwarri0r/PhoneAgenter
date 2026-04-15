# Implementation Plan: Trusted External Handoff Entry

**Branch**: `007-external-runtime-entry` | **Date**: 2026-04-09 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/007-external-runtime-entry/spec.md`

## Summary

Add the first real Android external handoff path so another app can send text content into Mobile Claw through the Android Sharesheet or compatible intent resolution flow.
This milestone will accept `ACTION_SEND` `text/plain` intents in `MainActivity`, normalize them into a dedicated inbound handoff contract at the Android ingress boundary, enrich them with best-effort caller/source metadata, and then convert them into the same `RuntimeRequest` shape already used by the internal workspace.

The feature must preserve the existing workspace flow, keep Android entry details out of downstream runtime layers, and let the user see source and trust outcome information when the external handoff becomes a new or resumed runtime session.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain  
**Primary Dependencies**: Android Gradle Plugin 8.8.x, Jetpack Compose Material 3, Hilt, Kotlin coroutines and Flow, existing runtime/session, runtime/policy, runtime/capability, and runtime/memory layers  
**Storage**: Existing Room-backed memory/policy/audit persistence plus lightweight in-memory inbound handoff coordination at the app boundary  
**Testing**: Build, lint, and manual quickstart validation using Android Sharesheet or `adb am start` intent flows  
**Target Platform**: Android 12+ phones and emulators, single-user local-first runtime  
**Project Type**: Single-module mobile app (Android)  
**Performance Goals**: Supported text handoffs should land in the workspace and begin runtime submission without noticeable delay; normalization should stay lightweight and avoid duplicate submissions on configuration changes  
**Constraints**: First milestone is text-first only; Android entry parsing must stay out of downstream runtime planning/execution; caller identity may be best-effort on current Android entry paths and must degrade safely; existing internal workspace submission must remain unchanged  
**Scale/Scope**: One inbound Android handoff path, one normalized ingress contract, one workspace landing flow, and one source/trust explainability slice

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: PASS. External handoff still lands in the local runtime and keeps policy/approval inside the device.
- **Persona and Memory Are Separate Systems**: PASS. This feature changes request ingress only; persona and memory remain separate and are reused through the existing context pipeline.
- **Safety Gates Override Convenience**: PASS. External requests still flow through risk classification, policy, approval, and audit rather than bypassing them.
- **Adapter-Based Capability Integration**: PASS. Android share/intent handling is introduced as an ingress adapter boundary, not as a new runtime core protocol.
- **Privacy, Scope, and Audit by Default**: PASS. Source metadata is explicit, trust outcome is auditable, and unsupported or ambiguous requests fail safely.

Post-design expectation: still passes, provided external handoff parsing stays at the Android boundary, normalized requests use the existing runtime contract, and missing caller/source information never silently upgrades restricted execution.

## Project Structure

### Documentation (this feature)

```text
specs/007-external-runtime-entry/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── external-runtime-entry-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
└── src/
    └── main/
        ├── AndroidManifest.xml
        ├── java/com/mobileclaw/app/
        │   ├── MainActivity.kt
        │   ├── di/
        │   │   └── AppModule.kt
        │   ├── runtime/
        │   │   ├── ingress/
        │   │   │   ├── ExternalEntryRegistration.kt
        │   │   │   ├── ExternalHandoffPayload.kt
        │   │   │   ├── CallerIngressMetadata.kt
        │   │   │   ├── InboundRuntimeRequest.kt
        │   │   │   ├── ExternalInvocationRecord.kt
        │   │   │   ├── ExternalHandoffParser.kt
        │   │   │   ├── ExternalRuntimeRequestMapper.kt
        │   │   │   └── ExternalHandoffCoordinator.kt
        │   │   ├── capability/
        │   │   │   └── CallerVerifier.kt
        │   │   ├── policy/
        │   │   │   └── AuditRepository.kt
        │   │   ├── session/
        │   │   │   ├── RuntimeRequest.kt
        │   │   │   ├── RuntimeSessionEvent.kt
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

**Structure Decision**: Introduce a small `runtime/ingress` package to contain Android-specific entry registration, parsing, caller/source normalization, and one-shot handoff coordination. `MainActivity` will remain the manifest entry point, but it should hand off only normalized ingress models into the coordinator. Existing runtime/session, capability, policy, and UI layers should consume canonical `RuntimeRequest` data plus normalized source metadata rather than parsing Android intents directly.

## Complexity Tracking

No constitution violations are expected for this feature.
