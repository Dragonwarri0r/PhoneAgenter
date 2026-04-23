# Research: Knowledge Ingestion And Retrieval

## Decision 1: Keep durable knowledge separate from memory from the start

- **Decision**: Represent managed knowledge assets and their ingestion state separately from persona and conversational memory.
- **Why**: The updated roadmap explicitly creates a Knowledge area distinct from memory and task-time context. `020` needs to preserve that boundary in both storage and UI language.
- **Alternatives considered**:
  - Extend `MemoryItem` directly into a full knowledge corpus: rejected because it would blur durable corpus management with memory behavior.
  - Treat all knowledge as ephemeral request-time context: rejected because durable ingestion and revisitability are core value in this milestone.

## Decision 2: Start with a managed local corpus, not a broad connector ecosystem

- **Decision**: The first slice should focus on ingesting supported local file, folder, or document-based sources into a managed corpus.
- **Why**: This is enough to prove local knowledge value without depending on cloud infrastructure or a large connector marketplace.
- **Alternatives considered**:
  - Add many remote or connector-driven sources immediately: rejected because it would inflate scope and undermine local-first discipline.
  - Delay ingestion and only add retrieval contracts: rejected because `020` needs to make knowledge a usable product layer, not just a contract.

## Decision 3: Show concise retrieval support in the task flow and deep provenance in the Knowledge area

- **Decision**: Current-task surfaces should show source-linked retrieval support summaries, while corpus management and deeper source inspection live in the Knowledge area and object detail views.
- **Why**: This keeps conversation/session layers focused on task progression while still making knowledge use explainable.
- **Alternatives considered**:
  - Show full corpus detail inline with active requests: rejected because it would overwhelm the task flow.
  - Hide retrieval provenance until deep management views are opened: rejected because it would make knowledge use feel opaque.

## Decision 4: Prioritize reversible availability and freshness controls over destructive operations

- **Decision**: The first knowledge slice should support refresh and retrieval inclusion/exclusion style controls before destructive purge or advanced diagnostics.
- **Why**: The product needs a manageable corpus first. Destructive and advanced controls can come later once the managed corpus model is stable.
- **Alternatives considered**:
  - Add full destructive corpus controls immediately: rejected because they increase risk before users even have stable knowledge visibility.
  - Make knowledge assets inspect-only: rejected because then the Knowledge area would not feel truly manageable.

## Decision 5: Build retrieval on top of `019` contribution semantics while preserving corpus boundaries

- **Decision**: Retrieval results should flow through request-time contribution/explainability contracts from `019`, while corpus assets and ingestion state remain durable managed objects in their own layer.
- **Why**: This gives `020` a clean split between durable knowledge management and request-time usage.
- **Alternatives considered**:
  - Create a knowledge-specific explainability model separate from `019`: rejected because it would fragment request-time visibility again.
  - Push all knowledge behavior down into `019`: rejected because corpus management is the new product problem of `020`.
