# Tasks: Public Hub Interop Contract Stabilization

**Input**: Design documents from `/specs/027-public-interop-contract-stabilization/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), `024-026` interop implementation context

**Tests**: Focused contract and Android binding tests are included because this feature stabilizes a public API boundary.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g. `US1`, `US2`, `US3`)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare the contract modules and existing implemented specs for stabilization

- [X] T001 Audit current method, status, descriptor, compatibility, and Bundle codec usage in `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/`, `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/`, and `app/src/main/java/com/mobileclaw/app/runtime/interop/`
- [X] T002 Reconcile implemented `024` checklist state in `specs/024-shared-interop-contract/tasks.md`
- [X] T003 Reconcile implemented `025` checklist state in `specs/025-mobileclaw-interop-host/tasks.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the public vocabulary that all stabilized stories depend on

**CRITICAL**: No user story work should begin until this phase is complete.

- [X] T004 Expand public status taxonomy in `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/HubInteropStatus.kt`
- [X] T005 Update status mapping and public error envelopes in `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/HubInteropStatusMapper.kt` and `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/HubInteropError.kt`
- [X] T006 [P] Add descriptor availability and lifecycle primitives in `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/InteropCapabilityDescriptor.kt`, `InteropTaskDescriptor.kt`, and `InteropArtifactDescriptor.kt`
- [X] T007 [P] Extend compatibility reason primitives for required unknown, optional unknown, and extension namespace handling in `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/CompatibilitySignal.kt`

**Checkpoint**: Contract modules have the public vocabulary needed for method/status, descriptor, and compatibility stabilization.

---

## Phase 3: User Story 1 - Stable Method And Status Contracts (Priority: P1) MVP

**Goal**: External callers can interpret every public method outcome without host-private knowledge.

**Independent Test**: Compile an Android caller against only the public contract modules and verify status mappings distinguish authorization, ownership, expiry, compatibility, and execution failures.

- [X] T008 [P] [US1] Add method/status taxonomy tests in `hub-interop-android-contract/src/test/java/com/mobileclaw/interop/android/HubInteropStatusMapperTest.kt`
- [X] T009 [US1] Stabilize public method constants in `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/HubInteropMethod.kt`
- [X] T010 [US1] Update discovery, authorization, invocation, task, and artifact response status handling in `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/call/`
- [X] T011 [US1] Update public request factory status assumptions in `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/HubInteropRequestFactory.kt`
- [X] T012 [US1] Align host/probe compile usage with stabilized statuses in `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropMethodDispatcher.kt` and `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/client/CompatibilityInspector.kt`

**Checkpoint**: User Story 1 is independently testable through public status mapping tests and compile adoption.

---

## Phase 4: User Story 2 - Stable Descriptor V1 Shapes (Priority: P1)

**Goal**: Host and caller code can exchange stable surface, capability, grant, task, artifact, and compatibility descriptors without host internals.

**Independent Test**: Validate representative descriptor v1 objects and roundtrip them through Android Bundle codecs.

- [X] T013 [P] [US2] Add descriptor validator tests in `hub-interop-contract-core/src/test/kotlin/com/mobileclaw/interop/contract/SharedPublicContractTest.kt`
- [X] T014 [US2] Expand capability descriptor v1 fields in `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/InteropCapabilityDescriptor.kt`
- [X] T015 [US2] Expand grant descriptor v1 fields in `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/InteropGrantDescriptor.kt`
- [X] T016 [US2] Expand task and artifact descriptor lifecycle fields in `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/InteropTaskDescriptor.kt` and `InteropArtifactDescriptor.kt`
- [X] T017 [US2] Update descriptor validation in `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/InteropContractValidator.kt`
- [X] T018 [US2] Update Bundle serializers for descriptor v1 fields in `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/bundle/DiscoveryBundles.kt`, `AuthorizationBundles.kt`, `TaskBundles.kt`, and `ArtifactBundles.kt`

**Checkpoint**: User Story 2 is independently testable through descriptor validation and Bundle roundtrips.

---

## Phase 5: User Story 3 - Explicit Compatibility Evolution (Priority: P2)

**Goal**: Protocol evolution can distinguish supported, downgraded, incompatible, required unknown, optional unknown, and extension namespace cases.

**Independent Test**: Compatibility evaluator tests cover every compatibility outcome and Android compatibility bundles preserve those diagnostics.

- [X] T019 [P] [US3] Add compatibility evaluator tests in `hub-interop-contract-core/src/test/kotlin/com/mobileclaw/interop/contract/CompatibilityEvaluatorTest.kt`
- [X] T020 [P] [US3] Add Android compatibility Bundle tests in `hub-interop-android-contract/src/test/java/com/mobileclaw/interop/android/CompatibilityBundleAdapterTest.kt`
- [X] T021 [US3] Implement explicit unknown-field policy in `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/CompatibilitySignal.kt`
- [X] T022 [US3] Update Android compatibility adapter in `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/CompatibilityBundleAdapter.kt`
- [X] T023 [US3] Document compatibility and unknown-field behavior in `docs/hub-interop-android-ipc-v1.md` and `docs/hub-interop-protocol-design-v1.md`

**Checkpoint**: User Story 3 is independently testable through compatibility evaluator and Bundle adapter coverage.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final documentation, validation, and downstream readiness

- [X] T024 [P] Update module packaging guidance in `docs/hub-interop-module-packaging-v1.md` and `docs/hub-interop-docs-index-v1.md`
- [X] T025 Run `./gradlew :hub-interop-contract-core:test :hub-interop-android-contract:testDebugUnitTest :app:compileDebugKotlin :interop-probe-app:testDebugUnitTest --no-daemon` and resolve contract-related failures
- [X] T026 Update `docs/hub-interop-027-029-spec-split-v1.md` with final `027` validation notes

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1: Setup**: No dependencies; can start immediately.
- **Phase 2: Foundational**: Depends on Phase 1 and blocks all user stories.
- **Phase 3: US1**: Depends on Phase 2.
- **Phase 4: US2**: Depends on Phase 2 and can proceed alongside US1 after status vocabulary stabilizes.
- **Phase 5: US3**: Depends on Phase 2 and can proceed after compatibility primitives are updated.
- **Phase 6: Polish**: Depends on desired user stories being complete.

### User Story Dependencies

- **US1 (P1)**: First MVP because all callers need stable method/status outcomes.
- **US2 (P1)**: Can proceed after foundational descriptor primitives and remains independently testable.
- **US3 (P2)**: Can proceed after compatibility primitives and remains independently testable.

### Parallel Opportunities

- `T006` and `T007` can run in parallel after `T004-T005` are understood.
- `T008`, `T013`, `T019`, and `T020` can run in parallel once public API decisions are drafted.
- `T024` can run in parallel with final validation.

## Implementation Strategy

### MVP First

1. Complete Phase 1 and Phase 2.
2. Complete US1 to stabilize method/status behavior.
3. Validate with contract and Android binding tests.

### Incremental Delivery

1. Deliver US1 method/status taxonomy.
2. Deliver US2 descriptor v1 stabilization.
3. Deliver US3 compatibility evolution.
4. Run full contract, host compile, and probe validation.
