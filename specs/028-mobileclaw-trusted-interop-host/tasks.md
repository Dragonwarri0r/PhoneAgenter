# Tasks: Mobile Claw Trusted Interop Host

**Input**: Design documents from `/specs/028-mobileclaw-trusted-interop-host/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), `027-public-interop-contract-stabilization`

**Tests**: Focused host-runtime tests are included because this feature hardens an exported trust boundary.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g. `US1`, `US2`, `US3`)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare host code to consume the stabilized public contract and thread caller context

- [ ] T001 Review `027` status/descriptor adoption in `app/src/main/java/com/mobileclaw/app/runtime/interop/` and `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/`
- [ ] T002 Add interop host string placeholders for identity mismatch, ownership denial, calendar read, expiry, and permission/provider unavailability in `app/src/main/res/values/strings.xml` and `app/src/main/res/values-zh/strings.xml`
- [ ] T003 [P] Add host test fixtures for attested caller identity and claimed metadata in `app/src/test/java/com/mobileclaw/app/runtime/interop/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish host-attested identity and service signatures before user stories

**CRITICAL**: No user story work should begin until this phase is complete.

- [ ] T004 Create `HostAttestedCallerIdentity` and `ClaimedCallerMetadata` models in `app/src/main/java/com/mobileclaw/app/runtime/interop/InteropCallerContext.kt`
- [ ] T005 Implement provider-boundary caller identity resolution in `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropProvider.kt` and a new `app/src/main/java/com/mobileclaw/app/runtime/interop/HostCallerIdentityResolver.kt`
- [ ] T006 Thread host-attested caller identity through `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropMethodDispatcher.kt`, `HubInteropAuthorizationService.kt`, `HubCapabilityInvocationService.kt`, and `HubInteropTaskService.kt`
- [ ] T007 Update `InteropRequestContext` to separate host-attested identity from claimed metadata in `app/src/main/java/com/mobileclaw/app/runtime/interop/InteropRequestContext.kt`

**Checkpoint**: All host services receive trusted identity from the provider boundary and can still inspect claimed metadata for diagnostics.

---

## Phase 3: User Story 1 - Trust Host-Attested Caller Identity (Priority: P1) MVP

**Goal**: Spoofed request metadata cannot influence grant lookup, task ownership, artifact access, or audit identity.

**Independent Test**: Submit a request with mismatched claimed package and verify the host uses the attested caller fingerprint.

- [ ] T008 [P] [US1] Add spoofed caller identity tests in `app/src/test/java/com/mobileclaw/app/runtime/interop/HubInteropAuthorizationServiceTest.kt`
- [ ] T009 [P] [US1] Add provider-boundary identity resolver tests in `app/src/test/java/com/mobileclaw/app/runtime/interop/HostCallerIdentityResolverTest.kt`
- [ ] T010 [US1] Generate caller fingerprints from package, UID, user/profile context, and signing digests in `app/src/main/java/com/mobileclaw/app/runtime/interop/HostCallerIdentityResolver.kt`
- [ ] T011 [US1] Use host-attested caller identity for governance observation and grant lookup in `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropAuthorizationService.kt`
- [ ] T012 [US1] Preserve claimed caller metadata only for display and mismatch diagnostics in `app/src/main/java/com/mobileclaw/app/runtime/interop/InteropCallerContext.kt`
- [ ] T013 [US1] Enrich audit/governance summaries with host-attested identity in `app/src/main/java/com/mobileclaw/app/runtime/governance/DefaultGovernanceRepository.kt` and `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropAuthorizationService.kt`

**Checkpoint**: User Story 1 is independently testable through spoof and resolver tests.

---

## Phase 4: User Story 2 - Govern Authorization, Task, And Artifact Ownership (Priority: P1)

**Goal**: Authorization, tasks, and artifacts are owner-checked and restart-safe or explicitly lifecycle-safe.

**Independent Test**: Create task/artifact state for caller A and verify caller B receives forbidden or unauthorized outcomes.

