# Implementation Plan: Runtime Hooks And Context Sources

**Branch**: `019-runtime-hooks-and-context-sources` | **Date**: 2026-04-22 | **Spec**: [/Users/youxuezhe/StudioProjects/mobile_claw/specs/019-runtime-hooks-and-context-sources/spec.md](/Users/youxuezhe/StudioProjects/mobile_claw/specs/019-runtime-hooks-and-context-sources/spec.md)
**Input**: Feature specification from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/019-runtime-hooks-and-context-sources/spec.md`

## Summary

Introduce one unified runtime contribution surface for lifecycle hooks, context sources, and future knowledge-source contributors. Keep the conversation/session/control-center/detail layering established by `018`, but let runtime contributions register, evaluate, explain themselves, and expose reversible availability state without turning into ad hoc callbacks, hidden side paths, or a second management model.

## Roadmap Fit

`019` is the first post-`018` expansion milestone.
It exists to stabilize how runtime contributors plug into the lifecycle before `020` adds durable knowledge ingestion and before `021` adds workflow and automation.

This milestone assumes a short `018.x` control-surface hardening pass may happen before or alongside it, but it does **not** redefine that UI work.
Its job is to make lifecycle contribution contracts coherent enough that later knowledge and workflow features can reuse them instead of reopening runtime architecture.

## Technical Context

**Language/Version**: Kotlin 2.2.x on Android with Java 11 toolchain  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing runtime/extension/session/systemsource/memory/policy/governance layers, current runtime control-center surfaces from `018`  
**Storage**: Reuse existing local Room/DataStore-backed runtime state and audit paths; add runtime-local contributor descriptors and outcome summaries without introducing remote persistence or durable knowledge-corpus storage  
**Testing**: Build/lint plus quickstart walkthroughs for unified contributor registration, task-level contribution visibility, and reversible availability management  
**Target Platform**: Android application rooted at `/Users/youxuezhe/StudioProjects/mobile_claw/app`  
**Project Type**: Android application with conversation-first runtime workspace and local-first extensibility  
**Performance Goals**: Contributor evaluation and request-time context attachment should remain lightweight enough to run inline with active request handling without making the chat or session surfaces feel delayed  
**Constraints**: Must remain local-first, preserve existing extension/system-source/runtime control-center semantics, keep current-task summaries concise, and stop short of full corpus management or workflow orchestration  
**Scale/Scope**: First unified runtime contribution language covering lifecycle hooks, request-time context contributors, contribution outcomes, and reversible availability state for supported contributor families

## Constitution Check

- `local-first`: Pass. Contribution registration, eligibility, explainability, and management remain on-device.
- `persona-and-memory-are-separate`: Pass. `019` does not collapse durable knowledge or memory into one contribution blob; it only defines request-time contribution contracts.
- `safety-gates-override-convenience`: Pass. Contributors remain subject to policy, trust, scope, and approval constraints before they influence execution.
- `adapter-based-capability-integration`: Pass. Existing extension, system-source, and runtime layers are normalized into a shared contribution surface instead of growing new one-off callbacks.
- `privacy-scope-audit-by-default`: Pass. Every applied, skipped, degraded, or blocked contribution remains explainable and auditable.

## Project Structure

### Feature Artifacts

```text
specs/019-runtime-hooks-and-context-sources/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/
│   └── runtime-hooks-and-context-sources.md
├── quickstart.md
└── tasks.md
```

### Code Touch Points

```text
app/src/main/java/com/mobileclaw/app/runtime/contribution/
app/src/main/java/com/mobileclaw/app/runtime/extension/
app/src/main/java/com/mobileclaw/app/runtime/systemsource/
app/src/main/java/com/mobileclaw/app/runtime/memory/
app/src/main/java/com/mobileclaw/app/runtime/session/
app/src/main/java/com/mobileclaw/app/runtime/policy/
app/src/main/java/com/mobileclaw/app/runtime/governance/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/
app/src/main/java/com/mobileclaw/app/runtime/strings/
app/src/main/java/com/mobileclaw/app/di/AppModule.kt
app/src/main/res/values/
app/src/main/res/values-zh/
```

## Phase 0: Research

Generate `research.md` to lock:

- whether to introduce a dedicated runtime contribution package or keep contribution semantics scattered across extension, system-source, and session code
- how to represent lifecycle-oriented and context-oriented contributors through one shared registration language
- how to expose concise contribution summaries in the active task flow without turning the current task UI into a second control center
- which reversible contributor-management controls belong in this slice and which richer configuration concerns must wait
- how to keep `019` clearly separate from durable knowledge-corpus management and workflow execution

## Phase 1: Design & Contracts

Generate:

- `data-model.md`
- `contracts/runtime-hooks-and-context-sources.md`
- `quickstart.md`

Then refresh agent context with:

```bash
SPECIFY_FEATURE=019-runtime-hooks-and-context-sources ./.specify/scripts/bash/update-agent-context.sh codex
```

## Implementation Strategy

### MVP First

1. Introduce shared runtime contribution registration, lifecycle point, eligibility, and outcome models
2. Normalize at least one lifecycle-oriented contributor family and one context-oriented contributor family into the new contribution language
3. Feed current-task and control-center contribution summaries from the same outcome records
4. Add reversible availability management for supported contributors
5. Refine bilingual wording and validation

### Incremental Delivery

1. Add runtime contribution contracts and registry/evaluation services
2. Adapt covered extension, system-source, memory, and policy-facing contributor paths into the shared model
3. Expose current-task summaries, provenance, and degraded/blocked states in existing UI layers
4. Add reversible contributor-management actions and limitation messaging
5. Run quickstart validation and capture follow-up notes

## Risks

- If `019` only adds internal hook contracts without end-user visibility, it will not produce enough product value to support later knowledge and workflow milestones.
- If `019` absorbs durable corpus management, it will blur the boundary that `020` is meant to establish between request-time contribution and managed knowledge.
- If contributor summaries are too verbose, the conversation/session flow can regress into a technical console rather than a task-first runtime surface.
