# Feature Specification: Sync-Ready Share and Extension Hooks

**Feature Branch**: `006-sync-extension-hooks`  
**Created**: 2026-04-08  
**Status**: Draft  
**Input**: User description: "Add sync ready share merge and extension hooks"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Mark Data for Future Sharing Without Syncing Yet (Priority: P1)

As a user, my data should already carry the metadata needed for future sharing and sync policies, even though `v0` still keeps everything local.

**Why this priority**: If metadata is missing in `v0`, future sync work will require schema-breaking rewrites.

**Independent Test**: Can be tested independently by creating memory and execution records and verifying that exposure, shareability, and sync metadata are present even when no sync execution occurs.

**Acceptance Scenarios**:

1. **Given** a new memory item is written in `v0`, **When** it is stored, **Then** it carries exposure and sync metadata even though it remains local-only by default.
2. **Given** a record is marked private, **When** an exportability decision is evaluated, **Then** the system can determine that it must not be shared.

---

### User Story 2 - Prepare for Future Merge Inputs (Priority: P2)

As a platform builder, I can add future multi-device merge logic without replacing the existing memory and runtime schemas.

**Why this priority**: Merge readiness is cheaper to design early than to retrofit later.

**Independent Test**: Can be tested independently by validating that stored records contain enough versioning and origin metadata to support a later merge contract.

**Acceptance Scenarios**:

1. **Given** a stored memory item, **When** its metadata is inspected, **Then** it includes origin and logical version information for future merge decisions.
2. **Given** a future merge candidate is simulated, **When** the system evaluates its metadata, **Then** the current schema can represent the conflict input without requiring a schema rewrite.

---

### User Story 3 - Add Future Providers and Export Paths Safely (Priority: P3)

As a platform builder, I can add new providers, export paths, or portability features later without redesigning the runtime core.

**Why this priority**: Extensibility is a deliberate project goal and should not be postponed until after schemas harden.

**Independent Test**: Can be tested independently by validating that extension registration and export contracts can be described against current runtime entities without changing the core domain model.

**Acceptance Scenarios**:

1. **Given** a new provider type is proposed, **When** it is described against the runtime contracts, **Then** it fits through a defined extension hook rather than requiring a new core abstraction.
2. **Given** a shareable summary export is needed, **When** an export bundle is generated, **Then** the runtime can represent it without exposing private raw evidence by default.

### Edge Cases

- What happens when a memory item is shareable as a summary but not shareable in full?
- How should the system represent records created before sync metadata rules become stricter later?
- What happens when two future devices claim incompatible edits to the same logical record?
- How does the runtime handle extension providers that require metadata not present on older records?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST store exposure and sync metadata on memory records even though `v0` does not execute cross-device sync.
- **FR-002**: The system MUST distinguish at minimum private, shareable summary, and shareable full exposure policies.
- **FR-003**: The system MUST distinguish at minimum local-only, summary-sync-ready, and full-sync-ready sync policies.
- **FR-004**: The system MUST record origin and logical version metadata sufficient for future merge evaluation.
- **FR-005**: The system MUST support redaction-aware export decisions so private raw evidence is not assumed shareable.
- **FR-006**: The system MUST define extension-ready contracts for future provider registration and future data portability paths.
- **FR-007**: The system MUST allow `v0` functionality to operate entirely with local-only defaults.
- **FR-008**: The system MUST avoid introducing schema assumptions that force a breaking rewrite when later sync, merge, or export features are implemented.

### Key Entities *(include if feature involves data)*

- **SyncEnvelope**: The metadata wrapper that describes future sync and merge properties for a record.
- **MergeCandidate**: The structured representation of a record version that may later need conflict handling.
- **ExportBundle**: The shareable representation of a record or summary after exposure policy is applied.
- **ExtensionRegistration**: The description of a future provider or portability extension joining the runtime.
- **DataRedactionPolicy**: The rule set that determines what must remain local or be summarized before sharing.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All new memory records created after this milestone contain explicit exposure and sync metadata.
- **SC-002**: A future merge input can be represented against the `v0` schema without requiring core entity replacement.
- **SC-003**: Shareability decisions can be made from stored metadata alone in milestone validation scenarios.
- **SC-004**: `v0` local-only behavior remains the default in all functional flows after these hooks are added.

## Assumptions

- Full multi-device sync remains outside `v0`.
- Export and import user interfaces may arrive after this milestone, but the required contracts should exist first.
- Redaction-aware sharing is more important than full-fidelity portability in the first extensibility slice.
- This spec exists to preserve architectural flexibility and should not expand `v0` into a full synchronization project.
