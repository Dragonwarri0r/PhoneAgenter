# Feature Specification: External Caller Interop Contracts

**Feature Branch**: `016-external-caller-interop-contracts`  
**Created**: 2026-04-13  
**Status**: Draft  
**Input**: User description: "Continue the roadmap and define stable app-to-app and agent-to-agent calling contracts so external integrations do not fragment into one-off adapters."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Accept External Requests Through Stable Inbound Contracts (Priority: P1)

As a user, I want Mobile Claw to accept external requests through a stable contract instead of per-app special cases, so sending something from another app feels consistent and trustworthy.

**Why this priority**: This is the first user-visible outcome of `016`. Without a stable inbound contract, every new caller becomes another adapter branch and the product gets harder to reason about.

**Independent Test**: Trigger inbound requests from at least two external entry styles, such as text/media share and structured external invocation, and verify they normalize into the same canonical handoff metadata shape.

**Acceptance Scenarios**:

1. **Given** an external app shares text or media into Mobile Claw, **When** the request is accepted, **Then** the inbound request is normalized into a stable interop contract with caller, trust, and URI grant metadata.
2. **Given** an external caller lacks enough metadata to be trusted automatically, **When** the handoff is processed, **Then** the request still lands in a safe unverified state rather than bypassing governance.

---

### User Story 2 - Reuse Stable Caller Identity, Trust, And Grant Semantics Across Entry Types (Priority: P2)

As a user, I want caller identity, trust state, and content grant semantics to mean the same thing across external entry types, so the runtime does not explain the same source differently depending on how it arrived.

**Why this priority**: `007` proved the first trusted handoff path, but `016` is where that trust model becomes portable across multiple external contracts.

**Independent Test**: Trigger multiple inbound contracts that carry different caller metadata and verify the same trust states, source labels, package identity, and grant summaries are used consistently in runtime state, approval, and governance.

**Acceptance Scenarios**:

1. **Given** two different external entry contracts from the same app, **When** Mobile Claw processes them, **Then** the same caller identity and trust semantics are applied.
2. **Given** a handoff includes granted content URIs, **When** the request is normalized, **Then** URI grant metadata is preserved and explainable rather than discarded.

---

### User Story 3 - Expose A Minimal Stable Callable Surface For Future External Agents (Priority: P3)

As a platform user, I want Mobile Claw to define a minimal stable callable contract for future assistant or agent callers, so future integrations do not depend only on implicit Android share behavior.

**Why this priority**: If Mobile Claw only standardizes inbound share, it remains a special-case app target. This milestone defines the first general interop contract without swallowing the full extension system.

**Independent Test**: Validate that a future callable request can be represented by a documented interop envelope carrying canonical request fields, caller identity, scopes, and attachment grants without inventing a new runtime path.

**Acceptance Scenarios**:

1. **Given** a future external caller wants to invoke Mobile Claw using structured fields, **When** the request is described against the interop contract, **Then** it maps into the same runtime request shape as existing handoff flows.
2. **Given** a callable contract requests restricted scopes, **When** Mobile Claw evaluates it, **Then** the governance and policy layers can process it using the same caller and scope semantics as other external requests.

---

### Edge Cases

- What happens when an external request contains partial caller identity but valid URI grants?
- What happens when different entry types provide conflicting package or referrer metadata?
- What happens when a caller can hand off text safely but not restricted scopes?
- What happens when an inbound contract references attachments or grants that expire before execution?
- What happens when a future callable request shape includes fields unknown to the current runtime version?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST define a stable inbound interop contract for external caller requests instead of relying only on per-entry parsing rules.
- **FR-002**: The interop contract MUST carry canonical caller identity metadata including entry type, origin app, package identity, trust state, trust reason, and URI grant summary when available.
- **FR-003**: The system MUST normalize share-based text and media handoffs into the same interop request family used by future structured external callers.
- **FR-004**: The system MUST preserve explainable URI/content grant metadata for inbound requests that rely on cross-app content access.
- **FR-005**: Governance, policy, and approval flows MUST evaluate external callers through the same stable caller identity and scope semantics regardless of entry type.
- **FR-006**: The system MUST define a minimal stable callable contract for future external assistant or agent callers that maps into the canonical runtime request shape.
- **FR-007**: The interop contract MUST support safe versioning or forward-compatibility signaling so unknown fields do not silently corrupt runtime behavior.
- **FR-008**: User-facing caller, source, and trust explanations for standardized interop paths MUST support English and Simplified Chinese automatically via device locale.
- **FR-009**: This milestone MUST remain local-first and MUST not require cloud registration or remote orchestration to accept external caller contracts.
- **FR-010**: This milestone MUST build on the existing external entry, governance, and tool standardization work instead of replacing the current runtime ingress pipeline.

### Key Entities *(include if feature involves data)*

- **InteropRequestEnvelope**: Canonical normalized inbound envelope used for external caller requests before mapping into `RuntimeRequest`.
- **CallerContractIdentity**: Stable identity model for caller package, source label, trust state, trust reason, and interop version.
- **UriGrantSummary**: User-explainable summary of granted content URIs and their access mode.
- **CallableSurfaceDescriptor**: Minimal structured description of a future externally callable Mobile Claw request surface.
- **InteropCompatibilitySignal**: Versioning and forward-compatibility metadata for inbound contracts.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: At least two different external entry styles map into the same canonical interop envelope before runtime execution.
- **SC-002**: Caller identity, trust outcome, and URI grant summaries are expressed consistently across covered external flows.
- **SC-003**: Governance and policy can evaluate covered external contracts without entry-specific trust logic for each caller path.
- **SC-004**: A minimal future callable contract exists that can map into the same canonical runtime request shape as current external handoffs.

## Assumptions

- This milestone standardizes interop contracts and caller semantics; it does not attempt to implement every future external transport.
- Share-based text/media handoff remains the first live external path and acts as the compatibility anchor.
- Real third-party ecosystem adoption can come later; `016` focuses on preserving contract stability before more integrations appear.
- This milestone is a contract-hardening slice and MUST NOT absorb the broader unified extension work from `017` or the in-app management surface work from `018`.
