# Contract: Read Tool Provider

## Purpose

Define the normalized provider contract for explicit read capabilities so first-party and future external-app providers can plug into the same runtime path.

## Registration Contract

Every read provider registration must expose:

| Field | Description |
|---|---|
| `provider_id` | Stable provider identity |
| `capability_id` | Read capability served by the provider |
| `provider_type` | Read provider family classification |
| `availability_state` | Current provider availability |
| `required_permissions` | Permission requirements needed for execution |
| `required_scope` | Scope label used for policy and audit |
| `route_reason` | User-visible reason this provider can or cannot execute |

## Execution Request Contract

| Field | Required | Description |
|---|---|---|
| `request_id` | Yes | Runtime request identity |
| `capability_id` | Yes | Explicit read capability being executed |
| `query_text` | Yes | User request or normalized query text |
| `query_scope` | Yes | Bounded lookup scope |
| `result_limit` | Yes | Maximum allowed result count |
| `permission_state` | Yes | Current access eligibility summary |
| `selection_explanation` | Yes | Reason the runtime chose this path |

## Execution Result Contract

| Field | Description |
|---|---|
| `outcome_kind` | `matched`, `no_results`, `unavailable`, or `failed` |
| `provider_id` | Executed provider |
| `matched_records` | Bounded summaries of matched records |
| `result_count` | Number of matched results |
| `user_message` | User-visible execution outcome |
| `audit_summary` | Audit-facing outcome summary |

## Invariants

- Read providers must return bounded results suitable for conversational display.
- `no_results` and `unavailable` are first-class outcomes, not failures to be hidden.
- A read provider must never masquerade as a write or dispatch path.
- Permission failures must be truthful and explicit.
- The same capability can have multiple providers, but the runtime must choose one normalized route.
