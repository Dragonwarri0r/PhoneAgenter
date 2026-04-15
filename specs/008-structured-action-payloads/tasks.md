# Tasks: Structured Action Payloads

**Input**: Design documents from `/specs/008-structured-action-payloads/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/structured-action-contract.md
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by runtime walkthroughs, build/lint checks, and the quickstart flows.
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add localization and package boundaries for structured execution contracts

- [X] T001 Add English and Simplified Chinese strings for structured payload preview, completeness state, and extraction warnings in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T002 Update `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt` with localization helpers for structured action type labels and completeness state labels
- [X] T003 Add structured action package scaffolding in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/action/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the shared structured payload contracts used by every story

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T004 [P] Implement shared action contracts in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/action/StructuredActionType.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/action/PayloadCompletenessState.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/action/PayloadFieldEvidence.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/action/StructuredActionPayload.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/action/ActionNormalizationResult.kt`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/action/StructuredExecutionPreview.kt`
- [X] T005 [P] Implement the structured action normalization service in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/action/StructuredActionNormalizer.kt`
- [X] T006 [P] Extend `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimePlan.kt`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionEvent.kt` with structured payload and preview metadata
- [X] T007 [P] Wire the structured action normalizer into `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

**Checkpoint**: Structured payload contracts and normalization services exist and can be consumed by story-level work

---

## Phase 3: User Story 1 - Preview a Structured Message Before Sending (Priority: P1) 🎯 MVP

**Goal**: Produce a structured message draft before message execution continues

**Independent Test**: Submit a `message.send` request and verify the runtime shows structured recipient/content fields before execution continues

### Implementation for User Story 1

- [X] T008 [P] [US1] Add structured normalization rules for `message.send` in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/action/StructuredActionNormalizer.kt`
- [X] T009 [P] [US1] Feed structured message payloads into runtime orchestration and gating in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/PolicyEngine.kt`
- [X] T010 [US1] Surface structured message preview fields in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/RuntimeStatusUiModel.kt`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ApprovalSheet.kt`
- [X] T011 [US1] Update `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/provider/AndroidIntentCapabilityProvider.kt` to use structured message fields for execution

**Checkpoint**: User Story 1 is independently functional and proves message execution no longer depends on raw prompt passthrough alone

---

## Phase 4: User Story 2 - Use Structured Event Fields for Calendar Writes (Priority: P2)

**Goal**: Produce structured calendar fields for preview and downstream execution

**Independent Test**: Submit a `calendar.write` request and verify the runtime shows structured event fields and handles incomplete scheduling data safely

### Implementation for User Story 2

- [X] T012 [P] [US2] Add structured normalization rules for `calendar.write` in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/action/StructuredActionNormalizer.kt`
- [X] T013 [P] [US2] Use structured calendar completeness state in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/RiskClassifier.kt`
- [X] T014 [US2] Surface structured calendar preview fields and warnings in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`
- [X] T015 [US2] Update `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/provider/AndroidIntentCapabilityProvider.kt` to use structured calendar fields for execution

**Checkpoint**: User Story 2 works independently and proves calendar writes are now structured and safer

---

## Phase 5: User Story 3 - Share Structured Output Instead of Raw Prompt Text (Priority: P3)

**Goal**: Use a structured share payload for preview and downstream share execution

**Independent Test**: Submit an `external.share` request and verify the runtime shows outbound share content and uses it for provider execution

### Implementation for User Story 3

- [X] T016 [P] [US3] Add structured normalization rules for `external.share` in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/action/StructuredActionNormalizer.kt`
- [X] T017 [P] [US3] Record structured share preview and normalization outcomes in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/AuditRepository.kt`
- [X] T018 [US3] Surface structured share preview fields in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`
- [X] T019 [US3] Update `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/provider/AndroidIntentCapabilityProvider.kt` to use structured share content for execution

**Checkpoint**: User Story 3 works independently and proves outbound share execution uses a normalized payload

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize explainability wording, runtime consistency, and milestone validation

- [X] T020 [P] Refine bilingual structured payload wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T021 [P] Align runtime status, approval, and failure surfaces with structured payload messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`
- [X] T022 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/008-structured-action-payloads/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/008-structured-action-payloads/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on Foundational completion and reuses the same normalization contracts
- **User Story 3 (Phase 5)**: Depends on Foundational completion and reuses the same normalization contracts
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because it proves the value of structured execution most clearly
- **User Story 2 (P2)**: Depends on the shared normalization layer and remains independently testable
- **User Story 3 (P3)**: Depends on the same shared layer and remains independently testable

### Parallel Opportunities

- `T004`, `T005`, and `T006` can run in parallel after setup files exist
- `T008` and `T009` can run in parallel within User Story 1
- `T012` and `T013` can run in parallel within User Story 2
- `T016` and `T017` can run in parallel within User Story 3
- `T020` and `T021` can run in parallel during polish

## Implementation Strategy

### MVP First

1. Finish Setup
2. Finish Foundational work
3. Finish User Story 1
4. Validate structured message execution before moving on

### Incremental Delivery

1. Ship shared structured action contracts and normalization
2. Add structured message execution
3. Add structured calendar execution
4. Add structured share execution and polish explainability

## Notes

- `008` should improve execution reliability for a small set of high-value actions before broader tool standardization
- Completeness state is part of the safety contract, not just a UI hint
- Providers for supported action types should stop reconstructing their main payload only from raw request text
