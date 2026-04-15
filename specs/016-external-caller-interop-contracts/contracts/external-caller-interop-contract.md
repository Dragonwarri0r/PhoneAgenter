# Contract: External Caller Interop Contracts

## Purpose

Define one stable interop contract family for external callers so share targets, future callable request surfaces, governance, and policy all converge on the same caller and trust semantics.

## Canonical Inbound Contract

Covered external entry types must normalize into a shared `InteropRequestEnvelope` carrying:

- `interopRequestId`
- `entryType`
- `callerIdentity`
- `sharedText`
- `attachments`
- `requestedScopes`
- `uriGrantSummary`
- `compatibilitySignal`

## Covered Inbound Styles In `016`

- text share handoff
- media share handoff
- future minimal structured callable request

## Caller Identity Contract

All covered entry types must use the same normalized caller model with:

- `originApp`
- `packageName`
- `sourceLabel`
- `trustState`
- `trustReason`
- `referrerUri`
- `contractVersion`

## URI Grant Contract

Inbound requests that depend on cross-app content must preserve an explainable URI/content grant summary including:

- number of granted items
- summarized MIME families
- access mode summary
- whether the grant is expected to be short-lived

## Governance And Policy Contract

Governance and policy must evaluate covered interop requests using:

- the normalized caller identity
- requested scopes
- trust state
- URI grant summary when content access matters

Entry-specific trust rules are allowed only at the parsing boundary and must resolve into shared caller semantics before runtime execution.

## Forward Compatibility Contract

Interop envelopes must carry version compatibility metadata so:

- unsupported future fields can be detected
- unknown data does not silently become runtime behavior
- rejection or downgrade can be explained
