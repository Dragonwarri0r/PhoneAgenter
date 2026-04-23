# Implementation Plan: Workflow Graph And Automation

**Branch**: `021-workflow-graph-and-automation` | **Date**: 2026-04-22 | **Spec**: [/Users/youxuezhe/StudioProjects/mobile_claw/specs/021-workflow-graph-and-automation/spec.md](/Users/youxuezhe/StudioProjects/mobile_claw/specs/021-workflow-graph-and-automation/spec.md)
**Input**: Feature specification from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/021-workflow-graph-and-automation/spec.md`

## Summary

Add a stable local-first workflow graph and automation layer that can define reusable multi-step work, preserve resumable execution state, reuse existing proposal/approval/result language, and expose automations through the existing conversation/session/control-center/detail product model instead of introducing a separate orchestration console.

## Roadmap Fit

`021` is the third post-`018` expansion milestone.
It depends on `019` for runtime contribution contracts and on `020` for knowledge-layer participation, but it solves a distinct product problem: making multi-step work durable, resumable, and governable.

This milestone should prove:

- reusable workflow definitions without a heavy graph editor
- resumable workflow runs with explainable checkpoints
- automation management inside the existing Automation area and object detail model

It should **not** expand into remote orchestration, multi-device execution, or marketplace-style workflow sharing.

## Technical Context

**Language/Version**: Kotlin 2.2.x on Android with Java 11 toolchain  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Coroutines/Flow, Room, existing runtime/session/policy/approval/contribution/knowledge layers, current runtime control-center surfaces  
**Storage**: Reuse local Room/DataStore-backed runtime state; add durable workflow definitions, run records, and resumable checkpoints locally; if `MemoryDatabase` schema changes, bump `MemoryDatabase.version` in the same patch  
**Testing**: Build/lint plus quickstart walkthroughs for workflow definition availability, approval-gated run state, resumability, automation summaries, and bilingual user-facing wording  
**Target Platform**: Android application rooted at `/Users/youxuezhe/StudioProjects/mobile_claw/app`  
**Project Type**: Android application with local-first runtime, durable workflow objects, and control-center automation management  
**Performance Goals**: Workflow run-state updates and checkpoint reads should remain lightweight enough to coexist with the active workspace without making the conversation UI feel blocked  
**Constraints**: Must remain local-first, reuse the existing proposal/policy/approval/result language, keep global automation management in control surfaces rather than chat, and avoid starting with a heavy visual graph builder  
**Scale/Scope**: First durable workflow definition contract, resumable run/checkpoint model, automation management summaries, and local multi-step execution support

## Constitution Check

- `local-first`: Pass. Workflow definitions, run state, and checkpointing remain local and do not depend on remote orchestration.
- `persona-and-memory-are-separate`: Pass. Workflow execution may use memory or knowledge, but it does not collapse those systems into workflow definitions.
- `safety-gates-override-convenience`: Pass. Side-effecting workflow steps reuse the existing proposal, policy, and approval semantics rather than bypassing them.
- `adapter-based-capability-integration`: Pass. Workflow nodes build on established tool/contribution/runtime contracts instead of inventing a separate execution stack.
- `privacy-scope-audit-by-default`: Pass. Workflow runs remain explainable, auditable, and checkpointed rather than behaving like invisible background jobs.

## Project Structure

### Feature Artifacts

```text
specs/021-workflow-graph-and-automation/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/
│   └── workflow-graph-and-automation.md
├── quickstart.md
└── tasks.md
```

### Code Touch Points

```text
app/src/main/java/com/mobileclaw/app/runtime/workflow/
app/src/main/java/com/mobileclaw/app/runtime/session/
app/src/main/java/com/mobileclaw/app/runtime/policy/
app/src/main/java/com/mobileclaw/app/runtime/contribution/
app/src/main/java/com/mobileclaw/app/runtime/knowledge/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/
app/src/main/java/com/mobileclaw/app/runtime/strings/
app/src/main/java/com/mobileclaw/app/di/AppModule.kt
app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryDatabase.kt
app/src/main/res/values/
app/src/main/res/values-zh/
```

## Phase 0: Research

Generate `research.md` to lock:

- how to represent a stable workflow definition contract without requiring a full graph editor in the first slice
- how workflow steps should reuse existing tool, contribution, approval, and result language instead of introducing automation-specific variants
- how resumable checkpointing and run-state visibility should work in a local-first mobile runtime
- how global automation management should fit into the existing Automation area and detail-view model
- how to keep `021` away from remote orchestration, distributed execution, and marketplace sharing

## Phase 1: Design & Contracts

Generate:

- `data-model.md`
- `contracts/workflow-graph-and-automation.md`
- `quickstart.md`

Then refresh agent context with:

```bash
SPECIFY_FEATURE=021-workflow-graph-and-automation ./.specify/scripts/bash/update-agent-context.sh codex
```

## Implementation Strategy

### MVP First

1. Introduce durable workflow definition, step, trigger, run, and checkpoint models
2. Add the first lightweight workflow-definition authoring and availability flow
3. Add local workflow run orchestration with pause, approval, and resume semantics
4. Surface automation summaries and run history through the existing control surfaces
5. Refine bilingual wording and validation

### Incremental Delivery

1. Add workflow persistence and contract models
2. Add definition authoring/inspection and activation availability
3. Add run orchestration, checkpoints, and approval-gated pauses
4. Add automation management summaries, run history, and recovery guidance
5. Run quickstart validation and capture follow-up notes

## Risks

- If `021` starts with a heavy graph editor, the milestone will spend too much time on editing surface complexity before workflow semantics are stable.
- If workflow steps do not reuse current proposal/policy/approval language, automation will become a second execution system that is harder to trust and maintain.
- If resumability is underspecified, local automation will feel brittle and undermine the value of multi-step execution.
