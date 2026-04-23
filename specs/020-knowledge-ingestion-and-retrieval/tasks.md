# Tasks: Knowledge Ingestion And Retrieval

**Input**: Design documents from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/020-knowledge-ingestion-and-retrieval/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/knowledge-ingestion-and-retrieval.md  
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by build/lint checks and quickstart walkthroughs.  
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare shared wording and documentation for the managed knowledge layer

- [X] T001 Add English and Simplified Chinese strings for knowledge ingestion, freshness, retrieval, citation, and availability wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T002 Create `020` feature documentation artifacts and refresh agent context in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/020-knowledge-ingestion-and-retrieval/` and `/Users/youxuezhe/StudioProjects/mobile_claw/AGENTS.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish managed knowledge persistence, retrieval contracts, and UI plumbing

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T003 [P] Add durable knowledge asset, ingestion, and availability entities plus DAO/database wiring in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/knowledge/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryDatabase.kt` and bump `MemoryDatabase.version` in the same patch
- [X] T004 [P] Add knowledge ingestion, retrieval, citation, and support-summary service contracts in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/knowledge/`
- [X] T005 [P] Add shared knowledge-area summary and request-time retrieval visibility models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/contribution/`
- [X] T006 Update DI and runtime wiring for managed knowledge ingestion and retrieval in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/`

**Checkpoint**: One managed knowledge layer exists with durable persistence, retrieval contracts, and UI-ready summaries

---

## Phase 3: User Story 1 - Ingest Local Knowledge Into A Managed Corpus (Priority: P1) 🎯 MVP

**Goal**: Let users ingest supported local sources into a managed knowledge corpus with visible provenance and ingestion state

**Independent Test**: Ingest at least one supported local source and verify it appears as a managed knowledge asset with source identity and availability state

### Implementation for User Story 1

- [X] T007 [P] [US1] Add supported local knowledge-source intake and asset creation flows in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/knowledge/`
- [X] T008 [P] [US1] Add knowledge-area summaries and ingestion-state UI models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/`
- [X] T009 [US1] Persist managed knowledge assets and ingestion records through the local corpus layer in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/knowledge/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryDatabase.kt`
- [X] T010 [US1] Surface managed knowledge asset entries and ingestion state in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`

**Checkpoint**: Users can ingest and revisit managed local knowledge assets

---

## Phase 4: User Story 2 - Use Retrieved Knowledge In Active Requests With Visible Provenance (Priority: P2)

**Goal**: Make request-time knowledge support visible, source-linked, and distinct from memory/context

**Independent Test**: Trigger a request that uses ingested knowledge and verify the active task flow surfaces source-linked retrieval support and clear provenance

### Implementation for User Story 2

- [X] T011 [P] [US2] Add retrieval query, citation, and support-summary models for active requests in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/knowledge/`
- [X] T012 [P] [US2] Integrate knowledge retrieval into the runtime contribution/request-time context path in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/contribution/`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/knowledge/`
- [X] T013 [US2] Render request-time knowledge support, citations, and limitation messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [X] T014 [US2] Align trace and wording so knowledge support is distinguishable from memory-derived context in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`

**Checkpoint**: Requests can use managed knowledge with visible provenance and clear separation from memory

---

## Phase 5: User Story 3 - Manage Knowledge Freshness And Availability Without Mixing It Into Memory Or Workflow Controls (Priority: P3)

**Goal**: Support dedicated Knowledge-area management for freshness and reversible availability state

**Independent Test**: Open managed knowledge assets, inspect freshness and availability state, apply supported reversible controls, and confirm the flow stays separate from memory and automation surfaces

### Implementation for User Story 3

- [X] T015 [P] [US3] Add reversible knowledge availability and freshness-state actions in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/knowledge/`
- [X] T016 [P] [US3] Add knowledge detail routing and management summaries in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/`
- [X] T017 [US3] Surface stale, partial, excluded, missing, and refresh-related states with non-destructive actions in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [X] T018 [US3] Preserve provenance and recent-usage summaries for managed knowledge assets in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/knowledge/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`

**Checkpoint**: The Knowledge area can manage corpus freshness and availability without collapsing into memory or workflow controls

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize wording, consistency, and milestone validation

- [X] T019 [P] Refine bilingual knowledge ingestion, retrieval, citation, and freshness wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T020 [P] Align knowledge ingestion, request-time visibility, and reversible management behavior across `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [X] T021 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/020-knowledge-ingestion-and-retrieval/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/020-knowledge-ingestion-and-retrieval/quickstart.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on durable knowledge assets and ingestion state already existing from earlier work
- **User Story 3 (Phase 5)**: Depends on stable knowledge identity, retrieval visibility, and availability-state models
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because it proves local knowledge can exist as a managed runtime layer
- **User Story 2 (P2)**: Depends on managed knowledge assets and ingestion state from earlier phases
- **User Story 3 (P3)**: Depends on stable knowledge identity, availability, and visibility language

### Parallel Opportunities

- `T003`, `T004`, and `T005` can run in parallel during Foundational work
- `T007` and `T008` can run in parallel within User Story 1
- `T011` and `T012` can run in parallel within User Story 2
- `T015` and `T016` can run in parallel within User Story 3
- `T019` and `T020` can run in parallel during polish

## Implementation Strategy

### MVP First

1. Finish Setup
2. Finish Foundational work
3. Finish User Story 1
4. Validate that local knowledge now exists as a managed corpus layer before adding request-time retrieval visibility and reversible management

### Incremental Delivery

1. Add durable knowledge persistence and ingestion contracts
2. Add managed corpus visibility and asset creation
3. Add request-time retrieval support and source-linked citations
4. Add freshness and reversible availability management in the Knowledge area

## Notes

- `020` should make durable knowledge usable and inspectable, not collapse knowledge back into memory or jump ahead into workflow automation
- Any Room-managed schema changes in `MemoryDatabase` must bump `MemoryDatabase.version` in the same patch
