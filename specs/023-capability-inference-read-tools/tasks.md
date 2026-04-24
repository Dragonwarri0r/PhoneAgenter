# Tasks: Capability Inference and Read Tools

**Input**: Design documents from `/specs/023-capability-inference-read-tools/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: No dedicated TDD-first task set is included because the specification does not require test-first delivery. Validation is still required in the final phase.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g. US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish shared runtime vocabulary and base models for explicit capability selection and read-tool execution

- [X] T001 [P] Add shared capability-selection and explicit-read localization scaffolding in `app/src/main/res/values/strings.xml` and `app/src/main/res/values-zh/strings.xml`
- [X] T002 [P] Create shared selection and read-result runtime models in `app/src/main/java/com/mobileclaw/app/runtime/session/CapabilitySelectionOutcome.kt` and `app/src/main/java/com/mobileclaw/app/runtime/provider/ReadToolResult.kt`
- [X] T003 [P] Expand shared tool and provider metadata for explicit read routing in `app/src/main/java/com/mobileclaw/app/runtime/capability/ToolContracts.kt` and `app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRegistration.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build the reusable capability-selection and read-provider infrastructure that all user stories depend on

**⚠️ CRITICAL**: No user story work should begin until this phase is complete

- [X] T004 Implement unified read-provider discovery in `app/src/main/java/com/mobileclaw/app/runtime/capability/ReadCapabilityBridge.kt`, `app/src/main/java/com/mobileclaw/app/runtime/capability/AppFunctionBridge.kt`, `app/src/main/java/com/mobileclaw/app/runtime/capability/IntentFallbackBridge.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRegistry.kt`
- [X] T005 [P] Refactor planner and session flow to preserve explicit capability hints and carry selection outcomes in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt`, `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionEvent.kt`
- [X] T006 [P] Extend preview, policy, and audit plumbing for explicit read-tool paths in `app/src/main/java/com/mobileclaw/app/runtime/capability/ToolPreviewFactory.kt`, `app/src/main/java/com/mobileclaw/app/runtime/policy/ApprovalRepository.kt`, `app/src/main/java/com/mobileclaw/app/runtime/policy/ActionScope.kt`, `app/src/main/java/com/mobileclaw/app/runtime/policy/RiskClassifier.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/policy/PolicyEngine.kt`
- [X] T007 Wire read-provider discovery and execution components in `app/src/main/java/com/mobileclaw/app/runtime/provider/CapabilityProviderRegistry.kt` and `app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

**Checkpoint**: The runtime can now represent explicit read capabilities and route them through the common capability surface

---

## Phase 3: User Story 1 - Ask The Workspace To Read My Calendar (Priority: P1) 🎯 MVP

**Goal**: Deliver the first end-to-end explicit read capability through the workspace using `calendar.read`

**Independent Test**: Grant calendar access, ask a calendar lookup question from the workspace, and verify that the runtime selects `calendar.read`, explains the route, and returns bounded results or a truthful no-results outcome

- [X] T008 [US1] Add `calendar.read` tool metadata and normalized explicit-read request shaping in `app/src/main/java/com/mobileclaw/app/runtime/capability/StandardToolCatalog.kt` and `app/src/main/java/com/mobileclaw/app/runtime/provider/ReadToolRequestBuilder.kt`
- [X] T009 [US1] Implement bounded explicit calendar lookup execution in `app/src/main/java/com/mobileclaw/app/runtime/provider/CalendarReadCapabilityProvider.kt` and separate passive-ingestion concerns in `app/src/main/java/com/mobileclaw/app/runtime/systemsource/SystemSourceIngestionService.kt`
- [X] T010 [US1] Register calendar read availability and routing in `app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRegistry.kt`, `app/src/main/java/com/mobileclaw/app/runtime/capability/ReadCapabilityBridge.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/provider/CapabilityProviderRegistry.kt`
- [X] T011 [US1] Surface calendar read route explanation, no-results, and unavailable states in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`, `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/RuntimeStatusUiModel.kt`, and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/RuntimeControlCenterSheet.kt`
- [X] T012 [P] [US1] Add localized calendar read selection, no-results, and recovery messages in `app/src/main/res/values/strings.xml` and `app/src/main/res/values-zh/strings.xml`

**Checkpoint**: The workspace can perform explicit calendar lookup as a bounded, explainable read capability

---

## Phase 4: User Story 2 - Keep Freeform Workspace Input Safe And Predictable (Priority: P2)

**Goal**: Allow freeform workspace requests to infer safe capabilities conservatively without breaking normal conversational behavior

**Independent Test**: Submit normal conversational prompts, ambiguous prompts, clear low-risk read prompts, and higher-risk action prompts from the workspace and verify that the runtime chooses the correct path or safely falls back

