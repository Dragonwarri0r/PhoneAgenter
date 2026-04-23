# Tasks: Workflow Graph And Automation

**Input**: Design documents from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/021-workflow-graph-and-automation/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/workflow-graph-and-automation.md  
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by build/lint checks and quickstart walkthroughs.  
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare shared wording and documentation for workflow and automation support

- [x] T001 Add English and Simplified Chinese strings for workflow definition, run state, checkpoint, automation control, and recovery wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [x] T002 Create `021` feature documentation artifacts and refresh agent context in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/021-workflow-graph-and-automation/` and `/Users/youxuezhe/StudioProjects/mobile_claw/AGENTS.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish durable workflow persistence, run-state contracts, and automation control wiring

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [x] T003 [P] Add workflow definition, step, trigger, run, and checkpoint entities plus DAO/database wiring in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/workflow/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryDatabase.kt` and bump `MemoryDatabase.version` in the same patch
- [x] T004 [P] Add workflow graph evaluation, transition, and availability contracts in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/workflow/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/`
- [x] T005 [P] Add workflow run, checkpoint, and approval-reuse orchestration wiring in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/workflow/`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/`
- [x] T006 Update DI and shared automation-entry wiring in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`

**Checkpoint**: One durable local workflow layer exists with definition, run, checkpoint, and UI-ready summary contracts

---

## Phase 3: User Story 1 - Define Reusable Multi-Step Work Without A Heavy Graph Builder (Priority: P1) 🎯 MVP

**Goal**: Let users create and inspect durable workflow definitions with entry conditions and availability state

**Independent Test**: Create a supported first-slice workflow definition and verify it persists as a durable automation object with identifiable steps and availability state

### Implementation for User Story 1

- [x] T007 [P] [US1] Add workflow definition authoring, validation, and summary contracts in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/workflow/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/`
- [x] T008 [P] [US1] Add trigger and workflow-availability validation for the first supported workflow slice in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/workflow/`
- [x] T009 [US1] Surface Automation-area summaries and workflow-definition creation or edit entry flows in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [x] T010 [US1] Persist durable workflow definitions and current availability state in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/workflow/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryDatabase.kt`

**Checkpoint**: Users can define and revisit durable local workflows without a heavy graph editor

---

## Phase 4: User Story 2 - Run, Pause, Approve, And Resume Workflow Executions (Priority: P2)

**Goal**: Make workflow runs resumable, approval-aware, and explainable during multi-step execution

**Independent Test**: Start a workflow that pauses for approval or interruption, then verify the run preserves checkpoint state and can be resumed or explained clearly

### Implementation for User Story 2

- [x] T011 [P] [US2] Add workflow run orchestration and step-transition evaluation in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/workflow/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/`
- [x] T012 [P] [US2] Add checkpoint persistence, pause or resume handling, and approval-gated run state transitions in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/workflow/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/`
- [x] T013 [US2] Surface active workflow-run status, waiting state, and next-step guidance in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [x] T014 [US2] Align workflow failure, timeout, block, cancellation, and resume-limit messaging with recent activity and audit language in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/workflow/`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`

**Checkpoint**: Workflow runs preserve explainable checkpoint and approval-aware state across interruptions

---

## Phase 5: User Story 3 - Manage Automations And Run History Through The Existing Control Surfaces (Priority: P3)

**Goal**: Support automation summaries, reversible management actions, and run history inside the existing control/detail model

**Independent Test**: Inspect workflow definitions and workflow runs from the Automation area, then verify state, recent activity, and supported actions remain understandable without a separate automation console

### Implementation for User Story 3

- [x] T015 [P] [US3] Add workflow run history and automation management summaries in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/workflow/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/`
- [x] T016 [P] [US3] Add enable/disable/pause/resume management actions for workflow definitions and runs in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/workflow/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [x] T017 [US3] Route workflow definition and workflow-run detail entries through the control-center Automation surfaces in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt`
- [x] T018 [US3] Surface traceable run outcomes, provenance, and recovery guidance in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/workflow/`

**Checkpoint**: Automation management and run history fit into the existing control/detail model

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize wording, consistency, and milestone validation

- [x] T019 [P] Refine bilingual workflow definition, run-state, checkpoint, and recovery wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [x] T020 [P] Align workflow definition, run state, checkpoint, and automation management behavior across `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [x] T021 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/021-workflow-graph-and-automation/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/021-workflow-graph-and-automation/quickstart.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on durable workflow definitions and shared run/checkpoint contracts already existing
- **User Story 3 (Phase 5)**: Depends on stable workflow identity, run outcome, and management summaries from earlier phases
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because it proves durable local workflows can exist as managed runtime objects
- **User Story 2 (P2)**: Depends on workflow definitions, triggers, and run/checkpoint persistence from earlier phases
- **User Story 3 (P3)**: Depends on stable workflow identity, run-state semantics, and management summaries

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
4. Validate that durable local workflows now exist before broadening run-state and automation management behavior

### Incremental Delivery

1. Add workflow persistence and contract models
2. Add workflow definition availability and first-slice authoring/inspection
3. Add resumable run orchestration and approval-aware checkpoints
4. Add automation summaries, run history, and recovery guidance in control/detail surfaces

## Notes

- `021` should prove local multi-step execution and resumability before a heavy graph editor or remote orchestration are considered
- Any Room-managed schema changes in `MemoryDatabase` must bump `MemoryDatabase.version` in the same patch
