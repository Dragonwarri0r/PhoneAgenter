# Tasks: Runtime Control Center

**Input**: Design documents from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/018-runtime-control-center/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/runtime-control-center-contract.md  
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by build/lint checks and quickstart walkthroughs.  
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare shared wording and design artifacts for the runtime control center

- [x] T001 Add English and Simplified Chinese strings for runtime control-center sections, artifact states, and trace wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [x] T002 Create `018` feature documentation artifacts and refresh agent context in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/018-runtime-control-center/` and `/Users/youxuezhe/StudioProjects/mobile_claw/AGENTS.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish shared control-center models and entry contracts

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [x] T003 [P] Add runtime control-center state, trace, and managed-artifact models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/`
- [x] T004 [P] Add shared view-model mapping helpers for runtime trace and managed artifact entries in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [x] T005 [P] Add control-center component skeletons and section contracts in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [x] T006 Update workspace state wiring for a primary runtime control-center entry in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`

**Checkpoint**: One shared control-center model exists for trace + artifact entry

---

## Phase 3: User Story 1 - Use Chat As The Primary Control Entry (Priority: P1) 🎯 MVP

**Goal**: Keep chat primary while introducing one coherent control-center entry

**Independent Test**: Open text and media-backed sessions and verify deeper control views are reachable from the conversation flow without replacing chat

### Implementation for User Story 1

- [x] T007 [P] [US1] Add the primary runtime control-center sheet and entry actions in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [x] T008 [P] [US1] Update workspace digest and secondary entry routing to open the control center in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [x] T009 [US1] Consolidate current workspace detail entry to route through the new control center in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt`
- [x] T010 [US1] Preserve conversation-first behavior and sheet transitions during active chat in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`

**Checkpoint**: The runtime control center is reachable from the active chat without displacing conversation

---

## Phase 4: User Story 2 - Read A Coherent Runtime Trace (Priority: P2)

**Goal**: Present one readable trace for source, tool path, approvals, context, and extension involvement

**Independent Test**: Run normal, approval-gated, degraded, and external-source requests and verify one coherent trace covers the major contributors and constraints

### Implementation for User Story 2

- [x] T011 [P] [US2] Build runtime trace snapshot mapping from session status, audit, context, approval, and extension data in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [x] T012 [P] [US2] Render trace sections for source, tool path, approvals, context, extensions, and constraints in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [x] T013 [US2] Replace fragmented runtime-detail wording with coherent trace wording for covered flows in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [x] T014 [US2] Surface degraded, denied, or unavailable states with next-step explanations inside the control center in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`

**Checkpoint**: Users can read a coherent runtime trace instead of reconstructing it from separate sheets

---

## Phase 5: User Story 3 - Inspect And Edit Supported Managed Artifacts In One Place (Priority: P3)

**Goal**: Make supported managed artifact families inspectable and actionable through one coherent control-center surface

**Independent Test**: Open supported memory, governance, and extension artifact entries from the control center and verify supported edits persist while unavailable states are explained clearly

### Implementation for User Story 3

- [x] T015 [P] [US3] Add managed-artifact entries for memory, governance, approval, and extension families in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [x] T016 [P] [US3] Route artifact entries into existing supported editors or detail flows from the control center in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt`
- [x] T017 [US3] Add editability/unavailability messaging for supported artifact entries in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [x] T018 [US3] Preserve persisted changes and revisitability for covered artifact families in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`

**Checkpoint**: Supported managed artifacts are inspectable and actionable from one coherent in-app control surface

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize localized wording, consistency, and milestone validation

- [x] T019 [P] Refine bilingual runtime-control wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [x] T020 [P] Align control-center trace, artifact entry, and conversation-preserving behavior across `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [x] T021 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/018-runtime-control-center/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/018-runtime-control-center/quickstart.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on the control-center shell from User Story 1 and foundational trace models
- **User Story 3 (Phase 5)**: Depends on the control-center shell and benefits from the trace grouping established in User Story 2
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because it proves the app can become one control surface without losing chat-first UX
- **User Story 2 (P2)**: Depends on shared control-center models already existing
- **User Story 3 (P3)**: Depends on stable artifact-entry language and control-center routing

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
4. Validate that the app has one coherent control-center entry before broadening trace and artifact editing

### Incremental Delivery

1. Add control-center state and section contracts
2. Add the unified control-center shell
3. Add coherent runtime trace
4. Add managed artifact routing and editability messaging

## Notes

- `018` should consolidate existing runtime surfaces into one product-level control experience, not create another disconnected admin sheet
- Existing supported editors may be reused, but their entry language should come from the control center
