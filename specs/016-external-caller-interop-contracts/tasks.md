# Tasks: External Caller Interop Contracts

**Input**: Design documents from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/016-external-caller-interop-contracts/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/external-caller-interop-contract.md  
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by build/lint checks and quickstart walkthroughs.  
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare shared wording and documentation for external interop contracts

- [X] T001 Add English and Simplified Chinese strings for interop caller, trust, and URI grant wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T002 Create `016` feature documentation artifacts and refresh agent context in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/016-external-caller-interop-contracts/` and `/Users/youxuezhe/StudioProjects/mobile_claw/AGENTS.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the canonical interop envelope, caller identity, and grant contracts

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T003 [P] Add canonical interop envelope and compatibility models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/`
- [X] T004 [P] Add normalized caller identity and URI grant summary models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/` and related runtime packages
- [X] T005 [P] Add shared mapping from interop envelopes into canonical runtime requests in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/`
- [X] T006 Update DI and shared wiring for contract-driven external ingress in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

**Checkpoint**: External entry flows can normalize into one shared interop envelope before runtime execution

---

## Phase 3: User Story 1 - Accept External Requests Through Stable Inbound Contracts (Priority: P1) 🎯 MVP

**Goal**: Make share-text and share-media handoffs converge on one stable inbound contract family

**Independent Test**: Trigger text and media handoffs and verify both produce the same interop envelope shape before runtime execution

### Implementation for User Story 1

- [X] T007 [P] [US1] Refactor existing share-text and share-media parsing to emit the canonical interop envelope in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/`
- [X] T008 [P] [US1] Add compatibility and missing-field handling for inbound contract parsing in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/`
- [X] T009 [US1] Update runtime/session entry handling to consume the canonical interop envelope instead of parser-specific models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [X] T010 [US1] Surface stable source and interop status summaries in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and related UI models

**Checkpoint**: Covered external entry types now share one inbound contract family

---

## Phase 4: User Story 2 - Reuse Stable Caller Identity, Trust, And Grant Semantics Across Entry Types (Priority: P2)

**Goal**: Keep caller identity, trust, and grant semantics consistent regardless of entry type

**Independent Test**: Trigger multiple covered entry types and verify governance, approval, and runtime state use the same caller/trust/grant language

### Implementation for User Story 2

- [X] T011 [P] [US2] Align caller verification and governance observation with normalized caller identity in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/governance/`
- [X] T012 [P] [US2] Add URI grant explainability and lifecycle summaries in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/` and related UI models
- [X] T013 [US2] Update approval, denial, and runtime status summaries to use shared caller semantics in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [X] T014 [US2] Remove or reduce entry-specific trust wording for covered flows in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`

**Checkpoint**: Caller identity, trust, and grant semantics are stable across covered entry types

---

## Phase 5: User Story 3 - Expose A Minimal Stable Callable Surface For Future External Agents (Priority: P3)

**Goal**: Define and route a minimal future callable contract without inventing a second runtime path

**Independent Test**: Validate a structured external request shape against the callable surface descriptor and confirm it maps into canonical runtime request fields and scope semantics

### Implementation for User Story 3

- [X] T015 [P] [US3] Add callable surface descriptor models for future external callers in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/` or a shared interop package
- [X] T016 [P] [US3] Add validation and compatibility mapping from callable requests into the canonical interop envelope in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/`
- [X] T017 [US3] Align requested scope handling for future callable requests with current governance/policy semantics in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/` and related files
- [X] T018 [US3] Surface callable contract compatibility state or denial messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and related UI files

**Checkpoint**: A future external caller contract can be represented and evaluated without a new runtime path

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize wording, consistency, and milestone validation

- [X] T019 [P] Refine bilingual interop wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T020 [P] Align interop envelope, caller trust, URI grant, and governance behavior across `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [X] T021 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/016-external-caller-interop-contracts/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/016-external-caller-interop-contracts/quickstart.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on the normalized caller/interop models established in Foundational work
- **User Story 3 (Phase 5)**: Depends on the canonical interop envelope from Foundational work and benefits from User Story 2 trust alignment
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because stable inbound normalization is the first visible result
- **User Story 2 (P2)**: Depends on shared caller identity and grant models already existing
- **User Story 3 (P3)**: Depends on the interop envelope being stable enough to host future callable requests

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
4. Validate that multiple inbound entry types share one interop envelope before widening the callable surface

### Incremental Delivery

1. Add interop envelope and caller/grant contracts
2. Normalize current inbound share flows into that contract
3. Align governance and trust semantics
4. Add the minimal future callable contract

## Notes

- `016` should stabilize external interop contracts, not become the broader extension system
- Covered flows should reuse the current runtime ingress backbone rather than introducing a new orchestration path
