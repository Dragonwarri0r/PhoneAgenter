# Tasks: Persona and Scoped Memory Fabric

**Input**: Design documents from `/specs/003-persona-memory-fabric/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/persona-memory-context-contract.md
**Tests**: No dedicated test tasks are included in this milestone plan; validation is driven by the independent story checks and quickstart flows.
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add the storage and package boundaries needed for persona and memory work

- [X] T001 Add persona-memory persistence dependencies and configuration in `app/build.gradle.kts`
- [X] T002 Create the persona and memory package skeleton in `app/src/main/java/com/mobileclaw/app/runtime/persona/PersonaProfile.kt`, `app/src/main/java/com/mobileclaw/app/runtime/persona/PersonaRepository.kt`, `app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryItem.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/memory/ScopedMemoryRepository.kt`
- [X] T003 Update dependency injection scaffolding for persona and memory services in `app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish shared persona, memory, retrieval, and workspace context contracts required by every user story

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T004 [P] Implement the stable persona profile model and repository contract in `app/src/main/java/com/mobileclaw/app/runtime/persona/PersonaProfile.kt`, `app/src/main/java/com/mobileclaw/app/runtime/persona/PersonaRepository.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/persona/PreferenceBackedPersonaRepository.kt`
- [X] T005 [P] Implement the memory entity, enums, DAO, and database scaffolding in `app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryItem.kt`, `app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryDao.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryDatabase.kt`
- [X] T006 [P] Define retrieval query, active context summary, and context-loader contracts in `app/src/main/java/com/mobileclaw/app/runtime/memory/ActiveContextSummary.kt`, `app/src/main/java/com/mobileclaw/app/runtime/memory/PersonaMemoryContextLoader.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt`
- [X] T007 [P] Define workspace-facing context inspector models in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/ContextInspectorUiModel.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/RuntimeStatusUiModel.kt`
- [X] T008 Wire persona-memory repositories, Room database, and runtime context loader into `app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

**Checkpoint**: Persona-memory foundations are ready for story-level implementation

---

## Phase 3: User Story 1 - Use Stable Persona and Relevant Context (Priority: P1) 🎯 MVP

**Goal**: Make the runtime load stable persona constraints and relevant in-scope memory through one explainable context assembly path

**Independent Test**: Define persona traits and a small set of memory items, submit a request, and verify the runtime uses both while keeping them distinct

### Implementation for User Story 1

- [X] T009 [P] [US1] Implement default persona loading, persistence, and editing in `app/src/main/java/com/mobileclaw/app/runtime/persona/PreferenceBackedPersonaRepository.kt`
- [X] T010 [P] [US1] Implement deterministic memory retrieval and relevance ranking in `app/src/main/java/com/mobileclaw/app/runtime/memory/ScopedMemoryRepository.kt` and `app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryRetrievalService.kt`
- [X] T011 [P] [US1] Implement runtime context assembly and safe active-context summaries in `app/src/main/java/com/mobileclaw/app/runtime/memory/ActiveContextSummary.kt` and `app/src/main/java/com/mobileclaw/app/runtime/memory/PersonaMemoryContextLoader.kt`
- [X] T012 [US1] Replace the no-op context loader with the persona-memory-backed loader in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt` and `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt`
- [X] T013 [US1] Surface active persona and retrieved-context summaries in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`

**Checkpoint**: User Story 1 is independently functional as the MVP persona-plus-context retrieval loop

---

## Phase 4: User Story 2 - Keep Memory Safely Scoped (Priority: P2)

**Goal**: Enforce scope isolation, exposure defaults, and safe writeback rules so memory does not leak across unrelated requests

**Independent Test**: Create memory with different scopes and verify that retrieval returns only entries allowed for the current request scope

### Implementation for User Story 2

