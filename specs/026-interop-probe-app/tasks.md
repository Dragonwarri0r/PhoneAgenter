# Tasks: Hub Interop Probe App

**Input**: Design documents from `/specs/026-interop-probe-app/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), `024` shared contract modules, `025` Mobile Claw host implementation

**Tests**: Focused probe-client unit coverage and compile validation are included because this feature exists to prove the protocol is externally consumable.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g. `US1`, `US2`, `US3`)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create the standalone probe-app module and basic UI shell

- [x] T001 Add the standalone probe-app module in `settings.gradle.kts`, `build.gradle.kts`, and `interop-probe-app/build.gradle.kts`
- [x] T002 Create the probe-app manifest and application shell in `interop-probe-app/src/main/AndroidManifest.xml`, `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ProbeApplication.kt`, and `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/MainActivity.kt`
- [x] T003 [P] Add localized probe resources and theme scaffolding in `interop-probe-app/src/main/res/values/strings.xml`, `interop-probe-app/src/main/res/values-zh/strings.xml`, `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/theme/Color.kt`, `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/theme/Theme.kt`, and `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/theme/Typography.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build a reusable external-consumer client layer that depends only on the shared public contract

**⚠️ CRITICAL**: No user story work should begin until this phase is complete

- [x] T004 Implement a shared-contract-only interop client boundary in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/HubInteropClient.kt`, `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/DiscoveryClient.kt`, and `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/InvocationClient.kt`
- [x] T005 [P] Implement probe-side validation and compatibility models in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/model/ProbeHostSummary.kt`, `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/model/ProbeValidationOutcome.kt`, and `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/model/ProbeTaskState.kt`
- [x] T006 [P] Implement reusable validation state handling in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ProbeViewModel.kt` and `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ProbeUiState.kt`
- [x] T007 Implement Compose navigation and reusable result widgets in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/ProbeNavGraph.kt`, `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/ProbeHomeScreen.kt`, and `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/ProbeResultCard.kt`

**Checkpoint**: The probe app can now talk to the shared contract without depending on Mobile Claw internals

---

## Phase 3: User Story 1 - Discover Mobile Claw As An External Consumer (Priority: P1) 🎯 MVP

**Goal**: Prove that Mobile Claw is discoverable through the shared public contract from a real external app

**Independent Test**: Install the probe app with Mobile Claw and verify the probe app can discover Mobile Claw, read its public surface, and surface explicit incompatibility or unavailability outcomes

- [x] T008 [US1] Implement Mobile Claw discovery and public-surface rendering in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/DiscoveryClient.kt`, `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/DiscoveryScreen.kt`, and `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/model/ProbeHostSummary.kt`
- [x] T009 [US1] Surface availability, incompatibility, and downgrade outcomes in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/CompatibilityBanner.kt`, `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ProbeViewModel.kt`, `interop-probe-app/src/main/res/values/strings.xml`, and `interop-probe-app/src/main/res/values-zh/strings.xml`
- [x] T010 [P] [US1] Add discovery coverage in `interop-probe-app/src/test/java/com/mobileclaw/interop/probe/client/DiscoveryClientTest.kt` and `interop-probe-app/src/test/java/com/mobileclaw/interop/probe/ProbeViewModelDiscoveryTest.kt`

**Checkpoint**: The protocol is now externally discoverable from a separate installed app

---

## Phase 4: User Story 2 - Exercise Governed Authorization And Invocation (Priority: P1)

**Goal**: Validate the real authorization and invocation path rather than only a discovery happy path

**Independent Test**: Trigger one authorization-required flow from the probe app, complete or reject it, and verify the resulting invoke, retry, or blocked outcome remains explicit

