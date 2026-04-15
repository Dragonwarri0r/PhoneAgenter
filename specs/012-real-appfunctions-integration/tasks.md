# Tasks: Real AppFunctions Integration

**Input**: Design documents from `/specs/012-real-appfunctions-integration/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/real-appfunctions-contract.md  
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by build/lint checks, generated-source inspection, and quickstart walkthroughs.  
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare build tooling and bilingual wording for real AppFunctions support

- [X] T001 Add English and Simplified Chinese strings for real AppFunctions availability, unsupported-state wording, and service-backed status in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T002 Add AppFunctions build/plugin scaffolding in `/Users/youxuezhe/StudioProjects/mobile_claw/build.gradle.kts`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/build.gradle.kts`, and `/Users/youxuezhe/StudioProjects/mobile_claw/gradle/libs.versions.toml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the shared service and bridge infrastructure used by every story

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T003 [P] Upgrade app SDK/tooling for real AppFunctions support in `/Users/youxuezhe/StudioProjects/mobile_claw/app/build.gradle.kts`
- [X] T004 [P] Add AppFunctions runtime/service/compiler dependencies and KSP wiring in `/Users/youxuezhe/StudioProjects/mobile_claw/build.gradle.kts`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/build.gradle.kts`, and `/Users/youxuezhe/StudioProjects/mobile_claw/gradle/libs.versions.toml`
- [X] T005 [P] Create AppFunctions package scaffolding in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/appfunctions/`
- [X] T006 [P] Register a real AppFunction service in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/AndroidManifest.xml`

**Checkpoint**: The project can compile real AppFunctions artifacts and service registration

---

## Phase 3: User Story 1 - Expose Real App Functions From Mobile Claw (Priority: P1) 🎯 MVP

**Goal**: Expose a real Mobile Claw AppFunctions surface with generated metadata

**Independent Test**: Build the app and verify service registration plus generated metadata

### Implementation for User Story 1

- [X] T007 [P] [US1] Implement exposed Mobile Claw app function definitions in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/appfunctions/`
- [X] T008 [P] [US1] Implement a real AndroidX AppFunction service and configuration provider in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/appfunctions/`
- [X] T009 [US1] Wire required runtime dependencies for app function implementations using Hilt entry points or equivalent in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/appfunctions/`
- [X] T010 [US1] Validate generated AppFunctions outputs and manifest wiring, then document them in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/012-real-appfunctions-integration/quickstart.md`

**Checkpoint**: Mobile Claw exposes a real AppFunctions service

---

## Phase 4: User Story 2 - Use Real Framework-Backed Discovery In The Bridge (Priority: P2)

**Goal**: Replace seeded AppFunctions availability with real framework-backed probing where supported

**Independent Test**: Resolve an AppFunctions-mapped capability and verify provider status comes from real framework-backed checks

### Implementation for User Story 2

- [X] T011 [P] [US2] Replace the seeded bridge with a real bridge implementation in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/AppFunctionBridge.kt` and related files under `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/`
- [X] T012 [P] [US2] Add capability-to-appfunction mapping and supported-device probing in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/`
- [X] T013 [US2] Update DI wiring in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt` to use the real bridge

**Checkpoint**: Capability resolution uses real AppFunctions status when available

---

## Phase 5: User Story 3 - Keep AppFunctions Product State Visible And Honest (Priority: P3)

**Goal**: Make AppFunctions support status visible and truthful in the workspace

**Independent Test**: Workspace status distinguishes real AppFunctions support from fallback behavior

### Implementation for User Story 3

- [X] T014 [P] [US3] Extend route/status UI models with real AppFunctions state in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/`
- [X] T015 [P] [US3] Surface real AppFunctions availability and unsupported fallback wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`
- [X] T016 [US3] Update workspace surfaces for AppFunctions status wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`

**Checkpoint**: The product no longer overclaims AppFunctions support

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize wording, consistency, and milestone validation

- [X] T017 [P] Refine bilingual AppFunctions wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T018 [P] Align AppFunctions bridge metadata and workspace visibility across `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [X] T019 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/012-real-appfunctions-integration/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/012-real-appfunctions-integration/quickstart.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on User Story 1 and Foundational completion
- **User Story 3 (Phase 5)**: Depends on User Story 2 visibility state
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because real service exposure is the defining outcome
- **User Story 2 (P2)**: Depends on real service exposure and build integration
- **User Story 3 (P3)**: Depends on bridge state becoming meaningful

### Parallel Opportunities

- `T003`, `T004`, `T005`, and `T006` can run in parallel after setup files exist
- `T007` and `T008` can run in parallel within User Story 1
- `T011` and `T012` can run in parallel within User Story 2
- `T014` and `T015` can run in parallel within User Story 3
- `T017` and `T018` can run in parallel during polish

## Implementation Strategy

### MVP First

1. Finish Setup
2. Finish Foundational work
3. Finish User Story 1
4. Validate real service exposure before bridge upgrades

### Incremental Delivery

1. Ship build/tooling uplift
2. Add real service exposure
3. Upgrade bridge discovery
4. Refine user-facing status

## Notes

- `012` should prove real AppFunctions integration, not a full third-party interop marketplace
- Unsupported devices must continue through existing fallback routing
- The milestone is allowed to focus on self-package AppFunctions discovery first