- [X] T013 [US2] Implement conservative workspace capability selection in `app/src/main/java/com/mobileclaw/app/runtime/session/WorkspaceCapabilitySelector.kt`, `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/intent/RuntimeIntentHeuristics.kt`
- [X] T014 [US2] Enforce reply fallback and governed escalation for inferred read, write, and dispatch candidates in `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`, `app/src/main/java/com/mobileclaw/app/runtime/policy/RiskClassifier.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/policy/PolicyEngine.kt`
- [X] T015 [US2] Surface selected-capability versus reply-fallback reasoning in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`, `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/WorkspaceStatusDigest.kt`, and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/RuntimeControlCenterSheet.kt`
- [X] T016 [P] [US2] Update fallback, ambiguity, and route-summary wording in `app/src/main/java/com/mobileclaw/app/runtime/provider/LocalGenerationPromptComposer.kt`, `app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `app/src/main/res/values/strings.xml`, and `app/src/main/res/values-zh/strings.xml`

**Checkpoint**: Freeform workspace input can safely choose explicit read paths while preserving predictable conversational fallback

---

## Phase 5: User Story 3 - Add New Read Capabilities Through One Extension Surface (Priority: P3)

**Goal**: Make explicit read capabilities and providers reusable across future first-party and external-app integrations

**Independent Test**: Add a second read-oriented registration and verify that discovery, availability, and workspace explanation use the same capability and extension surface without a new core-specific branch

- [X] T017 [US3] Extend extension metadata to represent explicit read-tool providers in `app/src/main/java/com/mobileclaw/app/runtime/extension/RuntimeExtensionModels.kt` and `app/src/main/java/com/mobileclaw/app/runtime/extension/RuntimeExtensionRegistry.kt`
- [X] T018 [US3] Thread read-provider enablement and compatibility summaries through `app/src/main/java/com/mobileclaw/app/runtime/contribution/RuntimeContributionRegistry.kt`, `app/src/main/java/com/mobileclaw/app/runtime/capability/CapabilityRegistration.kt`, and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/RuntimeControlCenterUiModel.kt`
- [X] T019 [US3] Seed a second read-oriented registration and user-visible discovery labels in `app/src/main/java/com/mobileclaw/app/runtime/extension/RuntimeExtensionModels.kt`, `app/src/main/java/com/mobileclaw/app/runtime/capability/StandardToolCatalog.kt`, `app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `app/src/main/res/values/strings.xml`, and `app/src/main/res/values-zh/strings.xml`

**Checkpoint**: Read capabilities are no longer calendar-specific and can enter through the shared extension surface

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final consistency, documentation, and validation across all stories

- [X] T020 [P] Update capability standards documentation for explicit read tools versus passive context in `docs/tool-capability-and-extension-standards-v1.md`
- [X] T021 Run final validation against `specs/023-capability-inference-read-tools/quickstart.md`, `app/src/test/java/com/mobileclaw/app/runtime/intent/`, and `app/src/test/java/com/mobileclaw/app/runtime/policy/`, then resolve any final integration issues in touched feature files

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1: Setup**: No dependencies; can start immediately
- **Phase 2: Foundational**: Depends on Phase 1 and blocks all user stories
- **Phase 3: User Story 1**: Depends on Phase 2
- **Phase 4: User Story 2**: Depends on Phase 2; may reuse `calendar.read` infrastructure but remains independently testable
- **Phase 5: User Story 3**: Depends on Phase 2 and benefits from prior story abstractions
- **Phase 6: Polish**: Depends on completion of desired user stories

### User Story Dependencies

- **US1 (P1)**: First MVP slice after foundational work
- **US2 (P2)**: Depends on foundational capability-selection plumbing, but not on US3
- **US3 (P3)**: Depends on the shared abstraction established in foundational work and validated by at least one explicit read capability

### Parallel Opportunities

- `T001`, `T002`, and `T003` can run in parallel
- `T005` and `T006` can run in parallel after `T004`
- `T012` can run in parallel with the later half of US1 implementation once result states are known
- `T016` can run in parallel with the later half of US2 implementation once selection behavior is defined
- `T020` can run in parallel with final validation

---

## Parallel Example: User Story 1

```bash
# After foundational work is complete:
Task: "Add calendar.read tool metadata and normalized explicit-read request shaping in app/src/main/java/com/mobileclaw/app/runtime/capability/StandardToolCatalog.kt and app/src/main/java/com/mobileclaw/app/runtime/provider/ReadToolRequestBuilder.kt"
Task: "Add localized calendar read selection, no-results, and recovery messages in app/src/main/res/values/strings.xml and app/src/main/res/values-zh/strings.xml"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. Validate calendar lookup from the main workspace before expanding inference behavior

### Incremental Delivery

1. Ship the reusable read-tool foundation
2. Deliver `calendar.read` as the first explicit read capability
3. Expand workspace inference safety and explanation behavior
4. Finish by making the abstraction reusable for additional read capabilities

### Team Strategy

1. One engineer can own foundational capability-selection and bridge changes
2. A second engineer can take UI/status explanation work after foundational models settle
3. A third engineer can extend extension-surface metadata after the first explicit read capability path is proven

---

## Notes

- The first independently valuable slice is **US1**.
- `calendar.read` must stay distinct from passive system-source ingestion and from `calendar.write`.
- Future read capabilities should reuse the same abstraction created in this feature rather than adding new routing branches.
