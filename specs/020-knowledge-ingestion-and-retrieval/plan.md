# Implementation Plan: Knowledge Ingestion And Retrieval

**Branch**: `020-knowledge-ingestion-and-retrieval` | **Date**: 2026-04-22 | **Spec**: [/Users/youxuezhe/StudioProjects/mobile_claw/specs/020-knowledge-ingestion-and-retrieval/spec.md](/Users/youxuezhe/StudioProjects/mobile_claw/specs/020-knowledge-ingestion-and-retrieval/spec.md)
**Input**: Feature specification from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/020-knowledge-ingestion-and-retrieval/spec.md`

## Summary

Turn local knowledge into a first-class managed runtime layer through ingestion, corpus visibility, retrieval contracts, and redaction-aware request-time support. Keep durable knowledge distinct from persona, conversational memory, and ephemeral context while preserving the control-center grouping that treats Knowledge as its own management area.

## Roadmap Fit

`020` follows `019` and depends on its request-time contribution contracts, but it solves a different product problem.
`019` defines how contributors attach behavior and context during a request; `020` defines how durable local knowledge is ingested, managed, surfaced, and cited as a separate runtime layer.

This milestone should strengthen the updated roadmap’s product model:

- conversation/session layers show concise retrieval support
- the control center’s Knowledge area owns corpus management
- durable knowledge remains separate from memory and workflow controls

## Technical Context

**Language/Version**: Kotlin 2.2.x on Android with Java 11 toolchain  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Coroutines/Flow, Room, existing runtime/memory/systemsource/session/contribution layers, Android local file/content access, current runtime control-center surfaces  
**Storage**: Reuse local Room-backed runtime storage and app-managed local file access; add durable knowledge asset, ingestion, and retrieval metadata locally; if `MemoryDatabase` schema changes, bump `MemoryDatabase.version` in the same patch  
**Testing**: Build/lint plus quickstart walkthroughs for local ingestion, retrieval visibility, knowledge-area management, and bilingual user-facing wording  
**Target Platform**: Android application rooted at `/Users/youxuezhe/StudioProjects/mobile_claw/app`  
**Project Type**: Android application with local-first runtime, durable knowledge layer, and conversation-first control surfaces  
**Performance Goals**: Ingestion should remain bounded enough for local-first mobile use, and retrieval summaries should stay lightweight enough to support active task flow without making request handling feel stalled  
**Constraints**: Must remain local-first, preserve clear separation between knowledge and memory, avoid remote indexing or hosting, and keep corpus management out of the chat-first task flow except for concise request-relevant summaries  
**Scale/Scope**: First managed knowledge corpus slice covering local ingestion, durable asset visibility, retrieval support, availability state, and reversible knowledge management

## Constitution Check

- `local-first`: Pass. Ingestion, corpus management, and retrieval visibility all remain on-device.
- `persona-and-memory-are-separate`: Pass. This milestone explicitly separates durable knowledge from persona and conversational memory rather than collapsing them into one store.
- `safety-gates-override-convenience`: Pass. Knowledge use remains explainable and subject to redaction-aware presentation rather than silently bypassing privacy or policy considerations.
- `adapter-based-capability-integration`: Pass. The knowledge layer builds on runtime contribution contracts instead of inventing a separate backend execution model.
- `privacy-scope-audit-by-default`: Pass. Knowledge support remains source-visible and explainable instead of becoming opaque background context.

## Project Structure

### Feature Artifacts

```text
specs/020-knowledge-ingestion-and-retrieval/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/
│   └── knowledge-ingestion-and-retrieval.md
├── quickstart.md
└── tasks.md
```

### Code Touch Points

```text
app/src/main/java/com/mobileclaw/app/runtime/knowledge/
app/src/main/java/com/mobileclaw/app/runtime/memory/
app/src/main/java/com/mobileclaw/app/runtime/contribution/
app/src/main/java/com/mobileclaw/app/runtime/session/
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

- how to represent durable knowledge assets and ingestion records separately from memory items
- which local source types belong in the first managed corpus slice without requiring a broad connector ecosystem
- how retrieval support should appear in the active task flow without confusing users about whether something came from memory or knowledge
- which reversible knowledge-management actions belong in the first slice and which destructive or advanced controls should wait
- how to keep knowledge visibility source-linked and redaction-aware while still remaining concise during active requests

## Phase 1: Design & Contracts

Generate:

- `data-model.md`
- `contracts/knowledge-ingestion-and-retrieval.md`
- `quickstart.md`

Then refresh agent context with:

```bash
SPECIFY_FEATURE=020-knowledge-ingestion-and-retrieval ./.specify/scripts/bash/update-agent-context.sh codex
```

## Implementation Strategy

### MVP First

1. Introduce durable knowledge asset, ingestion, and retrieval-support models
2. Add the first local knowledge ingestion path and managed corpus visibility
3. Show concise source-linked retrieval support inside active requests
4. Add knowledge-area management for freshness and reversible availability state
5. Refine bilingual wording and validation

### Incremental Delivery

1. Add local knowledge persistence and ingestion contracts
2. Add request-time retrieval and citation contracts on top of `019` contribution semantics
3. Distinguish knowledge from memory in current-task and management surfaces
4. Add reversible availability/freshness actions and limitation messaging
5. Run quickstart validation and capture follow-up notes

## Risks

- If durable knowledge and memory are modeled too similarly, the product will lose the conceptual separation the roadmap now depends on.
- If retrieval visibility is too deep or too noisy, the conversation/session layers can become a corpus browser instead of staying task-first.
- If the first slice tries to solve broad connector coverage, advanced ranking, and destructive corpus controls at once, `020` will become too large and delay `021`.
