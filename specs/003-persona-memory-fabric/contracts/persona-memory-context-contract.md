# Contract: Persona and Scoped Memory Context

## Purpose

This contract defines how the local runtime reads persona, stores scoped memory, retrieves active context, and exposes a safe user-visible summary.
It extends the `002` runtime session pipeline without changing the top-level session lifecycle contract.

## Contract Surface

### 1. Read and Update Persona Profile

The runtime and lightweight workspace management UI must be able to read and update a stable persona profile.

#### Persona fields

| Field | Description |
|---|---|
| `persona_id` | Stable persona identifier |
| `verbosity` | Stable expression preference |
| `warmth` | Stable tone preference |
| `confirmation_style` | Stable decision preference |
| `guardrails` | Stable behavioral constraints |
| `role_overrides` | Optional role-specific adjustments |

#### Behavioral guarantees

- Persona remains separate from factual memory.
- Persona updates are durable and user-editable.
- Persona reads must always return a valid default profile even before any manual edits.

### 2. Write or Update Memory Item

The runtime must be able to create or update memory records during writeback or manual management flows.

#### Memory fields

| Field | Description |
|---|---|
| `memory_id` | Stable memory identifier |
| `content_text` | Stored fact, summary, or event text |
| `lifecycle` | `durable`, `working`, or `ephemeral` |
| `scope` | `global`, `app_scoped`, `contact_scoped`, or `device_scoped` |
| `exposure_policy` | `private`, `shareable_summary`, or `shareable_full` |
| `sync_policy` | `local_only`, `summary_sync_ready`, or `full_sync_ready` |
| `source_type` | Provenance source classification |
| `confidence` | Confidence score |
| `origin_app` | Optional source app |
| `subject_key` | Optional contact or thread key |
| `expires_at` | Optional expiration time |
| `is_pinned` | Manual promotion flag |

#### Behavioral guarantees

- Inferred app memory defaults to `app_scoped` and `private`.
- Manual edits and pin actions must preserve provenance metadata.
- Raw evidence references remain local-only even when the item summary is shareable.

### 3. Retrieve Runtime Context

The runtime submits a query and receives persona plus eligible scoped memory for the current request.

#### Retrieval query fields

| Field | Description |
|---|---|
| `request_id` | Current runtime request id |
| `user_input` | Current request text |
| `origin_app` | Requesting app or workspace scope |
| `subject_key` | Optional contact or thread context |
| `device_id` | Optional device scope |
| `allow_private` | Whether local private memory may participate |
| `max_items` | Maximum selected memory items |

#### Retrieval response

| Field | Description |
|---|---|
| `persona_profile` | Stable persona profile for the request |
| `memory_items` | Ordered in-scope relevant memory items |
| `retrieval_summary` | User-safe explanation of selected context |
| `excluded_count` | Count of excluded items |

#### Behavioral guarantees

- Out-of-scope memory must not be returned.
- Expired non-pinned memory must not be returned.
- Retrieval must apply filtering before relevance ranking.
- Returned memory should stay user-safe enough to support later explanation and debugging.

### 4. Build Active Context Summary

The runtime must expose a user-visible summary for the workspace context surface.

#### Summary fields

| Field | Description |
|---|---|
| `headline` | Short explanation of active context |
| `persona_summary` | User-safe summary of active persona constraints |
| `memory_chips` | Short labels for selected memory |
| `hidden_private_count` | Count of hidden private or raw items |

#### Behavioral guarantees

- The summary must not reveal private raw evidence by default.
- The summary should explain when context was filtered for scope or privacy reasons.
- The summary must remain stable enough for the `ContextWindowCard` and a lightweight context inspector.

### 5. Manual Lifecycle Operations

The user-facing management surface may request lifecycle changes.

#### Supported actions

- `promote`
- `demote`
- `pin`
- `unpin`
- `edit`
- `expire`

#### Behavioral guarantees

- Promotion and demotion change lifecycle without deleting provenance.
- Pinning may preserve retrievability past normal expiration thresholds.
- Edit actions must update timestamps and preserve original source type.

## Out of Scope for This Contract

- Full policy and approval enforcement from `004`
- Android cross-app capability routing from `005`
- Real sync, merge, or remote sharing behavior from `006`
- Embedding-based semantic retrieval or long-context summarization pipelines
