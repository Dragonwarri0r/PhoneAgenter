# Implementation Plan: Workspace Information Architecture

**Branch**: `013-workspace-information-architecture` | **Date**: 2026-04-10 | **Spec**: [/Users/youxuezhe/StudioProjects/mobile_claw/specs/013-workspace-information-architecture/spec.md](/Users/youxuezhe/StudioProjects/mobile_claw/specs/013-workspace-information-architecture/spec.md)
**Input**: Feature specification from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/013-workspace-information-architecture/spec.md`

## Summary

Re-shape the workspace into a conversation-first surface by replacing the large persistent top-panel stack with a compact runtime digest, relocating quick actions into a lighter accessory zone, and using progressive disclosure for model, context, governance, portability, and diagnostic detail. The runtime behavior stays the same; this milestone is a presentation-layer re-architecture over the existing `001-012` feature set.

## Technical Context

**Language/Version**: Kotlin 2.2.x on Android with Java 11 toolchain  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing runtime/policy/memory/capability layers  
**Storage**: Reuse existing DataStore and Room-backed runtime state; no new durable storage required  
**Testing**: Build/lint plus quickstart walkthroughs across idle, streaming, approval, failure, and preparing states  
**Target Platform**: Android application rooted at `/Users/youxuezhe/StudioProjects/mobile_claw/app`  
**Project Type**: Android application with a local-first workspace UI  
**Performance Goals**: UI changes should keep interaction responsive and avoid increasing layout complexity enough to cause noticeable jank during streaming  
**Constraints**: Must preserve existing runtime features, bilingual messaging, IME behavior, and bottom-sheet/dialog flows while reducing permanent screen occupation by secondary information  
**Scale/Scope**: Single workspace surface refactor; no new navigation graph or multi-screen settings architecture

## Constitution Check

- `local-first`: Pass. This is a UI architecture milestone only and does not add remote dependencies.
- `private-by-default`: Pass. Sensitive runtime/context data remains behind on-demand surfaces and existing policy controls.
- `policy-overrides-convenience`: Pass. The milestone changes how approval, trust, and route information are displayed, not how they are enforced.
- `android-first`: Pass. The design targets the current Compose Android workspace and existing Android entry/runtime integrations.

## Project Structure

### Feature Artifacts

```text
specs/013-workspace-information-architecture/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/
│   └── workspace-information-architecture-contract.md
├── quickstart.md
└── tasks.md
```

### Code Touch Points

```text
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/
app/src/main/java/com/mobileclaw/app/runtime/strings/
app/src/main/res/values/
app/src/main/res/values-zh/
```

## Phase 0: Research

Generate `research.md` to lock:

- how to keep conversation visually primary while still surfacing execution state
- which information belongs in the default digest versus on-demand surfaces
- how to stabilize entry points for model, context, governance, portability, and system-source follow-up without growing a permanent dashboard
- how to preserve current approval/failure prominence without regressing IME behavior

## Phase 1: Design & Contracts

Generate:

- `data-model.md`
- `contracts/workspace-information-architecture-contract.md`
- `quickstart.md`

Then refresh agent context with:

```bash
SPECIFY_FEATURE=013-workspace-information-architecture ./.specify/scripts/bash/update-agent-context.sh codex
```

## Implementation Strategy

### MVP First

1. Introduce a compact workspace status digest and stable action affordances
2. Remove dependence on large persistent top cards for model/context visibility
3. Move quick actions into a lighter accessory position near the composer
4. Preserve deeper capability surfaces through on-demand sheets and dialogs

### Incremental Delivery

1. Add new UI models/components for digest and secondary entry grouping
2. Refactor `AgentWorkspaceScreen` layout around header + digest + conversation + accessory + composer
3. Rework existing model/context components into compact summaries or on-demand detail content
4. Refine attention states, locale strings, and quickstart validation

## Risks

- The workspace already coordinates many product surfaces, so moving them without losing discoverability requires careful hierarchy choices.
- It is easy to accidentally create a smaller but still cluttered dashboard if the digest tries to show everything at once.
- Approval, failure, and system-permission prompts compete for attention; the new IA must elevate them without pushing conversation out of view.
