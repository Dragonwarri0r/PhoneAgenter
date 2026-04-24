# Tasks: Mobile Claw Hub Interop Host Implementation

**Input**: Design documents from `/specs/025-mobileclaw-interop-host/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), `024` shared contract modules

**Tests**: Focused host-runtime unit coverage and compile validation are included because this feature introduces a governed public boundary and authorization behavior.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g. `US1`, `US2`, `US3`)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare Mobile Claw to consume the shared contract and expose a real host boundary

- [X] T001 Add shared interop-module dependencies and provider manifest wiring in `app/build.gradle.kts` and `app/src/main/AndroidManifest.xml`
- [X] T002 Create the host interop package skeleton in `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropProvider.kt`, `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropMethodDispatcher.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/interop/HubSurfaceDescriptorAssembler.kt`
- [X] T003 [P] Add localized host interop strings and status copy in `app/src/main/res/values/strings.xml` and `app/src/main/res/values-zh/strings.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build the governed host boundary that all public discovery, authorization, and task flows will use

**⚠️ CRITICAL**: No user story work should begin until this phase is complete

- [X] T004 Implement shared-contract-aware descriptor assembly and compatibility services in `app/src/main/java/com/mobileclaw/app/runtime/interop/HubSurfaceDescriptorAssembler.kt` and `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropCompatibilityService.kt`
- [X] T005 [P] Implement baseline `ContentProvider` transport and Bundle adapters in `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropProvider.kt`, `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropMethodDispatcher.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropBundleAdapters.kt`
- [X] T006 [P] Implement governed caller and request-context mapping in `app/src/main/java/com/mobileclaw/app/runtime/interop/InteropCallerContext.kt`, `app/src/main/java/com/mobileclaw/app/runtime/interop/InteropRequestContext.kt`, `app/src/main/java/com/mobileclaw/app/runtime/ingress/ExternalRuntimeRequestMapper.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/capability/CallerIdentity.kt`
- [X] T007 Align AppFunctions exposure and capability routing with shared contract identifiers in `app/src/main/java/com/mobileclaw/app/runtime/appfunctions/MobileClawAppFunctions.kt`, `app/src/main/java/com/mobileclaw/app/runtime/appfunctions/AppFunctionExposureCatalog.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/capability/AppFunctionBridge.kt`
- [X] T008 Wire DI and runtime registry support for interop host services in `app/src/main/java/com/mobileclaw/app/di/AppModule.kt`, `app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRegistry.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionFacade.kt`

**Checkpoint**: Mobile Claw now has one shared-contract-aware host boundary that can route public requests into the governed runtime

---

## Phase 3: User Story 1 - Discover And Invoke Mobile Claw Through The Shared Public Contract (Priority: P1) 🎯 MVP

**Goal**: Expose one real discovery surface and one governed capability invocation path through the shared public contract

**Independent Test**: Use only the shared contract to discover Mobile Claw, inspect one callable capability, and submit one governed invocation without relying on share heuristics

- [X] T009 [US1] Implement public discovery and capability summary responses in `app/src/main/java/com/mobileclaw/app/runtime/interop/HubDiscoveryService.kt`, `app/src/main/java/com/mobileclaw/app/runtime/interop/HubSurfaceDescriptorAssembler.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropProvider.kt`
- [X] T010 [US1] Implement governed capability invocation handoff in `app/src/main/java/com/mobileclaw/app/runtime/interop/HubCapabilityInvocationService.kt`, `app/src/main/java/com/mobileclaw/app/runtime/ingress/CallableInteropMapper.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`
- [X] T011 [US1] Keep Android share as a compatibility ingress while separating governed interop in `app/src/main/java/com/mobileclaw/app/runtime/ingress/ExternalHandoffParser.kt`, `app/src/main/java/com/mobileclaw/app/runtime/ingress/ExternalHandoffCoordinator.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/capability/ShareFallbackBridge.kt`
- [X] T012 [P] [US1] Add discovery and invocation coverage in `app/src/test/java/com/mobileclaw/app/runtime/interop/HubInteropProviderDiscoveryTest.kt` and `app/src/test/java/com/mobileclaw/app/runtime/interop/HubCapabilityInvocationServiceTest.kt`

**Checkpoint**: External callers can discover Mobile Claw and submit one governed request through the shared contract

---

## Phase 4: User Story 2 - Govern Authorization, Grant, And Connected-App Behavior (Priority: P1)

**Goal**: Make inbound interop authorization, grant tracking, and connected-app state explicit and user-manageable

**Independent Test**: Trigger one authorization-required request, then verify Mobile Claw surfaces the required grant, records the connected-app relationship, and allows or rejects the request with explicit reasoning

