# Research: External Caller Interop Contracts

## Decision 1: Introduce a canonical interop envelope above `RuntimeRequest`

- **Decision**: Add a normalized interop envelope for external caller requests before they are mapped into `RuntimeRequest`.
- **Why**: `007` proved the first live handoff path, but current ingress still mixes transport-specific parsing with runtime mapping. A canonical envelope keeps external contracts stable without disturbing the core runtime shape.
- **Alternatives considered**:
  - Map every external contract directly into `RuntimeRequest`: rejected because it keeps transport quirks scattered through ingress code.
  - Replace `RuntimeRequest` with a fully externalized request object: rejected because it would create too much churn for the current runtime backbone.

## Decision 2: Caller identity and trust semantics must be shared across entry types

- **Decision**: Represent caller identity, trust state, trust reason, and URI grant summary through one shared model used by all covered inbound contracts.
- **Why**: Governance and explainability depend on consistent caller semantics. Different trust language for share-text, share-media, and future structured callers would fragment the product.
- **Alternatives considered**:
  - Keep trust logic embedded in each parser: rejected because it scales poorly and conflicts with the roadmap.
  - Collapse everything to a single package name string: rejected because that would discard source and grant explainability.

## Decision 3: The first future callable surface should be minimal

- **Decision**: Define a minimal callable surface descriptor carrying canonical input text, optional attachments/grants, requested scopes, caller identity, and compatibility versioning.
- **Why**: `016` needs to preserve a path beyond share intents, but it should not become a full generic extension bus.
- **Alternatives considered**:
  - Standardize only share-based inbound flows: rejected because it would leave the next external contract undefined.
  - Define a fully generic plugin manifest now: rejected because that belongs in `017`.

## Decision 4: URI/content grants must be modeled explicitly but boundedly

- **Decision**: Preserve URI grant metadata as an explainable summary rather than treating it as opaque parser-only state.
- **Why**: Cross-app content access is a core Android interop concern and affects trust, attachment handling, and future auditability.
- **Alternatives considered**:
  - Drop grant metadata after attachment import: rejected because it loses explainability.
  - Preserve raw platform objects through the whole runtime: rejected because it would couple the contract too tightly to Android internals.

## Decision 5: Forward compatibility should be explicit

- **Decision**: Add interop versioning and compatibility signaling to the canonical envelope and future callable descriptor.
- **Why**: External contracts inevitably evolve, and `016` is the right place to make version compatibility deliberate.
- **Alternatives considered**:
  - Ignore versioning until a third-party ecosystem exists: rejected because it invites ad hoc breakage later.
