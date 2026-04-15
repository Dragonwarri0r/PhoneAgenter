# Feature Specification: Trusted External Handoff Entry

**Feature Branch**: `007-external-runtime-entry`  
**Created**: 2026-04-09  
**Status**: Draft  
**Input**: User description: "Add the first trusted external handoff entry so content can be sent from another Android app into Mobile Claw, normalized into a runtime request, and evaluated through the existing runtime, policy, and capability pipeline."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Send Content to Mobile Claw From Another App (Priority: P1)

As a user, I can hand off content from another Android app into Mobile Claw so the agent can continue the task from outside the internal workspace.

**Why this priority**: This is the first real product path that proves Mobile Claw is more than a workspace-contained demo.

**Independent Test**: Can be fully tested by sending supported text content through the first external entry and verifying that Mobile Claw opens a new runtime session with the incoming content and source attribution visible.

**Acceptance Scenarios**:

1. **Given** a trusted Android app sends supported text content to Mobile Claw, **When** the handoff is received, **Then** Mobile Claw opens or lands on a runtime session that contains the incoming content and source app attribution.
2. **Given** an external handoff is accepted, **When** the runtime session begins, **Then** the request continues through the existing context, policy, capability, and audit pipeline.

---

### User Story 2 - Normalize External Handoffs Before Execution (Priority: P2)

As a platform builder, I can normalize external handoffs into the same runtime request contract used by the internal workspace so downstream runtime layers do not need to special-case Android entry details.

**Why this priority**: The first external path should strengthen the runtime contract, not fork it.

**Independent Test**: Can be tested independently by comparing an internal workspace request and an external handoff and verifying both become the same canonical runtime request shape before planning and execution.

**Acceptance Scenarios**:

1. **Given** a request arrives from the workspace and another arrives from the external entry, **When** both are normalized, **Then** the runtime receives the same canonical request shape with different source metadata.
2. **Given** the incoming handoff payload or caller metadata is incomplete or malformed, **When** normalization runs, **Then** the system returns a safe denial or failure path instead of passing ambiguous state deeper into execution.

---

### User Story 3 - Understand Source and Trust Outcome (Priority: P3)

As a user, I can see which app handed content to Mobile Claw, whether that source was trusted, and why the runtime accepted or denied the request.

**Why this priority**: Once Mobile Claw accepts work from outside apps, source visibility and trust explanation become part of the user experience.

**Independent Test**: Can be tested independently by sending trusted and untrusted external handoffs and verifying that the UI and audit surfaces show source and trust outcomes clearly.

**Acceptance Scenarios**:

1. **Given** a trusted external handoff is accepted, **When** the user inspects runtime status or audit history, **Then** the source app and trust result are visible.
2. **Given** an untrusted or unsupported external handoff is rejected, **When** the user inspects the outcome, **Then** the denial reason is visible and auditable.

---

### Edge Cases

- What happens when an external app hands off unsupported content or omits required payload fields?
- What happens when the caller package is installed but no longer passes signature verification?
- What happens when the inbound request arrives while the app is backgrounded or the selected model is unavailable?
- How does the runtime handle repeated handoffs from the same source when a previous request is still active?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST provide at least one real Android external handoff path that lets a trusted app send supported text-first content into Mobile Claw.
- **FR-002**: The system MUST convert the incoming handoff into the same canonical runtime request contract used by the internal workspace before planning and execution begin.
- **FR-003**: The system MUST attach source metadata to normalized inbound requests, including at minimum source entry type and package identity when available.
- **FR-004**: The system MUST reject or safely fail external handoffs whose required payload or caller metadata is missing, malformed, or unsupported.
- **FR-005**: The system MUST route accepted external handoffs through the existing context loading, policy, capability, and audit flow rather than bypassing current runtime layers.
- **FR-006**: The system MUST allow caller trust evaluation to run against normalized external caller metadata before restricted capability execution proceeds.
- **FR-007**: The system MUST surface source app and trust outcome information through existing runtime status, approval, or audit surfaces for accepted and denied handoffs.
- **FR-008**: The system MUST preserve the existing internal workspace request flow while adding the first external handoff path.
- **FR-009**: The system MUST keep Android entry-point details out of downstream runtime planning and execution layers after normalization.
- **FR-010**: The system MUST let the user see that an incoming external handoff has been received as a new or resumed agent session instead of silently treating it as a hidden internal request.

### Key Entities *(include if feature involves data)*

- **ExternalHandoffPayload**: The incoming content and source fields received from the first supported external entry path.
- **InboundRuntimeRequest**: The normalized request shape created from an external handoff before it enters runtime orchestration.
- **CallerIngressMetadata**: The normalized metadata describing source entry point, package identity, and caller trust inputs.
- **ExternalEntryRegistration**: The description of a supported inbound Android handoff path and the payload it can accept.
- **ExternalInvocationRecord**: The auditable record linking an external handoff to a runtime session outcome.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A supported external Android handoff can create a runtime request that reaches the existing orchestration pipeline without relying on workspace-only marker strings.
- **SC-002**: Trusted and untrusted external handoff flows can be distinguished through normalized caller metadata and produce different policy-visible outcomes during validation.
- **SC-003**: The system can show source and trust information for external handoffs in runtime status or audit surfaces during milestone validation.
- **SC-004**: Existing internal workspace request behavior remains functional after the external entry path is added.

## Assumptions

- The first external entry path is text-first and may be implemented through one Android handoff surface rather than a full third-party SDK.
- The existing runtime, policy, and capability pipeline from `002-005` remains the execution backbone and will be reused.
- Caller trust will continue to build on the package/signature verification work already established in `005`.
- Full structured action payload extraction remains out of scope for this spec and belongs to the next milestone.
