# Tasks: Trusted External Handoff Entry

**Input**: Design documents from `/specs/007-external-runtime-entry/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/external-runtime-entry-contract.md
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by Android share/intent walkthroughs, build/lint checks, and the quickstart flows.
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add the shared localization and ingress package boundaries needed for the first external handoff path

- [X] T001 Add English and Simplified Chinese strings for external handoff entry, source attribution, trust outcome, and malformed handoff messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T002 Update `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt` with localization helpers for external source labels, trust-state labels, and inbound handoff failure messaging
- [X] T003 Add ingress package scaffolding for the new external handoff models and services in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the normalized ingress contracts and coordinator used by every user story

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T004 [P] Implement normalized ingress data contracts in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/ExternalEntryRegistration.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/ExternalHandoffPayload.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/CallerIngressMetadata.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/InboundRuntimeRequest.kt`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/ExternalInvocationRecord.kt`
- [X] T005 [P] Implement external handoff parsing and request normalization services in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/ExternalHandoffParser.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/ExternalRuntimeRequestMapper.kt`
- [X] T006 [P] Implement one-shot external handoff coordination in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/ExternalHandoffCoordinator.kt`
- [X] T007 [P] Extend `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionEvent.kt` with normalized external source and handoff metadata
- [X] T008 Wire ingress services into `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

**Checkpoint**: Ingress contracts, mapping services, and coordinator are ready for story-level implementation

---

## Phase 3: User Story 1 - Send Content to Mobile Claw From Another App (Priority: P1) 🎯 MVP

**Goal**: Accept supported text handoffs from another Android app and land them as visible runtime sessions inside the workspace

**Independent Test**: Share supported plain text to Mobile Claw and verify that the app opens a visible new or resumed session containing the incoming content and source attribution

### Implementation for User Story 1

- [X] T009 [P] [US1] Add the exported `ACTION_SEND` `text/plain` activity entry in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/AndroidManifest.xml`
- [X] T010 [P] [US1] Handle `onCreate()` and `onNewIntent()` external handoff capture in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/MainActivity.kt`
- [X] T011 [US1] Consume normalized inbound handoffs and start visible external sessions in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`
- [X] T012 [US1] Extend workspace state for external handoff landing in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceUiState.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/RuntimeStatusUiModel.kt`
- [X] T013 [US1] Surface incoming source attribution and external-session visibility in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt`

**Checkpoint**: User Story 1 is independently functional and proves Mobile Claw can receive a real external handoff

---

## Phase 4: User Story 2 - Normalize External Handoffs Before Execution (Priority: P2)

**Goal**: Ensure external handoffs converge on the same canonical runtime request contract used by the workspace and fail safely when malformed

**Independent Test**: Compare an internal workspace request and an external handoff and verify both reach the runtime as the same canonical request shape, while malformed handoffs stop before execution

### Implementation for User Story 2

- [X] T014 [P] [US2] Finalize canonical external-to-runtime mapping in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/ExternalRuntimeRequestMapper.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt`
- [X] T015 [P] [US2] Keep Android entry details out of downstream orchestration by consuming only normalized request data in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`
- [X] T016 [US2] Add safe malformed and unsupported handoff rejection paths in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/ExternalHandoffParser.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/MainActivity.kt`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`
- [X] T017 [US2] Record normalized external invocation linkage and audit-ready source details in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/ExternalInvocationRecord.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/AuditRepository.kt`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`

**Checkpoint**: User Story 2 works independently and proves external handoffs strengthen the runtime contract instead of forking it

---

## Phase 5: User Story 3 - Understand Source and Trust Outcome (Priority: P3)

**Goal**: Show which app handed off content, how trust was resolved, and why the runtime accepted or denied the request

**Independent Test**: Compare accepted and unverified or denied external handoffs and verify source/trust details remain visible in runtime status and audit surfaces

### Implementation for User Story 3

- [X] T018 [P] [US3] Extend caller verification to consume normalized ingress metadata and best-effort package or referrer hints in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CallerVerifier.kt`
- [X] T019 [P] [US3] Emit source and trust session events for accepted and denied external handoffs in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionEvent.kt`
- [X] T020 [US3] Expose localized source label, trust state, and denial reason in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/RuntimeStatusUiModel.kt`
- [X] T021 [US3] Surface source and trust explanations in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/InlineFailureBanner.kt`

**Checkpoint**: User Story 3 works independently and makes source/trust outcome part of the product experience

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize bilingual wording, UX consistency, and milestone validation

- [X] T022 [P] Refine bilingual external handoff, source, and trust wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T023 [P] Align current activity, workspace, and runtime surfaces with external handoff messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/MainActivity.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`
- [X] T024 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/007-external-runtime-entry/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/007-external-runtime-entry/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on Foundational completion and builds on the same ingress contract used by US1
- **User Story 3 (Phase 5)**: Depends on Foundational completion and reuses the same ingress and runtime metadata path
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because it proves the first real external handoff path
- **User Story 2 (P2)**: Depends on the normalized ingress foundation and strengthens contract parity with the internal workspace flow
- **User Story 3 (P3)**: Depends on the same normalized metadata path and makes source/trust outcome visible to the user

### Parallel Opportunities

- `T004`, `T005`, `T006`, and `T007` can run in parallel after setup files exist
- `T009` and `T010` can run in parallel within User Story 1
- `T014` and `T015` can run in parallel within User Story 2
- `T018` and `T019` can run in parallel within User Story 3
- `T022` and `T023` can run in parallel during polish

## Implementation Strategy

### MVP First

1. Finish Setup
2. Finish Foundational work
3. Finish User Story 1
4. Validate that Mobile Claw can receive a real Android text handoff as a visible session

### Incremental Delivery

1. Ship the first external activity entry and visible landing flow
2. Tighten canonical normalization and malformed-input rejection
3. Add source/trust explainability across runtime and audit surfaces
4. Finish bilingual wording and milestone validation

## Notes

- `007` should add exactly one productized external handoff path before any richer payload or multi-entry work
- Caller identity is intentionally best-effort at ingress and must degrade safely instead of silently widening trust
- The runtime core should continue to consume canonical request data, not raw Android intents
