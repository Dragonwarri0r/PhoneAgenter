# Data Model: Sync-Ready Share and Extension Hooks

## Overview

This feature preserves future sync, merge, export, and extension flexibility by enriching the current memory model and adding stable portability contracts.
It does not introduce active synchronization; it introduces the persisted and in-memory structures that later sync work can safely build on.

## Entities

### MemoryItem

Represents one persisted memory record already used by the runtime.
`006` extends it so future merge and portability work can rely on explicit metadata.

| Field | Description |
|---|---|
| `memory_id` | Stable persisted record id |
| `logical_record_id` | Stable cross-version identity used by future merge logic |
| `title` | User-facing label |
| `content_text` | Full raw content stored locally |
| `summary_text` | Redacted/share-safe summary text |
| `lifecycle` | `durable`, `working`, or `ephemeral` |
| `scope` | `global`, `app_scoped`, `contact_scoped`, or `device_scoped` |
| `exposure_policy` | `private`, `shareable_summary`, or `shareable_full` |
| `sync_policy` | `local_only`, `summary_sync_ready`, or `full_sync_ready` |
| `subject_key` | Optional contact or device scoped key |
| `origin_app` | Source application identifier |
| `origin_device_id` | Device identity for future merge/source attribution |
| `origin_user_id` | User identity placeholder for future portability/sync |
| `source_type` | `user_edit`, `system_source`, `inferred`, or `runtime_writeback` |
| `confidence` | Stored confidence score |
| `logical_version` | Monotonic logical version used for merge inputs |
| `schema_version` | Schema compatibility marker for future migrations |
| `is_pinned` | User pin state |
| `is_manually_edited` | Whether user explicitly modified the record |
| `created_at` | Initial creation time |
| `updated_at` | Last update time |
| `expires_at` | Optional expiry time |
| `evidence_ref` | Optional pointer to private raw evidence |

### SyncEnvelope

Represents the normalized future-sync metadata view over a stored record.

| Field | Description |
|---|---|
| `record_id` | Persisted record id |
| `logical_record_id` | Stable merge identity |
| `origin_device_id` | Originating device |
| `origin_user_id` | Owning user |
| `logical_version` | Version counter |
| `sync_policy` | Declared sync readiness |
| `exposure_policy` | Declared sharing allowance |
| `schema_version` | Version of the stored metadata contract |

### MergeCandidate

Represents one future conflict input normalized from persisted metadata.

| Field | Description |
|---|---|
| `logical_record_id` | Cross-version record identity |
| `record_id` | Concrete persisted version id |
| `origin_device_id` | Device that produced the candidate |
| `origin_user_id` | User identity for later multi-device scenarios |
| `logical_version` | Candidate version number |
| `updated_at` | Timestamp used for deterministic tie-breaking |
| `exposure_policy` | Sharing restriction attached to the candidate |
| `sync_policy` | Sync-readiness attached to the candidate |
| `summary_payload` | Safe summary payload available for merge/explainability |
| `raw_payload_ref` | Optional local-only pointer to raw evidence |

### ExportBundle

Represents the portability/share output generated after redaction policy is applied.

| Field | Description |
|---|---|
| `bundle_id` | Stable generated bundle id |
| `record_id` | Source record |
| `logical_record_id` | Stable source identity |
| `export_mode` | `summary_only` or `full_record` |
| `payload_text` | Text actually exported |
| `included_fields` | Fields included in the bundle |
| `redacted_fields` | Fields withheld because of privacy policy |
| `exposure_policy` | Source exposure policy |
| `sync_policy` | Source sync policy |
| `generated_at` | Bundle creation time |

### DataRedactionPolicy

Represents the rule evaluation used before export or future share/sync actions.

| Field | Description |
|---|---|
| `record_id` | Evaluated record |
| `exposure_policy` | Current exposure state |
| `allow_full_export` | Whether full raw payload may leave the device |
| `allow_summary_export` | Whether summary payload may leave the device |
| `must_redact_evidence` | Whether evidence references must stay local |
| `reason` | Explainable policy result |

### ExtensionRegistration

Represents a future provider or portability extension registered against current runtime contracts.

| Field | Description |
|---|---|
| `extension_id` | Stable extension id |
| `extension_type` | `provider`, `export`, `import`, or `sync_transport` |
| `display_name` | Human-readable label |
| `supported_payload_modes` | Supported export/import payload modes |
| `required_record_fields` | Metadata fields the extension requires |
| `minimum_schema_version` | Oldest compatible record schema |
| `privacy_guarantee` | Declared handling level for private data |
| `enabled_by_default` | Whether the hook is passive-enabled in `v0` |

## Relationships

- One `MemoryItem` yields one `SyncEnvelope` projection.
- One `MemoryItem` can be normalized into one `MergeCandidate`.
- One `MemoryItem` can generate one or more `ExportBundle` variants depending on exposure policy.
- One `DataRedactionPolicy` evaluation governs how one `ExportBundle` is produced.
- One `ExtensionRegistration` may declare compatibility requirements against `SyncEnvelope`, `MergeCandidate`, or `ExportBundle`.

## Validation Rules

- Every persisted `MemoryItem` must have explicit `exposure_policy` and `sync_policy`.
- Every persisted `MemoryItem` must have `logical_record_id`, `logical_version`, and `schema_version` after this milestone.
- `private` records must never produce a full export bundle.
- `shareable_summary` records may only export `summary_text`, not `content_text` or raw evidence.
- `full_sync_ready` records must still respect exposure policy; sync-readiness does not override privacy.
- Extension registrations must declare required record fields rather than assuming all records carry all future metadata.

## State Transitions

### Sync Readiness

`local_only -> summary_sync_ready -> full_sync_ready`

Downgrades remain legal when privacy constraints tighten.

### Export Mode Resolution

`private -> summary_blocked`

`shareable_summary -> summary_only`

`shareable_full -> full_record | summary_only`

### Future Merge Input

`persisted_record -> sync_envelope -> merge_candidate`

No actual merge result is produced in this milestone.
