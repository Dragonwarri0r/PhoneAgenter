# Feature Specification: Structured Action Payloads

**Feature Branch**: `008-structured-action-payloads`  
**Created**: 2026-04-09  
**Status**: Draft  
**Input**: User description: "Normalize high-value actions into structured payloads before execution so message, calendar, and share flows become more reliable, previewable, and explainable than raw prompt passthrough."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Preview a Structured Message Before Sending (Priority: P1)

As a user, I can see a structured message draft before Mobile Claw continues into message execution, so I understand who the target is and what content will be sent.

**Why this priority**: Messaging is a high-frequency, high-risk action where raw prompt passthrough is too brittle.

**Independent Test**: Submit a request that maps to `message.send` and verify the runtime produces a structured message draft with visible recipient/content fields before execution continues.

**Acceptance Scenarios**:

1. **Given** a user asks Mobile Claw to send a message, **When** the request is normalized, **Then** the system produces a structured payload that includes at least the intended recipient hint and message body.
2. **Given** a structured message payload is incomplete or ambiguous, **When** execution is evaluated, **Then** the system does not silently execute the raw text request and instead requires preview, clarification, or denial.

---

### User Story 2 - Use Structured Event Fields for Calendar Writes (Priority: P2)

As a user, I can see calendar writes expressed as structured event fields rather than an opaque natural-language string, so the action is easier to verify before it reaches the Android calendar flow.

**Why this priority**: Calendar writes are higher impact and benefit from explicit title/time/description style fields even if extraction is partial.

**Independent Test**: Submit a request that maps to `calendar.write` and verify the runtime produces a structured event payload and uses those fields in preview and downstream execution preparation.

**Acceptance Scenarios**:

1. **Given** a user asks Mobile Claw to create or update a calendar event, **When** the request is normalized, **Then** the system produces a structured event payload with the fields it could confidently extract.
2. **Given** the extracted event fields are missing required scheduling information, **When** the runtime reaches execution gating, **Then** the system does not treat the action as a fully normalized safe write.

---

### User Story 3 - Share Structured Output Instead of Raw Prompt Text (Priority: P3)

As a user, I can see what content Mobile Claw intends to share through external share actions, so I understand the final outbound payload instead of relying on the original natural-language request.

**Why this priority**: Share flows are user-visible and benefit from a clean, explicit outbound payload.

**Independent Test**: Submit a request that maps to `external.share` and verify the runtime produces a structured share payload used for preview and downstream provider execution.

**Acceptance Scenarios**:

1. **Given** a user asks Mobile Claw to share content externally, **When** the request is normalized, **Then** the system produces a structured share payload with the outbound content and any extracted destination hint.
2. **Given** the share request contains sensitive or unsupported content, **When** the structured payload is evaluated, **Then** the policy and preview surfaces reflect the structured interpretation and its constraints.

---

### Edge Cases

- What happens when a request partially matches a structured action but does not provide enough fields to execute safely?
- What happens when the same request contains multiple plausible action targets, such as messaging one person and scheduling another?
- What happens when extraction yields conflicting fields, such as one implied time in text and another in an imported context item?
- What happens when the request should remain a pure text-generation task and not be upgraded into a structured action at all?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST introduce a structured action normalization step between runtime planning and provider execution for at least `message.send`, `calendar.write`, and `external.share`.
- **FR-002**: The system MUST preserve the existing canonical runtime request while producing a separate structured action payload when the selected capability supports structured execution.
- **FR-003**: The system MUST include user-visible payload fields in runtime status, preview, approval, or audit surfaces for structured actions.
- **FR-004**: The system MUST prevent provider execution from relying only on raw user text for structured action types once a structured payload is available.
- **FR-005**: The system MUST record whether a structured payload is complete, partial, or insufficient for safe execution.
- **FR-006**: The system MUST route incomplete or ambiguous structured payloads into an appropriate safety path such as preview, confirmation, or denial instead of silently auto-executing.
- **FR-007**: The system MUST keep pure text-generation or low-risk draft flows working even when no structured payload applies.
- **FR-008**: The system MUST allow the original natural-language request to remain visible for explainability, but structured fields must become the primary execution contract for supported action types.
- **FR-009**: The system MUST keep provider-specific intent or share construction details outside the normalization layer.
- **FR-010**: The system MUST preserve compatibility with the current risk, policy, approval, and capability routing stack.

### Key Entities *(include if feature involves data)*

- **StructuredActionPayload**: The normalized execution payload for a supported action type, including extracted fields, completeness state, and execution-safe values.
- **ActionNormalizationResult**: The result of turning a canonical runtime request into a structured payload or deciding that no structured payload applies.
- **PayloadFieldEvidence**: The explanation of which user text or context signal produced each extracted field.
- **PayloadCompletenessState**: The normalized status of a payload such as `complete`, `partial`, or `insufficient`.
- **StructuredExecutionPreview**: The user-visible summary of the action that will be executed from the structured payload.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Requests for `message.send`, `calendar.write`, and `external.share` can produce structured payloads without relying solely on raw prompt passthrough.
- **SC-002**: The runtime can distinguish complete versus partial payloads and produce different safety-visible outcomes for them.
- **SC-003**: Preview or runtime status surfaces show structured fields for supported actions during milestone validation.
- **SC-004**: Existing non-structured text-generation flows remain functional after structured payload normalization is added.

## Assumptions

- The first milestone focuses on a small set of high-value action types rather than universal action extraction.
- Structured payload extraction may be partial and heuristic-driven in this milestone as long as the resulting completeness state is explicit.
- Provider adapters from `005` will consume structured fields for supported actions after normalization rather than reconstructing everything from raw request text.
- Contacts/calendar deep source ingestion beyond current runtime context remains out of scope for this milestone and belongs to later specs.
