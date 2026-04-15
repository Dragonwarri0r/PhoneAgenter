# Data Model: External Caller Interop Contracts

## InteropRequestEnvelope

Canonical normalized request envelope for external callers.

**Fields**
- `interopRequestId`: stable request identifier
- `entryType`: inbound contract type such as share-text, share-media, or callable
- `callerIdentity`: normalized caller metadata
- `sharedText`: optional normalized text payload
- `attachments`: canonical attachment references
- `requestedScopes`: requested scopes from the caller, if any
- `uriGrantSummary`: explainable content grant summary
- `compatibilitySignal`: interop versioning and compatibility metadata
- `receivedAtEpochMillis`: ingestion timestamp

## CallerContractIdentity

Stable caller identity shared across entry types.

**Fields**
- `originApp`: high-level source identity
- `packageName`: Android package name when available
- `sourceLabel`: user-facing label
- `trustState`: trusted, unverified, denied
- `trustReason`: explainable reason
- `referrerUri`: optional referrer summary
- `contractVersion`: caller contract version label

## UriGrantSummary

Explainable summary of granted cross-app content access.

**Fields**
- `grantCount`: number of granted items
- `grantedMimeTypes`: summarized MIME families
- `grantMode`: read-only, write-capable, mixed, or unknown
- `expiresWithSession`: whether access is expected to be short-lived
- `summaryText`: user-facing explanation

## CallableSurfaceDescriptor

Minimal structured description of a future external callable request surface.

**Fields**
- `surfaceId`: stable callable surface id
- `displayName`: user-facing label
- `supportedFields`: canonical request field names
- `supportedScopes`: scope ids available to the surface
- `supportsAttachments`: whether attachment grants are allowed
- `interopVersion`: supported envelope version

## InteropCompatibilitySignal

Forward-compatibility metadata for inbound requests.

**Fields**
- `interopVersion`: declared request version
- `isCompatible`: whether current runtime can accept it
- `compatibilityReason`: explanation or migration hint
- `unknownFieldCount`: count of unsupported fields seen during parsing
