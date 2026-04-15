# Implementation Plan: Runtime Control Center

**Branch**: `018-runtime-control-center` | **Date**: 2026-04-13 | **Spec**: [/Users/youxuezhe/StudioProjects/mobile_claw/specs/018-runtime-control-center/spec.md](/Users/youxuezhe/StudioProjects/mobile_claw/specs/018-runtime-control-center/spec.md)
**Input**: Feature specification from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/018-runtime-control-center/spec.md`

## Summary

Consolidate Mobile Claw's existing runtime visibility and editable runtime objects into one conversation-first runtime control center. Preserve built-in multimodal chat as the primary entry, but make runtime trace, managed artifacts, and supported edit actions reachable through one coherent in-app control surface instead of scattered sheets.

## Roadmap Fit

`018` is the product-consolidation milestone after `016` and `017`.
Its job is to gather the already-stable contracts for interop, tools, governance, memory, multimodal input, and extensions into one readable and editable control experience without replacing chat with a dashboard.

## Technical Context

**Language/Version**: Kotlin 2.2.x on Android with Java 11 toolchain  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing runtime/session/memory/governance/capability/extension layers, current workspace IA shell  
**Storage**: Reuse existing Room/DataStore-backed runtime state; no new durable storage required for the first control-center slice  
**Testing**: Build/lint plus quickstart walkthroughs for active-session trace visibility, managed-artifact entry routing, and localized control-center wording  
**Target Platform**: Android application rooted at `/Users/youxuezhe/StudioProjects/mobile_claw/app`  
**Project Type**: Android application with conversation-first runtime workspace  
**Performance Goals**: Control-center rendering should remain lightweight enough to open from an active conversation without disrupting streaming or composer responsiveness  
**Constraints**: Must remain conversation-first, local-first, and preserve existing artifact contracts instead of creating a parallel management model  
**Scale/Scope**: First unified runtime control center surface, runtime trace snapshot, and managed-artifact entry system

## Constitution Check

- `local-first`: Pass. All control-center inspection and editing remains on-device.
- `private-by-default`: Pass. Managed artifacts only expose already-supported editable surfaces and keep private/redacted boundaries intact.
- `policy-overrides-convenience`: Pass. Approval, governance, and extension state stay explainable and policy-aware inside the control center.
- `android-first`: Pass. The experience is built directly into the Android workspace and reuses existing Android-specific ingress/runtime features.

## Project Structure

### Feature Artifacts

```text
specs/018-runtime-control-center/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/
│   └── runtime-control-center-contract.md
├── quickstart.md
└── tasks.md
```

### Code Touch Points

```text
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/
app/src/main/java/com/mobileclaw/app/runtime/session/
app/src/main/java/com/mobileclaw/app/runtime/memory/
app/src/main/java/com/mobileclaw/app/runtime/governance/
app/src/main/java/com/mobileclaw/app/runtime/extension/
app/src/main/res/values/
app/src/main/res/values-zh/
```

## Phase 0: Research

Generate `research.md` to lock:

- how to unify the current detail sheet, context inspector, governance sheet, approval sheet, and extension visibility into one control-center language
- which managed artifact families should be directly editable in the first slice versus only inspectable
- how the runtime trace should be summarized so it remains readable during active conversation use
- how to keep chat primary while still making deeper control actions discoverable
- how to reuse current artifact actions instead of creating a second set of edit flows

## Phase 1: Design & Contracts

Generate:

- `data-model.md`
- `contracts/runtime-control-center-contract.md`
- `quickstart.md`

Then refresh agent context with:

```bash
SPECIFY_FEATURE=018-runtime-control-center ./.specify/scripts/bash/update-agent-context.sh codex
```

## Implementation Strategy

### MVP First

1. Introduce a unified runtime control-center state model
2. Add a coherent runtime trace snapshot for the active or recent request
3. Add managed artifact entries for memory, governance, approval, and extension state
4. Route the current scattered sheets through one primary control-center entry
5. Refine localized wording and validation

### Incremental Delivery

1. Build shared control-center models and section contracts
2. Replace the current generic workspace detail sheet with a true runtime control center
3. Feed runtime trace data from session, tool, approval, memory, and extension signals
4. Add entry points into supported artifact families from within the control center
5. Polish chat-preserving transitions and localized copy

## Risks

- If `018` only wraps existing sheets, it will not produce enough product change and the control experience will still feel fragmented.
- If `018` tries to replace every existing editor in one pass, the scope will balloon and slow the roadmap.
- The runtime trace can become visually noisy unless the control center uses progressive disclosure and stable grouping.
