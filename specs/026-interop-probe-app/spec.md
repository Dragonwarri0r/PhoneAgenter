# Feature Specification: Hub Interop Probe App

**Feature Branch**: `026-interop-probe-app`
**Created**: 2026-04-23
**Status**: Implemented
**Input**: User description: "Build a separate app that validates the shared Hub Interop protocol against Mobile Claw. The app should consume the shared public protocol contract directly and verify discovery, authorization, invocation, task, and artifact flows without depending on Mobile Claw internals."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Discover Mobile Claw As An External Consumer (Priority: P1)

As a platform developer, I want a separate probe app to discover Mobile Claw through the shared public protocol contract, so we can prove the contract is externally consumable instead of only documented.

**Why this priority**: Without a real external consumer, it is too easy for the host implementation to leak internal assumptions into the so-called public contract.

**Independent Test**: Install the probe app alongside Mobile Claw, then verify the probe app can discover Mobile Claw and read its public surface using only the shared public protocol contract.

**Acceptance Scenarios**:

1. **Given** the probe app is installed with Mobile Claw, **When** it attempts protocol discovery, **Then** it can read Mobile Claw's public surface and compatibility information through the shared public contract.
2. **Given** Mobile Claw is unavailable, incompatible, or partially supported, **When** the probe app performs discovery, **Then** the app surfaces an explicit compatibility or availability outcome rather than assuming success.

---

### User Story 2 - Exercise Governed Authorization And Invocation (Priority: P1)

As a platform developer, I want the probe app to exercise governed authorization and invocation flows against Mobile Claw, so protocol validation includes the real permission and grant path instead of only a happy-path direct call.

**Why this priority**: Authorization is a core protocol feature. If the probe app cannot validate it externally, we still do not know whether the contract is externally usable.

**Independent Test**: Trigger one authorization-required flow from the probe app, complete or reject the authorization path, then verify the probe app can observe the resulting governed outcome and continue or remain blocked accordingly.

**Acceptance Scenarios**:

1. **Given** the probe app requests a governed capability without the needed grant, **When** Mobile Claw responds, **Then** the probe app can observe and follow the explicit authorization-required path.
2. **Given** authorization is granted or denied, **When** the probe app retries or resumes the flow, **Then** the resulting success, rejection, or downgrade remains explicit and explainable.

---

### User Story 3 - Validate Task, Artifact, And Contract Drift Signals (Priority: P2)

As a platform developer, I want the probe app to validate task, artifact, and compatibility behavior, so we can detect contract drift between the shared public protocol contract and the Mobile Claw implementation early.

**Why this priority**: Discovery and one invocation are not enough. The separate consumer app should also prove that continuation handles and compatibility signals survive outside the host app.

**Independent Test**: Exercise one supported task or artifact path from the probe app, then verify the app can observe explicit task or artifact semantics and surface compatibility failures when the host and contract drift apart.

**Acceptance Scenarios**:

1. **Given** a Mobile Claw flow returns a task or artifact continuation, **When** the probe app follows that continuation, **Then** it can observe supported status and result semantics through the shared public contract.
2. **Given** the shared public contract and Mobile Claw implementation drift apart, **When** the probe app exercises a supported flow, **Then** the incompatibility appears as an explicit contract or compatibility failure rather than hidden runtime breakage.

---

### Edge Cases

- What happens when the probe app can use the shared public protocol contract but not a preferred Android adapter path on the device?
- What happens when the probe app and Mobile Claw reference different compatible but non-identical public contract versions?
- What happens when a governed request remains permanently denied or pending?
- What happens when a task continuation exists but the host implementation no longer supports resuming it?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST provide a separate probe app that consumes the shared public Hub Interop protocol contract as an external consumer.
- **FR-002**: The probe app MUST discover Mobile Claw through the shared public contract without depending on Mobile Claw host-internal implementation classes.
- **FR-003**: The probe app MUST exercise at least one governed authorization and invocation flow against Mobile Claw.
- **FR-004**: The probe app MUST exercise at least one task or artifact continuation flow against Mobile Claw when such a flow is exposed by the current host slice.
- **FR-005**: The probe app MUST surface explicit compatibility, downgrade, unavailability, or rejection outcomes rather than assuming happy-path success.
- **FR-006**: The probe app MUST validate the externally consumable protocol path only and MUST NOT depend on Mobile Claw host-internal runtime, repository, governance, or UI implementation details.
- **FR-007**: User-visible probe-app validation output MUST support English and Simplified Chinese automatically via device locale.

### Key Entities *(include if feature involves data)*

- **InteropProbeApp**: Separate external consumer used to validate the shared public Hub Interop contract against Mobile Claw.
- **ProbeValidationFlow**: Explicit validation path for discovery, authorization, invocation, task, artifact, and compatibility outcomes.
- **CompatibilitySignal**: Public compatibility metadata surfaced to the probe app when a flow is supported, downgraded, or incompatible.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The probe app can discover Mobile Claw through the shared public protocol contract without copying host implementation constants.
- **SC-002**: The probe app can complete or explicitly fail one governed authorization plus invocation flow against Mobile Claw without depending on host-internal classes.
- **SC-003**: The probe app can follow at least one task or artifact continuation path, or explicitly surface why that path is unavailable in the current host slice.
- **SC-004**: The probe app surfaces at least one compatibility, downgrade, or rejection outcome as an explicit external-consumer-facing result rather than hidden runtime breakage.

## Assumptions

- The shared public protocol contract from `024` and the Mobile Claw host implementation from `025` are prerequisites for the probe app.
- The probe app is a validation consumer first; it does not need to replicate the full Mobile Claw product surface.
