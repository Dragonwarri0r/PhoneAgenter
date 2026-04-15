# Tasks: Unified Extension Surface

**Input**: Design documents from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/017-unified-extension-surface/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/unified-extension-surface.md  
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by build/lint checks and quickstart walkthroughs.  
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare shared wording and documentation for the unified extension surface

- [X] T001 Add English and Simplified Chinese strings for extension type, compatibility, enablement, and privacy wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T002 Create `017` feature documentation artifacts and refresh agent context in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/017-unified-extension-surface/` and `/Users/youxuezhe/StudioProjects/mobile_claw/AGENTS.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish unified registration, compatibility, and enablement contracts

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T003 [P] Add unified extension registration and type models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/` or a shared runtime extension package
- [X] T004 [P] Add compatibility and enablement state models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`
- [X] T005 [P] Add unified extension discovery and inspection contracts in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`
- [X] T006 Update DI and shared wiring for unified extension registration in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

**Checkpoint**: One shared extension registration and discovery surface exists for multiple extension families

---

## Phase 3: User Story 1 - Add New Runtime Capabilities Through One Registration Surface (Priority: P1) 🎯 MVP

**Goal**: Represent several runtime extension families through one unified registration model

**Independent Test**: Register multiple extension families and verify they all fit through the same registration contract

### Implementation for User Story 1

- [X] T007 [P] [US1] Evolve seeded `006` portability registrations into the unified extension model in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/` and related files
- [X] T008 [P] [US1] Add registrations for at least several extension families such as ingress, tool provider, context source, and export in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`
- [X] T009 [US1] Add runtime enumeration of unified extension registrations in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`
- [X] T010 [US1] Surface compact extension discovery summaries in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and related UI models

**Checkpoint**: Multiple extension families are registered through one shared surface

---

## Phase 4: User Story 2 - Keep Privacy, Dependency, And Enablement Rules Consistent Across Extensions (Priority: P2)

**Goal**: Make extension registrations uniformly declare privacy, dependency, and activation semantics

**Independent Test**: Inspect multiple extension families and verify privacy, dependency, and enablement metadata are consistent

### Implementation for User Story 2

- [X] T011 [P] [US2] Add dependency and privacy metadata to unified extension registrations in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`
- [X] T012 [P] [US2] Add enablement state handling for active, disabled, degraded, and incompatible extensions in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`
- [X] T013 [US2] Update any extension inspection or summary UI to render privacy and enablement semantics in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [X] T014 [US2] Remove or reduce extension-specific metadata branches for covered registrations in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`

**Checkpoint**: Registered extensions share one privacy/dependency/enablement language

---

## Phase 5: User Story 3 - Route Runtime Discovery And Compatibility Checks Through The Same Extension Model (Priority: P3)

**Goal**: Make compatibility validation and discovery summaries use the unified extension model for covered extension families

**Independent Test**: Validate compatible and incompatible registrations and confirm discovery summaries explain both consistently

### Implementation for User Story 3

- [X] T015 [P] [US3] Add compatibility evaluation for unified extension registrations in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`
- [X] T016 [P] [US3] Add missing-dependency and unsupported-version checks for registered extensions in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`
- [X] T017 [US3] Update extension discovery summaries to include compatibility state and reason in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and related UI models
- [X] T018 [US3] Preserve backward compatibility for existing portability registrations while routing them through the unified extension surface in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/memory/` and related files

**Checkpoint**: Extension discovery and compatibility no longer depend on isolated per-family logic

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize wording, consistency, and milestone validation

- [X] T019 [P] Refine bilingual extension wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [X] T020 [P] Align unified registration, compatibility, enablement, and discovery behavior across `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [X] T021 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/017-unified-extension-surface/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/017-unified-extension-surface/quickstart.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on unified registration already existing
- **User Story 3 (Phase 5)**: Depends on compatibility and discovery models from Foundational work and benefits from User Story 2 metadata alignment
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because it proves multiple extension families can share one registration surface
- **User Story 2 (P2)**: Depends on unified registration models already existing
- **User Story 3 (P3)**: Depends on extension identity and enablement semantics being stable enough to support compatibility and discovery

### Parallel Opportunities

- `T003`, `T004`, and `T005` can run in parallel during Foundational work
- `T007` and `T008` can run in parallel within User Story 1
- `T011` and `T012` can run in parallel within User Story 2
- `T015` and `T016` can run in parallel within User Story 3
- `T019` and `T020` can run in parallel during polish

## Implementation Strategy

### MVP First

1. Finish Setup
2. Finish Foundational work
3. Finish User Story 1
4. Validate that multiple extension families now share one registration surface before broadening compatibility logic

### Incremental Delivery

1. Add unified registration and compatibility contracts
2. Adapt existing `006` registrations
3. Register several extension families through the new surface
4. Add compatibility and discovery summaries

## Notes

- `017` should unify extension registration and discovery, not become a marketplace or external distribution system
- Existing `006` portability hooks should migrate forward, not fork into a second extension mechanism
