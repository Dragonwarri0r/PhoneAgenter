# Implementation Plan: Permission Governance Center

**Branch**: `009-permission-governance-center` | **Date**: 2026-04-09 | **Spec**: [/Users/youxuezhe/StudioProjects/mobile_claw/specs/009-permission-governance-center/spec.md](/Users/youxuezhe/StudioProjects/mobile_claw/specs/009-permission-governance-center/spec.md)

## Summary

Add a workspace-embedded governance center that lets users review caller trust, inspect recent approval/denial activity, and manage per-caller trust and scope grants. Reuse the existing Room-backed policy/audit stack and current workspace screen instead of introducing a separate settings architecture.

## Technical Context

**Language/Version**: Kotlin 2.2.x on Android with Java 11 toolchain  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Coroutines/Flow, Room  
**Storage**: Extend existing Room database with governance tables  
**Testing**: Build/lint plus quickstart walkthroughs  
**Target Platform**: Android app rooted at `/Users/youxuezhe/StudioProjects/mobile_claw/app`  
**Project Type**: Android application with local-first runtime  
**Performance Goals**: Governance center should load bounded recent state without introducing long-lived blocking queries  
**Constraints**: English + Simplified Chinese, local-only governance records, no sync dependencies  
**Scale/Scope**: One embedded governance center, bounded recent history, bounded actionable scope set

## Constitution Check

- `local-first`: Pass. Governance records remain local.
- `private-by-default`: Pass. Governance exposes metadata and recent decisions, not raw secret content.
- `policy-overrides-convenience`: Pass. Stored governance overrides will affect runtime routing.
- `android-first`: Pass. The surface stays embedded in the current Android workspace.

## Project Structure

### Feature Artifacts

```text
specs/009-permission-governance-center/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/
│   └── governance-center-contract.md
├── quickstart.md
└── tasks.md
```

### Code Touch Points

```text
app/src/main/java/com/mobileclaw/app/runtime/governance/
app/src/main/java/com/mobileclaw/app/runtime/capability/
app/src/main/java/com/mobileclaw/app/runtime/policy/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/
app/src/main/res/values/
app/src/main/res/values-zh/
```

## Phase 0: Research

Generate `research.md` to lock:

- governance data shape and where it should live
- how caller trust overrides interact with existing `CallerVerifier`
- how scope grant overrides interact with runtime routing/policy
- how the workspace should host the governance center without becoming a new navigation project

## Phase 1: Design & Contracts

Generate:

- `data-model.md`
- `contracts/governance-center-contract.md`
- `quickstart.md`

Then refresh agent context with:

```bash
SPECIFY_FEATURE=009-permission-governance-center ./.specify/scripts/bash/update-agent-context.sh codex
```

## Implementation Strategy

### MVP First

1. Add governance data layer and repository
2. Surface a governance center sheet from the workspace
3. Show recent callers and approval activity
4. Make trust/scope overrides persist
5. Enforce overrides during runtime routing

### Incremental Delivery

1. Observation-only governance center
2. Editable caller trust and scope grants
3. Runtime enforcement and explainability polish

## Risks

- Mixing governance state with raw verifier logic can create duplicated sources of truth if we do not centralize override resolution.
- A large, unbounded history surface would slow the workspace; queries should remain bounded.
- Scope editing can become too broad if we try to support every possible scope in this milestone; keep the set intentionally small.
