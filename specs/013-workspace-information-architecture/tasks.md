# Tasks: Workspace Information Architecture

**Input**: Design documents from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/013-workspace-information-architecture/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/workspace-information-architecture-contract.md  
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by build/lint checks and quickstart walkthroughs.  
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare shared wording and UI scaffolding for the workspace IA refactor

- [X] T001 Add English and Simplified Chinese strings for workspace digest, compact entries, and progressive-disclosure wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T002 Create `013` feature documentation artifacts and refresh agent context in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/013-workspace-information-architecture/` and `/Users/youxuezhe/StudioProjects/mobile_claw/AGENTS.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish reusable UI state and component primitives for the new workspace hierarchy

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T003 [P] Introduce workspace IA UI models for attention state, digest, and secondary entries in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/`
- [X] T004 [P] Create compact digest and secondary-entry components in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [X] T005 [P] Extend workspace UI state to carry the new digest/entry presentation models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceUiState.kt`
- [X] T006 Update `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` to map runtime/model/context/governance data into the new IA models

**Checkpoint**: The workspace has the presentation primitives needed for a conversation-first layout

---

## Phase 3: User Story 1 - Keep Conversation As The Primary Workspace Surface (Priority: P1) 🎯 MVP

**Goal**: Make the transcript and composer the clear visual center in normal workspace states

**Independent Test**: Open the workspace in ready, streaming, and approval-adjacent states and verify the transcript remains the dominant surface while top chrome stays compact

### Implementation for User Story 1

- [X] T007 [P] [US1] Refactor the base layout in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt` around header + compact digest + conversation + accessory rail + composer
- [X] T008 [P] [US1] Update `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/WorkspaceHeader.kt` to use compact stable actions instead of panel-toggle driven behavior
- [X] T009 [US1] Reposition quick prompts into a lighter accessory zone by updating `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/QuickActionStrip.kt` and related screen wiring
- [X] T010 [US1] Preserve keyboard-friendly compaction behavior while removing dependency on the old expanded top stack in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt`

**Checkpoint**: Conversation is visually primary and the composer remains anchored without large permanent cards

---

## Phase 4: User Story 2 - See Key Runtime State At A Glance (Priority: P2)

**Goal**: Surface the most important execution signals in a compact stable digest

**Independent Test**: Trigger runtime paths with source/trust/route/structured-action metadata and verify the digest makes them legible without opening a deep sheet

### Implementation for User Story 2

- [X] T011 [P] [US2] Implement digest content mapping and bounded signal selection in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`
- [X] T012 [P] [US2] Render the compact digest and priority attention treatment in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/WorkspaceStatusDigest.kt`
- [X] T013 [US2] Rework `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ModelHealthCard.kt` into detail-oriented surfaces rather than default top-stack cards
- [X] T014 [US2] Align inline failure and high-priority state emphasis with the new digest hierarchy in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/InlineFailureBanner.kt` and related screen wiring

**Checkpoint**: Users can read key runtime state from a compact digest without losing conversation focus

---

## Phase 5: User Story 3 - Use Progressive Disclosure For Secondary Capability Surfaces (Priority: P3)

**Goal**: Keep deeper capability surfaces discoverable without permanently expanding them into the workspace

**Independent Test**: Discover model/context/governance/detail entry points from the compact workspace, open the deeper surfaces, and return to a calm base layout

### Implementation for User Story 3

- [X] T015 [P] [US3] Add a compact secondary-entry rail for model, context, governance, and related detail access in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/WorkspaceSecondaryEntryRow.kt`
- [X] T016 [P] [US3] Wire secondary entry actions and summaries through `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceUiState.kt`
- [X] T017 [US3] Update `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt` to use progressive disclosure for model/context/governance/detail surfaces while preserving existing sheets and dialogs
- [X] T018 [US3] Ensure preparing, unavailable, permission-warning, and empty-session states remain coherent under the new IA in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt` and related components

**Checkpoint**: Secondary capability surfaces are discoverable without default panel bloat

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize wording, consistency, and milestone validation

- [X] T019 [P] Refine bilingual workspace IA wording and labels in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T020 [P] Align the workspace components and view-model mapping across `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [X] T021 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/013-workspace-information-architecture/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/013-workspace-information-architecture/quickstart.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on User Story 1 hierarchy primitives being in place
- **User Story 3 (Phase 5)**: Depends on the new base layout and digest surfaces being established
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because conversation-first hierarchy is the defining outcome
- **User Story 2 (P2)**: Depends on the new base layout but can focus on digest content once hierarchy is stable
- **User Story 3 (P3)**: Depends on both hierarchy and digest so secondary entries feel anchored rather than floating

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
4. Validate conversation-first layout before refining digest and secondary entry behavior

### Incremental Delivery

1. Ship compact layout primitives
2. Establish the conversation-first base screen
3. Add stable glanceable digest behavior
4. Reintroduce deeper capabilities through compact progressive-disclosure entries

## Notes

- `013` should reorganize visibility, not invent new runtime semantics
- Existing sheets/dialogs remain valid as long as the base workspace stays compact
- The milestone should prefer bounded summaries over another ever-growing dashboard
