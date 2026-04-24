# Feature Specification: Shared Hub Interop Contract And Android Binding

**Feature Branch**: `024-shared-interop-contract`
**Created**: 2026-04-23
**Status**: Implemented
**Input**: User description: "Isolate the Hub Interop protocol itself into a shared public contract that external apps can reference directly. Mobile Claw is only one implementation of that protocol, and Android `v1` needs a reusable binding layer that both the host and outside apps can consume."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consume One Shared Public Protocol Contract (Priority: P1)

As an Android app developer, I want a shared public protocol contract that is isolated from any single host implementation, so I can integrate with a Hub Interop implementation without copying constants or depending on host-only code.

**Why this priority**: If the shared contract is not independently consumable, the protocol collapses into documentation plus host internals instead of becoming a real reusable integration surface.

**Independent Test**: Reference the shared public contract from two independent Android modules or apps, then verify both can resolve the same identifiers, handle semantics, and compatibility metadata without copying implementation details.

**Acceptance Scenarios**:

1. **Given** an external Android app wants to integrate with a Hub Interop implementation, **When** it references the shared public contract, **Then** it can discover public identifiers, handle semantics, and supported request or response conventions without depending on host-internal classes.
2. **Given** Mobile Claw also implements the same protocol, **When** it references the shared public contract, **Then** the host and external consumer use the same versioned contract surface rather than parallel duplicated definitions.

---

### User Story 2 - Consume A Stable Android Binding Layer (Priority: P1)

As an Android app developer, I want a stable Android-specific binding layer on top of the shared protocol contract, so I can use supported authorities, URIs, method families, and status semantics through one public integration surface.

**Why this priority**: The first platform slice is Android. Even with a shared protocol core, callers still need one stable Android-facing contract instead of reverse-engineering transport details from the host app.

**Independent Test**: Reference the Android binding layer from an external Android caller, then verify the caller can construct supported discovery, invocation, authorization, and task-handle requests using the public binding definitions alone.

**Acceptance Scenarios**:

1. **Given** an Android caller needs to interact with a protocol implementation, **When** it uses the public Android binding layer, **Then** it can construct supported public requests and interpret supported response states without hard-coding opaque host-owned strings.
2. **Given** a preferred Android adapter path is unavailable to a caller, **When** the caller falls back to the baseline Android transport defined by the binding layer, **Then** protocol semantics remain consistent and explainable.

---

### User Story 3 - Surface Explicit Version And Compatibility Signals (Priority: P2)

As an Android app developer or platform maintainer, I want explicit version and compatibility signals in the shared public contract, so contract drift is detectable before it becomes silent runtime breakage.

**Why this priority**: The protocol will be consumed by multiple apps. Without explicit shared compatibility semantics, the first mismatch turns into fragile undefined behavior.

**Independent Test**: Represent one supported and one downgraded compatibility case through the shared public contract, then verify a caller can distinguish them through explicit compatibility signals.

**Acceptance Scenarios**:

1. **Given** a caller and implementation support the same contract slice, **When** the caller inspects the public compatibility signal, **Then** the caller can recognize the interaction as supported.
2. **Given** a caller and implementation do not fully align, **When** the caller inspects the public compatibility signal, **Then** the downgrade or incompatibility is visible through explicit public metadata instead of silent failure.

---

### Edge Cases

- What happens when a caller supports the shared protocol contract but not the full Android binding slice?
- What happens when the Android binding layer evolves while the shared protocol core remains compatible?
- What happens when a host implementation supports only part of the public method family?
- What happens when two apps reference different public contract versions at the same time?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST provide a shared public Hub Interop protocol contract that is isolated from any single host implementation.
- **FR-002**: The shared public protocol contract MUST be consumable by both Mobile Claw and external Android apps without duplicating protocol identifiers or handle semantics.
- **FR-003**: The shared public protocol contract MUST expose stable protocol-level entities for surfaces, capabilities, grants, tasks, artifacts, and compatibility signals.
- **FR-004**: The first Android slice MUST provide a stable Android binding layer on top of the shared public protocol contract for discovery, governed invocation, authorization, task status, and artifact or resource handle flows.
- **FR-005**: The Android binding layer MUST define stable public identifiers for supported Android-facing authorities, handle families, request or response conventions, and status semantics.
- **FR-006**: The shared public protocol contract MUST remain narrower than any host implementation and MUST NOT expose host-only execution, governance, persistence, model-serving, or UI internals.
- **FR-007**: The shared public protocol contract MUST support explicit versioning and compatibility signaling that external callers can interpret without relying on host-specific code.
- **FR-008**: The Android binding layer MUST support a baseline transport path that remains available even when a preferred Android adapter path is unavailable or unsupported for a caller.
- **FR-009**: The first Android-facing shared protocol contract MUST support English and Simplified Chinese user-visible compatibility or contract explanations via device locale where such explanations are exposed to users.

### Key Entities *(include if feature involves data)*

- **SharedPublicProtocolContract**: Versioned public contract surface shared across implementations and consumers.
- **AndroidBindingContract**: Stable Android-facing binding layer that defines authorities, handle families, request or response conventions, and status semantics on top of the shared protocol contract.
- **InteropHandleContract**: Public definition of task, artifact, grant-request, and connected-app handle semantics.
- **CompatibilitySignal**: Public compatibility metadata that explains support, downgrade, or incompatibility outcomes.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Mobile Claw and at least one separate Android consumer can both reference the same shared public protocol contract without duplicating opaque implementation constants.
- **SC-002**: An external Android caller can construct at least one supported discovery flow and one governed invocation flow using only the shared public contract and Android binding layer.
- **SC-003**: At least one compatibility downgrade or mismatch can be surfaced through explicit public compatibility metadata rather than silent runtime failure.

## Assumptions

- The shared public protocol contract is the prerequisite for both the Mobile Claw host implementation and the independent probe app.
- The exact packaging and publication mechanics can evolve during planning and implementation as long as the shared public contract remains independently consumable and isolated from host internals.
