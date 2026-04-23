# Tasks: Runtime Hooks And Context Sources

**Input**: Design documents from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/019-runtime-hooks-and-context-sources/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/runtime-hooks-and-context-sources.md  
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by build/lint checks and quickstart walkthroughs.  
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare shared wording and documentation for the runtime contribution surface

- [x] T001 Add English and Simplified Chinese strings for runtime contribution, lifecycle point, availability, and limitation wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [x] T002 Create `019` feature documentation artifacts and refresh agent context in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/019-runtime-hooks-and-context-sources/` and `/Users/youxuezhe/StudioProjects/mobile_claw/AGENTS.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish shared runtime contribution contracts and evaluation wiring

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [x] T003 [P] Add runtime contribution registration, lifecycle point, eligibility, and availability models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/contribution/`
- [x] T004 [P] Add request-time context contribution and contribution outcome models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/contribution/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/`
- [x] T005 [P] Add shared contributor registry, discovery, and adaptation contracts for extension/system-source/memory families in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/contribution/`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/extension/`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/systemsource/`
- [x] T006 Update DI and runtime session wiring for contribution evaluation and summaries in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/`

**Checkpoint**: One shared runtime contribution surface exists for lifecycle hooks and request-time context contributors

---

## Phase 3: User Story 1 - Register Lifecycle Contributors Through One Runtime Language (Priority: P1) 🎯 MVP

**Goal**: Represent lifecycle-oriented and context-oriented contributors through one shared runtime contribution contract

**Independent Test**: Register at least one lifecycle-oriented contributor and one context-oriented contributor and verify both fit through the same registration, eligibility, and outcome model

### Implementation for User Story 1

- [x] T007 [P] [US1] Normalize covered context-oriented contributors such as memory/system-source inputs into runtime contribution registrations in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/systemsource/`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/contribution/`
- [x] T008 [P] [US1] Add lifecycle-oriented contributor registrations for covered policy/approval/extension-aware flows in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/extension/`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/contribution/`
- [x] T009 [US1] Add lifecycle-point filtering and contribution application into the runtime path in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/contribution/`
- [x] T010 [US1] Preserve backward compatibility between existing extension/system-source behavior and the new contribution language in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/extension/`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/systemsource/`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/`

**Checkpoint**: Covered lifecycle and context contributors now share one runtime contribution language

---

## Phase 4: User Story 2 - See Hook And Context Contributions Inside The Current Task Flow (Priority: P2)

**Goal**: Make covered contributor effects visible and explainable in the active task flow without creating a second task workspace

**Independent Test**: Trigger requests with applied, skipped, and blocked contributors and verify the active task flow exposes concise summaries plus limitation messaging

### Implementation for User Story 2

- [x] T011 [P] [US2] Add contribution summary mapping from request/session state into workspace view-models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [x] T012 [P] [US2] Extend runtime control and task-level UI models for contribution summaries, provenance, and blocked/degraded states in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [x] T013 [US2] Render current-task contribution summaries and limitation messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [x] T014 [US2] Align audit and trace wording for applied, skipped, degraded, and blocked contributors in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`

**Checkpoint**: Users can understand contributor effects from the active task flow and deeper control surfaces

---

## Phase 5: User Story 3 - Manage Compatible Contributors Without Turning This Slice Into Corpus Or Workflow Management (Priority: P3)

**Goal**: Support reversible contributor availability management and clear limitation messaging for covered contributor families

**Independent Test**: Inspect multiple managed contributors, adjust supported availability state, and verify unsupported contributors explain why they cannot be changed

### Implementation for User Story 3

- [x] T015 [P] [US3] Add contributor availability-state handling and reversible management actions in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/contribution/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/extension/`
- [x] T016 [P] [US3] Add managed contributor entry routing and summary models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/`
- [x] T017 [US3] Surface supported enable/disable/inspect states and unavailable reasons for contributors in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [x] T018 [US3] Preserve scope, trust, privacy, and policy explanations for managed contributors across runtime and governance surfaces in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/contribution/`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/governance/`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`

**Checkpoint**: Covered contributors can be inspected and reversibly managed without collapsing into knowledge or workflow administration

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize wording, consistency, and milestone validation

- [x] T019 [P] Refine bilingual runtime contribution, lifecycle, and limitation wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [x] T020 [P] Align contribution registration, evaluation, trace visibility, and reversible management behavior across `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [x] T021 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/019-runtime-hooks-and-context-sources/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/019-runtime-hooks-and-context-sources/quickstart.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on shared contribution contracts and outcome records from Foundational work and benefits from US1 contributor normalization
- **User Story 3 (Phase 5)**: Depends on stable contributor identity and visibility language from earlier phases
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because it proves lifecycle hooks and context contributors now share one runtime language
- **User Story 2 (P2)**: Depends on contribution registration and outcome models already existing
- **User Story 3 (P3)**: Depends on stable contribution identity, visibility, and limitation semantics

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
4. Validate that lifecycle-oriented and context-oriented contributors now share one runtime language before broadening UI and management behavior

### Incremental Delivery

1. Add runtime contribution contracts and registry/evaluation wiring
2. Normalize covered contributor families into the shared surface
3. Add current-task and control-surface visibility for contributor effects
4. Add reversible contributor-management actions and limitation messaging

## Notes

- `019` should stabilize runtime contribution contracts, not absorb durable knowledge corpus management or workflow execution
- Current-task summaries should stay concise and should complement the control center rather than duplicating it
