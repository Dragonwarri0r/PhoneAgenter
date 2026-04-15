# Feature Specification: Safe Execution Policy and Approval Flow

**Feature Branch**: `004-safe-execution-policy`  
**Created**: 2026-04-08  
**Status**: Draft  
**Input**: User description: "Add risk classification policy approval and audit"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Auto-Execute Low-Risk Actions (Priority: P1)

As a user, safe low-risk actions should complete automatically so the agent feels useful instead of constantly asking for confirmation.

**Why this priority**: Controlled automation is one of the central user benefits of the product.

**Independent Test**: Can be tested independently by submitting low-risk requests and verifying that they complete without a manual approval prompt while still being recorded as policy decisions.

**Acceptance Scenarios**:

1. **Given** a request is classified as low risk and no hard confirmation rule applies, **When** policy is evaluated, **Then** the action proceeds automatically.
2. **Given** a low-risk action completes, **When** the session ends, **Then** the system records the decision and execution result in audit history.

---

### User Story 2 - Confirm High-Risk Actions Before Execution (Priority: P2)

As a user, high-risk actions should pause for explicit confirmation with a clear preview and explanation.

**Why this priority**: Without visible approval gates, users cannot trust cross-app agent execution.

**Independent Test**: Can be tested independently by submitting high-risk requests and verifying that the system generates a preview, presents a clear action hierarchy, waits for user confirmation, and only proceeds after approval.

**Acceptance Scenarios**:

1. **Given** a request is classified as high risk, **When** policy is evaluated, **Then** the system presents a confirmation request instead of executing immediately.
2. **Given** a confirmation request is shown, **When** the user rejects it, **Then** the action does not execute and the session ends with a denied outcome.

---

### User Story 3 - Enforce Hard Rules and Explain Decisions (Priority: P3)

As a user, I can trust that clearly sensitive actions are never silently auto-executed, even if the classifier is overly optimistic.

**Why this priority**: Safety requires hard boundaries beyond model judgment.

**Independent Test**: Can be tested independently by submitting requests for hard-confirm or blocked actions and verifying that policy rules override classifier optimism while still exposing a human-readable reason.

**Acceptance Scenarios**:

1. **Given** a request targets a hard-confirm scope such as sending a message, **When** policy evaluates the request, **Then** the system requires confirmation even if the classifier returned low risk.
2. **Given** a request targets a blocked action, **When** policy evaluates the request, **Then** the system denies execution and records the reason.

### Edge Cases

- What happens when the classifier returns low risk but the action scope is on the hard-confirm list?
- How does the system behave if the user dismisses a confirmation request without choosing approve or deny?
- What happens when a previously approved action becomes invalid before execution finishes?
- How should the system treat requests whose risk cannot be classified confidently?
- How does the system present a clear decision when preview data is long or visually dense?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST classify candidate actions into `low`, `medium`, `high`, or `blocked` risk levels before final policy resolution.
- **FR-002**: The system MUST keep risk classification separate from final authorization and let policy make the final execution decision.
- **FR-003**: The system MUST support execution outcomes of `auto_execute`, `preview_first`, `require_confirmation`, and `deny`.
- **FR-004**: The system MUST maintain a hard-confirm rule set for sensitive actions, including at minimum message sending, calendar writes, external sharing or posting, direct UI actions, and other sensitive writes chosen by policy.
- **FR-005**: The system MUST deny actions that are blocked by policy even if a classifier produces a lower risk result.
- **FR-006**: The system MUST produce a preview and human-readable rationale for actions that require confirmation.
- **FR-007**: The system MUST support explicit approve and reject outcomes for confirmation requests.
- **FR-008**: The system MUST record audit events for risk assessment, policy resolution, user approval outcome, and execution result.
- **FR-009**: The system MUST expose enough explanation data for the user to understand why an action was auto-executed, paused, or denied.
- **FR-010**: The system MUST present confirmable actions with a clear visual action hierarchy so the user can distinguish the primary next step from secondary or dismissive actions.
- **FR-011**: The system MUST provide lightweight success and failure feedback after approval-driven actions complete or fail, in addition to persistent audit records where applicable.
- **FR-012**: The system MUST not rely on dense log output as the only way to understand approval state, execution outcome, or denial reason.
- **FR-013**: The system MUST support localized user-facing approval, audit, and execution messaging, with `v0` at minimum providing English and Simplified Chinese.
- **FR-014**: The system MUST automatically select English or Chinese for user-facing policy and approval surfaces based on the device locale, falling back to English when no localized text is available.

### Key Entities *(include if feature involves data)*

- **RiskAssessment**: The classifier-produced evaluation of the candidate action and its risk rationale.
- **PolicyDecision**: The final authorization result after rules, scopes, caller trust, and user state are evaluated.
- **ApprovalRequest**: The user-facing preview and explanation generated for a confirmable action.
- **ApprovalOutcome**: The user decision to approve, reject, or abandon a pending confirmation.
- **AuditEvent**: The structured record of how a runtime action was evaluated and what result followed.
- **ActionScope**: The normalized scope label used to decide whether an action can auto-execute, must confirm, or must deny.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All actions mapped to the hard-confirm list require confirmation in every milestone validation flow.
- **SC-002**: Low-risk actions without hard-confirm rules auto-execute without user approval in all tested scenarios.
- **SC-003**: Users can identify why an action was auto-executed, paused, or denied from the approval or audit surface in usability review.
- **SC-004**: Every policy decision produces an audit record linked to the originating execution session.
- **SC-005**: In milestone validation, users can correctly identify the primary approval action and the cancellation or rejection path without additional explanation from the team.

## Assumptions

- Risk classification may mix model-based and rule-based signals.
- `v0` remains single-user, so approvals are always resolved by the local device user.
- The first approval milestone focuses on correctness and safety rather than advanced batching or delegation flows.
- Richer audit browsing UI may arrive later as long as the structured audit data exists in this milestone.
- Approval and outcome surfaces should align with the calm, layered interaction style defined in `DESIGN.md`.
