# Tasks: Local Runtime Session Pipeline

**Input**: Design documents from `/specs/002-runtime-session-pipeline/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/runtime-session-contract.md
**Tests**: No dedicated test tasks are included in this milestone plan; validation is driven by the independent story checks and quickstart flows.
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Scaffold the runtime session package boundaries and establish the top-level contracts

- [X] T001 Create the runtime session package skeleton in `app/src/main/java/com/mobileclaw/app/runtime/session/ExecutionSession.kt`, `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt`, `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionEvent.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeStatusSummary.kt`
- [X] T002 Create the provider package skeleton in `app/src/main/java/com/mobileclaw/app/runtime/provider/CapabilityProvider.kt`, `app/src/main/java/com/mobileclaw/app/runtime/provider/CapabilityProviderRegistry.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/provider/LocalGenerationProvider.kt`
- [X] T003 Update dependency injection scaffolding for runtime session services in `app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish shared runtime registry, orchestration hooks, and UI-facing facade contracts required by every user story

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T004 [P] Implement the session registry and in-memory state store in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionRegistry.kt`
- [X] T005 [P] Implement the session facade contract in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionFacade.kt`
- [X] T006 [P] Implement placeholder orchestration hook contracts in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt` and `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt`
- [X] T007 [P] Define a UI-facing runtime status model in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/RuntimeStatusUiModel.kt`
- [X] T008 Wire the base runtime session services into dependency injection in `app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

**Checkpoint**: Runtime session foundation is ready for story-level implementation

---

## Phase 3: User Story 1 - Run Every Request Through a Unified Session (Priority: P1) 🎯 MVP

**Goal**: Ensure every accepted request creates one execution session and completes through one unified session lifecycle contract

**Independent Test**: Submit a request into the runtime and verify that one execution session is created, tracked, and completed with a structured outcome

### Implementation for User Story 1

- [X] T009 [P] [US1] Implement the execution session and terminal outcome models in `app/src/main/java/com/mobileclaw/app/runtime/session/ExecutionSession.kt`
- [X] T010 [P] [US1] Implement the normalized runtime request model and transcript request mapping in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt`
- [X] T011 [P] [US1] Adapt the existing local generation path into a provider implementation in `app/src/main/java/com/mobileclaw/app/runtime/provider/LocalGenerationProvider.kt`
- [X] T012 [US1] Implement request acceptance, session creation, and unified completion handling in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`
- [X] T013 [US1] Replace direct `LocalChatGateway` request execution with the runtime session facade in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`

**Checkpoint**: User Story 1 is independently functional as the MVP runtime session loop

---

## Phase 4: User Story 2 - Observe Intermediate Runtime Stages (Priority: P2)

**Goal**: Emit ordered stage updates and a compact status summary that the workspace can render clearly

**Independent Test**: Run requests that stop at different stages and verify ordered stage updates and a compact user-facing summary are exposed

### Implementation for User Story 2

- [X] T014 [P] [US2] Implement ordered session stage events in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionEvent.kt`
- [X] T015 [P] [US2] Implement derived compact status summaries in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeStatusSummary.kt`
- [X] T016 [P] [US2] Add placeholder lifecycle transitions for context loading, planning, capability selection, and gating in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`
- [X] T017 [US2] Map runtime session summaries into workspace-friendly UI state in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/RuntimeStatusUiModel.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`
- [X] T018 [US2] Update the workspace context/status presentation to consume runtime-stage summaries in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`

**Checkpoint**: User Story 2 works independently and makes runtime progress understandable

---

## Phase 5: User Story 3 - Use Stable Contracts for Different Capability Providers (Priority: P3)

**Goal**: Keep provider substitution behind a stable runtime session contract

**Independent Test**: Wire two providers that satisfy the same capability contract and verify the top-level session lifecycle remains unchanged

### Implementation for User Story 3

- [X] T019 [P] [US3] Implement the provider abstraction contract in `app/src/main/java/com/mobileclaw/app/runtime/provider/CapabilityProvider.kt`
- [X] T020 [P] [US3] Implement the provider registry and lookup strategy in `app/src/main/java/com/mobileclaw/app/runtime/provider/CapabilityProviderRegistry.kt`
- [X] T021 [P] [US3] Create a second mock or fixture provider for substitution validation in `app/src/main/java/com/mobileclaw/app/runtime/provider/MockCapabilityProvider.kt`
- [X] T022 [US3] Implement provider selection and execution routing inside the session orchestrator in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`
- [X] T023 [US3] Ensure the workspace continues to consume only the stable runtime contract in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`

**Checkpoint**: User Story 3 works independently and preserves the top-level session contract across provider changes

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize validation, lifecycle edge handling, and integration notes for the milestone

- [X] T024 [P] Harden duplicate terminal-signal, cancellation, and failure-collapse handling in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionRegistry.kt` and `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`
- [X] T025 [P] Refine workspace status wording and compact stage presentation in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeStatusSummary.kt`, `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/RuntimeStatusUiModel.kt`, and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`
- [X] T026 Validate the implemented milestone against `specs/002-runtime-session-pipeline/quickstart.md` and update any follow-up notes in `specs/002-runtime-session-pipeline/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on Foundational completion and extends the MVP runtime loop
- **User Story 3 (Phase 5)**: Depends on Foundational completion and builds on the stable runtime contract
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP
- **User Story 2 (P2)**: Depends on the MVP runtime loop but should remain independently verifiable
- **User Story 3 (P3)**: Depends on the MVP runtime loop and provider abstraction but should remain independently verifiable

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
4. Validate that the workspace now uses one runtime session contract before moving on

### Incremental Delivery

1. Ship the unified execution-session loop
2. Add explicit ordered stage visibility and compact status summaries
3. Add provider substitution behind a stable runtime contract
4. Finish with terminal-state hardening and validation polish

## Notes

- `002` is the runtime backbone milestone and intentionally stops short of full memory, policy, and Android capability integrations
- The orchestration stages for context, planning, and gating should exist here even when their detailed logic is placeholder-only
- The `001` workspace should be migrated onto this contract rather than preserving a second direct execution path
