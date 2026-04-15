# Tasks: Android Agent Shell and Local Model Workspace

**Input**: Design documents from `/specs/001-android-agent-shell/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/local-chat-gateway.md
**Tests**: No dedicated test tasks are included in this milestone plan; validation is driven by the independent story checks and quickstart flows.
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Scaffold the Android project and establish the basic app entry points

- [X] T001 Create the Android Gradle scaffold in `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, and `gradle/libs.versions.toml`
- [X] T002 Create the application module and manifest in `app/build.gradle.kts` and `app/src/main/AndroidManifest.xml`
- [X] T003 [P] Create the base app entry files in `app/src/main/java/com/mobileclaw/app/MobileClawApplication.kt`, `app/src/main/java/com/mobileclaw/app/MainActivity.kt`, and `app/src/main/res/values/strings.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the shared UI, dependency, and adapter foundations required by every user story

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [X] T004 [P] Implement the Digital Atrium theme tokens in `app/src/main/java/com/mobileclaw/app/ui/theme/Color.kt`, `app/src/main/java/com/mobileclaw/app/ui/theme/Typography.kt`, `app/src/main/java/com/mobileclaw/app/ui/theme/Shape.kt`, and `app/src/main/java/com/mobileclaw/app/ui/theme/Theme.kt`
- [X] T005 [P] Define the local chat and model contracts in `app/src/main/java/com/mobileclaw/app/runtime/localchat/LocalChatGateway.kt`, `app/src/main/java/com/mobileclaw/app/runtime/localchat/LocalModelCatalog.kt`, and `app/src/main/java/com/mobileclaw/app/runtime/localchat/SessionStreamEvent.kt`
- [X] T006 [P] Add dependency injection wiring in `app/src/main/java/com/mobileclaw/app/di/AppModule.kt`
- [X] T007 [P] Create shared workspace UI models in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/ChatTurnUiModel.kt`, `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/ModelHealthUiModel.kt`, and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/WorkspaceFeedbackUiModel.kt`
- [X] T008 Create the workspace route and app scaffold in `app/src/main/java/com/mobileclaw/app/ui/navigation/AppScaffold.kt` and `app/src/main/java/com/mobileclaw/app/ui/navigation/MobileClawNavGraph.kt`
- [X] T009 Create the base workspace state and view model skeleton in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceUiState.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`

**Checkpoint**: Foundation ready for story-level implementation

---

## Phase 3: User Story 1 - Start a Local Agent Session (Priority: P1) 🎯 MVP

**Goal**: Deliver a working chat workspace where the user can select a ready local model, send a prompt, and receive streamed assistant output

**Independent Test**: Launch the app, select a ready model, send a prompt, and verify ordered user and assistant turns with streamed output

### Implementation for User Story 1

- [X] T010 [P] [US1] Build the conversation transcript surfaces in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ConversationLayer.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/MessageBubble.kt`
- [X] T011 [P] [US1] Build the composer dock and send controls in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ComposerDock.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/SendButton.kt`
- [X] T012 [P] [US1] Implement the local streaming adapter and active session store in `app/src/main/java/com/mobileclaw/app/runtime/localchat/LiteRtLocalChatGateway.kt` and `app/src/main/java/com/mobileclaw/app/runtime/localchat/InMemoryChatSessionStore.kt`
- [X] T013 [US1] Wire send, stream, and turn-append behavior in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`
- [X] T014 [US1] Assemble the primary workspace screen in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt`

**Checkpoint**: User Story 1 is independently functional as the MVP chat loop

---

## Phase 4: User Story 2 - Handle Local Model Readiness (Priority: P2)

**Goal**: Surface model readiness, context visibility, and workspace zoning clearly without making the screen feel dense

**Independent Test**: Open the workspace with unavailable, preparing, ready, and failed model states and verify the model health, context, conversation, and composer zones remain understandable

### Implementation for User Story 2

- [X] T015 [P] [US2] Implement the model health surface and picker sheet in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ModelHealthCard.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ModelPickerSheet.kt`
- [X] T016 [P] [US2] Implement the context window and quick action strip in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/QuickActionStrip.kt`
- [X] T017 [P] [US2] Implement unavailable, preparing, and failure states in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/WorkspaceEmptyState.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/InlineFailureBanner.kt`
- [X] T018 [US2] Wire model availability, screen zoning, and responsive layout behavior in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`

**Checkpoint**: User Story 2 works independently and keeps the workspace legible across readiness states

---

## Phase 5: User Story 3 - Inspect and Reset the Current Session (Priority: P3)

**Goal**: Let the user understand the active session, reset it safely, and receive lightweight feedback about session actions

**Independent Test**: Send multiple turns, inspect session state, reset the session, and verify the transcript clears with visible feedback

### Implementation for User Story 3

- [X] T019 [P] [US3] Implement the workspace header and reset confirmation surfaces in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/WorkspaceHeader.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ResetSessionDialog.kt`
- [X] T020 [P] [US3] Implement transient success and failure feedback in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/WorkspaceFeedbackHost.kt`
- [X] T021 [US3] Implement reset-session behavior and session-summary updates in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and `app/src/main/java/com/mobileclaw/app/runtime/localchat/InMemoryChatSessionStore.kt`
- [X] T022 [US3] Integrate reset flow and current-session metadata surfaces in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt` and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ContextWindowCard.kt`

**Checkpoint**: User Story 3 works independently and adds session control without breaking earlier stories

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Refine the workspace feel and validate the milestone against the screen-level spec

- [X] T023 [P] Refine adaptive spacing, accessibility labels, and scroll behavior in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt`, `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ConversationLayer.kt`, `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ComposerDock.kt`, and `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/WorkspaceHeader.kt`
- [X] T024 [P] Finalize feedback and tonal-layer polish in `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/WorkspaceFeedbackHost.kt`, `app/src/main/java/com/mobileclaw/app/ui/theme/Color.kt`, and `app/src/main/java/com/mobileclaw/app/ui/theme/Theme.kt`
- [X] T025 Validate the implemented milestone against `specs/001-android-agent-shell/quickstart.md` and update any follow-up notes in `specs/001-android-agent-shell/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on Foundational completion and integrates with the MVP workspace
- **User Story 3 (Phase 5)**: Depends on Foundational completion and builds on the active session loop
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP
- **User Story 2 (P2)**: Depends on the MVP shell but should remain independently verifiable
- **User Story 3 (P3)**: Depends on the MVP shell and should remain independently verifiable

### Parallel Opportunities

- `T004`, `T005`, `T006`, and `T007` can run in parallel after project scaffold exists
- `T010`, `T011`, and `T012` can run in parallel within User Story 1
- `T015`, `T016`, and `T017` can run in parallel within User Story 2
- `T019` and `T020` can run in parallel within User Story 3
- `T023` and `T024` can run in parallel during polish

## Implementation Strategy

### MVP First

1. Finish Setup
2. Finish Foundational work
3. Finish User Story 1
4. Validate the first real local chat loop before moving on

### Incremental Delivery

1. Ship the shell and local chat loop
2. Add strong model readiness and screen zoning
3. Add reset and lightweight session feedback
4. Finish with visual and accessibility polish

## Notes

- `001` is intentionally UI- and workspace-centric
- The fuller runtime orchestration contract is deferred to `002`
- The local chat adapter should stay thin so it can later be wrapped by the runtime session pipeline
