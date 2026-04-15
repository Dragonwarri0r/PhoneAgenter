# Data Model: Structured Action Payloads

## Overview

This feature adds a structured execution contract between runtime planning and provider execution.
The new model set captures action type, extracted fields, completeness, extraction evidence, and the preview object shown to the user before or during execution.

## Entities

### StructuredActionPayload

Represents the normalized payload used for structured execution of a supported action type.

| Field | Description |
|---|---|
| `payload_id` | Stable identifier for the structured payload |
| `session_id` | Owning runtime session |
| `action_type` | Structured action family such as `message_send`, `calendar_write`, or `external_share` |
| `completeness` | `complete`, `partial`, or `insufficient` |
| `recipient_hint` | Optional normalized recipient or target hint |
| `message_body` | Optional outbound message body |
| `event_title` | Optional calendar event title |
| `event_time_hint` | Optional event time or scheduling hint |
| `share_content` | Optional outbound share body |
| `destination_hint` | Optional share or app destination hint |
| `raw_request_text` | Original user-visible request text |

### ActionNormalizationResult

Represents the outcome of attempting to normalize a runtime request into a structured action.

| Field | Description |
|---|---|
| `result_id` | Stable normalization result id |
| `session_id` | Owning runtime session |
| `capability_id` | Runtime capability selected before normalization |
| `structured_payload` | Optional structured payload when supported |
| `applies` | Whether structured normalization applies to this action |
| `normalization_reason` | Human-readable explanation of the result |
| `evidence` | Collected field extraction evidence |

### PayloadFieldEvidence

Represents why a structured field was extracted.

| Field | Description |
|---|---|
| `field_name` | Payload field being explained |
| `source_kind` | `request_text`, `memory_context`, `source_metadata`, or `heuristic_default` |
| `source_excerpt` | Safe excerpt or hint used for extraction |
| `confidence` | Extraction confidence for the field |

### StructuredExecutionPreview

Represents the user-visible preview of what will execute from the structured payload.

| Field | Description |
|---|---|
| `preview_id` | Stable preview identifier |
| `session_id` | Owning runtime session |
| `action_type` | Structured action family |
| `title` | User-safe preview headline |
| `primary_fields` | Ordered preview fields shown to the user |
| `completeness` | Completeness state mirrored from the payload |
| `warning_text` | Optional warning or clarification text |

## Relationships

- One runtime session may produce zero or one `ActionNormalizationResult` per selected capability.
- One successful normalization produces one `StructuredActionPayload`.
- One `StructuredActionPayload` may have multiple `PayloadFieldEvidence` records.
- One `StructuredActionPayload` produces one `StructuredExecutionPreview` for runtime or approval surfaces.

## Validation Rules

- Only supported structured action types may produce structured payloads in this milestone.
- A structured payload marked `complete` must include the minimum execution fields required by that action type.
- A structured payload marked `partial` may proceed only through preview or confirmation-aware paths.
- A structured payload marked `insufficient` must not silently execute.
- Providers for supported action types must prefer structured fields over raw request text once a structured payload exists.

## State Transitions

### Normalization Flow

`planned -> normalization_attempted -> complete | partial | insufficient | not_applicable`

### Structured Execution Flow

`payload_created -> preview_available -> gated -> executed | denied | failed`