- [x] T011 [US2] Implement authorization-required and grant-status flows in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/AuthorizationClient.kt`, `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/AuthorizationScreen.kt`, and `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ProbeViewModel.kt`
- [x] T012 [US2] Implement governed capability invocation plus retry or resume behavior in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/InvocationClient.kt`, `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/InvocationScreen.kt`, and `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/model/ProbeValidationOutcome.kt`
- [x] T013 [US2] Add explicit rejection, pending, and downgrade messaging timeline in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/ValidationTimeline.kt`, `interop-probe-app/src/main/res/values/strings.xml`, and `interop-probe-app/src/main/res/values-zh/strings.xml`
- [x] T014 [P] [US2] Add authorization and invocation coverage in `interop-probe-app/src/test/java/com/mobileclaw/interop/probe/client/AuthorizationClientTest.kt` and `interop-probe-app/src/test/java/com/mobileclaw/interop/probe/client/InvocationClientTest.kt`

**Checkpoint**: The probe app can now validate authorization and governed invocation without using host internals

---

## Phase 5: User Story 3 - Validate Task, Artifact, And Contract Drift Signals (Priority: P2)

**Goal**: Prove that continuation handles and compatibility failures are visible to a real external consumer

**Independent Test**: Exercise one task or artifact continuation path and one compatibility-drift case, then verify the probe app surfaces explicit task, result, downgrade, or failure semantics

- [x] T015 [US3] Implement task polling and artifact follow-up clients in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/TaskClient.kt`, `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/ArtifactClient.kt`, and `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/TaskScreen.kt`
- [x] T016 [US3] Implement contract-drift and compatibility diagnostics in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/CompatibilityInspector.kt`, `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/ContractDriftCard.kt`, and `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/model/ProbeValidationOutcome.kt`
- [x] T017 [US3] Surface final validation summary and exportable session details in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/ProbeSummaryScreen.kt` and `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ProbeViewModel.kt`
- [x] T018 [P] [US3] Add task and drift coverage in `interop-probe-app/src/test/java/com/mobileclaw/interop/probe/client/TaskClientTest.kt` and `interop-probe-app/src/test/java/com/mobileclaw/interop/probe/client/CompatibilityInspectorTest.kt`

**Checkpoint**: The probe app can now validate task continuations and explicit contract drift as a true external consumer

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final isolation checks and side-by-side protocol validation

- [x] T019 [P] Audit isolation so the probe app references only `:hub-interop-contract-core` and `:hub-interop-android-contract` in `interop-probe-app/build.gradle.kts` and `settings.gradle.kts`
- [x] T020 Run final validation with `./gradlew :interop-probe-app:testDebugUnitTest :interop-probe-app:compileDebugKotlin` and side-by-side `assembleDebug` checks against `:app`; perform install smoke on a real device when available, then resolve issues in touched probe-app files

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1: Setup**: No dependencies; can start immediately after `024` is available
- **Phase 2: Foundational**: Depends on Phase 1 and blocks all user stories
- **Phase 3: US1**: Depends on Phase 2 plus a discoverable `025` host implementation
- **Phase 4: US2**: Depends on Phase 2 and on at least one authorization-capable `025` host flow
- **Phase 5: US3**: Depends on Phase 2 and on the task or artifact paths added by `025`
- **Phase 6: Polish**: Depends on completion of desired user stories

### User Story Dependencies

- **US1 (P1)**: First MVP slice because the probe app must first discover the host through the shared contract
- **US2 (P1)**: Depends on the shared client boundary and a governed host flow, but remains independently testable once authorization exists
- **US3 (P2)**: Depends on host continuation semantics and compatibility metadata from prior work

### Parallel Opportunities

- `T003` can run in parallel with `T002`
- `T005` and `T006` can run in parallel after the base module is created
- `T010`, `T014`, and `T018` can run in parallel with their respective implementation tasks once the client APIs stabilize
- `T019` can run in parallel with final validation

---

## Implementation Strategy

### MVP First (User Stories 1 and 2)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: US1
4. Complete Phase 4: US2
5. Validate the probe app as a true external consumer before adding task-continuation coverage

### Incremental Delivery

1. Add the standalone probe-app module
2. Ship discovery plus compatibility visibility
3. Add authorization and invocation validation
4. Finish with task, artifact, and contract-drift validation

### Team Strategy

1. One engineer can own the standalone app shell and shared-contract client boundary
2. A second engineer can own discovery and authorization UI flows once the client boundary exists
3. A third engineer can own task polling and drift diagnostics after the host continuation paths stabilize

---

## Notes

- The probe app is a protocol verifier, not a second Mobile Claw front end.
- Any dependency from `interop-probe-app` to `:app` is a design regression and should fail review.
- Discovery, rejection, downgrade, and unavailability are all first-class expected outcomes for this app.
