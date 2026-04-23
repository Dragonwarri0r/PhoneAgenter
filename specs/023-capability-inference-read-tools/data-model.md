# Data Model: Capability Inference and Read Tools

## 1. Capability Candidate

Represents one possible capability path considered for a workspace request before final selection.

| Field | Description |
|---|---|
| `capability_id` | Stable capability identity such as `generate.reply` or `calendar.read` |
| `tool_id` | Stable tool identity shown in preview, routing, and audit |
| `selection_source` | Whether the candidate came from explicit hinting, freeform inference, fallback behavior, or provider suggestion |
| `confidence` | Relative confidence that the candidate matches user intent |
| `side_effect_class` | Read, write, or dispatch classification |
| `availability_state` | Whether the candidate is currently available, degraded, denied, or unavailable |
| `policy_eligibility` | Whether current policy and trust state allow this candidate to proceed |
| `selection_reason` | User-understandable summary of why the candidate is relevant |
| `provider_options` | Ranked provider options for this capability |

**Validation Rules**

- Explicitly requested capabilities outrank inferred candidates.
- Read candidates may auto-select only when confidence, availability, and policy all permit it.
- Write and dispatch candidates must never bypass existing confirmation and policy rules.
- A candidate without a stable tool identity is not eligible for final selection.

## 2. Capability Selection Outcome

Represents the runtime’s final interpretation of a workspace request.

| Field | Description |
|---|---|
| `request_id` | Runtime request identity |
| `selected_capability_id` | Final selected capability, if any |
| `selected_tool_id` | Final selected tool identity, if any |
| `selected_provider_id` | Final selected provider route, if any |
| `resolution_mode` | Reply fallback, explicit read execution, explicit side-effect path, denial, or clarification-needed outcome |
| `confidence` | Confidence attached to the final selection |
| `explanation` | User-visible reason for the final choice |
| `warnings` | Additional caution or degradation notes |
| `visibility_snapshot` | Request-scoped availability and eligibility summary for the chosen tool |

**State Transitions**

1. `candidate_generation`
2. `candidate_evaluation`
3. `reply_fallback` or `capability_selected` or `clarification_needed` or `denied`
4. If capability selected: `provider_routed`
5. If routed: `executed` or `unavailable` or `failed`

## 3. Read Tool Request

Represents the normalized execution input for an explicit read capability.

| Field | Description |
|---|---|
| `request_id` | Runtime request identity |
| `capability_id` | Explicit read capability being executed |
| `query_text` | User’s lookup request or derived search text |
| `query_scope` | Bounded lookup scope such as today, next 7 days, or a named filter |
| `result_limit` | Maximum number of results allowed in one execution |
| `permission_requirements` | Required user-visible permission gates |
| `route_explanation` | Explanation shown to the user before or during execution |
| `selection_context` | Capability-selection metadata that led to this request |

**Validation Rules**

- Every explicit read request must define a bounded query scope.
- Every explicit read request must define a bounded result limit.
- Read requests cannot include write or dispatch semantics.
- Permission requirements must be explicit and truthful.

## 4. Read Tool Result

Represents the normalized result of an explicit read capability.

| Field | Description |
|---|---|
| `request_id` | Runtime request identity |
| `capability_id` | Executed capability |
| `provider_id` | Provider route used for execution |
| `outcome_kind` | `matched`, `no_results`, `unavailable`, or `failed` |
| `record_summaries` | Bounded conversational summaries of matched records |
| `result_count` | Number of matched records returned |
| `user_message` | Final user-visible outcome summary |
| `audit_summary` | Explainable audit text for the execution path |

**Validation Rules**

- `matched` results must not exceed the declared `result_limit`.
- `no_results` must be explicit and must not contain fabricated records.
- `unavailable` must explain the real limiting factor.
- Results must remain conversationally displayable without requiring a secondary browsing UI.

## 5. Capability Extension Registration

Represents the shared registration entry used to add explicit read capabilities or providers without introducing new core patterns.

| Field | Description |
|---|---|
| `extension_id` | Stable registration identity |
| `extension_type` | Capability provider, context source, ingress, or related extension kind |
| `contributed_capabilities` | Capability ids contributed by the extension |
| `privacy_guarantee` | Declared privacy behavior |
| `required_dependencies` | Required metadata, permissions, or runtime conditions |
| `default_enablement` | Default enablement or degraded state |
| `compatibility_notes` | Human-readable compatibility and dependency summary |

**Relationships**

- A `Capability Extension Registration` may contribute one or more `Capability Candidate` entries.
- A `Capability Selection Outcome` chooses at most one candidate for execution.
- A selected read candidate produces one `Read Tool Request`.
- A `Read Tool Request` produces one `Read Tool Result`.
