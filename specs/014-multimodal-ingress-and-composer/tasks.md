# Tasks: Multimodal Ingress And Composer

**Input**: Design documents from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/014-multimodal-ingress-and-composer/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, contracts/multimodal-runtime-contract.md  
**Tests**: No dedicated automated test tasks are included in this milestone plan; validation is driven by build/lint checks and quickstart walkthroughs.  
**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare shared strings and documentation for multimodal ingress

- [x] T001 Add English and Simplified Chinese strings for multimodal capability, attachment preview, and limitation messaging in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [x] T002 Create `014` feature documentation artifacts and refresh agent context in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/014-multimodal-ingress-and-composer/` and `/Users/youxuezhe/StudioProjects/mobile_claw/AGENTS.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish shared model capability and attachment contracts for the whole milestone

**⚠️ CRITICAL**: No user story work should start until this phase is complete

- [x] T003 [P] Extend model/runtime contracts for modality capabilities and attachments in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/localchat/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/`
- [x] T004 [P] Add app-managed attachment import/storage support in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/` or a new runtime attachment package
- [x] T005 [P] Extend workspace UI state/models for pending multimodal attachments in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/`
- [x] T006 Update DI/wiring as needed in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/di/AppModule.kt`

**Checkpoint**: Shared capability and attachment contracts are available to composer, ingress, and runtime

---

## Phase 3: User Story 1 - Attach Image Or Audio From The Workspace Composer (Priority: P1) 🎯 MVP

**Goal**: Add model-aware image/audio attachment entry to the composer with lightweight previews and removal

**Independent Test**: Select a multimodal-capable model, attach media from the composer, and verify compact preview/removal behavior; then switch to a text-only model and verify gating

### Implementation for User Story 1

- [x] T007 [P] [US1] Extend local model catalog/profile capability metadata in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/localchat/`
- [x] T008 [P] [US1] Add attachment picker actions and compact preview rail in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/`
- [x] T009 [US1] Wire composer attachment staging, remove actions, and model-aware gating in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt`
- [x] T010 [US1] Keep the `013` conversation-first layout intact while adding multimodal entry in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ComposerDock.kt` and related files

**Checkpoint**: Users can stage/remove multimodal attachments from the workspace without bloating the composer

---

## Phase 4: User Story 2 - Normalize Multimodal Requests Into Runtime Input (Priority: P2)

**Goal**: Send image/audio attachments through the existing runtime/provider/local-generation path

**Independent Test**: Submit a request with staged media and verify normalized attachments reach runtime and LiteRT-LM content construction

### Implementation for User Story 2

- [x] T011 [P] [US2] Extend runtime request/context/provider contracts for attachments in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/session/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/provider/`
- [x] T012 [P] [US2] Update local generation prompt/content building in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/provider/LocalGenerationProvider.kt` and related files
- [x] T013 [US2] Update `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/localchat/LiteRtLocalChatGateway.kt` to send text plus `Content.ImageFile` / `Content.AudioFile` payloads
- [x] T014 [US2] Surface attachment-aware runtime feedback in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` and related UI models

**Checkpoint**: Multimodal composer input becomes real runtime input rather than UI-only state

---

## Phase 5: User Story 3 - Accept External Shared Media Through The Same Attachment Semantics (Priority: P3)

**Goal**: Make Android external media share reuse the same attachment contract as internal composer input

**Independent Test**: Share an image or audio item into Mobile Claw and verify normalized attachment previews plus consistent runtime handling

### Implementation for User Story 3

- [x] T015 [P] [US3] Extend external share registration/parser for image/audio handoff in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/AndroidManifest.xml` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/`
- [x] T016 [P] [US3] Normalize external media payloads into the canonical runtime attachment model in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/`
- [x] T017 [US3] Update workspace external handoff handling in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceViewModel.kt` to show and use shared media attachments
- [x] T018 [US3] Add safe unsupported-model and failed-copy messaging for external media in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/ingress/` and related UI files

**Checkpoint**: External media handoff uses the same attachment semantics as internal composer input

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize consistency, wording, and validation

- [x] T019 [P] Refine bilingual multimodal wording in `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/strings/AppStrings.kt`, `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values/strings.xml`, and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/res/values-zh/strings.xml`
- [x] T020 [P] Align multimodal attachment preview, runtime mapping, and external handoff behavior across `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/runtime/` and `/Users/youxuezhe/StudioProjects/mobile_claw/app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- [x] T021 Validate the implemented milestone against `/Users/youxuezhe/StudioProjects/mobile_claw/specs/014-multimodal-ingress-and-composer/quickstart.md` and update follow-up notes in `/Users/youxuezhe/StudioProjects/mobile_claw/specs/014-multimodal-ingress-and-composer/quickstart.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on User Story 1 attachment state and Foundational contracts
- **User Story 3 (Phase 5)**: Depends on the canonical attachment contract from Foundational and benefits from User Story 2 runtime wiring
- **Polish (Phase 6)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP because model-aware composer gating is the first user-visible multimodal capability
- **User Story 2 (P2)**: Depends on staged attachments existing in the workspace
- **User Story 3 (P3)**: Depends on the canonical attachment model to avoid a separate media path

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
4. Validate model-aware attachment gating and preview before wiring the full backend path

### Incremental Delivery

1. Add capability and attachment contracts
2. Add workspace attachment UX
3. Add runtime/backend multimodal forwarding
4. Add external media handoff reuse

## Notes

- `014` should make imported multimodal models genuinely usable, not add a full gallery or media editor
- Composer additions must stay compact enough to preserve the `013` conversation-first workspace