- [X] T013 [US2] Extend connected-app and grant models for direction-aware interop governance in `app/src/main/java/com/mobileclaw/app/runtime/governance/CallerGovernanceRecord.kt`, `app/src/main/java/com/mobileclaw/app/runtime/governance/ScopeGrantRecord.kt`, `app/src/main/java/com/mobileclaw/app/runtime/governance/GovernanceModels.kt`, `app/src/main/java/com/mobileclaw/app/runtime/governance/GovernanceGrantState.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/governance/GovernanceRepository.kt`
- [X] T014 [US2] Implement authorization request, grant-status, and revoke flows in `app/src/main/java/com/mobileclaw/app/runtime/interop/InteropAuthorizationService.kt`, `app/src/main/java/com/mobileclaw/app/runtime/capability/CallerVerifier.kt`, `app/src/main/java/com/mobileclaw/app/runtime/policy/PendingApprovalCoordinator.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/policy/ApprovalRepository.kt`
- [X] T015 [US2] Surface connected apps, inbound/outbound permission state, and recent interop outcomes in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/GovernanceCenterUiModel.kt`, `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/GovernanceCenterSheet.kt`, and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`
- [X] T016 [P] [US2] Add authorization and governance coverage in `app/src/test/java/com/mobileclaw/app/runtime/interop/InteropAuthorizationServiceTest.kt` and `app/src/test/java/com/mobileclaw/app/runtime/policy/PendingApprovalCoordinatorTest.kt`

**Checkpoint**: Mobile Claw can now require authorization, track grants, and let users inspect connected-app interop state

---

## Phase 5: User Story 3 - Return Governed Task And Artifact Flows (Priority: P2)

**Goal**: Preserve task continuation and governed artifact access for longer-running interop work

**Independent Test**: Trigger one flow that returns a task or artifact path, then verify external callers can follow the continuation through explicit shared-contract semantics

- [X] T017 [US3] Implement governed interop task handles and status lookup in `app/src/main/java/com/mobileclaw/app/runtime/interop/InteropTaskService.kt`, `app/src/main/java/com/mobileclaw/app/runtime/interop/InteropTaskRecord.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionRegistry.kt`
- [X] T018 [US3] Implement governed artifact and result-handle exposure in `app/src/main/java/com/mobileclaw/app/runtime/interop/InteropArtifactService.kt`, `app/src/main/java/com/mobileclaw/app/runtime/interop/InteropArtifactMapper.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/contribution/RuntimeContributionRegistry.kt`
- [X] T019 [US3] Surface task and artifact continuation state in `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropProvider.kt`, `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/RuntimeControlCenterUiModel.kt`, and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/RuntimeControlCenterSheet.kt`
- [X] T020 [P] [US3] Add task and artifact coverage in `app/src/test/java/com/mobileclaw/app/runtime/interop/InteropTaskServiceTest.kt` and `app/src/test/java/com/mobileclaw/app/runtime/interop/InteropArtifactServiceTest.kt`

**Checkpoint**: Mobile Claw can now continue governed cross-app work beyond one inline response

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final rollout alignment, docs sync, and host validation

- [X] T021 [P] Update host-facing rollout guidance in `docs/hub-interop-android-ipc-v1.md`, `docs/hub-interop-protocol-design-v1.md`, and `docs/project-roadmap-v1.md`
- [X] T022 Run final validation with `./gradlew :app:testDebugUnitTest :app:compileDebugKotlin` and resolve issues across touched interop host files

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1: Setup**: No dependencies; can start immediately after `024`
- **Phase 2: Foundational**: Depends on Phase 1 and blocks all user stories
- **Phase 3: US1**: Depends on Phase 2
- **Phase 4: US2**: Depends on Phase 2 and can proceed in parallel with late US1 work after the host boundary is stable
- **Phase 5: US3**: Depends on Phase 2 and on the governed invocation path from US1
- **Phase 6: Polish**: Depends on completion of desired user stories

### User Story Dependencies

- **US1 (P1)**: First MVP slice after foundational work because the host must first be discoverable and invokable
- **US2 (P1)**: Depends on the same foundational host boundary, but remains independently testable as the governed authorization slice
- **US3 (P2)**: Depends on the invocation path and authorization model because task and artifact continuations extend governed requests rather than replacing them

### Parallel Opportunities

- `T003` can run in parallel with `T002`
- `T005`, `T006`, and `T007` can run in parallel after the setup phase
- `T012`, `T016`, and `T020` can run in parallel with their respective implementation tasks once the APIs stabilize
- `T021` can run in parallel with final validation

---

## Implementation Strategy

### MVP First (User Stories 1 and 2)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: US1
4. Complete Phase 4: US2
5. Validate discovery plus one authorization-required invocation before building task continuations

### Incremental Delivery

1. Add the shared-contract-aware host boundary
2. Ship discovery plus one governed invocation path
3. Add connected-app authorization management
4. Finish with task and artifact continuations

### Team Strategy

1. One engineer can own the provider boundary and descriptor assembly
2. A second engineer can own governance and control-center surfaces once the host boundary exists
3. A third engineer can own task/artifact continuation once invocation and authorization semantics are stable

---

## Notes

- `ACTION_SEND` remains a compatibility ingress and must not become the primary governed interop contract again.
- AppFunctions stay aligned with the shared contract, but the baseline provider transport must remain usable on its own.
- Connected-app visibility belongs inside the existing governed control-center model, not in a separate unmanaged screen.
