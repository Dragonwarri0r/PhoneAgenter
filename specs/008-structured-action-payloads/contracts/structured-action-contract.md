# Contract: Structured Action Payloads

## Purpose

This contract defines how Mobile Claw turns selected high-value actions into structured payloads before provider execution, and how those payloads remain visible through preview, approval, and audit surfaces.

## Contract Surface

### 1. Normalize Supported Action Types

The runtime must attempt structured normalization for supported action types.

#### Required fields

| Field | Description |
|---|---|
| `capability_id` | Selected runtime capability |
| `action_type` | Structured action family |
| `completeness` | `complete`, `partial`, or `insufficient` |
| `normalization_reason` | User-safe explanation |

#### Behavioral guarantees

- `message.send`, `calendar.write`, and `external.share` must be eligible for structured normalization.
- Unsupported or non-applicable requests must remain on the normal text-generation path without breaking runtime submission.

### 2. Produce Structured Payload

The runtime must create a structured payload for supported action types.

#### Required fields

| Field | Description |
|---|---|
| `payload_id` | Stable payload id |
| `action_type` | Structured action family |
| `raw_request_text` | Original user text |
| `structured_fields` | Execution-safe extracted fields |
| `completeness` | Payload completeness state |

#### Behavioral guarantees

- Structured fields must become the primary execution contract for supported action types.
- Original request text must remain visible for explainability but must not be the only execution input once a structured payload exists.

### 3. Surface Preview and Safety State

The runtime must expose a structured preview and completeness state before or during execution.

#### Required fields

| Field | Description |
|---|---|
| `title` | User-safe preview title |
| `primary_fields` | Ordered structured fields for user review |
| `completeness` | Completeness state |
| `warning_text` | Optional warning or clarification text |

#### Behavioral guarantees

- Partial or insufficient payloads must not silently auto-execute.
- Preview and approval surfaces must show structured fields for supported actions.

### 4. Execute Providers From Structured Fields

Providers for supported actions must consume normalized structured fields.

#### Required fields

| Field | Description |
|---|---|
| `action_type` | Structured action family |
| `provider_id` | Downstream provider |
| `execution_fields` | Structured values passed to the provider |

#### Behavioral guarantees

- Android intent/share providers must not rebuild their main execution payload solely from the raw request string when a structured payload is available.
- Provider-specific Android details remain outside the normalization layer.

## Out of Scope for This Contract

- Universal action extraction for every capability
- Multi-step tool plans or compound action orchestration
- Deep system-source enrichment beyond the current runtime context
- Full tool contract standardization across every future provider
