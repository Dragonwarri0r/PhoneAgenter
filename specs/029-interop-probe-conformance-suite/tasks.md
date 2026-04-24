# Tasks: Interop Probe Conformance Suite

**Input**: Design documents from `/specs/029-interop-probe-conformance-suite/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), `027-public-interop-contract-stabilization`, `028-mobileclaw-trusted-interop-host`

**Tests**: Focused probe tests are included because this feature turns the probe into a repeatable conformance tool.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g. `US1`, `US2`, `US3`)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare probe models and copy for manual/conformance split

- [ ] T001 Review current probe manual flows in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ProbeViewModel.kt` and `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/`
- [ ] T002 Add conformance/report copy in `interop-probe-app/src/main/res/values/strings.xml` and `interop-probe-app/src/main/res/values-zh/strings.xml`
- [ ] T003 [P] Add shared fake response helpers for conformance tests in `interop-probe-app/src/test/java/com/mobileclaw/interop/probe/client/FakeHubInteropClient.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add conformance result and report models before UI or runner work

**CRITICAL**: No user story work should begin until this phase is complete.

- [ ] T004 Create conformance case and run models in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/model/ConformanceCase.kt` and `ConformanceRun.kt`
- [ ] T005 [P] Create report model in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/model/ProbeReport.kt`
- [ ] T006 [P] Create expected status/result reducer in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/model/ConformanceResultReducer.kt`
- [ ] T007 Add model tests in `interop-probe-app/src/test/java/com/mobileclaw/interop/probe/model/ConformanceResultReducerTest.kt` and `ProbeReportTest.kt`

**Checkpoint**: Probe can represent conformance cases, runs, and reports independent of UI.

---

## Phase 3: User Story 1 - Run Manual Protocol Diagnostics (Priority: P1) MVP

**Goal**: Preserve and clarify manual diagnostics for discovery, authorization, invocation, task, artifact, revoke, and export.

**Independent Test**: Use a fake host client to exercise each manual action and verify timeline/report state updates.

- [ ] T008 [P] [US1] Add manual lifecycle state tests in `interop-probe-app/src/test/java/com/mobileclaw/interop/probe/ProbeViewModelManualFlowTest.kt`
- [ ] T009 [US1] Refactor manual actions into a manual flow coordinator in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ProbeViewModel.kt`
- [ ] T010 [US1] Add supported method and capability summaries to discovery UI in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/DiscoveryScreen.kt`
- [ ] T011 [US1] Add raw status and compatibility details to result cards in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/ProbeResultCard.kt`
- [ ] T012 [US1] Update summary export to include host package, authority, protocol version, supported methods, supported capabilities, and timeline in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ProbeViewModel.kt`

**Checkpoint**: User Story 1 is independently testable through manual flow ViewModel tests.

---

## Phase 4: User Story 2 - Run A Repeatable Conformance Matrix (Priority: P1)

**Goal**: One conformance action runs a bounded pass/fail matrix across compatibility, auth lifecycle, spoof, invocation, task, artifact, revoke, malformed, downgraded, and incompatible cases.

**Independent Test**: Run conformance against fake host responses and verify expected statuses become pass/fail matrix entries.

- [ ] T013 [P] [US2] Add conformance runner tests in `interop-probe-app/src/test/java/com/mobileclaw/interop/probe/client/ConformanceRunnerTest.kt`
- [ ] T014 [P] [US2] Add spoof diagnostic tests in `interop-probe-app/src/test/java/com/mobileclaw/interop/probe/client/SpoofDiagnosticTest.kt`
- [ ] T015 [US2] Implement conformance runner in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/ConformanceRunner.kt`
- [ ] T016 [US2] Add compatibility and version cases in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/ConformanceCases.kt`
- [ ] T017 [US2] Add authorization lifecycle, spoof, revoke, and malformed request cases in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/ConformanceCases.kt`
- [ ] T018 [US2] Add `generate.reply`, bounded `calendar.read`, task polling, and artifact loading cases in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/ConformanceCases.kt`
- [ ] T019 [US2] Add conformance run state and actions to `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ProbeViewModel.kt` and `ProbeUiState.kt`
- [ ] T020 [US2] Add conformance UI entry and matrix display in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/ProbeHomeScreen.kt` and a new `ConformanceScreen.kt`

**Checkpoint**: User Story 2 is independently testable through conformance runner tests and fake client responses.

---

## Phase 5: User Story 3 - Export Shareable Host Behavior Reports (Priority: P2)

**Goal**: Manual and conformance runs produce a shareable report with enough evidence for review.

**Independent Test**: Format a mixed pass/fail conformance run and verify the report includes host identity, matrix, raw statuses, failure reasons, and timeline.

- [ ] T021 [P] [US3] Add report formatter tests in `interop-probe-app/src/test/java/com/mobileclaw/interop/probe/model/ProbeReportFormatterTest.kt`
- [ ] T022 [US3] Implement report formatter in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/model/ProbeReportFormatter.kt`
- [ ] T023 [US3] Wire manual and conformance report export in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ProbeViewModel.kt`
- [ ] T024 [US3] Add report preview UI in `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/ui/ProbeSummaryScreen.kt`
- [ ] T025 [US3] Add dependency-isolation guard test or Gradle check documentation in `interop-probe-app/src/test/java/com/mobileclaw/interop/probe/client/ProbeDependencyBoundaryTest.kt`

**Checkpoint**: User Story 3 is independently testable through report formatter and dependency boundary tests.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and documentation alignment

- [ ] T026 [P] Update conformance usage notes in `docs/hub-interop-android-ipc-v1.md` and `docs/hub-interop-docs-index-v1.md`
- [ ] T027 [P] Update `docs/hub-interop-027-029-spec-split-v1.md` with final `029` validation notes
- [ ] T028 Run `./gradlew :interop-probe-app:testDebugUnitTest :hub-interop-android-contract:testDebugUnitTest :hub-interop-contract-core:test --no-daemon` and resolve probe conformance failures

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1: Setup**: No dependencies beyond completed `027/028` behavior.
- **Phase 2: Foundational**: Depends on Phase 1 and blocks all user stories.
- **Phase 3: US1**: Depends on Phase 2 and is the manual-mode MVP.
- **Phase 4: US2**: Depends on Phase 2 and can reuse manual clients from US1.
- **Phase 5: US3**: Depends on Phase 2 and benefits from US1/US2 outputs.
- **Phase 6: Polish**: Depends on desired user stories being complete.

### User Story Dependencies

- **US1 (P1)**: Keeps manual diagnostics usable and should land first.
- **US2 (P1)**: Builds the automated conformance matrix and can proceed after foundational models.
- **US3 (P2)**: Builds shareable reports from manual/conformance run data.

### Parallel Opportunities

- `T003`, `T005`, and `T006` can run in parallel after setup.
- `T008`, `T013`, `T014`, and `T021` can be prepared in parallel with implementation.
- `T016`, `T017`, and `T018` can split conformance case families across workers if needed.
- `T026` and `T027` can run in parallel with validation.

## Implementation Strategy

### MVP First

1. Add foundational conformance models.
2. Preserve and improve manual diagnostics.
3. Validate manual lifecycle with fake clients.

### Incremental Delivery

1. Manual mode remains stable.
2. Add conformance runner and matrix.
3. Add report export and dependency boundary checks.
4. Run probe and contract module validation.
