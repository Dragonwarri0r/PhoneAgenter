# Tasks: Safe Execution Policy and Approval Flow

**Input**: Design documents from `/specs/004-safe-execution-policy/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/safe-execution-contract.md
**Tests**: No dedicated test tasks are included in this milestone plan; validation is driven by the independent story checks and quickstart flows.
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add the package boundaries, persistence scaffolding, and localization support needed for policy work

- [X] T001 Add policy and localization dependencies/configuration if needed in `/Users/youxuezhe/StudioProjects/mobile_claw/app/build.gradle.kts`
- [X] T002 Create the runtime policy package skeleton in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/` and add the shared string resolver in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`
- [X] T003 Add English and Simplified Chinese string resources for current workspace and policy surfaces in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish shared risk, policy, approval, audit, and localization contracts required by every user story

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T004 [P] Implement the core risk and policy models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/RiskAssessment.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/PolicyDecision.kt`
- [X] T005 [P] Implement approval and audit entities plus Room scaffolding in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/ApprovalRequest.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/AuditEvent.kt`, and related DAO/database files
- [X] T006 [P] Extend runtime session contracts for classifier output, policy decisions, and pending approvals in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionEvent.kt`
- [X] T007 [P] Add shared localization helpers for non-Compose runtime and ViewModel messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`
- [X] T008 Wire policy services, repositories, and localization into DI in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

**Checkpoint**: Policy, approval, audit, and localization foundations are ready for story-level implementation

---

## Phase 3: User Story 1 - Auto-Execute Low-Risk Actions (Priority: P1) 🎯 MVP

**Goal**: Let low-risk actions proceed automatically while producing policy and audit records

**Independent Test**: Submit low-risk requests and verify they execute without approval while creating visible policy and audit state

### Implementation for User Story 1

- [X] T009 [P] [US1] Implement a first-pass risk classifier and normalized action scopes in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/RiskClassifier.kt`
- [X] T010 [P] [US1] Implement the final policy engine for low-risk auto-execute flows in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/PolicyEngine.kt`
- [X] T011 [US1] Integrate risk and policy resolution into the runtime session pipeline in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`
- [X] T012 [US1] Persist risk, policy, and audit records for successful low-risk execution in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/AuditRepository.kt` and related repository files
- [X] T013 [US1] Surface localized runtime status and low-risk decision explanations in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`

**Checkpoint**: User Story 1 is independently functional as the MVP safety layer

---

## Phase 4: User Story 2 - Confirm High-Risk Actions Before Execution (Priority: P2)

**Goal**: Pause high-risk or hard-confirm actions for explicit user confirmation with a preview and explanation

**Independent Test**: Submit a confirmable request and verify approval is required before execution continues

### Implementation for User Story 2

- [X] T014 [P] [US2] Implement approval-request creation and approval-outcome persistence in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/ApprovalRepository.kt`
- [X] T015 [P] [US2] Extend the policy engine with hard-confirm rules and preview-first outcomes in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/PolicyEngine.kt`
- [X] T016 [US2] Update runtime orchestration to pause, resume, or deny based on approval outcomes in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`
- [X] T017 [US2] Build a localized approval surface and approval UI models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ApprovalSheet.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/ApprovalUiModel.kt`
- [X] T018 [US2] Wire approval request presentation and user actions into `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`

**Checkpoint**: User Story 2 works independently and safely gates confirmable actions

---

## Phase 5: User Story 3 - Enforce Hard Rules and Explain Decisions (Priority: P3)

**Goal**: Ensure blocked and hard-confirm scopes override classifier optimism while staying explainable

**Independent Test**: Submit blocked and hard-confirm requests and verify policy overrides are visible and auditable

### Implementation for User Story 3

- [X] T019 [P] [US3] Implement blocked-scope and hard-confirm rule sources in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/PolicyEngine.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/ActionScope.kt`
- [X] T020 [P] [US3] Implement localized audit-event formatting and explanation mapping in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/AuditFormatter.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`
- [X] T021 [US3] Surface denial, override, and explainability details in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/AuditUiModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/InlineFailureBanner.kt`
- [X] T022 [US3] Ensure approval, denial, and failure paths all generate audit records in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/AuditRepository.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`
- [X] T023 [US3] Align current workspace surfaces with localized English/Chinese messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values*/strings.xml`

**Checkpoint**: User Story 3 works independently and makes hard safety boundaries visible to the user

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize locale coverage, lightweight audit UX, and milestone validation notes

- [X] T024 [P] Refine success/failure feedback wording, audit summaries, and locale fallback behavior in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T025 [P] Update current workspace strings so existing `001-003` surfaces also follow device-locale English/Chinese selection in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`
- [X] T026 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/004-safe-execution-policy/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/004-safe-execution-policy/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on Foundational completion and extends the low-risk policy path
- **User Story 3 (Phase 5)**: Depends on Foundational completion and builds on the same policy/audit contracts
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP
- **User Story 2 (P2)**: Depends on the MVP policy path but remains independently verifiable
- **User Story 3 (P3)**: Depends on the same policy contracts and remains independently verifiable

### Parallel Opportunities

- `T004`, `T005`, `T006`, and `T007` can run in parallel after setup files exist
- `T009`, `T010`, and `T012` can run in parallel within User Story 1
- `T014`, `T015`, and `T017` can run in parallel within User Story 2
- `T019`, `T020`, and `T021` can run in parallel within User Story 3
- `T024` and `T025` can run in parallel during polish

## Implementation Strategy

### MVP First

1. Finish Setup
2. Finish Foundational work
3. Finish User Story 1
4. Validate low-risk auto-execution plus audit before moving on

### Incremental Delivery

1. Ship structured risk and policy resolution
2. Add approval requests and pending confirmation handling
3. Add hard-rule override visibility and lightweight audit explanations
4. Finish locale coverage and milestone validation

## Notes

- `004` should deepen the existing runtime contract instead of creating a separate safety subsystem
- Localization is a cross-cutting requirement for this feature, not an optional UI polish item
- `v0` locale support targets English and Simplified Chinese only
