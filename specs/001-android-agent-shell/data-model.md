# Data Model: Android Agent Shell and Local Model Workspace

## Overview

This feature focuses on the screen and local session model needed to power the first Android agent workspace.
The data model intentionally stays narrower than the later runtime model planned in `002`.

## Entities

### ChatWorkspace

Represents the primary screen and its visible state.

| Field | Description |
|---|---|
| `workspace_id` | Stable identifier for the workspace surface |
| `active_model_id` | The currently selected local model |
| `active_session_id` | The current in-memory chat session |
| `screen_state` | High-level workspace state: `ready_idle`, `streaming`, `unavailable`, `preparing`, `recoverable_failure` |
| `feedback_message` | Optional transient feedback shown to the user |

### LocalModelProfile

Represents a selectable local model shown in the workspace.

| Field | Description |
|---|---|
| `model_id` | Stable model identifier |
| `display_name` | User-facing model name |
| `provider_label` | Local backend/provider label |
| `availability_status` | `unavailable`, `preparing`, `ready`, or `failed` |
| `status_message` | User-facing guidance for the current model state |
| `is_selectable` | Whether the model may currently be picked |

### ModelHealthState

Represents the model status surface shown near the top of the workspace.

| Field | Description |
|---|---|
| `model_id` | Associated local model |
| `availability_status` | Current readiness state |
| `headline` | Short primary status summary |
| `supporting_text` | Secondary explanation or recovery hint |
| `primary_action_label` | Optional primary action such as retry or choose model |

### ContextWindowState

Represents the compact context/status surface for the current session.

| Field | Description |
|---|---|
| `session_id` | Associated session |
| `summary_text` | Safe user-visible summary of active context |
| `stage_label` | Lightweight label such as `Ready`, `Thinking`, or `Preparing model` |
| `is_busy` | Whether the current session is mid-process |

### ChatSession

Represents the active conversation bound to the selected model.

| Field | Description |
|---|---|
| `session_id` | Stable in-memory session identifier |
| `model_id` | Selected local model |
| `created_at` | Session creation time |
| `updated_at` | Last session update time |
| `state` | `idle`, `streaming`, `completed`, `failed`, or `reset` |

### ChatTurn

Represents one visible turn in the conversation layer.

| Field | Description |
|---|---|
| `turn_id` | Stable turn identifier |
| `session_id` | Owning session |
| `role` | `user` or `assistant` |
| `content` | Current visible content |
| `turn_state` | `complete`, `streaming`, or `failed` |
| `created_at` | Turn creation time |

### ComposerState

Represents the input dock state.

| Field | Description |
|---|---|
| `draft_text` | Current text in the composer |
| `can_send` | Whether send is currently allowed |
| `is_guarded` | Whether send is blocked due to in-progress work |
| `placeholder_text` | Composer hint shown to the user |

### FeedbackMessage

Represents transient workspace feedback.

| Field | Description |
|---|---|
| `message_id` | Stable feedback identifier |
| `kind` | `success`, `error`, or `info` |
| `text` | User-facing feedback text |
| `scope` | Associated action such as `send`, `reset`, or `generation_failure` |

## Relationships

- One `ChatWorkspace` owns one active `ChatSession` at a time.
- One `ChatSession` contains many `ChatTurn` records.
- One `ChatWorkspace` references one active `LocalModelProfile`.
- One `LocalModelProfile` produces one `ModelHealthState` for display.
- One `ChatSession` may expose one current `ContextWindowState`.
- One `ChatWorkspace` may show zero or one active `FeedbackMessage`.

## Validation Rules

- `ChatWorkspace.active_model_id` must reference a selectable `LocalModelProfile`.
- `ComposerState.can_send` must be false when the active model is not ready.
- A `ChatTurn` in `streaming` state must belong to the currently active session.
- A `FeedbackMessage.kind` of `success` or `error` must be tied to a known action scope.
- `ContextWindowState.summary_text` must remain safe for user display and must not assume raw private evidence exposure.

## State Transitions

### LocalModelProfile

`unavailable -> preparing -> ready`

`ready -> failed`

`failed -> preparing`

### ChatSession

`idle -> streaming -> completed`

`idle -> streaming -> failed`

`completed -> streaming` is allowed only when a new user turn starts.

Any active state may transition to `reset` when the user confirms reset.

### ChatWorkspace

`unavailable / preparing -> ready_idle`

`ready_idle -> streaming`

`streaming -> ready_idle`

`streaming -> recoverable_failure`

`recoverable_failure -> ready_idle`
