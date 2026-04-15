# Data Model: Persona and Scoped Memory Fabric

## Overview

This feature introduces the first durable persona and memory layer for the local runtime.
Persona remains a stable behavioral profile.
Memory remains dynamic, scoped, and policy-governed context.
Runtime context assembly combines them only at request time.

## Entities

### PersonaProfile

Represents the stable behavioral profile used to shape responses and execution style.

| Field | Description |
|---|---|
| `persona_id` | Stable persona identifier |
| `display_name` | User-facing persona name |
| `verbosity` | Stable style preference such as `low`, `medium`, or `high` |
| `warmth` | Stable tone preference |
| `confirmation_style` | Preference for when the agent should seek confirmation |
| `guardrails` | Stable behavior constraints such as `avoid_overcommitment` |
| `role_overrides` | Optional role-specific adjustments |
| `updated_at` | Last manual update time |

### MemoryItem

Represents one stored memory unit with lifecycle, scope, provenance, and policy metadata.

| Field | Description |
|---|---|
| `memory_id` | Stable memory identifier |
| `title` | Short user-safe label |
| `content_text` | Stored normalized fact, summary, or context text |
| `summary_text` | Optional safer summary for UI or shareable use |
| `lifecycle` | `durable`, `working`, or `ephemeral` |
| `scope` | `global`, `app_scoped`, `contact_scoped`, or `device_scoped` |
| `exposure_policy` | `private`, `shareable_summary`, or `shareable_full` |
| `sync_policy` | `local_only`, `summary_sync_ready`, or `full_sync_ready` |
| `subject_key` | Optional subject key such as a contact, thread, or task id |
| `origin_app` | Optional originating app or workspace source |
| `source_type` | `user_edit`, `system_source`, `inferred`, or `runtime_writeback` |
| `confidence` | Confidence score for inferred or imported context |
| `is_pinned` | Whether the user promoted the item for stronger retention |
| `is_manually_edited` | Whether the content was manually edited after creation |
| `created_at` | Creation time |
| `updated_at` | Last update time |
| `expires_at` | Optional expiration time |
| `evidence_ref` | Optional raw evidence pointer kept local-only |

### RetrievalQuery

Represents one request-time context lookup.

| Field | Description |
|---|---|
| `request_id` | Owning runtime request |
| `user_input` | Current request text used for relevance scoring |
| `origin_app` | Current request app or workspace scope |
| `subject_key` | Optional current subject or contact |
| `device_id` | Optional current device scope |
| `selected_model_id` | Current active model |
| `max_items` | Maximum eligible memory items to return |
| `allow_private` | Whether private memory may be used for the current local request |
| `now` | Current evaluation time for expiration checks |

### RetrievedContext

Represents the assembled context payload returned to the runtime before planning and execution.

| Field | Description |
|---|---|
| `persona_profile` | The stable persona profile applied to the request |
| `memory_items` | Ordered in-scope memory items selected for the request |
| `retrieval_summary` | User-safe explanation of why this context set was chosen |
| `excluded_count` | Count of filtered or expired items omitted from active context |

### ActiveContextSummary

Represents the safe context explanation surface exposed to the workspace.

| Field | Description |
|---|---|
| `headline` | Primary short summary such as `Using 3 scoped memories` |
| `persona_summary` | User-safe summary of active persona constraints |
| `memory_chips` | Short user-safe memory descriptors |
| `hidden_private_count` | Count of relevant but intentionally hidden private/raw items |
| `updated_at` | Last summary refresh time |

### MemoryLifecycleAction

Represents a manual lifecycle change requested by the user.

| Field | Description |
|---|---|
| `memory_id` | Target memory item |
| `action` | `promote`, `demote`, `pin`, `unpin`, `edit`, or `expire` |
| `requested_by` | User or system actor |
| `requested_at` | Action time |

## Relationships

- One `PersonaProfile` is active for the primary local user.
- One `RetrievalQuery` selects zero or many eligible `MemoryItem` records.
- One `RetrievedContext` contains exactly one `PersonaProfile` and zero or many selected `MemoryItem` records.
- One `ActiveContextSummary` is derived from one `RetrievedContext`.
- One `MemoryItem` may receive zero or many `MemoryLifecycleAction` mutations over time.

## Validation Rules

- Persona fields must remain editable independently of memory storage.
- `MemoryItem.lifecycle`, `scope`, `exposure_policy`, and `sync_policy` must always be present.
- Newly inferred app-specific memory must default to `app_scoped` and `private` unless explicitly promoted.
- Expired non-pinned memory must not be returned as active context.
- `shareable_summary` items must not expose raw evidence content through `ActiveContextSummary`.
- `global` memory may be retrieved only when it still passes exposure and relevance filters.
- Manual persona edits and memory pinning must preserve provenance and update timestamps.

## State Transitions

### MemoryItem Lifecycle

`ephemeral -> working`

`working -> durable`

`durable -> working`

`working -> expired`

`ephemeral -> expired`

Pinned items may remain retrievable even after a normal expiration threshold, but the item must retain its original provenance and expiration metadata.

### Exposure Handling

`private -> shareable_summary`

`shareable_summary -> shareable_full`

`shareable_full -> shareable_summary`

Exposure changes must not remove the raw evidence reference; they only change what later consumers are allowed to see.

### PersonaProfile

Persona updates are in-place manual edits with versioned timestamps.
They do not create or consume `MemoryItem` records.
