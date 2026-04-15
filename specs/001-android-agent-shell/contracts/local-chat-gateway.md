# Contract: Local Chat Gateway

## Purpose

This contract decouples the Android workspace UI from the concrete local model backend used in milestone `001`.
It provides just enough surface area for model selection, model health, streaming assistant turns, and session reset.

## Contract Surface

### 1. Enumerate Available Models

The workspace needs a way to list local models that may appear in the model picker.

| Field | Description |
|---|---|
| `model_id` | Stable model identifier |
| `display_name` | User-facing model name |
| `provider_label` | Backend/provider label |
| `availability_status` | `unavailable`, `preparing`, `ready`, or `failed` |
| `status_message` | Optional explanation or recovery hint |
| `is_selectable` | Whether the workspace may currently select this model |

### 2. Observe Model Health

The workspace needs current health updates for the selected model.

Model health updates must include:

- `model_id`
- `availability_status`
- `headline`
- `supporting_text`
- `primary_action_label` when applicable

### 3. Create or Reuse Session

The workspace needs a local session handle tied to the selected model.

Session data must include:

| Field | Description |
|---|---|
| `session_id` | Stable session identifier |
| `model_id` | Selected local model |
| `state` | `idle`, `streaming`, `completed`, `failed`, or `reset` |

### 4. Stream Assistant Turn

The workspace sends a user turn and receives ordered events for the assistant turn.

#### Request payload

| Field | Description |
|---|---|
| `session_id` | Active session identifier |
| `model_id` | Selected local model |
| `user_text` | Current user prompt text |
| `visible_transcript` | Existing visible turns used as chat context |

#### Ordered stream events

| Event | Description |
|---|---|
| `session_preparing` | The backend has accepted the request and is preparing |
| `assistant_started` | The assistant turn has started |
| `assistant_chunk` | Append-only streamed text delta |
| `assistant_completed` | Final assistant output and optional latency summary |
| `assistant_failed` | Recoverable or terminal failure with user-safe message |

### 5. Reset Session

The workspace needs to explicitly clear the active session.

Reset result must include:

- `session_id`
- `reset_at`
- `was_cleared`
- optional user-facing confirmation message

## Behavioral Guarantees

- Events for a single send must be ordered.
- A send must produce exactly one terminal event: either `assistant_completed` or `assistant_failed`.
- `assistant_chunk` events are append-only and must not rewrite prior assistant content unexpectedly.
- If failure occurs after partial output, the contract must preserve the already visible content and surface a user-safe error.
- Session reset must not leave the workspace in an ambiguous partially-cleared state.

## Out of Scope for This Contract

- Full runtime orchestration lifecycle from spec `002`
- Persona and memory retrieval from spec `003`
- Approval and policy gating from spec `004`
- Cross-app capability routing from spec `005`
