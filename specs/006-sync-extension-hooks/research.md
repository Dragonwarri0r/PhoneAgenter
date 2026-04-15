# Research: Sync-Ready Share and Extension Hooks

## Decision 1: Extend `MemoryItem` directly instead of introducing a parallel sync table

**Decision**: Add future sync and merge metadata directly to the existing `MemoryItem` persistence model instead of creating a second sync-envelope table in `v0`.

**Rationale**:
- `MemoryItem` already carries `exposurePolicy` and `syncPolicy`, so the current design direction is to keep shareability and sync-readiness attached to the record itself.
- Directly extending `MemoryItem` keeps repository and retrieval logic coherent and reduces the risk of future dual-write drift.
- `006` is specifically about avoiding schema-breaking rewrites later; adding the extra fields now is the simplest way to preserve continuity.

**Alternatives considered**:
- A separate `SyncEnvelope` Room entity: rejected because it would add coordination complexity before there is any real sync executor.
- A purely in-memory wrapper: rejected because it would not satisfy the requirement that persisted records already carry the needed metadata.

## Decision 2: Keep all new sync behavior declarative in `v0`

**Decision**: Add metadata, export decisions, merge-input normalization, and extension contracts, but do not implement real sync transport, conflict resolution workflows, or remote import/export UI in this milestone.

**Rationale**:
- The roadmap and constitution both keep `v0` local-only.
- The goal of `006` is architectural readiness, not delivering distributed behavior.
- Declarative hooks are enough to validate that later sync work can layer on top of the current schema.

**Alternatives considered**:
- Building a minimal sync executor now: rejected because it would expand the milestone beyond the stated scope.
- Deferring all sync concerns to a later milestone: rejected because it would increase the chance of schema churn and migration risk.

## Decision 3: Use redaction-aware export bundles as the portability boundary

**Decision**: Represent future sharing/export through an `ExportBundle` contract that can carry either redacted summaries or full content based on exposure policy.

**Rationale**:
- The spec explicitly requires distinguishing `private`, `shareable summary`, and `shareable full`.
- An export bundle gives the runtime a clean contract for future share sheets, backup/export features, and sync payloads without leaking raw evidence by default.
- It fits the current local-first model because bundles can be generated entirely on-device.

**Alternatives considered**:
- Exporting raw `MemoryItem` directly: rejected because it would couple persistence format to future portability paths and risk exposing private evidence.
- Defining only a textual summary export: rejected because later full-fidelity portability paths would still need a second contract.

## Decision 4: Represent merge-readiness through normalized conflict inputs, not conflict resolution yet

**Decision**: Add a `MergeCandidate` contract and logical-version/origin metadata so future merge logic has structured inputs, but do not implement an actual merge engine now.

**Rationale**:
- The current milestone only needs to prove that the schema can represent conflicting updates.
- Logical versioning, record identity, and origin metadata are the stable ingredients needed before conflict strategies can be designed.
- This keeps the implementation narrow while still satisfying the “future merge input” requirement.

**Alternatives considered**:
- Adding full CRDT logic now: rejected as far too large for the milestone.
- Using only timestamps with no logical identity: rejected because it would be too weak for future merge evaluation.

## Decision 5: Register future portability/providers through extension contracts, not ad hoc flags

**Decision**: Define an `ExtensionRegistration` contract for future export providers, import providers, and sync-capable portability extensions.

**Rationale**:
- The project already prefers adapter-based capability integration.
- A registration contract gives future providers a stable place to declare supported payload shapes, required metadata, and privacy guarantees.
- This avoids baking future provider assumptions directly into the runtime core.

**Alternatives considered**:
- Adding extension booleans to existing capability records: rejected because it would not scale to future transport or portability providers.
- Leaving extension work undefined until later: rejected because the milestone explicitly requires extension-ready contracts.
