# Implementation Plan: Tool Contract Standardization

**Branch**: `015-tool-contract-standardization` | **Date**: 2026-04-13 | **Spec**: [/Users/youxuezhe/StudioProjects/mobile_claw/specs/015-tool-contract-standardization/spec.md](/Users/youxuezhe/StudioProjects/mobile_claw/specs/015-tool-contract-standardization/spec.md)
**Input**: Feature specification from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/015-tool-contract-standardization/spec.md`

## Summary

Standardize Mobile Claw's execution surface around a first-class tool contract by introducing stable tool descriptors, schema-backed preview metadata, on-demand tool visibility, and a first productivity tool catalog spanning reply generation, calendar actions, alarms, messaging, and outbound sharing. Reuse the existing runtime, governance, structured action, and capability bridge layers rather than creating a second orchestration stack.

## Technical Context

**Language/Version**: Kotlin 2.2.x on Android with Java 11 toolchain  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing runtime/session/capability/policy/governance layers, Room, DataStore, current Android bridge providers  
**Storage**: Reuse existing Room-backed governance/audit state and in-memory capability registry; add descriptor metadata in runtime-managed catalogs or services without introducing remote storage  
**Testing**: Build/lint plus quickstart walkthroughs for tool visibility, preview consistency, and governance/audit alignment  
**Target Platform**: Android application rooted at `/Users/youxuezhe/StudioProjects/mobile_claw/app`  
**Project Type**: Android application with local-first runtime and workspace UI  
**Performance Goals**: Tool discovery and preview generation should stay lightweight enough to run inline with the existing session pipeline  
**Constraints**: Must remain local-first, preserve current runtime backbone, keep workspace conversation-first, and avoid proliferating one-off capability branches for covered tool families  
**Scale/Scope**: First standardized tool catalog plus shared contracts, visibility rules, preview metadata, and runtime/governance alignment

## Constitution Check

- `local-first`: Pass. Tool resolution and preview stay on-device and reuse existing local runtime/governance state.
- `private-by-default`: Pass. Standardization adds metadata and previews, not extra data export or background sharing.
- `policy-overrides-convenience`: Pass. Tool descriptors explicitly carry side-effect, scope, and confirmation metadata.
- `android-first`: Pass. Covered tool families map directly onto Android execution bindings and existing bridge providers.

## Project Structure

### Feature Artifacts

```text
specs/015-tool-contract-standardization/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/
│   └── tool-contract-standardization.md
├── quickstart.md
└── tasks.md
```

### Code Touch Points

```text
app/src/main/java/com/mobileclaw/app/runtime/capability/
app/src/main/java/com/mobileclaw/app/runtime/action/
app/src/main/java/com/mobileclaw/app/runtime/policy/
app/src/main/java/com/mobileclaw/app/runtime/session/
app/src/main/java/com/mobileclaw/app/runtime/governance/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/
app/src/main/res/values/
app/src/main/res/values-zh/
```

## Phase 0: Research

Generate `research.md` to lock:

- whether to evolve `CapabilityRegistration` into a richer tool descriptor or add a thin wrapper around it
- how to represent JSON-schema-first input contracts without overhauling the existing structured action extraction path
- how to model read/write/dispatch side-effect semantics for policy and preview
- how on-demand tool visibility should combine intent relevance, governance, provider availability, and current workspace/runtime context
- which common productivity tool families are in scope for the first standardized catalog

## Phase 1: Design & Contracts

Generate:

- `data-model.md`
- `contracts/tool-contract-standardization.md`
- `quickstart.md`

Then refresh agent context with:

```bash
SPECIFY_FEATURE=015-tool-contract-standardization ./.specify/scripts/bash/update-agent-context.sh codex
```

## Implementation Strategy

### MVP First

1. Introduce the shared tool descriptor, schema descriptor, visibility snapshot, and execution preview contracts
2. Refactor capability resolution to produce standardized tool metadata for covered tool families
3. Add the first standardized productivity tool catalog
4. Align runtime preview, governance, and audit to the same tool identity
5. Keep tool surfacing on-demand so the workspace stays conversation-first

### Incremental Delivery

1. Add descriptor and schema contracts
2. Add tool visibility and preview services
3. Standardize the first common productivity tool families
4. Route governance, approval, and audit through the standardized tool identity
5. Refine bilingual wording and validation

## Risks

- Existing capability and scope mappings are currently hardcoded in several places, so partial refactors can leave the system with mixed terminology if the standardization is not applied end to end.
- The first tool catalog needs enough breadth to prove the pattern without swallowing `016` external interop or `017` unified extension work.
- If tool visibility is not carefully bounded, the workspace could regress from the `013` conversation-first information architecture into a persistent tool console.
