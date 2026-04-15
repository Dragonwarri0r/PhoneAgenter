# Data Model: Safe Execution Policy and Approval Flow

## Overview

This feature introduces the first structured safety layer for runtime execution.
It separates classifier output from final policy decisions, persists approval and audit records locally, and keeps the user informed through localized explanations.

## Entities

### RiskAssessment

Represents the classifier-produced risk evaluation for a runtime request.

| Field | Description |
|---|---|
| `assessment_id` | Stable assessment identifier |
| `session_id` | Owning execution session |
| `request_id` | Owning runtime request |
| `capability_id` | Candidate capability under evaluation |
| `scope` | Normalized action scope |
| `risk_level` | `low`, `medium`, `high`, or `blocked` |
| `rationale` | Human-readable classifier rationale |
| `signals` | Structured signal list such as caller trust, write intent, or external side effects |
| `confidence` | Optional classifier confidence |
| `created_at` | Assessment timestamp |

### PolicyDecision

Represents the final authorization result after rules, scope checks, and classifier output are resolved.

| Field | Description |
|---|---|
| `decision_id` | Stable decision identifier |
| `session_id` | Owning execution session |
| `assessment_id` | Linked risk assessment |
| `decision` | `auto_execute`, `preview_first`, `require_confirmation`, or `deny` |
| `effective_scope` | Final normalized scope used by policy |
| `rule_source` | Why this decision won, such as `classifier`, `hard_confirm_rule`, or `blocked_rule` |
| `rationale` | Human-readable decision explanation |
| `awaiting_input` | Whether execution is paused for user action |
| `created_at` | Decision timestamp |

### ApprovalRequest

Represents the user-facing preview for a confirmable action.

| Field | Description |
|---|---|
| `approval_request_id` | Stable approval identifier |
| `session_id` | Owning execution session |
| `decision_id` | Linked policy decision |
| `title` | Short approval headline |
| `summary` | Short explanation of the action |
| `preview_payload` | Lightweight preview data for what will happen |
| `primary_action_label` | Approve action label |
| `secondary_action_label` | Reject or dismiss label |
| `locale_tag` | Device locale used when generating the user-facing strings |
| `created_at` | Request timestamp |

### ApprovalOutcome

Represents the user response to a pending approval request.

| Field | Description |
|---|---|
| `approval_outcome_id` | Stable outcome identifier |
| `approval_request_id` | Linked approval request |
| `session_id` | Owning execution session |
| `outcome` | `approved`, `rejected`, or `abandoned` |
| `actor` | For `v0`, always the local device user |
| `reason` | Optional explanation or auto-note |
| `created_at` | Outcome timestamp |

### AuditEvent

Represents one structured audit record for safety-relevant runtime behavior.

| Field | Description |
|---|---|
| `audit_event_id` | Stable audit identifier |
| `session_id` | Owning execution session |
| `event_type` | `risk_assessed`, `policy_decided`, `approval_requested`, `approval_resolved`, `execution_completed`, `execution_denied`, or `execution_failed` |
| `headline` | User-safe short summary |
| `details` | User-safe explanation text |
| `linked_record_id` | Optional linked risk, policy, or approval id |
| `locale_tag` | Locale used for user-facing text |
| `created_at` | Event timestamp |

### ActionScope

Represents the normalized scope label used by policy rules.

| Field | Description |
|---|---|
| `scope_id` | Stable scope name such as `reply.generate` or `message.send` |
| `risk_mode` | `auto_allowed`, `hard_confirm`, or `blocked` |
| `description` | Human-readable scope description |

## Relationships

- One `ExecutionSession` may have zero or many `RiskAssessment` records as policy evolves.
- One `RiskAssessment` maps to one final `PolicyDecision`.
- One `PolicyDecision` may create zero or one `ApprovalRequest`.
- One `ApprovalRequest` may end with zero or one `ApprovalOutcome`.
- One `ExecutionSession` may emit many `AuditEvent` records across its lifetime.
- One `ActionScope` may be referenced by many risk and policy records.

## Validation Rules

- Every execution path must create at least one `PolicyDecision`.
- A `blocked` risk assessment must never resolve to `auto_execute`.
- Any action on the hard-confirm list must resolve to `require_confirmation` or `deny`.
- `ApprovalRequest` records must only exist for confirmable decisions.
- `ApprovalOutcome.approved` must be recorded before a paused confirmable action resumes execution.
- All user-facing approval and audit strings must be representable in English and Simplified Chinese.
- Locale selection for user-facing strings must follow the device locale, with English fallback.

## State Transitions

### Policy Flow

`risk_assessed -> policy_decided`

`policy_decided(auto_execute) -> executing`

`policy_decided(require_confirmation) -> approval_requested`

`approval_requested -> approved -> executing`

`approval_requested -> rejected -> denied`

`policy_decided(deny) -> denied`

### Audit Flow

`risk_assessed`

`policy_decided`

`approval_requested` (optional)

`approval_resolved` (optional)

`execution_completed | execution_denied | execution_failed`
