# Contract: Safe Execution Policy and Approval Flow

## Purpose

This contract defines how the runtime classifies risk, resolves policy, pauses for approval, records outcomes, and exposes localized user-facing explanations.
It extends the existing runtime session pipeline without changing the core provider abstraction.

## Contract Surface

### 1. Classify Risk

The runtime submits an execution candidate and receives a structured risk assessment.

#### Required fields

| Field | Description |
|---|---|
| `session_id` | Owning execution session |
| `request_id` | Owning runtime request |
| `capability_id` | Candidate capability |
| `scope` | Normalized action scope |
| `risk_level` | `low`, `medium`, `high`, or `blocked` |
| `rationale` | Human-readable rationale |

#### Behavioral guarantees

- Risk classification is advisory only.
- Classifier output must be available to audit and explanation layers.
- Unknown or low-confidence classifications may still be overridden by policy.

### 2. Resolve Policy

The runtime combines risk assessment, hard rules, and request state into one final policy decision.

#### Required fields

| Field | Description |
|---|---|
| `assessment_id` | Linked risk assessment |
| `decision` | `auto_execute`, `preview_first`, `require_confirmation`, or `deny` |
| `rule_source` | Why the decision won |
| `rationale` | Human-readable explanation |
| `awaiting_input` | Whether execution must pause |

#### Behavioral guarantees

- Hard-confirm scopes override lower classifier risk.
- Blocked scopes override all lower-risk outcomes.
- Low-risk actions without hard rules may auto-execute.

### 3. Create Approval Request

When policy requires confirmation, the runtime must generate a lightweight approval request.

#### Required fields

| Field | Description |
|---|---|
| `approval_request_id` | Stable request id |
| `session_id` | Owning execution session |
| `title` | User-facing headline |
| `summary` | User-facing explanation |
| `preview_payload` | Preview of the pending action |
| `primary_action_label` | Approve label |
| `secondary_action_label` | Reject label |
| `locale_tag` | Device locale tag used for text |

#### Behavioral guarantees

- Approval requests must only be created for confirmable decisions.
- Approval requests must be understandable without reading raw logs.
- Preview data should stay lightweight and safe to render in the workspace.

### 4. Resolve Approval Outcome

The user-facing approval surface must emit one explicit outcome.

#### Supported outcomes

- `approved`
- `rejected`
- `abandoned`

#### Behavioral guarantees

- Confirmable execution must not continue until approval is `approved`.
- `rejected` and `abandoned` outcomes must terminate the paused request safely.
- Approval outcomes must be auditable.

### 5. Write Audit Events

Every safety-relevant step must produce a structured audit event.

#### Minimum audit event types

- `risk_assessed`
- `policy_decided`
- `approval_requested`
- `approval_resolved`
- `execution_completed`
- `execution_denied`
- `execution_failed`

#### Behavioral guarantees

- Audit events must link back to the execution session.
- Audit events must store user-facing text and locale metadata.
- Audit events must be readable without relying on provider-specific logs.

### 6. Localize User-Facing Policy Messaging

Policy, approval, and audit messaging must support device-locale-based localization.

#### Required locale behavior

- Use Simplified Chinese when the device language is Chinese.
- Use English when the device language is English.
- Fall back to English if no matching localized string exists.

#### Behavioral guarantees

- Locale selection is automatic; the user should not need a separate in-app language toggle for `v0`.
- Compose UI and non-Compose runtime messaging must use the same localized source of truth.

## Out of Scope for This Contract

- Android cross-app capability routing from `005`
- Multi-device sync and merge from `006`
- Third-party translation systems or remote locale services
- Rich timeline browsing for historical audit data beyond lightweight `v0` surfaces
