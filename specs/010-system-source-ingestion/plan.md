# Implementation Plan: System Source Ingestion

**Branch**: `010-system-source-ingestion` | **Date**: 2026-04-09 | **Spec**: [/Users/youxuezhe/StudioProjects/mobile_claw/specs/010-system-source-ingestion/spec.md](/Users/youxuezhe/StudioProjects/mobile_claw/specs/010-system-source-ingestion/spec.md)

## Summary

Add the first real Android system context connectors by ingesting bounded `Contacts` and `Calendar` data into the local runtime context pipeline. Keep ingestion local-first, permission-aware, and workspace-visible.

## Technical Context

**Language/Version**: Kotlin 2.2.x on Android with Java 11 toolchain  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Coroutines/Flow, Room, Android `ContentResolver` APIs  
**Storage**: Reuse Room-backed `MemoryItem` as the persistent system-source record store  
**Testing**: Build/lint plus quickstart walkthroughs  
**Target Platform**: Android application rooted at `/Users/youxuezhe/StudioProjects/mobile_claw/app`  
**Project Type**: Android application with local-first runtime  
**Performance Goals**: Keep source ingestion bounded and triggered only when relevant  
**Constraints**: Permission-aware, English + Simplified Chinese, no sync requirements  
**Scale/Scope**: First-party connectors for contacts + calendar only

## Constitution Check

- `local-first`: Pass. Source queries stay on-device and materialize into local records only.
- `private-by-default`: Pass. System-source ingestion remains permission-gated and bounded.
- `policy-overrides-convenience`: Pass. Permission state stays explicit and missing permissions remain visible.
- `android-first`: Pass. Uses Android system providers directly.

## Project Structure

### Feature Artifacts

```text
specs/010-system-source-ingestion/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/
│   └── system-source-contract.md
├── quickstart.md
└── tasks.md
```

### Code Touch Points

```text
app/src/main/java/com/mobileclaw/app/runtime/systemsource/
app/src/main/java/com/mobileclaw/app/runtime/memory/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/
app/src/main/res/values/
app/src/main/res/values-zh/
app/src/main/AndroidManifest.xml
```

## Phase 0: Research

Generate `research.md` to lock:

- which Android permissions and provider columns we actually need
- how to keep ingestion bounded and relevant
- how to represent source availability and contribution in the workspace
- how to refresh or replace old ingested system-source records safely

## Phase 1: Design & Contracts

Generate:

- `data-model.md`
- `contracts/system-source-contract.md`
- `quickstart.md`

Then refresh agent context with:

```bash
SPECIFY_FEATURE=010-system-source-ingestion ./.specify/scripts/bash/update-agent-context.sh codex
```

## Implementation Strategy

### MVP First

1. Add system source contracts and permission-aware repository/service
2. Implement contacts ingestion
3. Implement calendar ingestion
4. Expose permission/source contribution state in the workspace

### Incremental Delivery

1. Build source descriptors and ingestion service
2. Materialize contacts/calendar records into `MemoryItem`
3. Hook ingestion into `RuntimeContextLoader`
4. Add workspace permission + source status

## Risks

- Android provider queries are easy to over-broaden; this milestone must stay intentionally narrow.
- Permission prompts can sprawl the UI if we overbuild; keep them bound to the workspace.
- Calendar/contact ingestion can create stale duplicate memory if replacement/expiry rules are not explicit.
