# Contract: Android Capability Bridge

## Purpose

This contract defines how the runtime discovers Android capability providers, verifies callers, chooses a provider path, and returns normalized bridge results.

## Contract Surface

### 1. Resolve Caller Identity

The runtime must normalize the caller before routing restricted capabilities.

#### Required fields

| Field | Description |
|---|---|
| `origin_app` | Request origin string or package |
| `caller_id` | Normalized caller id |
| `is_trusted` | Whether the caller is allowed to request restricted capabilities |
| `trust_reason` | User-safe trust or denial explanation |

#### Behavioral guarantees

- Restricted capabilities must not continue when `is_trusted` is false.
- Caller verification must happen before provider routing for restricted capabilities.

### 2. Discover Capability Registration

The runtime must retrieve one normalized capability registration for the requested capability id.

#### Required fields

| Field | Description |
|---|---|
| `capability_id` | Requested runtime capability |
| `provider_descriptors` | Ordered providers for this capability |
| `availability` | Aggregated availability state |

#### Behavioral guarantees

- Registry entries must not expose app-specific behavior outside the normalized contract.
- AppFunctions descriptors should rank ahead of fallback descriptors for the same capability.

### 3. Route Provider

The runtime selects the best eligible provider for the current request and caller.

#### Required fields

| Field | Description |
|---|---|
| `provider_id` | Selected provider |
| `provider_type` | `app_functions`, `intent`, or `share` |
| `route_explanation` | User-safe explanation of the routing choice |

#### Behavioral guarantees

- Routing must prefer AppFunctions when available and eligible.
- If the preferred provider is unavailable, the router may choose the next approved fallback.
- If no provider is eligible, the router must return a normalized failure.

### 4. Invoke Provider

The chosen provider returns a normalized invocation result.

#### Required fields

| Field | Description |
|---|---|
| `invocation_id` | Stable invocation id |
| `success` | Whether the invocation succeeded |
| `output_text` | Optional normalized output |
| `failure_reason` | Optional normalized failure reason |
| `provider_id` | Chosen provider |
| `provider_type` | Chosen bridge type |

#### Behavioral guarantees

- Runtime callers must receive one stable result shape regardless of provider type.
- Provider-specific failures must be normalized into user-safe runtime messaging.

## Out of Scope for This Contract

- Real Android 16 framework-only AppFunctions APIs as a compile-time requirement
- Accessibility automation execution
- Multi-device capability sync
- Third-party plugin marketplaces
