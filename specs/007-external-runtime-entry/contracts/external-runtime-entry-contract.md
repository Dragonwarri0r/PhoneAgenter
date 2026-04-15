# Contract: Trusted External Runtime Entry

## Purpose

This contract defines how Mobile Claw receives a supported Android external handoff, normalizes it into a canonical runtime request, and surfaces source/trust information without leaking Android-specific intent details into downstream runtime layers.

## Contract Surface

### 1. Register Supported External Entry

The app must declare at least one supported external Android entry path.

#### Required fields

| Field | Description |
|---|---|
| `entry_id` | Stable external entry identifier |
| `entry_type` | External entry family |
| `supported_actions` | Accepted Android actions |
| `supported_mime_types` | Accepted MIME types |
| `status` | Current entry readiness |

#### Behavioral guarantees

- The first entry must support `ACTION_SEND` with `text/plain`.
- Unsupported content types must fail before runtime submission begins.

### 2. Normalize Incoming Handoff

The Android boundary must convert the raw incoming intent into a normalized inbound handoff contract.

#### Required fields

| Field | Description |
|---|---|
| `handoff_id` | Stable handoff identifier |
| `shared_text` | Normalized text content |
| `entry_id` | Matched external entry |
| `caller_metadata` | Normalized source/trust metadata |
| `runtime_request_id` | Canonical runtime request identifier |

#### Behavioral guarantees

- Missing or malformed text payloads must fail safely.
- Android-specific action, MIME, and extras parsing must stay in the ingress layer.
- Downstream runtime layers must receive canonical request data, not raw intent objects.

### 3. Submit Canonical Runtime Request

The inbound handoff must be converted into the same canonical runtime request shape used by the workspace.

#### Required fields

| Field | Description |
|---|---|
| `request_id` | Canonical runtime request id |
| `user_input` | Final runtime input text |
| `origin_app` | Normalized origin identifier |
| `requested_capabilities` | Optional capability hints |
| `created_at_epoch_millis` | Request creation timestamp |

#### Behavioral guarantees

- Internal and external submissions must share the same runtime request contract.
- Runtime planning, context loading, risk, policy, capability routing, and audit must all continue to use the existing execution path.

### 4. Surface Source and Trust Outcome

The workspace and audit layers must show where the handoff came from and how trust was resolved.

#### Required fields

| Field | Description |
|---|---|
| `source_label` | User-safe source attribution |
| `trust_state` | `trusted`, `unverified`, or `denied` |
| `trust_reason` | User-safe explanation |
| `session_id` | Runtime session tied to the handoff |

#### Behavioral guarantees

- Accepted handoffs must show source/trust context in runtime status or audit surfaces.
- Rejected handoffs must still produce a user-visible and auditable denial reason.
- The user must see the external handoff as a visible new or resumed agent session, not as hidden background work.

## Out of Scope for This Contract

- Structured action payload extraction beyond text-first handoff
- Full third-party SDKs or bound-service invocation
- Multi-device sync or portability bundles
- Real Android 16 AppFunctions framework integration
