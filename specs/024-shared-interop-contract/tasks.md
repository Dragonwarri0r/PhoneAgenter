# Tasks: Shared Hub Interop Contract And Android Binding

**Input**: Design documents from `/specs/024-shared-interop-contract/`
**Prerequisites**: plan.md (required), spec.md (required for user stories)

**Tests**: Focused module tests and compile validation are included because this feature establishes a reusable public contract and compatibility surface.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g. `US1`, `US2`, `US3`)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish the new public protocol modules and Gradle wiring

- [X] T001 Add shared-library plugin aliases and module includes in `gradle/libs.versions.toml`, `build.gradle.kts`, and `settings.gradle.kts`
- [X] T002 Create library module build configurations in `hub-interop-contract-core/build.gradle.kts` and `hub-interop-android-contract/build.gradle.kts`
- [X] T003 [P] Add Android-library manifest and localized public resource scaffolding in `hub-interop-android-contract/src/main/AndroidManifest.xml`, `hub-interop-android-contract/src/main/res/values/strings.xml`, and `hub-interop-android-contract/src/main/res/values-zh/strings.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build the core public vocabulary and Android binding primitives that every later story depends on

**⚠️ CRITICAL**: No user story work should begin until this phase is complete

- [X] T004 Create shared protocol version and compatibility primitives in `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/InteropVersion.kt` and `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/CompatibilitySignal.kt`
- [X] T005 [P] Create shared public identifiers and handle families in `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/InteropIds.kt` and `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/InteropHandles.kt`
- [X] T006 [P] Create shared descriptor models for surfaces, capabilities, grants, tasks, and artifacts in `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/HubSurfaceDescriptor.kt`, `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/InteropCapabilityDescriptor.kt`, `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/InteropGrantDescriptor.kt`, `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/InteropTaskDescriptor.kt`, and `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/InteropArtifactDescriptor.kt`
- [X] T007 Create baseline Android authority, method, and status constants in `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/HubInteropAndroidContract.kt`, `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/HubInteropMethod.kt`, and `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/HubInteropStatus.kt`
- [X] T008 [P] Create public Bundle and URI codecs for discovery, authorization, invocation, task, and artifact flows in `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/bundle/InteropBundleCodec.kt`, `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/bundle/DiscoveryBundles.kt`, `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/bundle/AuthorizationBundles.kt`, `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/bundle/InvocationBundles.kt`, `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/bundle/TaskBundles.kt`, and `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/bundle/ArtifactBundles.kt`
- [X] T009 Wire the shared contract modules into `app/build.gradle.kts` and add a compile-time adoption boundary in `app/src/main/java/com/mobileclaw/app/runtime/ingress/ExternalInteropContracts.kt`

**Checkpoint**: The repository can now compile shared contract modules plus one host consumer path against the same public vocabulary

---

## Phase 3: User Story 1 - Consume One Shared Public Protocol Contract (Priority: P1) 🎯 MVP

**Goal**: Make the shared public protocol contract independently consumable by both Mobile Claw and external callers

**Independent Test**: Compile Mobile Claw plus one separate consumer module against the shared public contract and verify both resolve the same protocol identifiers and handle semantics without copied constants

- [X] T010 [US1] Implement host-agnostic contract validation and descriptor factories in `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/InteropContractValidator.kt` and `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/HubSurfaceDescriptorFactory.kt`
- [X] T011 [US1] Migrate Mobile Claw shared interop vocabulary to the public contract in `app/src/main/java/com/mobileclaw/app/runtime/ingress/ExternalInteropContracts.kt`, `app/src/main/java/com/mobileclaw/app/runtime/ingress/CallableInteropMapper.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/appfunctions/AppFunctionExposureCatalog.kt`
- [X] T012 [P] [US1] Add JVM coverage for shared protocol identifiers and handles in `hub-interop-contract-core/src/test/kotlin/com/mobileclaw/interop/contract/SharedPublicContractTest.kt`

**Checkpoint**: The public contract stands on its own and Mobile Claw no longer defines parallel host-owned public identifiers

---

## Phase 4: User Story 2 - Consume A Stable Android Binding Layer (Priority: P1)

**Goal**: Provide one Android-facing contract surface for discovery, authorization, invocation, task, and artifact calls

**Independent Test**: Build one Android caller path using only the Android binding module and verify it can assemble supported public requests and interpret public statuses without hard-coded host strings

- [X] T013 [US2] Implement Android-side URI builders and request helper APIs in `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/HubInteropUriBuilder.kt`, `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/HubInteropRequestFactory.kt`, and `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/HubInteropCaller.kt`
- [X] T014 [US2] Implement public discovery, authorization, invocation, task, and artifact response adapters in `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/call/DiscoveryCall.kt`, `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/call/AuthorizationCall.kt`, `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/call/InvocationCall.kt`, `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/call/TaskCall.kt`, and `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/call/ArtifactCall.kt`
- [X] T015 [US2] Add localized public compatibility and error explanations in `hub-interop-android-contract/src/main/res/values/strings.xml` and `hub-interop-android-contract/src/main/res/values-zh/strings.xml`
- [X] T016 [P] [US2] Add Android binding coverage in `hub-interop-android-contract/src/test/java/com/mobileclaw/interop/android/HubInteropAndroidContractTest.kt`

**Checkpoint**: Android callers can rely on one stable public binding layer instead of reverse-engineering the host app

---

## Phase 5: User Story 3 - Surface Explicit Version And Compatibility Signals (Priority: P2)

**Goal**: Make compatibility support, downgrade, and incompatibility explicit public outcomes instead of silent runtime breakage

**Independent Test**: Represent one supported and one downgraded or incompatible case through the shared modules and verify callers can distinguish them through explicit public compatibility metadata

- [X] T017 [US3] Implement public compatibility negotiation helpers in `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/CompatibilityEvaluator.kt` and `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/CompatibilityBundleAdapter.kt`
- [X] T018 [US3] Map downgrade and unsupported conditions into stable public status envelopes in `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/HubInteropStatusMapper.kt` and `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/HubInteropError.kt`
- [X] T019 [P] [US3] Add regression coverage for supported, downgraded, and incompatible cases in `hub-interop-contract-core/src/test/kotlin/com/mobileclaw/interop/contract/CompatibilityEvaluatorTest.kt` and `hub-interop-android-contract/src/test/java/com/mobileclaw/interop/android/CompatibilityBundleAdapterTest.kt`

**Checkpoint**: Shared contract consumers can observe compatibility state explicitly before attempting risky interop flows

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final documentation alignment and repository-wide validation

- [X] T020 [P] Update protocol usage guidance in `docs/hub-interop-android-ipc-v1.md`, `docs/hub-interop-module-packaging-v1.md`, and `docs/hub-interop-docs-index-v1.md`
- [X] T021 Run final validation with `./gradlew :hub-interop-contract-core:test :hub-interop-android-contract:testDebugUnitTest :app:compileDebugKotlin` and resolve any issues in touched contract files

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1: Setup**: No dependencies; can start immediately
- **Phase 2: Foundational**: Depends on Phase 1 and blocks all user stories
- **Phase 3: US1**: Depends on Phase 2
- **Phase 4: US2**: Depends on Phase 2 and can proceed alongside the later part of US1 once core identifiers exist
- **Phase 5: US3**: Depends on Phases 2 and 4 because compatibility signals must integrate with the Android binding layer
- **Phase 6: Polish**: Depends on completion of desired user stories

### User Story Dependencies

- **US1 (P1)**: First independently valuable slice because it establishes the shared protocol core
- **US2 (P1)**: Depends on the shared protocol core from US1 but remains independently testable as the Android binding layer
- **US3 (P2)**: Depends on both the shared protocol core and the Android binding surface so compatibility signals can cross the full public boundary

### Parallel Opportunities

- `T003` can run in parallel with `T002`
- `T005`, `T006`, and `T008` can run in parallel after `T004`
- `T012`, `T016`, and `T019` can run in parallel with their respective implementation tasks once APIs stabilize
- `T020` can run in parallel with final validation

---

## Implementation Strategy

### MVP First (User Stories 1 and 2)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: US1
4. Complete Phase 4: US2
5. Validate one host consumer path before expanding compatibility coverage

### Incremental Delivery

1. Add the shared public protocol core
2. Add the Android binding layer
3. Finish explicit compatibility negotiation and downgrade signaling

### Team Strategy

1. One engineer can own Gradle/module setup plus core contract models
2. A second engineer can own Android binding helpers and Bundle codecs once the core identifiers settle
3. A third engineer can add compatibility coverage and regression tests after the public APIs stabilize

---

## Notes

- The first independent checkpoint is the shared protocol core compiling as a reusable module.
- The Android binding layer must remain usable even when AppFunctions are unavailable to the caller.
- No host-only runtime, persistence, or governance internals should leak into either shared module.
