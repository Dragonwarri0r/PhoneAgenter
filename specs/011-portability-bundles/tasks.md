# Tasks: Portability Bundles

**Input**: Design documents from `/specs/011-portability-bundles/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/portability-bundle-contract.md  
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by runtime walkthroughs, build/lint checks, and the quickstart flows.  
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add bilingual portability wording and package scaffolding

- [X] T001 Add English and Simplified Chinese strings for portability preview, blocked export, and share actions in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T002 Add portability bundle scaffolding in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish preview, formatting, and share contracts used by every story

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T003 [P] Implement portability preview/formatter contracts in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/PortabilityBundleFormatter.kt` and related model files in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/`
- [X] T004 [P] Implement Android share/export dispatch service in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/PortabilityBundleShareService.kt`
- [X] T005 [P] Extend workspace state with portability preview models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceUiState.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/`
- [X] T006 [P] Wire portability services into `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt` as needed

**Checkpoint**: Portability preview contracts and share service exist independently of the UI

---

## Phase 3: User Story 1 - Preview A Safe Portability Bundle (Priority: P1) 🎯 MVP

**Goal**: Let the user preview a safe portability bundle for an exportable memory record

**Independent Test**: Open the context inspector, preview an exportable record, and verify included/redacted fields are visible

### Implementation for User Story 1

- [X] T007 [P] [US1] Extend `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/ContextInspectorUiModel.kt` with export affordances and portability preview state
- [X] T008 [P] [US1] Build preview state from `ExportDecisionService` inside `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`
- [X] T009 [US1] Add a portability preview sheet in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/PortabilityBundleSheet.kt`
- [X] T010 [US1] Add export entry points to `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextInspectorSheet.kt` and present the preview from `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt`

**Checkpoint**: The user can inspect a real bundle preview before anything leaves the app

---

## Phase 4: User Story 2 - Share Or Export The Bundle Safely (Priority: P2)

**Goal**: Let the user share the formatted portability bundle only when policy allows it

**Independent Test**: Share a previewed bundle and verify Android share dispatch uses the formatted bundle text

### Implementation for User Story 2

- [X] T011 [P] [US2] Implement share actions and blocked-export feedback in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`
- [X] T012 [P] [US2] Connect Android share dispatch to the preview sheet in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/PortabilityBundleSheet.kt`
- [X] T013 [US2] Ensure formatted bundle text and mode switching stay redaction-aware in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/PortabilityBundleFormatter.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`

**Checkpoint**: Safe bundles can leave the app through Android share, while private bundles stay blocked

---

## Phase 5: User Story 3 - Understand Future Compatibility (Priority: P3)

**Goal**: Make bundle compatibility and limits understandable from the preview

**Independent Test**: Inspect a bundle preview and verify compatibility lines explain both supported and unsupported targets

### Implementation for User Story 3

- [X] T014 [P] [US3] Add compatibility preview models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/`
- [X] T015 [P] [US3] Populate compatibility and redaction explanations from `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/ExportDecisionService.kt` within `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`
- [X] T016 [US3] Refine the preview UI to show compatibility, included fields, and redacted fields clearly in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/PortabilityBundleSheet.kt`

**Checkpoint**: Bundle portability feels intentional, not opaque

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize wording, consistency, and milestone validation

- [X] T017 [P] Refine bilingual portability wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T018 [P] Align inspector/export preview state across `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [X] T019 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/011-portability-bundles/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/011-portability-bundles/quickstart.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on User Story 1 preview flow
- **User Story 3 (Phase 5)**: Depends on User Story 1 preview flow and benefits from User Story 2 formatting
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because portability must become visible before it can be shared
- **User Story 2 (P2)**: Depends on preview and formatter contracts
- **User Story 3 (P3)**: Depends on preview state and extension compatibility output

### Parallel Opportunities

- `T003`, `T004`, `T005`, and `T006` can run in parallel after setup files exist
- `T007` and `T008` can run in parallel within User Story 1
- `T011` and `T012` can run in parallel within User Story 2
- `T014` and `T015` can run in parallel within User Story 3
- `T017` and `T018` can run in parallel during polish

## Implementation Strategy

### MVP First

1. Finish Setup
2. Finish Foundational work
3. Finish User Story 1
4. Validate preview flow before adding share/export

### Incremental Delivery

1. Ship portability preview contracts
2. Add preview UI to the inspector
3. Add safe outbound share
4. Add compatibility polish

## Notes

- `011` should turn export metadata into a real product affordance rather than create a new sync system
- Portability must stay summary-safe by default
- Compatibility messaging should explain future readiness without overpromising import support
