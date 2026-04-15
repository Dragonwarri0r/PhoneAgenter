# Data Model: Android Capability Bridge

## Overview

This feature adds a normalized Android capability bridge between runtime planning and execution.
It introduces registry records, provider descriptors, caller trust evaluation, routing outcomes, and normalized invocation results.

## Entities

### CapabilityRegistration

Represents one capability known to the runtime capability registry.

| Field | Description |
|---|---|
| `capability_id` | Stable runtime capability id such as `reply.generate` or `message.send` |
| `display_name` | User-safe title for explainability |
| `required_scopes` | Policy scopes needed before execution |
| `risk_level_hint` | Registry-level risk hint used before full policy resolution |
| `confirmation_policy` | Registry-level confirmation preference |
| `provider_descriptors` | Ordered candidate providers for this capability |
| `availability` | Aggregated bridge availability snapshot |

### ProviderDescriptor

Represents one concrete Android-facing provider path for a capability.

| Field | Description |
|---|---|
| `provider_id` | Stable provider identifier |
| `capability_id` | Owning capability id |
| `provider_type` | `app_functions`, `intent`, `share`, or reserved `accessibility` |
| `priority` | Routing priority, lower wins |
| `required_scopes` | Additional provider-specific scopes |
| `availability` | Current readiness and eligibility |
| `provider_app` | Owning or target package name |
| `provider_label` | User-safe provider description |
| `route_metadata` | Bridge-specific execution metadata |

### CallerIdentity

Represents the normalized identity of the caller that initiated a capability request.

| Field | Description |
|---|---|
| `caller_id` | Stable normalized caller id |
| `origin_app` | Request origin string or package id |
| `is_trusted` | Whether the caller passed verification |
| `trust_reason` | Human-readable trust or denial explanation |
| `package_name` | Optional package identifier |
| `signature_digest` | Optional signature digest for future package verification |

### CapabilityAvailability

Represents current provider readiness.

| Field | Description |
|---|---|
| `provider_id` | Owning provider |
| `state` | `available`, `degraded`, `unavailable`, or `restricted` |
| `reason` | Explanation for the current state |
| `checked_at` | Availability check timestamp |

### InvocationResult

Represents the normalized outcome returned after routing and execution.

| Field | Description |
|---|---|
| `invocation_id` | Stable invocation id |
| `session_id` | Owning runtime session |
| `capability_id` | Requested capability |
| `provider_id` | Chosen provider |
| `provider_type` | Bridge type used |
| `success` | Whether execution succeeded |
| `output_text` | Optional normalized output |
| `failure_reason` | Optional normalized failure reason |
| `route_explanation` | User-safe explanation of why this provider was chosen |

## Relationships

- One `CapabilityRegistration` has one or more `ProviderDescriptor` candidates.
- One `ProviderDescriptor` has one current `CapabilityAvailability` snapshot at evaluation time.
- One runtime request produces one `CallerIdentity`.
- One routed capability execution yields one `InvocationResult`.
- One `InvocationResult` links back to one runtime session and one chosen provider.

## Validation Rules

- Every capability registration must have at least one provider descriptor.
- Provider descriptors must have unique `(capability_id, provider_id)` pairs.
- `app_functions` providers must have higher preference than `intent` or `share` fallbacks for the same capability.
- `restricted` or `untrusted` caller identities must not route restricted capabilities.
- Routing must fail cleanly when no provider is both available and eligible.

## State Transitions

### Routing Flow

`requested -> caller_verified -> capability_resolved -> provider_ranked -> provider_selected | no_provider`

### Provider Availability Flow

`available -> degraded -> unavailable`

`available -> restricted`

### Invocation Flow

`selected -> invoked -> succeeded | failed`
