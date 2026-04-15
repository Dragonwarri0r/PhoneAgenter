# Implementation Plan: External Caller Interop Contracts

**Branch**: `016-external-caller-interop-contracts` | **Date**: 2026-04-13 | **Spec**: [/Users/youxuezhe/StudioProjects/mobile_claw/specs/016-external-caller-interop-contracts/spec.md](/Users/youxuezhe/StudioProjects/mobile_claw/specs/016-external-caller-interop-contracts/spec.md)
**Input**: Feature specification from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/016-external-caller-interop-contracts/spec.md`

## Summary

Standardize Mobile Claw's external caller model by introducing a canonical interop request envelope, shared caller identity and URI grant semantics, and a minimal stable callable surface for future assistant or agent callers. Reuse the existing external handoff, governance, policy, and tool standardization layers instead of growing per-entry adapters.

## Roadmap Fit

`016` is the first post-`015` contract-hardening milestone in the current roadmap.
It exists to stabilize inbound external calling semantics before we broaden the runtime-wide extension surface in `017` and before we consolidate everything into the control-center product surface in `018`.

## Technical Context

**Language/Version**: Kotlin 2.2.x on Android with Java 11 toolchain  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing runtime ingress/session/capability/governance layers, current external handoff parser and share targets  
**Storage**: Reuse existing Room-backed governance/audit state and runtime-local request models; add contract metadata without introducing remote persistence  
**Testing**: Build/lint plus quickstart walkthroughs for multiple inbound contract styles, caller trust consistency, and URI grant explainability  
**Target Platform**: Android application rooted at `/Users/youxuezhe/StudioProjects/mobile_claw/app`  
**Project Type**: Android application with local-first runtime and external handoff support  
**Performance Goals**: Interop envelope normalization and trust mapping should remain lightweight enough to run inline during request ingress  
**Constraints**: Must remain local-first, preserve existing ingress pipeline, avoid inventing transport-specific runtime forks, and keep governance/policy semantics aligned with current caller management  
**Scale/Scope**: First canonical interop envelope plus caller/trust/grant semantics for live handoff flows and future callable request compatibility

## Constitution Check

- `local-first`: Pass. Interop normalization and trust evaluation stay entirely on-device.
- `private-by-default`: Pass. URI/content grants are modeled explicitly instead of broadening data exposure.
- `policy-overrides-convenience`: Pass. External caller contracts stay subject to the same governance and scope rules.
- `android-first`: Pass. The milestone is rooted in Android share/caller/grant semantics and future callable Android surfaces.

## Project Structure

### Feature Artifacts

```text
specs/016-external-caller-interop-contracts/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/
│   └── external-caller-interop-contract.md
├── quickstart.md
└── tasks.md
```

### Code Touch Points

```text
app/src/main/java/com/mobileclaw/app/runtime/ingress/
app/src/main/java/com/mobileclaw/app/runtime/session/
app/src/main/java/com/mobileclaw/app/runtime/capability/
app/src/main/java/com/mobileclaw/app/runtime/governance/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/
app/src/main/res/values/
app/src/main/res/values-zh/
app/src/main/AndroidManifest.xml
```

## Phase 0: Research

Generate `research.md` to lock:

- how to normalize existing share-based text/media handoffs into a canonical interop envelope
- how caller identity, trust state, package identity, and URI grants should be represented once instead of per-entry
- what the minimal future callable request contract should contain without redoing the runtime request shape
- how to support forward-compatibility and unknown field handling in inbound interop contracts
- how much of the current ingress pipeline can be reused directly

## Phase 1: Design & Contracts

Generate:

- `data-model.md`
- `contracts/external-caller-interop-contract.md`
- `quickstart.md`

Then refresh agent context with:

```bash
SPECIFY_FEATURE=016-external-caller-interop-contracts ./.specify/scripts/bash/update-agent-context.sh codex
```

## Implementation Strategy

### MVP First

1. Introduce the canonical interop envelope and caller identity/grant models
2. Refactor current external handoff normalization to emit the interop envelope
3. Make governance and policy consume stable caller/trust semantics across entry types
4. Define the minimal future callable contract that maps into existing runtime requests
5. Add bilingual source/trust/grant wording

### Incremental Delivery

1. Add interop envelope, identity, and compatibility models
2. Normalize current inbound share text/media flows into that contract
3. Align governance/policy/approval language to the new interop identity
4. Add future callable surface descriptors and validation notes
5. Refine wording and validation

## Risks

- Current ingress flows already work, so partial standardization could leave some entry types using old semantics unless the change is applied end to end.
- URI grant behavior can vary by caller and Android version, so the contract should model grant summaries without over-promising runtime guarantees.
- If the future callable contract becomes too broad here, `016` will start swallowing the broader extension work intended for `017`.
