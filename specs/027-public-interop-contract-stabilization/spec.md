# Feature Specification: Public Hub Interop Contract Stabilization

**Feature Branch**: `027-public-interop-contract-stabilization`
**Created**: 2026-04-24
**Status**: Implemented
**Input**: User description: "Stabilize the Hub Interop public protocol after the 024-026 implementation pass so third-party apps can depend on the contract modules before host hardening and probe conformance expand the surface."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Rely On Stable Public Method And Status Contracts (Priority: P1)

As an Android app developer, I want Hub Interop public methods, status codes, and response meanings to be stable and precise, so my app can call a compatible host without reverse-engineering host behavior.

**Why this priority**: Public interop becomes dangerous if callers cannot distinguish authorization, ownership, expiry, compatibility, and execution failures.

**Independent Test**: Build a caller using only the contract modules, then verify every public method can produce and interpret the documented status outcomes without depending on `:app`.

**Acceptance Scenarios**:

1. **Given** a caller receives an artifact handle that does not exist, **When** it calls `get_artifact`, **Then** the public status clearly reports not found rather than an authorization or execution failure.
2. **Given** a caller lacks a trusted identity or grant, **When** it invokes a capability, **Then** the public status distinguishes unauthorized, authorization required, pending, forbidden, and denied outcomes.

---

### User Story 2 - Publish Stable Descriptor V1 Shapes (Priority: P1)

As a host or caller implementer, I want stable descriptor shapes for surfaces, capabilities, grants, tasks, artifacts, and compatibility, so host implementations and external clients can evolve without leaking host internals.

**Why this priority**: The current modules expose useful first-pass models, but the public descriptor fields need to be explicit enough for third-party callers before more capabilities are exposed.

**Independent Test**: Validate representative surface, capability, grant, task, artifact, and compatibility descriptors against the contract validator and roundtrip them through Android Bundle codecs.

**Acceptance Scenarios**:

1. **Given** a host exposes `generate.reply` and `calendar.read`, **When** a caller reads the capability descriptors, **Then** it sees capability identity, user-facing label, input schema version, output artifact types, side-effect level, sensitivity, boundedness, grant and approval requirements, availability, and compatibility without provider internals.
2. **Given** a task or artifact descriptor is returned to a caller, **When** it is serialized through the Android binding, **Then** ownership-sensitive details remain opaque while lifecycle and availability are still understandable.

---

### User Story 3 - Make Compatibility Evolution Explicit (Priority: P2)

As a protocol maintainer, I want version and unknown-field behavior to be explicit, so minor evolution can be diagnosed or downgraded without treating every extension as a breaking change.

**Why this priority**: Without a clear compatibility policy, the first real third-party integration will either silently break or force every future field into a major version bump.

**Independent Test**: Exercise supported, minor-newer, major-mismatch, required-unknown, optional-unknown, and extension-namespace cases through shared evaluator and Android compatibility bundles.

**Acceptance Scenarios**:

1. **Given** a caller requests a newer minor protocol version, **When** the host supports the same major version, **Then** the compatibility signal reports a downgraded but diagnosable interaction.
2. **Given** a request includes unknown required fields, **When** the contract evaluator inspects the request metadata, **Then** the result is incompatible with a reason that callers can display or log.

---

### Edge Cases

- A caller uses a supported protocol core but an older Android binding helper.
- A caller sends malformed version text.
- A host supports a method family but not a specific capability.
- A request contains optional unknown fields that should be ignored with warning rather than rejected.
- A request contains extension namespace fields that should be preserved where possible.
- A caller receives an expired task or artifact handle that must not be collapsed into generic not found.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The public contract MUST define one stable method family for discovery, authorization request, grant status, grant revoke, capability invocation, task lookup, and artifact lookup.
- **FR-002**: The public Android binding MUST expose a status taxonomy that distinguishes OK, bad request, unauthorized, authorization required, authorization pending, forbidden, not found, expired, incompatible version, unsupported capability, provider unavailable, permission unavailable, policy denied, approval required, approval rejected, execution failed, and internal error.
- **FR-003**: The shared contract MUST define stable v1 descriptor fields for surface, capability, grant, task, artifact, and compatibility results.
- **FR-004**: Capability descriptors MUST describe caller-visible behavior without exposing host-only provider implementation details.
- **FR-005**: Task and artifact descriptors MUST expose lifecycle and availability semantics that allow callers to distinguish available, expired, deleted, not found, and forbidden outcomes.
- **FR-006**: Compatibility evaluation MUST explicitly cover supported, downgraded, incompatible, malformed version, major mismatch, minor newer, required unknown field, optional unknown field, and extension namespace cases.
- **FR-007**: Android Bundle codecs MUST roundtrip all stabilized request and response shapes without losing required public fields.
- **FR-008**: Contract modules MUST remain independent of `:app`, Room, Hilt, Compose, Mobile Claw policy implementation, and concrete provider implementations.
- **FR-009**: Documentation examples MUST use the same method names, authority convention, status values, and descriptor fields as the code.
- **FR-010**: The `024` and `025` task checklists MUST be reconciled with their implemented spec status so roadmap progress is not misleading.

### Key Entities *(include if feature involves data)*

- **PublicMethodContract**: Stable set of Android-callable method names and request/response families.
- **StatusCodeContract**: Public outcome taxonomy shared by caller, host, and probe.
- **CapabilityDescriptorV1**: Caller-visible capability metadata including side effect, sensitivity, boundedness, availability, grants, approvals, and artifacts.
- **GrantDescriptorV1**: Public grant or authorization request state visible to external callers.
- **TaskDescriptorV1**: Public task handle state, lifecycle, updated time, and artifact references.
- **ArtifactDescriptorV1**: Public artifact handle state, type, access mode, lifecycle, and availability.
- **CompatibilityPolicyV1**: Version and unknown-field rules used by all public consumers.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A third-party caller can compile and roundtrip every stabilized request and response shape using only `:hub-interop-contract-core` and `:hub-interop-android-contract`.
- **SC-002**: Unit tests cover supported, downgraded, incompatible, malformed version, required unknown, optional unknown, and extension namespace compatibility cases.
- **SC-003**: Public status tests demonstrate distinct outcomes for unauthorized, authorization required, pending, forbidden, not found, expired, unsupported capability, provider unavailable, permission unavailable, and execution failed.
- **SC-004**: Documentation and task checklists no longer contradict the implemented status of `024` and `025`.

## Assumptions

- `024-026` remain the first implemented interop slice and are treated as prerequisites.
- This feature stabilizes contract modules only; Mobile Claw host behavior changes are handled by `028`.
- This feature does not publish an external artifact outside the repository yet, but it keeps the module boundary ready for future distribution.
