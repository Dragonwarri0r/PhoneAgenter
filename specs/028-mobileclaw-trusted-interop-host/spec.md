# Feature Specification: Mobile Claw Trusted Interop Host

**Feature Branch**: `028-mobileclaw-trusted-interop-host`
**Created**: 2026-04-24
**Status**: Draft
**Input**: User description: "Harden Mobile Claw as the trusted Hub Interop host after public contract stabilization, using host-attested caller identity, governed authorization, durable task/artifact semantics, and a real bounded calendar.read capability."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Trust Host-Attested Caller Identity (Priority: P1)

As a Mobile Claw user, I want the host to verify external callers itself, so a malicious or mistaken app cannot spoof another caller through request payload metadata.

**Why this priority**: The exported provider is a public boundary. Grant lookup, task ownership, artifact access, and audit identity must not depend on caller-claimed Bundle fields.

**Independent Test**: Submit an interop request whose claimed caller metadata names another app, then verify Mobile Claw uses the host-attested caller identity for authorization, ownership, and audit.

**Acceptance Scenarios**:

1. **Given** a caller claims another package name in the request Bundle, **When** the host resolves authorization, **Then** grant lookup uses the host-attested package, UID, and signing digest rather than the claimed package.
2. **Given** a caller cannot be host-attested, **When** it invokes a sensitive method, **Then** Mobile Claw returns an explicit unauthorized outcome and does not enter runtime execution.

---

### User Story 2 - Govern Authorization, Task, And Artifact Ownership (Priority: P1)

As an external app developer, I want authorization, task handles, and artifact handles to have durable and owner-checked semantics, so my app can poll safely without receiving misleading not-found or cross-caller results.

**Why this priority**: Public task and artifact handles are protocol commitments. They need lifecycle and ownership behavior before more capabilities or resources are exposed.

**Independent Test**: Create a task as one caller, then attempt to poll or load it from another caller and verify Mobile Claw returns a forbidden or unauthorized outcome; restart behavior must be recoverable or explicitly expired/not found.

**Acceptance Scenarios**:

1. **Given** a caller's grant has been revoked, **When** it invokes the same capability again, **Then** the host rejects the request with a revocation-aware authorization outcome.
2. **Given** a task handle belongs to caller A, **When** caller B requests the task or artifact, **Then** the host denies access without leaking the result payload.

---

### User Story 3 - Execute Bounded Calendar Read Through The Host (Priority: P2)

As an external app developer, I want to request a bounded `calendar.read` capability through the public protocol, so I can validate Mobile Claw as a real Android local execution host rather than only a reply generator.

**Why this priority**: `generate.reply` proves the chain works, but `calendar.read` proves local data access, permission unavailable, bounded queries, no-result states, artifacts, and audit.

**Independent Test**: From the probe or a contract-only caller, request a `calendar.read` grant, invoke a bounded calendar query, poll the task, load the calendar summary artifact, revoke the grant, and verify a later invocation fails.

**Acceptance Scenarios**:

1. **Given** the user grants `calendar.read`, **When** a caller invokes a bounded calendar query, **Then** the request enters the runtime, returns a task, completes with a calendar summary artifact, and records audit/control visibility.
2. **Given** calendar permission is unavailable on device, **When** the caller invokes `calendar.read`, **Then** the host returns an explicit permission unavailable or provider unavailable outcome rather than a generic failure.

---

### Edge Cases

- The provider cannot determine a verified calling package.
- The calling package has multiple signing certificates or signing history.
- Claimed caller metadata disagrees with host-attested identity.
- A grant request is pending when invocation arrives.
- A task exists but is expired, deleted, or owned by another caller.
- Calendar provider permission is missing, provider query fails, or query returns no results.
- A calendar read request is unbounded or asks for raw full dump behavior.
- Host process restarts after accepting a task.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Mobile Claw Host MUST derive caller identity from the Android provider boundary and MUST NOT trust request Bundle caller metadata as authoritative.
- **FR-002**: Host-attested identity MUST include package name, UID where available, user/profile context where available, signing certificate digest set, and observation timestamps where stored.
- **FR-003**: Claimed caller metadata MUST be preserved only for display, diagnostics, and mismatch warning.
- **FR-004**: Grant lookup, task ownership, artifact access, and audit identity MUST use the host-attested caller fingerprint.
- **FR-005**: Authorization requests MUST have a visible lifecycle that can represent pending, granted, rejected, revoked, and expired states.
- **FR-006**: Mobile Claw MUST continue to reuse existing caller governance and scope grant records as the authorization truth unless a dedicated interop lifecycle record bridges to them.
- **FR-007**: Task and artifact handles MUST be durable or have explicit restart-safe expired/not-found semantics.
- **FR-008**: `get_task` and `get_artifact` MUST enforce ownership checks before returning descriptors or payload summaries.
- **FR-009**: Mobile Claw MUST keep `generate.reply` available as a governed baseline interop capability.
- **FR-010**: Mobile Claw MUST expose bounded `calendar.read` through Hub Interop with grant, permission, provider, no-result, task, artifact, and audit semantics.
- **FR-011**: `calendar.read` requests MUST reject or constrain unbounded reads and raw full-dump output.
- **FR-012**: User-visible audit or control-center summaries MUST show the host-attested caller, capability, authorization state, task, and artifact summary for interop invocations.

### Key Entities *(include if feature involves data)*

- **HostAttestedCallerIdentity**: Host-derived identity containing package, UID, user context, signing digests, timestamps, and fingerprint.
- **ClaimedCallerMetadata**: Caller-provided display or diagnostic metadata that is not used for trust decisions.
- **InteropGrantRequestRecord**: Lifecycle record for an external authorization request that bridges to existing governance grants.
- **InteropTaskRecord**: Owner-checked task lifecycle record for accepted interop work.
- **InteropArtifactRecord**: Owner-checked artifact lifecycle record for task outputs.
- **CalendarReadInteropRequest**: Bounded read request mapped into the runtime's calendar read provider.
- **CalendarSummaryArtifact**: Redacted caller-visible summary of calendar read results.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Spoofed caller metadata cannot reuse another caller's grant in host unit tests.
- **SC-002**: Cross-caller task and artifact access returns an explicit forbidden or unauthorized outcome in host tests.
- **SC-003**: Revoked callers cannot invoke `generate.reply` or `calendar.read` without reauthorization.
- **SC-004**: A bounded `calendar.read` flow can create a task, complete or fail with explicit provider/permission/no-result semantics, and produce a calendar summary artifact when data is available.
- **SC-005**: Audit or control-center summaries include host-attested caller identity for interop invocations.

## Assumptions

- `027-public-interop-contract-stabilization` has already stabilized required public statuses and descriptors.
- Android custom permission may be added later as defense-in-depth, but host-attested identity plus protocol grants remain the primary authorization model.
- `calendar.read` is the only new capability exposed in this feature.
- If durable Room records are added, the existing `MemoryDatabase` schema version must be bumped in the same implementation patch.
