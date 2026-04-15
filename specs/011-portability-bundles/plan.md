# Implementation Plan: Portability Bundles

**Branch**: `011-portability-bundles` | **Date**: 2026-04-09 | **Spec**: [/Users/youxuezhe/StudioProjects/mobile_claw/specs/011-portability-bundles/spec.md](/Users/youxuezhe/StudioProjects/mobile_claw/specs/011-portability-bundles/spec.md)

## Summary

Turn the existing redaction-aware export hooks from `006` into a real product surface: users can preview a portability bundle, understand what is included or redacted, and share the safe bundle through Android.

## Technical Context

**Language/Version**: Kotlin 2.2.x on Android with Java 11 toolchain  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing memory/export services, Android share intents  
**Storage**: Reuse existing `MemoryItem` and `ExportBundle`; no new durable storage required  
**Testing**: Build/lint plus quickstart walkthroughs  
**Target Platform**: Android application rooted at `/Users/youxuezhe/StudioProjects/mobile_claw/app`  
**Project Type**: Android application with local-first runtime  
**Performance Goals**: Bundle preview should be instant for inspector-visible records  
**Constraints**: Local-first, private-by-default, bilingual, no real sync/import system  
**Scale/Scope**: Portability preview and outbound share for current memory records only

## Constitution Check

- `local-first`: Pass. Bundles are built locally from local memory records.
- `private-by-default`: Pass. Export remains governed by existing exposure and redaction rules.
- `policy-overrides-convenience`: Pass. Private records remain blocked even if the user asks to export them.
- `android-first`: Pass. Uses Android share dispatch as the first outbound path.

## Project Structure

### Feature Artifacts

```text
specs/011-portability-bundles/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/
│   └── portability-bundle-contract.md
├── quickstart.md
└── tasks.md
```

### Code Touch Points

```text
app/src/main/java/com/mobileclaw/app/runtime/memory/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/
app/src/main/res/values/
app/src/main/res/values-zh/
```

## Phase 0: Research

Generate `research.md` to lock:

- how portability preview should relate to existing context inspector behavior
- how to format a safe outbound text bundle without exposing raw internals
- how to represent compatibility lines without promising real import support
- how to make export mode switching explicit but safe

## Phase 1: Design & Contracts

Generate:

- `data-model.md`
- `contracts/portability-bundle-contract.md`
- `quickstart.md`

Then refresh agent context with:

```bash
SPECIFY_FEATURE=011-portability-bundles ./.specify/scripts/bash/update-agent-context.sh codex
```

## Implementation Strategy

### MVP First

1. Add preview state and bundle formatter
2. Add export action from the context inspector
3. Add Android share dispatch for the formatted bundle text
4. Expose compatibility and redaction explanations in the preview

### Incremental Delivery

1. Build preview contracts on top of existing `ExportDecisionService`
2. Add export preview sheet to workspace UI
3. Add safe share path and blocked-export messaging
4. Refine compatibility, wording, and walkthrough validation

## Risks

- The preview can become too verbose if we dump raw metadata directly; it needs a productized summary.
- Export mode switching must never imply that private records can leave the app.
- Android share dispatch can fail on edge devices; user-visible failure messaging must stay clear.
