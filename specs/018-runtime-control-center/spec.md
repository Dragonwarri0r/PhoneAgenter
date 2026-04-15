# Feature Specification: Runtime Control Center

**Feature Branch**: `018-runtime-control-center`  
**Created**: 2026-04-13  
**Status**: Draft  
**Input**: User description: "Add a conversation-first runtime control center so Mobile Claw can clearly display and let users inspect and edit runtime state, memory, approvals, tools, and extensions inside the app while the built-in multimodal chat remains the primary testing and management entry."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Use Chat As The Primary Control Entry (Priority: P1)

As a user, I want the built-in multimodal chat to remain the main place where I start work, test capabilities, and open deeper control views, so Mobile Claw feels like one coherent agent product instead of a chat screen plus scattered management pages.

**Why this priority**: If the control surface displaces the conversation or turns the app into a dashboard, the product loses its primary interaction model. This story preserves the intended product character while adding manageability.

**Independent Test**: Open the app, submit text and media-backed requests from the built-in chat, and verify the user can reach deeper control details for the active request without leaving or collapsing the main conversation flow.

**Acceptance Scenarios**:

1. **Given** the user opens Mobile Claw, **When** the main workspace renders, **Then** the built-in chat remains the primary surface and runtime control entry points are reachable without replacing the conversation.
2. **Given** the user submits a text, image, or audio-backed request, **When** the active session updates, **Then** the user can open related control details from the same conversation context rather than navigating to a separate management area first.

---

### User Story 2 - Read A Coherent Runtime Trace (Priority: P2)

As a user, I want to clearly see what the system used and decided for a request, so I can understand the source, tool path, approval state, memory/context contribution, and extension involvement without reconstructing it from disconnected panels.

**Why this priority**: Mobile Claw already has several explainability signals, but they are still fragmented. This story turns them into a readable, trust-building trace.

**Independent Test**: Complete requests that trigger normal execution, approval-gated execution, degraded execution, and externally sourced execution, then verify the user can read one coherent trace of what happened and why.

**Acceptance Scenarios**:

1. **Given** a request has been planned or executed, **When** the user opens the control details for that request, **Then** the app shows a coherent trace covering request source, selected action or tool path, approval or denial outcome, and major context contributors.
2. **Given** a request is awaiting approval, denied, degraded, or otherwise constrained, **When** the user inspects its control details, **Then** the limiting condition and next relevant user action are visible in the same trace rather than hidden behind separate surfaces.

---

### User Story 3 - Inspect And Edit Supported Managed Artifacts In One Place (Priority: P3)

As a user, I want to inspect and edit supported managed artifacts such as memory management state, caller governance settings, and extension enablement from one coherent in-app control surface, so I can manage the agent without hunting across disconnected sheets.

**Why this priority**: Reading the system is not enough. The app also needs to become the place where the user actively manages the agent's behavior and available surfaces.

**Independent Test**: Open supported managed artifacts from the control center, adjust at least one editable item in each covered family, and verify the changes persist and remain understandable when the user revisits them later.

**Acceptance Scenarios**:

1. **Given** a managed artifact supports editing, **When** the user opens it from the control center, **Then** the app presents the supported edit actions in context and persists the result locally.
2. **Given** a managed artifact is unavailable, degraded, or not editable, **When** the user opens its detail, **Then** the app explains the limitation clearly instead of presenting dead or misleading controls.

---

### Edge Cases

- What happens when the app has no active conversation yet but the user still wants to inspect global control state?
- What happens when a recent request has no meaningful tool, memory, or extension contribution and the trace would otherwise appear empty?
- What happens when multiple relevant contributors exist for one request, such as caller metadata, memory items, approval state, system sources, and extension activity at the same time?
- What happens when a managed artifact appears in recent history but is no longer available because permissions were revoked, a source disappeared, or an extension became incompatible?
- What happens when the user is actively chatting while a managed artifact is being edited and the base conversation should remain stable?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST provide an in-app runtime control center that keeps the built-in chat as the primary interaction surface rather than replacing it with a standalone dashboard.
- **FR-002**: The runtime control center MUST let users open current-session or recent-session details for runtime state, request source, selected action or tool path, approval state, memory or context contribution, and extension contribution from the active conversation flow.
- **FR-003**: The system MUST present a coherent runtime trace for an active or recent request so users can understand what was requested, what was chosen, what was allowed or denied, and what major contributors were involved.
- **FR-004**: Supported multimodal chat requests MUST remain usable as the primary in-app path for testing and validating runtime behavior, with related control details reachable from the same session.
- **FR-005**: Users MUST be able to inspect and edit supported managed artifacts from the control center, including at minimum memory-management state, caller governance settings, and extension enablement state when those artifact families are available.
- **FR-006**: When a contributor or managed artifact is unavailable, degraded, incompatible, or not editable, the control center MUST explain the limitation clearly instead of presenting it as if it can be changed.
- **FR-007**: The control center MUST preserve conversation usability while deeper management views are opened, dismissed, refreshed, or updated.
- **FR-008**: The control center MUST provide a stable way to review pending approval context and recent approval outcomes in the same management language used for the rest of the runtime trace.
- **FR-009**: User-facing labels and explanations for runtime control, trace, managed artifacts, and multimodal testing MUST support English and Simplified Chinese automatically via device locale.
- **FR-010**: This milestone MUST remain local-first and MUST not require cloud administration or remote orchestration to inspect or edit supported artifacts.
- **FR-011**: This milestone MUST build on existing runtime, governance, tool, multimodal, and extension contracts rather than redefining them through a second management model.

### Key Entities *(include if feature involves data)*

- **RuntimeControlCenterState**: User-visible state that connects the active conversation, runtime trace, and available management entries into one coherent control surface.
- **RuntimeTraceSnapshot**: Readable summary of how one request moved through source recognition, action or tool selection, approval state, context contribution, and extension participation.
- **ManagedArtifactEntry**: Inspectable and possibly editable item representing a supported runtime-managed object such as a memory record, caller governance record, or extension registration.
- **ArtifactEditCapability**: User-visible description of whether an artifact can be edited, which changes are supported, and why edits may be unavailable.
- **MultimodalTestSession**: Active in-app conversation session that can send text, image, or audio-backed requests and then expose related runtime control details from the same flow.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can submit a text, image, or audio-backed request and inspect its related runtime trace from the same in-app flow without leaving the active conversation.
- **SC-002**: At least three managed artifact families can be inspected from one coherent control surface, and supported edits persist and remain understandable when revisited.
- **SC-003**: Users can determine for a recent request which action or tool path, approval outcome, major context contributors, and extension contributions were involved without opening unrelated screens.
- **SC-004**: The default app experience remains conversation-first and does not require users to learn a separate settings or dashboard flow to test or manage core agent behavior.

## Assumptions

- Existing detail sheets and management views from earlier milestones may be consolidated, restyled, or re-entry-wired instead of being rewritten from scratch.
- The first version only needs to expose supported artifact families that already have stable contracts or edit semantics; hidden internal diagnostics remain out of scope.
- The runtime trace focuses on active and recent requests rather than acting as a full developer log viewer.
- This milestone depends on the current direction of standardized tool, interop, governance, multimodal, and extension work remaining the source of truth for managed behavior.
