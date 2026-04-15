# Data Model: Local Runtime Session Pipeline

## Overview

This feature introduces the execution-session model that will anchor all later runtime work.
It sits between UI request entry and provider execution, and it preserves stable contracts whether the request uses capabilities or only local generation.

## Entities

### RuntimeRequest

Represents one accepted top-level request entering the runtime.

| Field | Description |
|---|---|
| `request_id` | Stable request identifier |
| `session_id` | Linked execution session identifier |
| `user_input` | Primary user text or normalized request content |
| `selected_model_id` | Active local model or runtime context target |
| `transcript_context` | Visible conversation context supplied by the UI |
| `requested_capabilities` | Optional normalized capability hints |
| `created_at` | Request creation time |

### ExecutionSession

Represents the top-level container for one runtime request and all of its lifecycle state.

| Field | Description |
|---|---|
| `session_id` | Stable execution-session identifier |
| `request_id` | Associated runtime request |
| `status` | `active`, `completed`, `failed`, `cancelled`, or `denied` |
| `current_stage` | Current orchestration stage |
| `started_at` | Session start time |
| `updated_at` | Last lifecycle update time |
| `outcome` | Optional terminal session outcome |
| `summary` | Compact current session summary for UI |

### SessionStage

Represents one named phase in the ordered lifecycle.

| Field | Description |
|---|---|
| `stage_id` | Stable stage event identifier |
| `session_id` | Owning execution session |
| `stage_type` | `ingress`, `context_loading`, `planning`, `capability_selection`, `execution_gating`, `executing`, `completed`, `failed`, `cancelled`, or `denied` |
| `label` | User-safe short stage label |
| `details` | Structured supporting details |
| `ordinal` | Monotonic order within session |
| `occurred_at` | Stage transition time |

### CapabilityRequest

Represents the normalized request for a provider-backed action.

| Field | Description |
|---|---|
| `capability_request_id` | Stable capability request identifier |
| `session_id` | Owning execution session |
| `capability_id` | Normalized capability name |
| `input_payload` | Provider-agnostic request body |
| `provider_hint` | Optional requested provider identity |
| `created_at` | Creation time |

### CapabilityExecution

Represents one provider invocation attempted during the session.

| Field | Description |
|---|---|
| `capability_execution_id` | Stable provider execution identifier |
| `session_id` | Owning execution session |
| `capability_request_id` | Source capability request |
| `provider_id` | Selected provider |
| `state` | `pending`, `running`, `completed`, or `failed` |
| `result_payload` | Optional normalized result |
| `failure_message` | Optional user-safe failure summary |
| `started_at` | Execution start time |
| `completed_at` | Optional completion time |

### SessionOutcome

Represents the terminal result of the session.

| Field | Description |
|---|---|
| `session_id` | Owning execution session |
| `terminal_state` | `success`, `failure`, `cancelled`, or `denied` |
| `user_message` | User-safe final summary |
| `output_text` | Optional assistant-visible text output |
| `provider_results` | Optional normalized provider results |
| `finished_at` | Terminal timestamp |

### RuntimeStatusSummary

Represents the compact status or context model exposed to UI.

| Field | Description |
|---|---|
| `session_id` | Associated execution session |
| `headline` | Short current status summary |
| `stage_label` | Compact stage label |
| `supporting_text` | User-safe explanation |
| `is_busy` | Whether the session is actively progressing |
| `awaiting_input` | Whether approval or user input is required |
| `is_terminal` | Whether the session has ended |

## Relationships

- One `RuntimeRequest` creates exactly one `ExecutionSession`.
- One `ExecutionSession` emits many ordered `SessionStage` records.
- One `ExecutionSession` may create zero or many `CapabilityRequest` records.
- One `CapabilityRequest` may produce zero or many `CapabilityExecution` attempts, depending on retries or provider substitution.
- One `ExecutionSession` ends with zero or one `SessionOutcome`.
- One `ExecutionSession` continuously derives one current `RuntimeStatusSummary`.

## Validation Rules

- Every accepted `RuntimeRequest` must map to exactly one `ExecutionSession`.
- `SessionStage.ordinal` must increase monotonically within a session.
- A session may only have one terminal `SessionOutcome`.
- Terminal stages must not be overwritten by later non-terminal stages.
- A `CapabilityExecution` must reference a valid `CapabilityRequest` in the same session.
- `RuntimeStatusSummary` must remain user-safe and must not expose provider-specific raw logs.
- A generation-only session may complete without any `CapabilityRequest`.

## State Transitions

### ExecutionSession

`active -> completed`

`active -> failed`

`active -> cancelled`

`active -> denied`

Duplicate terminal transitions are invalid and must be ignored or collapsed safely.

### CapabilityExecution

`pending -> running -> completed`

`pending -> running -> failed`

### RuntimeStatusSummary

`loading context -> planning -> selecting capability -> waiting approval -> executing -> success/failure/denied`

The summary may skip stages not used by a particular request, but it must still preserve ordered progression.

