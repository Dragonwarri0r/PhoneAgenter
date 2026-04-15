# Data Model: Trusted External Handoff Entry

## Overview

This feature adds a normalized Android ingress model between the external handoff surface and the existing runtime session pipeline.
The new models capture supported entry registration, inbound payload validation, best-effort caller/source metadata, normalized runtime mapping, and the auditable link between the external handoff and the resulting runtime session.

## Entities

### ExternalEntryRegistration

Represents one supported Android entry path that can hand content into Mobile Claw.

| Field | Description |
|---|---|
| `entry_id` | Stable identifier such as `android_share_text` |
| `entry_type` | Entry family such as `activity_share` |
| `supported_actions` | Accepted Android intent actions |
| `supported_mime_types` | Accepted MIME types |
| `content_mode` | High-level content class such as `text_first` |
| `requires_user_visible_landing` | Whether the entry must land in the workspace |
| `status` | `enabled`, `disabled`, or `degraded` |

### ExternalHandoffPayload

Represents the validated payload extracted from the incoming Android intent.

| Field | Description |
|---|---|
| `handoff_id` | Stable identifier for the inbound handoff |
| `entry_id` | Source registration id |
| `action` | Incoming Android action |
| `mime_type` | Incoming MIME type |
| `shared_text` | Normalized user-visible text content |
| `shared_subject` | Optional subject or title hint |
| `received_at_epoch_millis` | Receive timestamp |
| `raw_source_summary` | Safe summary of the inbound extras/source |

### CallerIngressMetadata

Represents normalized source metadata captured at the Android boundary.

| Field | Description |
|---|---|
| `entry_type` | Source entry family such as `activity_share` |
| `origin_app` | Stable origin string used by downstream runtime |
| `package_name` | Best-available package identity when available |
| `source_label` | User-safe source label for status and audit surfaces |
| `trust_state` | `trusted`, `unverified`, or `denied` |
| `trust_reason` | Human-readable reason for the trust state |
| `referrer_uri` | Optional referrer hint when available |
| `signature_digest` | Optional package signing digest if verification succeeds |

### InboundRuntimeRequest

Represents the normalized external request before it is converted into the canonical runtime request shape.

| Field | Description |
|---|---|
| `handoff_id` | Owning external handoff id |
| `runtime_request_id` | Canonical runtime request id |
| `user_input` | Final text forwarded into runtime |
| `selected_model_id` | Model chosen for the runtime submission |
| `origin_app` | Normalized runtime origin |
| `workspace_id` | Target workspace identifier |
| `subject_key` | Optional handoff grouping key |
| `caller_metadata` | Normalized source/trust metadata |
| `requested_capabilities` | Optional normalized capability hints |

### ExternalInvocationRecord

Represents the auditable link between an external handoff and the runtime session it produced.

| Field | Description |
|---|---|
| `handoff_id` | External handoff id |
| `runtime_request_id` | Canonical runtime request id |
| `session_id` | Runtime session id once created |
| `source_label` | User-safe source attribution |
| `trust_state` | Final normalized trust state at submission time |
| `accepted` | Whether the handoff reached runtime execution flow |
| `failure_reason` | Optional normalized denial or parse failure reason |
| `created_at_epoch_millis` | Audit timestamp |

## Relationships

- One `ExternalEntryRegistration` can accept many `ExternalHandoffPayload` instances.
- One `ExternalHandoffPayload` has exactly one `CallerIngressMetadata` record after normalization.
- One accepted `ExternalHandoffPayload` produces one `InboundRuntimeRequest`.
- One `InboundRuntimeRequest` is converted into one canonical `RuntimeRequest`.
- One `ExternalInvocationRecord` links the inbound handoff to its eventual runtime session outcome.

## Validation Rules

- The first milestone accepts only `Intent.ACTION_SEND` with `text/plain`.
- `shared_text` must be present and non-blank after normalization; otherwise the handoff fails safely.
- Unsupported actions or MIME types must never be forwarded to the runtime pipeline.
- Missing package identity must not silently upgrade trust; it should normalize to `unverified` and rely on existing policy/caller verification rules.
- Internal workspace submissions and external handoff submissions must converge on the same canonical `RuntimeRequest` contract before planning.

## State Transitions

### External Handoff Flow

`received -> validated -> normalized -> landed_in_workspace -> submitted_to_runtime -> accepted | denied | failed`

### Trust Flow

`unknown -> unverified -> trusted | denied`

### Session Landing Flow

`handoff_received -> workspace_visible -> runtime_started -> completed | denied | failed`
