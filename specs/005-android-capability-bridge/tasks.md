# Tasks: Android Capability Bridge

**Input**: Design documents from `/specs/005-android-capability-bridge/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/capability-bridge-contract.md
**Tests**: No dedicated test tasks are included in this milestone plan; validation is driven by runtime walkthroughs, build/lint checks, and the quickstart flows.
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add capability-bridge package boundaries and shared strings needed for Android routing

- [X] T001 Add capability bridge package scaffolding in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/`
- [X] T002 Add English and Simplified Chinese strings for routing, provider type, caller trust, and fallback messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T003 Update `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt` with capability bridge localization helpers

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish normalized capability, provider, caller, and routing contracts required by every user story

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T004 [P] Implement normalized capability bridge models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRegistration.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/ProviderDescriptor.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CallerIdentity.kt`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/InvocationResult.kt`
- [X] T005 [P] Implement a seeded AppFunctions-first bridge abstraction and fallback bridge interfaces in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/AppFunctionBridge.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/IntentFallbackBridge.kt`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/ShareFallbackBridge.kt`
- [X] T006 [P] Implement the capability registry and caller verifier in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRegistry.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CallerVerifier.kt`
- [X] T007 [P] Extend runtime request and event contracts with caller identity and route explanation data in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionEvent.kt`
- [X] T008 Wire capability bridge services into `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

**Checkpoint**: Capability bridge contracts, routing inputs, and caller verification are ready for story-level implementation

---

## Phase 3: User Story 1 - Use AppFunctions as the Primary Capability Path (Priority: P1) 🎯 MVP

**Goal**: Route supported capabilities through a normalized AppFunctions-first bridge

**Independent Test**: Register at least one AppFunctions-style capability and verify that the runtime discovers, selects, and executes it through the common bridge

### Implementation for User Story 1

- [X] T009 [P] [US1] Implement ordered provider selection and route explanation in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRouter.kt`
- [X] T010 [P] [US1] Seed at least one AppFunctions-style capability registration and provider descriptor in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRegistry.kt`
- [X] T011 [US1] Integrate capability routing into `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/provider/CapabilityProviderRegistry.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`
- [X] T012 [US1] Surface localized route explanations and selected provider details in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`

**Checkpoint**: User Story 1 is independently functional as the MVP Android capability bridge

---

## Phase 4: User Story 2 - Fallback When the Primary Bridge Is Unavailable (Priority: P2)

**Goal**: Use the next approved Android bridge path when AppFunctions is unavailable

**Independent Test**: Disable the primary route for a capability and verify fallback routing or normalized no-provider failure

### Implementation for User Story 2

- [X] T013 [P] [US2] Implement fallback provider descriptors and availability states in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/ProviderDescriptor.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRegistry.kt`
- [X] T014 [P] [US2] Extend the router with ordered fallback selection across AppFunctions, Intent, and Share in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRouter.kt`
- [X] T015 [US2] Normalize no-provider and fallback invocation outcomes in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/provider/CapabilityProviderRegistry.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`
- [X] T016 [US2] Surface fallback and no-provider messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/InlineFailureBanner.kt`

**Checkpoint**: User Story 2 works independently and exposes clean fallback behavior

---

## Phase 5: User Story 3 - Enforce Provider and Caller Trust Boundaries (Priority: P3)

**Goal**: Deny restricted routing when caller trust verification fails

**Independent Test**: Compare trusted and untrusted caller requests and verify only trusted flows reach restricted capability routing

### Implementation for User Story 3

- [X] T017 [P] [US3] Implement caller trust rules and restricted-capability verification in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CallerVerifier.kt`
- [X] T018 [P] [US3] Extend runtime requests and planner input to carry normalized caller metadata in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt`
- [X] T019 [US3] Enforce caller verification before restricted routing in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`
- [X] T020 [US3] Surface localized caller trust denial and route trust messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`

**Checkpoint**: User Story 3 works independently and enforces trusted Android bridge participation

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize route explanations, documentation sync, and milestone validation

- [X] T021 [P] Refine bilingual routing, provider-type, and trust wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T022 [P] Align current workspace and runtime surfaces with capability bridge messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`
- [X] T023 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/005-android-capability-bridge/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/005-android-capability-bridge/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on Foundational completion and extends the same routing layer
- **User Story 3 (Phase 5)**: Depends on Foundational completion and reuses the same registry and router contracts
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP
- **User Story 2 (P2)**: Depends on the registry and router path from US1 but remains independently verifiable
- **User Story 3 (P3)**: Depends on the same contracts and remains independently verifiable

### Parallel Opportunities

- `T004`, `T005`, `T006`, and `T007` can run in parallel after setup files exist
- `T009`, `T010`, and `T012` can run in parallel within User Story 1
- `T013`, `T014`, and `T016` can run in parallel within User Story 2
- `T017` and `T018` can run in parallel within User Story 3
- `T021` and `T022` can run in parallel during polish

## Implementation Strategy

### MVP First

1. Finish Setup
2. Finish Foundational work
3. Finish User Story 1
4. Validate AppFunctions-first routing before moving on

### Incremental Delivery

1. Ship normalized capability registry and route selection
2. Add ordered fallback behavior
3. Add caller trust enforcement
4. Finish bilingual route explanation and milestone validation

## Notes

- `005` should extend the current runtime/provider contract instead of bypassing it
- AppFunctions remains the preferred bridge path even if the first implementation uses a seeded adapter boundary on SDK 35
- Accessibility stays reserved but non-executable in this milestone