- [X] T014 [P] [US2] Implement scope, exposure, and sync-policy validation rules in `app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryItem.kt` and `app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryDao.kt`
- [X] T015 [P] [US2] Implement scoped filtering for global, app-scoped, contact-scoped, and device-scoped retrieval in `app/src/main/java/com/mobileclaw/app/runtime/memory/ScopedMemoryRepository.kt` and `app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryRetrievalService.kt`
- [X] T016 [P] [US2] Implement writeback defaults for inferred app memory in `app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryWritebackService.kt` and `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`
- [X] T017 [US2] Extend runtime requests with request-scope and origin metadata in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`
- [X] T018 [US2] Surface isolation-safe context explanations and hidden-private counts in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextInspectorSheet.kt`

**Checkpoint**: User Story 2 works independently and protects scoped memory from leaking across requests

---

## Phase 5: User Story 3 - Promote, Edit, and Expire Memory (Priority: P3)

**Goal**: Allow the user to manage durable and temporary context without losing provenance or lifecycle safety

**Independent Test**: Create durable, working, and ephemeral memory items and verify that promotion, manual editing, pinning, and expiration behave as expected

### Implementation for User Story 3

- [X] T019 [P] [US3] Implement lifecycle promotion, demotion, pinning, and expiration behavior in `app/src/main/java/com/mobileclaw/app/runtime/memory/ScopedMemoryRepository.kt` and `app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryItem.kt`
- [X] T020 [P] [US3] Implement a minimal context inspector and persona editor surface in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextInspectorSheet.kt`
- [X] T021 [P] [US3] Implement workspace actions for manual persona edits and memory lifecycle changes in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/ContextInspectorUiModel.kt`
- [X] T022 [US3] Ensure expired memory is excluded while pinned durable memory remains eligible in `app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryRetrievalService.kt` and `app/src/main/java/com/mobileclaw/app/runtime/memory/PersonaMemoryContextLoader.kt`
- [X] T023 [US3] Integrate context-inspector presentation and refresh behavior in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`

**Checkpoint**: User Story 3 works independently and gives the user lightweight control over persona and memory lifecycle

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize redaction behavior, seed validation data, and record milestone validation notes

- [X] T024 [P] Add demo fixtures or seed helpers for persona and scoped memory validation in `app/src/main/java/com/mobileclaw/app/runtime/persona/PersonaFixtures.kt` and `app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryFixtures.kt`
- [X] T025 [P] Refine safe-summary wording, provenance display, and redaction behavior in `app/src/main/java/com/mobileclaw/app/runtime/memory/ActiveContextSummary.kt`, `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`, and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextInspectorSheet.kt`
- [X] T026 Validate the implemented milestone against `specs/003-persona-memory-fabric/quickstart.md` and update follow-up notes in `specs/003-persona-memory-fabric/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on Foundational completion and extends the MVP context-retrieval path
- **User Story 3 (Phase 5)**: Depends on Foundational completion and builds on the same persona-memory contracts
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP
- **User Story 2 (P2)**: Depends on the MVP retrieval path but should remain independently verifiable
- **User Story 3 (P3)**: Depends on the MVP retrieval path and repository contracts but should remain independently verifiable

### Parallel Opportunities

- `T004`, `T005`, `T006`, and `T007` can run in parallel after the setup files exist
- `T009`, `T010`, and `T011` can run in parallel within User Story 1
- `T014`, `T015`, and `T016` can run in parallel within User Story 2
- `T019`, `T020`, and `T021` can run in parallel within User Story 3
- `T024` and `T025` can run in parallel during polish

## Implementation Strategy

### MVP First

1. Finish Setup
2. Finish Foundational work
3. Finish User Story 1
4. Validate that the runtime now loads persona and relevant memory before moving on

### Incremental Delivery

1. Ship persona persistence plus scoped memory retrieval
2. Add strict scope isolation and writeback defaults
3. Add manual lifecycle control and lightweight context inspection
4. Finish with redaction polish and milestone validation

## Notes

- `003` should deepen the `002` runtime contract instead of creating a second context path
- Persona remains a stable profile, not a bag of facts
- Memory retrieval should stay deterministic and explainable in this milestone, even if more advanced retrieval arrives later