- [ ] T014 [P] [US2] Add ownership and revoke tests in `app/src/test/java/com/mobileclaw/app/runtime/interop/HubInteropTaskServiceTest.kt`
- [ ] T015 [P] [US2] Add authorization lifecycle tests in `app/src/test/java/com/mobileclaw/app/runtime/interop/HubInteropAuthorizationServiceTest.kt`
- [ ] T016 [US2] Add interop grant request lifecycle model in `app/src/main/java/com/mobileclaw/app/runtime/interop/InteropGrantRequestRecord.kt`
- [ ] T017 [US2] Add durable interop task/artifact models in `app/src/main/java/com/mobileclaw/app/runtime/interop/InteropTaskRecord.kt` and `InteropArtifactRecord.kt`
- [ ] T018 [US2] Wire Room DAO support for interop lifecycle records in `app/src/main/java/com/mobileclaw/app/runtime/governance/GovernanceDao.kt` or a dedicated interop DAO
- [ ] T019 [US2] Register new Room entities and bump schema version in `app/src/main/java/com/mobileclaw/app/runtime/memory/MemoryDatabase.kt` if durable records are implemented
- [ ] T020 [US2] Enforce owner checks and expired/not-found/forbidden semantics in `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropTaskService.kt`
- [ ] T021 [US2] Update control-center task summaries for interop lifecycle state in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/RuntimeControlCenterUiModel.kt`

**Checkpoint**: User Story 2 is independently testable through ownership, revoke, and lifecycle tests.

---

## Phase 5: User Story 3 - Execute Bounded Calendar Read Through The Host (Priority: P2)

**Goal**: External callers can invoke a governed bounded `calendar.read` request and receive explicit task/artifact outcomes.

**Independent Test**: Request grant, invoke bounded `calendar.read`, poll the task, load the artifact, revoke the grant, and verify later invocation fails.

- [ ] T022 [P] [US3] Add calendar read invocation tests in `app/src/test/java/com/mobileclaw/app/runtime/interop/HubCapabilityInvocationServiceTest.kt`
- [ ] T023 [P] [US3] Add calendar artifact mapping tests in `app/src/test/java/com/mobileclaw/app/runtime/interop/HubInteropTaskServiceTest.kt`
- [ ] T024 [US3] Add `calendar.read` to host surface assembly in `app/src/main/java/com/mobileclaw/app/runtime/interop/HubSurfaceDescriptorAssembler.kt`
- [ ] T025 [US3] Map interop `calendar.read` input to bounded runtime read requests in `app/src/main/java/com/mobileclaw/app/runtime/interop/HubCapabilityInvocationService.kt` and `app/src/main/java/com/mobileclaw/app/runtime/provider/ReadToolRequestBuilder.kt`
- [ ] T026 [US3] Ensure calendar provider permission/provider/no-result outcomes map to stabilized public statuses in `app/src/main/java/com/mobileclaw/app/runtime/provider/CalendarReadCapabilityProvider.kt` and `app/src/main/java/com/mobileclaw/app/runtime/interop/HubCapabilityInvocationService.kt`
- [ ] T027 [US3] Emit calendar summary artifact descriptors in `app/src/main/java/com/mobileclaw/app/runtime/interop/HubInteropTaskService.kt`
- [ ] T028 [US3] Add minimal governance/control-center visibility for `calendar.read` interop invocations in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/RuntimeControlCenterSheet.kt`

**Checkpoint**: User Story 3 is independently testable through calendar read host tests and manual probe invocation.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and documentation alignment

- [ ] T029 [P] Update trusted host guidance in `docs/hub-interop-android-ipc-v1.md` and `docs/hub-interop-protocol-design-v1.md`
- [ ] T030 [P] Update `docs/hub-interop-027-029-spec-split-v1.md` with final `028` implementation notes
- [ ] T031 Run `./gradlew :app:testDebugUnitTest :app:compileDebugKotlin :interop-probe-app:testDebugUnitTest --no-daemon` and resolve interop host failures

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1: Setup**: No dependencies beyond completed `027`.
- **Phase 2: Foundational**: Depends on Phase 1 and blocks all user stories.
- **Phase 3: US1**: Depends on Phase 2 and is the MVP.
- **Phase 4: US2**: Depends on Phase 2 and host-attested identity from US1 for final semantics.
- **Phase 5: US3**: Depends on Phase 2 and the authorization/task paths from US1/US2.
- **Phase 6: Polish**: Depends on desired user stories being complete.

### User Story Dependencies

- **US1 (P1)**: Must land first because all trust decisions depend on attested identity.
- **US2 (P1)**: Can begin after identity models are threaded, but final ownership checks depend on US1.
- **US3 (P2)**: Depends on authorization and task/artifact lifecycle behavior from US1/US2.

### Parallel Opportunities

- `T003`, `T008`, `T009`, `T014`, `T015`, `T022`, and `T023` can be prepared in parallel with their implementation phases.
- `T016` and `T017` can run in parallel once storage decision is confirmed.
- `T029` and `T030` can run in parallel with final validation.

## Implementation Strategy

### MVP First

1. Complete identity resolution and threading.
2. Complete US1 spoof-resistance and grant lookup.
3. Validate with host unit tests before touching calendar exposure.

### Incremental Delivery

1. Deliver host-attested caller identity.
2. Add durable or explicit lifecycle-safe task/artifact semantics.
3. Add bounded `calendar.read`.
4. Run app and probe validation.
