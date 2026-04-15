# Tasks: Tool Contract Standardization

**Input**: Design documents from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/015-tool-contract-standardization/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/tool-contract-standardization.md  
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by build/lint checks and quickstart walkthroughs.  
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare shared documentation and wording for standardized tool contracts

- [x] T001 Add English and Simplified Chinese strings for tool identity, side-effect categories, and preview wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [x] T002 Create `015` feature documentation artifacts and refresh agent context in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/015-tool-contract-standardization/` and `/Users/youxuezhe/StudioProjects/mobile_claw/AGENTS.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the shared descriptor, schema, visibility, and preview contracts

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [x] T003 [P] Add standardized tool descriptor and schema models in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/` or a new runtime tool package
- [x] T004 [P] Add side-effect and tool preview contracts in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/action/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/`
- [x] T005 [P] Add request-scoped tool visibility resolution in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/` and related runtime/session files
- [x] T006 Update DI and shared wiring for descriptor-driven tool resolution in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

**Checkpoint**: Shared tool descriptor, schema, preview, and visibility contracts are available to planner, router, UI, governance, and audit

---

## Phase 3: User Story 1 - Surface Relevant Tools Only When They Are Actually Usable (Priority: P1) 🎯 MVP

**Goal**: Keep tool surfacing on-demand so the workspace stays conversation-first while still exposing relevant device actions

**Independent Test**: Trigger reply, calendar, alarm, and share-oriented requests and verify only relevant covered tools become visible or explainably degraded

### Implementation for User Story 1

- [x] T007 [P] [US1] Add the first standardized tool catalog entries in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/`
- [x] T008 [P] [US1] Refactor capability resolution to produce standardized tool visibility snapshots in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRegistry.kt` and related files
- [x] T009 [US1] Wire runtime planner/router to use standardized tool metadata for covered tool families in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/`
- [x] T010 [US1] Surface compact tool visibility and degradation explanations in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and related UI models

**Checkpoint**: Covered tools are no longer permanently exposed or silently inferred without a standardized visibility decision

---

## Phase 4: User Story 2 - Preview Common Productivity Actions Through Standardized Contracts (Priority: P2)

**Goal**: Use schema-backed standardized preview metadata for the first productivity tool catalog

**Independent Test**: Trigger at least three covered tool families and verify preview fields, side-effect classification, and confirmation metadata are consistent

### Implementation for User Story 2

- [x] T011 [P] [US2] Add schema-backed descriptor entries for `calendar.read`, `calendar.write`, `alarm.set`, `alarm.show`, `alarm.dismiss`, `message.send`, and `share.outbound` in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/`
- [x] T012 [P] [US2] Derive standardized execution preview models from structured actions and tool descriptors in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/action/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/`
- [x] T013 [US2] Update approval and preview UI to render standardized tool name, side-effect type, scopes, and ordered fields in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [x] T014 [US2] Replace scattered display/risk/confirmation lookups for covered tool families with descriptor-driven lookups in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/`

**Checkpoint**: Covered productivity actions share one preview and confirmation contract instead of one-off mappings

---

## Phase 5: User Story 3 - Keep Governance, Audit, And Routing Aligned To The Same Tool Identity (Priority: P3)

**Goal**: Ensure tool identity, scopes, and side effects remain stable across routing, governance, approval, and audit

**Independent Test**: Allow and deny multiple covered tools and verify governance, route explanation, approval, and audit all reference the same tool id and scope language

### Implementation for User Story 3

- [x] T015 [P] [US3] Extend governance records and route summaries to retain standardized tool identity in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/governance/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/`
- [x] T016 [P] [US3] Align policy scope resolution with standardized tool families in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/policy/`
- [x] T017 [US3] Update audit events and workspace outcome summaries to reference standardized tool identity and side-effect wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [x] T018 [US3] Keep legacy capability-to-tool compatibility for covered flows while routing and governance transition to descriptors in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/capability/`

**Checkpoint**: Governance, audit, route explanation, and preview all speak the same tool language for covered flows

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize wording, consistency, and milestone validation

- [x] T019 [P] Refine bilingual tool wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [x] T020 [P] Align tool visibility, preview, governance, and audit behavior across `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [x] T021 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/015-tool-contract-standardization/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/015-tool-contract-standardization/quickstart.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on User Story 1 visibility contracts and Foundational descriptor/schema work
- **User Story 3 (Phase 5)**: Depends on the standardized tool identity established by Foundational work and benefits from User Story 2 preview alignment
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because on-demand standardized tool visibility is the first user-facing outcome
- **User Story 2 (P2)**: Depends on standardized tool descriptors already existing
- **User Story 3 (P3)**: Depends on tool identity being stable enough to propagate into governance and audit

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
4. Validate that the workspace remains conversation-first while still surfacing relevant standardized tools

### Incremental Delivery

1. Add descriptor, schema, visibility, and preview contracts
2. Add the first standardized productivity tool catalog
3. Move preview and confirmation to descriptor-driven behavior
4. Align governance and audit to the same tool identity

## Notes

- `015` should standardize common productivity tools and execution semantics, not become a full external interop spec
- Covered flows should prefer descriptor-driven behavior, but transitional compatibility with legacy capability ids is acceptable during migration
