# Research: Portability Bundles

## Decision 1: Build on top of `ExportDecisionService`

**Decision**: Reuse the existing `ExportDecisionService` as the source of truth for redaction policy, export mode, and extension compatibility.

**Rationale**: `006` already established the privacy and future-sync contract. `011` should turn that into user value instead of adding a second export rules engine.

**Alternatives considered**:

- Create a separate portability export rules layer: rejected because it would duplicate privacy logic.
- Export raw `MemoryItem` objects directly: rejected because it would leak implementation details and bypass redaction decisions.

## Decision 2: Make the first outbound format text-based

**Decision**: Use a formatted text bundle for the first portability export path and dispatch it with Android share intents.

**Rationale**: Text is easy to preview, easy to validate for redaction, and aligns with the current local-first/mobile scope. It also keeps the milestone small enough to remain a single product slice.

**Alternatives considered**:

- File-based JSON export only: rejected because it adds friction before user value is proven.
- Zip/archive bundle format: rejected as too large for this milestone.

## Decision 3: Keep compatibility preview-only

**Decision**: Show compatibility and incompatibility lines for future import/extension surfaces, but do not implement actual import.

**Rationale**: The roadmap positions portability as a bridge to future import/sync, but actual import belongs in a later milestone. Preview-only compatibility keeps expectations honest.

**Alternatives considered**:

- Implement a full import validator: rejected as scope creep.
- Hide compatibility completely: rejected because it weakens the "portable" story.

## Decision 4: Put export affordances inside the context inspector

**Decision**: Add export entry points to the existing context inspector rather than creating a separate portability center.

**Rationale**: The inspector already explains active memory and export metadata. This is the smallest product step that keeps portability close to the records users are actually inspecting.

**Alternatives considered**:

- Add a dedicated new screen: rejected because it would fragment the workspace for a first milestone.
- Trigger export only from governance: rejected because portability is a record-level action, not a caller-level action.
