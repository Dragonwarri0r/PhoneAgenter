# Feature Specification: Multimodal Ingress And Composer

**Feature Branch**: `014-multimodal-ingress-and-composer`  
**Created**: 2026-04-10  
**Status**: Draft  
**Input**: User description: "Add model-aware image/audio input, attachment preview, and multimodal request normalization so imported multimodal models can actually use media instead of behaving like text-only models."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Attach Image Or Audio From The Workspace Composer (Priority: P1)

As a user, I want the workspace composer to expose image and audio attachment entry points only when my selected model supports them, so I can add media without turning the workspace into a permanent media dashboard.

**Why this priority**: This is the first visible product outcome of `014`. Without model-aware composer gating, multimodal support stays invisible or misleading.

**Independent Test**: Select a multimodal-capable model, attach an image or audio file from the composer, and verify the workspace shows a compact preview with remove support. Then select a text-only model and verify those entry points are hidden or disabled.

**Acceptance Scenarios**:

1. **Given** the selected model supports images, **When** the workspace composer renders, **Then** an image attachment entry is visible and the user can attach a local image with a lightweight preview.
2. **Given** the selected model does not support a media type, **When** the workspace composer renders, **Then** the unsupported media entry is hidden or clearly disabled rather than implying it will work.

---

### User Story 2 - Normalize Multimodal Requests Into Runtime Input (Priority: P2)

As a user, I want attached media to become part of the runtime request instead of staying only in the UI, so the agent can use both my text and selected media during generation.

**Why this priority**: Multimodal UI alone is not enough; the runtime and local generation backend must receive normalized image/audio inputs.

**Independent Test**: Submit a request with attached media and verify the runtime request, provider path, and local generation gateway all receive normalized multimodal content instead of only the original text.

**Acceptance Scenarios**:

1. **Given** the user submits text plus one or more supported attachments, **When** the runtime request is created, **Then** the request contains normalized multimodal attachment metadata.
2. **Given** the request reaches the local generation path, **When** the provider executes, **Then** the multimodal attachments are forwarded using the backend’s image/audio content types rather than being silently discarded.

---

### User Story 3 - Accept External Shared Media Through The Same Attachment Semantics (Priority: P3)

As a user, I want media shared from other Android apps to arrive through the same attachment model as composer-selected media, so external handoff and in-workspace creation feel consistent.

**Why this priority**: `007` added trusted external handoff for text. Multimodal support is incomplete if external media enters through a separate ad hoc path.

**Independent Test**: Share an image or audio item into Mobile Claw, land in the workspace, and verify the resulting session shows the media as normalized attachments that can flow through the same runtime path.

**Acceptance Scenarios**:

1. **Given** another app shares an image or audio item to Mobile Claw, **When** the handoff is accepted, **Then** the workspace shows a new session with normalized attachment previews and source metadata.
2. **Given** external media arrives but the selected model lacks support for that modality, **When** the handoff is processed, **Then** the workspace explains the limitation safely instead of pretending multimodal execution will continue.

---

### Edge Cases

- What happens when a shared media URI cannot be opened or copied into app-managed storage?
- What happens when the user attaches media and then switches to a model that does not support that modality?
- What happens when the request contains both text and media, but the text is blank?
- What happens when no model is ready yet and an external media handoff arrives first?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The workspace MUST expose image and audio attachment entry points based on the capabilities of the currently selected model.
- **FR-002**: The workspace MUST allow users to preview and remove pending attachments before sending a request.
- **FR-003**: The system MUST normalize attached image and audio inputs into the runtime request contract instead of leaving them only in UI state.
- **FR-004**: The local generation path MUST forward normalized multimodal inputs to the LiteRT-LM backend using its supported image/audio content types when applicable.
- **FR-005**: External Android share media handoffs MUST map into the same attachment model used by the in-workspace composer.
- **FR-006**: If the selected model does not support a media type, the product MUST explain the limitation and avoid pretending the unsupported attachment will be used.
- **FR-007**: The workspace MUST keep multimodal entry lightweight, with compact previews and removable attachments rather than a persistent media control panel.
- **FR-008**: User-facing multimodal labels and explanations MUST support English and Simplified Chinese automatically via device locale.
- **FR-009**: This milestone MUST remain local-first and MUST store transient attachment copies only on-device.
- **FR-010**: This milestone MUST reuse existing runtime, policy, and workspace flows instead of creating a separate multimodal execution stack.

### Key Entities *(include if feature involves data)*

- **ModelModalityCapabilities**: Presentation/runtime model describing whether the selected model supports text-only, image, and audio inputs.
- **PendingAttachment**: User-visible pending image or audio item with preview metadata, local storage reference, and removability state.
- **RuntimeAttachment**: Canonical runtime request attachment carrying modality, MIME type, local file path, and source metadata.
- **ExternalMediaHandoffPayload**: External handoff payload that combines optional shared text with one or more normalized media attachments.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: When a multimodal-capable model is selected, users can attach at least one image and one audio item from the composer and see compact removable previews.
- **SC-002**: Submitted multimodal requests reach the runtime/provider/gateway path with normalized attachment metadata instead of text-only fallback behavior.
- **SC-003**: External image/audio share intents can land in the workspace and reuse the same attachment semantics as internal composer attachments.
- **SC-004**: Multimodal labels and capability messaging render correctly in both English and Simplified Chinese.

## Assumptions

- Imported `.litertlm` models can expose coarse modality capabilities through app-managed metadata even if the underlying runtime does not provide rich self-describing capability introspection.
- The first version only needs compact attachment preview metadata and app-managed local file copies; it does not need waveform rendering or image editing.
- The first version can bound attachment count and avoid building a full asset library.
- Existing policy/risk behavior remains text/action-driven in this milestone; `014` focuses on ingress, normalization, and backend handoff.
