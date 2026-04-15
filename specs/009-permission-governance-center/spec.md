# Feature Specification: Permission Governance Center

**Feature Branch**: `009-permission-governance-center`  
**Created**: 2026-04-09  
**Status**: Draft  
**Input**: User description: "Add a user-manageable governance center so users can understand who is invoking Mobile Claw, what scopes they can request, and how approvals were resolved."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Review Caller Trust And Recent Approvals (Priority: P1)

As a user, I can open a governance center and review recent callers, their trust state, and recent approval outcomes, so I understand who has been interacting with the runtime.

**Why this priority**: Visibility is the first missing product layer; without it, governance stays invisible and users cannot build trust in the runtime.

**Independent Test**: Trigger a mix of local and external requests, open the governance center, and verify it shows recent callers, trust states, approval outcomes, and relevant scope information.

**Acceptance Scenarios**:

1. **Given** the runtime has processed requests from one or more callers, **When** the user opens the governance center, **Then** the system shows caller identity, trust state, and last-seen governance metadata.
2. **Given** the runtime has recent approval or denial activity, **When** the user opens the governance center, **Then** the system shows recent approval outcomes and the scopes involved.

---

### User Story 2 - Adjust Caller Trust And Scope Grants (Priority: P2)

As a user, I can change whether a caller is trusted and which scopes it may request, so I can govern runtime access without waiting for the next reactive prompt.

**Why this priority**: Governance is not complete if users can only observe; they must be able to proactively manage trust and scope access.

**Independent Test**: Change a caller from unverified to trusted or restricted, modify allowed scopes, and verify the new policy snapshot persists and appears in the governance center.

**Acceptance Scenarios**:

1. **Given** a caller appears in the governance center, **When** the user changes its trust mode, **Then** the new trust mode persists and becomes visible in subsequent governance views.
2. **Given** a caller has scope grants, **When** the user enables or disables a scope, **Then** that scope policy persists and is shown clearly in the governance center.

---

### User Story 3 - Enforce Governance Overrides During Runtime Routing (Priority: P3)

As a user, I can rely on governance settings to affect future runtime decisions, so trusted callers, denied callers, and restricted scopes behave according to my chosen policy.

**Why this priority**: A governance center that does not influence runtime behavior would be informational only and not a real control surface.

**Independent Test**: Configure a caller or scope override in the governance center, submit a matching request, and verify runtime routing/policy results honor the stored governance override.

**Acceptance Scenarios**:

1. **Given** a caller is explicitly denied in governance, **When** that caller submits another restricted request, **Then** runtime routing denies it with a governance-derived explanation.
2. **Given** a caller is trusted but a specific scope is disabled, **When** the caller requests that scope, **Then** the runtime denies or downgrades the request according to the stored scope policy.

---

### Edge Cases

- What happens when a caller has never been seen before and there is no governance record yet?
- What happens when a governance record exists for a package but the current request does not include package metadata?
- What happens when a caller is trusted overall but a single scope remains disabled?
- What happens when approval history grows large and the governance center only needs a bounded, recent view?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST provide a user-visible governance center surface inside the existing app experience.
- **FR-002**: The governance center MUST show recent caller records including identity, trust state, and last-seen metadata.
- **FR-003**: The governance center MUST show recent approval and denial history with related scope or decision information.
- **FR-004**: The system MUST persist caller governance records locally.
- **FR-005**: The system MUST allow the user to change caller trust mode for known callers.
- **FR-006**: The system MUST allow the user to enable or disable a bounded set of supported scopes per caller.
- **FR-007**: Runtime routing and caller verification MUST consult stored governance overrides before allowing restricted capability execution.
- **FR-008**: Governance-derived denials or restrictions MUST be explainable in runtime status, approval, or audit surfaces.
- **FR-009**: The governance center MUST support English and Simplified Chinese and follow device locale automatically.
- **FR-010**: The governance center MUST remain local-first and must not introduce cloud sync requirements in this milestone.

### Key Entities *(include if feature involves data)*

- **CallerGovernanceRecord**: Persistent local record describing a caller, its trust mode, visible metadata, and last-seen information.
- **ScopeGrantRecord**: Persistent local record describing whether a caller is allowed to request a specific scope.
- **GovernanceActivityItem**: User-visible item summarizing recent approval, denial, or trust-relevant events.
- **GovernanceDecisionSnapshot**: Runtime-ready view combining caller trust, scope grants, and explanation text for enforcement.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can open a governance surface and see recent caller trust and approval activity without leaving the app.
- **SC-002**: Caller trust changes and scope grant changes persist locally and remain visible after app restart.
- **SC-003**: At least one runtime path changes behavior because of a governance override rather than only default verifier logic.
- **SC-004**: Governance labels and user-facing explanations render correctly in both English and Simplified Chinese.

## Assumptions

- This milestone focuses on a governance center embedded in the current workspace experience rather than a standalone multi-screen settings app.
- The first version only needs a bounded set of actionable scopes already represented in the runtime policy layer.
- Existing approval, audit, and caller verification storage can be extended rather than replaced.
- The governance center is intended to manage local policy, not to introduce account systems or multi-device sync.
