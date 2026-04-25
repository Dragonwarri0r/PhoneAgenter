# Feature Specification: Mobile Claw Hub Interop Host Implementation

**Feature Branch**: `025-mobileclaw-interop-host`
**Created**: 2026-04-23
**Status**: Implemented
**Input**: User description: "Implement Mobile Claw as one governed host/provider of the shared Hub Interop protocol so external apps can discover it, request authorization, invoke capabilities, and follow task or artifact flows through the shared public contract."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Discover And Invoke Mobile Claw Through The Shared Public Contract (Priority: P1)

As an external app developer, I want Mobile Claw to expose governed discovery and invocation through the shared public protocol contract, so Mobile Claw behaves as a real protocol implementation rather than only as an internal runtime or share target.

**Why this priority**: The shared contract only becomes meaningful when Mobile Claw implements it as an externally consumable host surface.

**Independent Test**: Use the shared public contract to discover Mobile Claw, inspect at least one callable capability, and submit one governed invocation without relying on share-only heuristics.

**Acceptance Scenarios**:

1. **Given** an external caller discovers Mobile Claw through the shared public contract, **When** it reads Mobile Claw's public surface, **Then** it can understand the supported interaction shapes, callable capability summary, and compatibility or trust metadata.
2. **Given** the caller invokes a supported Mobile Claw capability, **When** Mobile Claw accepts the request, **Then** the request enters the governed runtime with explicit caller, scope, and compatibility semantics intact.

---

### User Story 2 - Govern Authorization, Grant, And Connected-App Behavior (Priority: P1)

As a user, I want Mobile Claw to govern inbound interop requests through authorization and connected-app controls, so external integration stays understandable and revocable rather than becoming an uncontrolled entry point.

**Why this priority**: External interop without authorization, grant tracking, and connected-app management would bypass the very control-center direction the product is built around.

**Independent Test**: Submit one request that requires authorization, then verify Mobile Claw exposes the authorization path, records the connected-app relationship, and allows the flow to continue or remain rejected with explicit reasoning.

**Acceptance Scenarios**:

1. **Given** an external caller lacks the required grant, **When** it requests or triggers an interop action, **Then** Mobile Claw returns an explicit authorization-required outcome rather than silently failing or bypassing governance.
2. **Given** a connected app has trust, compatibility, or enablement limits, **When** Mobile Claw displays or governs that app, **Then** the limitation is visible and manageable through Mobile Claw's governed control model.

---

### User Story 3 - Return Governed Task And Artifact Flows (Priority: P2)

As an external app developer, I want Mobile Claw to return governed task and artifact flows through the shared public contract, so longer-running work does not collapse into one synchronous response.

**Why this priority**: Some protocol interactions will not finish immediately. The host implementation needs to preserve task identity and result handles instead of forcing everything into one inline response.

**Independent Test**: Trigger one Mobile Claw flow that returns a task or artifact path, then verify the caller can follow status and result semantics through the shared public contract.

**Acceptance Scenarios**:

1. **Given** a supported request cannot complete inline, **When** Mobile Claw accepts it, **Then** the host returns a task-oriented continuation path rather than degrading the request into an opaque failure.
2. **Given** the task later completes or produces artifacts, **When** the caller inspects the governed result path, **Then** the caller can access supported result semantics through the shared public contract.

---

### Edge Cases

- What happens when Mobile Claw supports a capability in principle but current trust, approval, or prerequisites prevent execution?
- What happens when Mobile Claw can expose a callable capability but not full task collaboration for that flow?
- What happens when the caller is compatible with the shared public protocol contract but a preferred adapter path is not available on the device?
- What happens when a connected app relationship exists but has been revoked, disabled, or downgraded since the last successful call?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Mobile Claw MUST implement the shared public Hub Interop protocol contract as one externally consumable host or provider surface.
- **FR-002**: Mobile Claw MUST expose a stable public surface through the shared public contract that external Android apps can discover without depending on share-only heuristics.
- **FR-003**: Mobile Claw MUST expose at least one governed callable capability through the shared public contract.
- **FR-004**: Mobile Claw MUST preserve caller identity, scope intent, compatibility information, and governed execution semantics from interop ingress through runtime planning, approval, audit, and explainability surfaces.
- **FR-005**: Mobile Claw MUST support explicit authorization-required outcomes and connected-app relationship handling for governed external requests.
- **FR-006**: Mobile Claw MUST let users inspect and manage interop-related connected-app trust, compatibility, enablement, and recent interaction outcomes through the existing governed control model.
- **FR-007**: Mobile Claw MUST support governed task and artifact continuation semantics for supported flows that do not complete inline.
- **FR-008**: Mobile Claw MUST continue to treat share ingress as a compatibility path rather than as the sole long-term governed interop contract.
- **FR-009**: Mobile Claw MUST surface explicit compatibility or downgrade signals to external callers when an interaction cannot be fully supported.
- **FR-010**: User-visible interop governance explanations exposed by Mobile Claw MUST support English and Simplified Chinese automatically via device locale.

### Key Entities *(include if feature involves data)*

- **HubSurfaceDescriptor**: Mobile Claw's public host surface as seen through the shared public contract.
- **ConnectedAppRecord**: Managed representation of an external caller or peer relationship within Mobile Claw.
- **InteropGrant**: Governed authorization record for interop flows.
- **InteropTaskRecord**: Governed cross-app task continuation path exposed by Mobile Claw.
- **InteropArtifact**: Governed result handle or output exposed by Mobile Claw for supported flows.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: At least one external Android caller can discover Mobile Claw and invoke one governed capability through the shared public contract.
- **SC-002**: At least one authorization-required flow can be surfaced, explained, and continued or rejected through Mobile Claw's governed interop behavior.
- **SC-003**: At least one supported Mobile Claw interop flow can return a governed task or artifact continuation path instead of requiring an inline-only result.
- **SC-004**: Users can inspect connected-app trust, compatibility, or recent interop state through Mobile Claw's existing governed control experience.

## Assumptions

- The shared public protocol contract and Android binding layer from `024` are prerequisites for this host implementation.
- The first host slice can focus on one meaningful governed capability path plus one meaningful authorization and task or artifact path rather than exposing every possible Mobile Claw capability at once.
