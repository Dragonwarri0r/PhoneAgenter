# Tasks: Permission Governance Center

**Input**: Design documents from `/specs/009-permission-governance-center/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/governance-center-contract.md
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by runtime walkthroughs, build/lint checks, and the quickstart flows.
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add governance strings and package boundaries

- [X] T001 Add English and Simplified Chinese strings for governance center labels, trust modes, and scope grant states in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T002 Update `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt` with localization helpers for governance trust and scope grant labels
- [X] T003 Add governance package scaffolding in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/governance/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the shared governance data and repository layer used by every story

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T004 [P] Implement governance entities and enums in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/governance/CallerGovernanceRecord.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/governance/ScopeGrantRecord.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/governance/GovernanceTrustMode.kt`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/governance/GovernanceGrantState.kt`
- [X] T005 [P] Extend `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryDatabase.kt` with governance tables, converters, and DAO accessors
- [X] T006 [P] Implement governance DAO and repository in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/governance/GovernanceDao.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/governance/GovernanceRepository.kt`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/governance/DefaultGovernanceRepository.kt`
- [X] T007 [P] Wire governance repository into `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

**Checkpoint**: Governance state can persist locally and be queried independently of the UI

---

## Phase 3: User Story 1 - Review Caller Trust And Recent Approvals (Priority: P1) 🎯 MVP

**Goal**: Make governance visible through a workspace-embedded governance center

**Independent Test**: Open the governance center after several runtime requests and verify recent callers, trust states, and recent approval/denial activity appear

### Implementation for User Story 1

- [X] T008 [P] [US1] Add governance center UI models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/GovernanceCenterUiModel.kt`
- [X] T009 [P] [US1] Add a governance center sheet in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/GovernanceCenterSheet.kt`
- [X] T010 [US1] Feed governance center state from repository and recent audit/approval data in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`
- [X] T011 [US1] Expose the governance center from the workspace shell in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt`

**Checkpoint**: Governance center is visible and shows recent caller trust and recent governance activity

---

## Phase 4: User Story 2 - Adjust Caller Trust And Scope Grants (Priority: P2)

**Goal**: Let users proactively change trust and scope access

**Independent Test**: Modify trust and scope grants for a caller and verify the updated values persist and re-render in the governance center

### Implementation for User Story 2

- [X] T012 [P] [US2] Add governance repository mutations for trust mode and scope grants in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/governance/DefaultGovernanceRepository.kt`
- [X] T013 [P] [US2] Add governance actions and state updates in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`
- [X] T014 [US2] Add editable trust and scope grant controls in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/GovernanceCenterSheet.kt`

**Checkpoint**: Users can modify caller trust and scope grants from the governance center

---

## Phase 5: User Story 3 - Enforce Governance Overrides During Runtime Routing (Priority: P3)

**Goal**: Make governance behavior-changing rather than informational only

**Independent Test**: Configure a denial or restricted scope and verify a later runtime request is denied or downgraded because of governance

### Implementation for User Story 3

- [X] T015 [P] [US3] Integrate governance override resolution into `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CallerVerifier.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRouter.kt`
- [X] T016 [P] [US3] Record governance-aware caller observations and explanations in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/governance/DefaultGovernanceRepository.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRouter.kt`
- [X] T017 [US3] Surface governance-derived denial or restriction messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/RuntimeStatusUiModel.kt`

**Checkpoint**: Governance overrides affect real runtime behavior

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize wording, consistency, and validation

- [X] T018 [P] Refine bilingual governance wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T019 [P] Align governance center, runtime status, and approval/audit wording across `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`
- [X] T020 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/009-permission-governance-center/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/009-permission-governance-center/quickstart.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on Foundational completion and reuses the same governance repository
- **User Story 3 (Phase 5)**: Depends on Foundational completion and benefits from the observation UI created in User Story 1
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because visible governance is the first product win
- **User Story 2 (P2)**: Depends on shared governance storage and UI state
- **User Story 3 (P3)**: Depends on stored governance records and should land after editing exists

### Parallel Opportunities

- `T004`, `T005`, and `T006` can run in parallel after setup files exist
- `T008` and `T009` can run in parallel within User Story 1
- `T012` and `T013` can run in parallel within User Story 2
- `T015` and `T016` can run in parallel within User Story 3
- `T018` and `T019` can run in parallel during polish

## Implementation Strategy

### MVP First

1. Finish Setup
2. Finish Foundational work
3. Finish User Story 1
4. Validate visible governance before moving to editing and enforcement

### Incremental Delivery

1. Ship governance data contracts and repository
2. Add review-only governance center
3. Add caller trust and scope editing
4. Add runtime enforcement and polish explainability

## Notes

- `009` should make governance visible and user-controlled without becoming a larger settings/navigation refactor
- The governance center should remain bounded and recent rather than trying to expose the entire raw history table
- Stored governance must influence runtime behavior or the milestone is incomplete
