# Contract: Runtime Session Pipeline

## Purpose

This contract defines the stable top-level session lifecycle used by the local runtime.
It must remain valid whether a request produces pure generation, provider-backed execution, denial, cancellation, or failure.

## Contract Surface

### 1. Submit Runtime Request

The UI or calling layer submits a normalized request into the runtime.

#### Request fields

| Field | Description |
|---|---|
| `request_id` | Stable request identifier |
| `user_input` | Primary user text |
| `selected_model_id` | Active local model identifier |
| `transcript_context` | Existing visible transcript entries |
| `requested_capabilities` | Optional normalized capability hints |

#### Immediate result

The runtime must return:

- `session_id`
- initial session state
- initial compact status summary

### 2. Observe Session Events

Observers subscribe to ordered updates for a single execution session.

#### Ordered session events

| Event | Description |
|---|---|
| `session_started` | Session accepted and created |
| `stage_changed` | The lifecycle advanced to a new ordered stage |
| `status_summary_updated` | Compact user-facing summary changed |
| `capability_requested` | Runtime normalized a provider action |
| `capability_started` | Provider execution began |
| `capability_completed` | Provider execution completed |
| `capability_failed` | Provider execution failed |
| `session_completed` | Terminal success |
| `session_failed` | Terminal failure |
| `session_cancelled` | Terminal cancellation |
| `session_denied` | Terminal denial |

### 3. Terminal Outcome

Every session must end in exactly one terminal event and one terminal outcome.

#### Outcome fields

| Field | Description |
|---|---|
| `session_id` | Associated execution session |
| `terminal_state` | `success`, `failure`, `cancelled`, or `denied` |
| `user_message` | User-safe summary |
| `output_text` | Optional assistant-visible output |
| `provider_results` | Optional normalized provider results |

### 4. Compact Status Summary

At any point, the runtime must be able to provide a current compact summary suitable for the workspace context/status surface.

#### Summary fields

| Field | Description |
|---|---|
| `session_id` | Associated execution session |
| `headline` | Primary short summary |
| `stage_label` | Compact label such as `Loading context` or `Executing` |
| `supporting_text` | Supporting user-safe explanation |
| `is_busy` | Whether the runtime is still progressing |
| `awaiting_input` | Whether user input or approval is required |
| `is_terminal` | Whether the session is finished |

## Behavioral Guarantees

- Every accepted request creates exactly one `session_id`.
- Session events are ordered within a session.
- A session emits exactly one terminal event.
- Stage updates remain valid whether or not any capability provider is used.
- Provider substitution must not change the top-level session event taxonomy.
- Duplicate terminal signals must not corrupt the final session outcome.

## Out of Scope for This Contract

- Full persona and memory retrieval logic from `003`
- Final approval and policy engine logic from `004`
- Android AppFunctions and cross-app provider bridge logic from `005`
- Multi-device sync and merge behavior from `006`

