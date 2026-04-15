# Tasks: Sync-Ready Share and Extension Hooks

**Input**: Design documents from `/specs/006-sync-extension-hooks/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/sync-extension-contract.md
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by schema inspection, runtime walkthroughs, build/lint checks, and the quickstart flows.
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create the shared contract and localization surface needed for future-ready sync/share metadata

- [X] T001 Add sync/export contract package scaffolding in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/`
- [X] T002 Add English and Simplified Chinese strings for exposure, sync readiness, export mode, and extension compatibility messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T003 Update `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt` with localization helpers for sync policy, exposure policy, export mode, and extension compatibility labels

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the base metadata and contract models required by all three user stories

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T004 [P] Extend `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryItem.kt` with logical identity, origin, and version metadata required for future merge evaluation
- [X] T005 [P] Update `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryDatabase.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryDao.kt` for the new persisted metadata and type-converter support
- [X] T006 [P] Add normalized future-ready domain contracts in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/SyncEnvelope.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/MergeCandidate.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/ExportBundle.kt`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/ExtensionRegistration.kt`
- [X] T007 [P] Add a redaction/export evaluation layer in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/DataRedactionPolicy.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/ExportDecisionService.kt`
- [X] T008 Wire the new metadata and export services into `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

**Checkpoint**: Future-ready metadata, export contracts, and extension models exist and can be consumed by story-level work

---

## Phase 3: User Story 1 - Mark Data for Future Sharing Without Syncing Yet (Priority: P1) 🎯 MVP

**Goal**: Persist explicit exposure/share/sync metadata on memory records while preserving local-only defaults

**Independent Test**: Create or update memory records and verify that exposure, shareability, and sync metadata are stored explicitly even though no sync execution occurs

### Implementation for User Story 1

- [X] T009 [P] [US1] Update memory defaults and writeback behavior in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryWritebackService.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryFixtures.kt` so new records remain local-only and private-by-default with explicit metadata
- [X] T010 [P] [US1] Update repository persistence and update paths in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/ScopedMemoryRepository.kt` so metadata is preserved on insert, update, and lifecycle changes
- [X] T011 [US1] Extend context and inspector view models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/ContextInspectorUiModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` to expose exposure and sync metadata for validation
- [X] T012 [US1] Surface localized exposure and sync metadata in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextInspectorSheet.kt`

**Checkpoint**: User Story 1 is independently functional and proves that `v0` records already carry future-ready local metadata

---

## Phase 4: User Story 2 - Prepare for Future Merge Inputs (Priority: P2)

**Goal**: Normalize current persisted records into merge-ready inputs without introducing real merge execution

**Independent Test**: Inspect a stored memory item and verify the current schema can generate a merge candidate with origin, logical identity, and version metadata

### Implementation for User Story 2

- [X] T013 [P] [US2] Add merge-candidate normalization in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/MergeCandidate.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/SyncEnvelope.kt`
- [X] T014 [P] [US2] Add repository helpers for merge-readiness inspection in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/ScopedMemoryRepository.kt`
- [X] T015 [US2] Expose merge-readiness summaries through `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/ContextInspectorUiModel.kt`
- [X] T016 [US2] Surface merge identity, origin, and logical version details in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextInspectorSheet.kt`

**Checkpoint**: User Story 2 works independently and proves future merge inputs fit the current schema

---

## Phase 5: User Story 3 - Add Future Providers and Export Paths Safely (Priority: P3)

**Goal**: Define redaction-aware export bundles and extension registration hooks without redesigning runtime core entities

**Independent Test**: Generate export bundles under different exposure policies and verify a proposed extension can register against current contracts using declared required fields

### Implementation for User Story 3

- [X] T017 [P] [US3] Implement redaction-aware export bundle generation in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/ExportDecisionService.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/ExportBundle.kt`
- [X] T018 [P] [US3] Add extension registration and compatibility checks in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/ExtensionRegistration.kt`
- [X] T019 [US3] Connect exportability and extension summaries into `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/ContextInspectorUiModel.kt`
- [X] T020 [US3] Surface localized exportability, redaction, and extension compatibility details in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextInspectorSheet.kt`
- [X] T021 [US3] Align extension metadata entry points in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRegistration.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/ProviderDescriptor.kt` so future portability/provider hooks can reuse current contracts

**Checkpoint**: User Story 3 works independently and proves future export/provider hooks fit through defined extension contracts

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize bilingual wording, documentation sync, and milestone validation

- [X] T022 [P] Refine bilingual sync/exposure/export wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T023 [P] Align existing workspace and memory surfaces with the new metadata and explainability expectations in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/`
- [X] T024 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/006-sync-extension-hooks/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/006-sync-extension-hooks/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on Foundational completion and builds on the metadata model from US1
- **User Story 3 (Phase 5)**: Depends on Foundational completion and reuses the metadata model from US1
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because it locks the persisted metadata shape
- **User Story 2 (P2)**: Depends on persisted metadata from US1 but remains independently verifiable
- **User Story 3 (P3)**: Depends on the same metadata base and remains independently verifiable

### Parallel Opportunities

- `T004`, `T005`, `T006`, and `T007` can run in parallel after setup files exist
- `T009` and `T010` can run in parallel within User Story 1
- `T013` and `T014` can run in parallel within User Story 2
- `T017` and `T018` can run in parallel within User Story 3
- `T022` and `T023` can run in parallel during polish

## Implementation Strategy

### MVP First

1. Finish Setup
2. Finish Foundational work
3. Finish User Story 1
4. Validate that new records remain local-only but future-ready

### Incremental Delivery

1. Ship the persisted metadata expansion first
2. Add merge-candidate normalization and inspection
3. Add redaction-aware export bundles and extension registration hooks
4. Finish bilingual explainability and milestone validation

## Notes

- `006` must not become a real sync transport milestone
- Redaction-aware export is more important than raw-data portability in this slice
- Future provider hooks should adapt into current runtime entities instead of redefining the runtime core
