# Research: System Source Ingestion

## Decision 1: Reuse `MemoryItem` for system-source records

**Decision**: Materialize contacts/calendar results into `MemoryItem` with `sourceType = SYSTEM_SOURCE`.

**Rationale**:

- The runtime already retrieves memory through a common memory pipeline.
- Reusing `MemoryItem` keeps context loading simple and avoids parallel retrieval systems.

**Alternatives considered**:

- Separate system-source store: cleaner separation but duplicates retrieval logic too early.

## Decision 2: Keep ingestion heuristic-triggered and bounded

**Decision**: Run ingestion only when the request appears relevant to contacts or scheduling, and cap results to a small number of records.

**Rationale**:

- This avoids importing the entire contacts/calendar dataset.
- It keeps performance and privacy aligned with the milestone goals.

**Alternatives considered**:

- Full background import: too broad and too invasive.
- Manual-only import: lower risk, but not enough product value for this milestone.

## Decision 3: Handle permissions in the workspace

**Decision**: Request contacts/calendar permissions from the current workspace surface and expose current source status there.

**Rationale**:

- Keeps the feature product-visible without creating a separate settings architecture.
- Lets the user understand permission state in the same place where context is shown.

**Alternatives considered**:

- Separate settings screen: too large for this slice.

## Decision 4: Contacts + calendar only

**Decision**: Limit `010` to contacts and calendar.

**Rationale**:

- They are the most valuable and stable first-party sources for the current runtime flows.
- This avoids turning `010` into a general connector framework.

**Alternatives considered**:

- Notifications/files/browser: useful later, but broader and more variable.
