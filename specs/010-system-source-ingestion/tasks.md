# Tasks: System Source Ingestion

**Input**: Design documents from `/specs/010-system-source-ingestion/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/system-source-contract.md
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by runtime walkthroughs, build/lint checks, and the quickstart flows.
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add permission strings, manifest permissions, and package boundaries

- [X] T001 Add English and Simplified Chinese strings for system source labels, permission state, and contribution summaries in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T002 Update `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/AndroidManifest.xml` with contacts and calendar read permissions
- [X] T003 Add system source package scaffolding in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/systemsource/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the shared source descriptors and ingestion contracts used by every story

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T004 [P] Implement shared system-source contracts in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/systemsource/SystemSourceModels.kt`
- [X] T005 [P] Implement permission-aware source repository and ingestion service in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/systemsource/SystemSourceRepository.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/systemsource/AndroidSystemSourceRepository.kt`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/systemsource/SystemSourceIngestionService.kt`
- [X] T006 [P] Extend `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/PersonaMemoryContextLoader.kt` with system-source contribution metadata
- [X] T007 [P] Wire system-source repository and ingestion service into `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

**Checkpoint**: System-source descriptors and ingestion services exist independently of the UI

---

## Phase 3: User Story 1 - Use Contacts As Runtime Context (Priority: P1) 🎯 MVP

**Goal**: Ingest relevant contacts into runtime context when the request references people

**Independent Test**: Grant contacts permission, submit a person-referencing request, and verify contacts contribute to runtime context

### Implementation for User Story 1

- [X] T008 [P] [US1] Implement contacts ingestion and bounded contact-to-memory materialization in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/systemsource/SystemSourceIngestionService.kt`
- [X] T009 [P] [US1] Store ingested contact results as `SYSTEM_SOURCE` memory through the existing repository write path in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/systemsource/SystemSourceIngestionService.kt`
- [X] T010 [US1] Surface contacts source status and contributions in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`

**Checkpoint**: Contacts become a real runtime context source

---

## Phase 4: User Story 2 - Use Calendar As Runtime Context (Priority: P2)

**Goal**: Ingest relevant upcoming calendar records into runtime context for scheduling-related requests

**Independent Test**: Grant calendar permission, submit a schedule-related request, and verify calendar contributes bounded runtime context

### Implementation for User Story 2

- [X] T011 [P] [US2] Implement calendar ingestion and bounded event-to-memory materialization in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/systemsource/SystemSourceIngestionService.kt`
- [X] T012 [P] [US2] Add calendar contribution summaries to runtime context assembly in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/PersonaMemoryContextLoader.kt`
- [X] T013 [US2] Surface calendar source status and contributions in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`

**Checkpoint**: Calendar becomes a real runtime context source

---

## Phase 5: User Story 3 - Expose Permission And Source Status Clearly (Priority: P3)

**Goal**: Make source availability, permission state, and current contribution legible in the workspace

**Independent Test**: Grant or deny permissions and verify the workspace clearly reflects source availability and usage

### Implementation for User Story 3

- [X] T014 [P] [US3] Add permission request handling for contacts/calendar in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt`
- [X] T015 [P] [US3] Add system-source UI models and state in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/RuntimeStatusUiModel.kt`
- [X] T016 [US3] Align workspace surfaces with permission-missing and no-results states in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`

**Checkpoint**: Source permissions and contribution states are understandable from the workspace

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize wording, consistency, and milestone validation

- [X] T017 [P] Refine bilingual system-source wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T018 [P] Align system-source summaries, runtime status, and memory visibility across `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [X] T019 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/010-system-source-ingestion/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/010-system-source-ingestion/quickstart.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on Foundational completion
- **User Story 3 (Phase 5)**: Depends on Foundational completion and benefits from source state from earlier stories
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because contacts best complements current message-oriented runtime flows
- **User Story 2 (P2)**: Depends on shared ingestion contracts and storage path
- **User Story 3 (P3)**: Depends on system-source descriptors and contribution state being available

### Parallel Opportunities

- `T004`, `T005`, and `T006` can run in parallel after setup files exist
- `T008` and `T009` can run in parallel within User Story 1
- `T011` and `T012` can run in parallel within User Story 2
- `T014` and `T015` can run in parallel within User Story 3
- `T017` and `T018` can run in parallel during polish

## Implementation Strategy

### MVP First

1. Finish Setup
2. Finish Foundational work
3. Finish User Story 1
4. Validate real contacts ingestion before moving to calendar and permission polish

### Incremental Delivery

1. Ship source descriptors and ingestion contracts
2. Add contacts ingestion
3. Add calendar ingestion
4. Add permission and contribution visibility

## Notes

- `010` should connect real Android system context while remaining intentionally bounded
- Ingested system-source records should stay local and expire naturally rather than accumulating forever
- Source status should be explicit even when permissions are denied or no results are found
