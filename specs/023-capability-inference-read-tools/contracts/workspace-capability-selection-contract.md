# Contract: Workspace Capability Selection

## Purpose

Define the normalized contract for turning a freeform workspace request into either:

- reply fallback
- explicit read-tool execution
- explicit side-effect path
- clarification-needed outcome
- denied outcome

## Input Envelope

| Field | Required | Description |
|---|---|---|
| `request_id` | Yes | Unique runtime request identity |
| `user_input` | Yes | Raw workspace request text |
| `requested_capabilities` | No | Explicit capability hints that override inference |
| `source_metadata` | No | Caller and trust metadata |
| `attachments` | No | Multimodal inputs that may shape selection |
| `current_context_summary` | No | Relevant context already assembled for the request |

## Candidate Contract

Each evaluated candidate must provide:

| Field | Description |
|---|---|
| `capability_id` | Stable capability identity |
| `tool_id` | Stable tool identity |
| `selection_source` | Explicit, inferred, or fallback |
| `confidence` | Relative match confidence |
| `side_effect_class` | Read, write, or dispatch |
| `availability_state` | Availability summary |
| `selection_reason` | User-visible reason |

## Selection Rules

1. Explicit requested capabilities take priority over inferred candidates.
2. If no explicit capability exists, the runtime evaluates inferred candidates.
3. The runtime may select a read capability only when:
   - confidence is sufficient
   - availability is acceptable
   - trust and policy allow the path
4. The runtime must not silently auto-execute a higher-risk write or dispatch capability merely because it was inferred from freeform input.
5. If confidence or safety is insufficient, the runtime falls back to reply behavior or requests clearer intent.

## Output Contract

| Field | Description |
|---|---|
| `selected_capability_id` | Final capability, if any |
| `selected_tool_id` | Final stable tool identity, if any |
| `resolution_mode` | Reply fallback, read execution, side-effect path, denied, or clarification-needed |
| `explanation` | User-visible reason for the decision |
| `warnings` | Degradation or caution notes |
| `visibility_snapshot` | Availability and policy summary for the chosen path |

## Invariants

- Every non-reply selection must have a stable tool identity.
- Every selected path must carry a user-visible explanation.
- Reply fallback is a valid outcome and not considered an error.
- Ambiguous freeform input must never produce silent write or dispatch execution.
