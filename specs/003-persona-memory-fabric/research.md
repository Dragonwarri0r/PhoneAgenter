# Research: Persona and Scoped Memory Fabric

## Decision 1: Use Preferences DataStore for Persona and Room for Memory

**Decision**: Store the stable persona profile in Preferences DataStore and store memory items plus retrieval metadata in a Room database.

**Rationale**:

- Persona is a small, low-churn profile with predictable fields and simple update semantics.
- Memory retrieval needs queryable filters across lifecycle, scope, exposure policy, sync policy, timestamps, and pin state.
- Room gives `003` a strong local storage foundation without forcing sync or network infrastructure into the design.

**Alternatives considered**:

- **Store both persona and memory in DataStore**: Rejected because memory retrieval and mutation would become awkward and inefficient as the item count grows.
- **Keep both persona and memory in memory-only repositories for v0**: Rejected because this milestone should establish durable local behavior, not just transient demo state.

## Decision 2: Retrieve Context with Deterministic Filtering and Simple Relevance Ranking

**Decision**: Use deterministic retrieval with hard filtering first, then rank eligible memory items using lightweight heuristics such as lexical overlap, recency, and pin/manual-importance boosts.

**Rationale**:

- The milestone goal is correctness, scope safety, and explainability before advanced semantic retrieval.
- Deterministic filters make isolation rules auditable and easier to reason about during development.
- A simple ranking model is sufficient for the first scoped memory milestone and keeps context assembly predictable.

**Alternatives considered**:

- **Introduce vector search or embeddings now**: Rejected because it increases storage, tooling, and evaluation complexity before the governance model is stable.
- **Use timestamp-only retrieval**: Rejected because it would not reflect relevance well enough for even the first useful agent behaviors.

## Decision 3: Keep Scope, Exposure, and Sync Policy as Orthogonal Metadata

**Decision**: Model lifecycle, scope, exposure policy, and sync policy as independent fields rather than collapsing them into one memory type.

**Rationale**:

- The roadmap already assumes that one item may be durable yet private, or app-scoped yet shareable only as a summary.
- Orthogonal metadata allows `003` to support the governance model needed by later sync and policy milestones.
- This keeps promotion, demotion, and manual editing rules precise instead of forcing one dimension to imply another.

**Alternatives considered**:

- **Use one combined memory-type enum**: Rejected because it would explode into too many coupled cases and make later sync/policy changes brittle.
- **Delay sync and exposure metadata to `006`**: Rejected because `v0` schemas need those hooks from the start.

## Decision 4: Integrate Persona and Memory Through the Existing Runtime Context Loader

**Decision**: Replace the no-op context loader from `002` with a persona-and-memory-backed loader that assembles a safe runtime context payload and a separate user-visible summary.

**Rationale**:

- `002` already established the right seam for context loading before planning and execution.
- Reusing that seam keeps the runtime contract stable and prevents the workspace from learning repository details directly.
- A separate safe summary lets the UI explain active context without exposing private raw evidence.

**Alternatives considered**:

- **Inject memory logic directly into the ViewModel**: Rejected because it would couple UI to runtime retrieval rules.
- **Let the planner fetch persona and memory itself**: Rejected because it would blur responsibilities and make later policy auditing harder.

## Decision 5: Add a Minimal Context Inspector Surface for Manual Persona and Memory Actions

**Decision**: Provide a lightweight context inspector sheet in the workspace so the user can review the safe active context, edit stable persona traits, and pin or promote important memory.

**Rationale**:

- User Story 3 requires manual edit, pin, and lifecycle control rather than fully hidden background behavior.
- A small inspector surface satisfies the user value without turning `003` into a full memory-management product.
- The inspector also creates a practical validation path for the safe summary model introduced in this milestone.

**Alternatives considered**:

- **Leave editing and pinning entirely developer-facing**: Rejected because it would not satisfy the user-facing lifecycle story.
- **Build a full dedicated memory-management screen**: Rejected because it would make the milestone too large and distract from runtime integration.
