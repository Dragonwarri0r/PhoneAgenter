# Feature Specification: Knowledge Ingestion And Retrieval

**Feature Branch**: `020-knowledge-ingestion-and-retrieval`  
**Created**: 2026-04-22  
**Status**: Draft  
**Input**: User description: "Continue the roadmap after runtime hooks and context sources and make local knowledge a first-class runtime layer through ingestion, indexing metadata, retrieval, source visibility, and redaction-aware summaries while keeping durable knowledge separate from memory and keeping task-time knowledge usage explainable."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ingest Local Knowledge Into A Managed Corpus (Priority: P1)

As a user, I want to ingest local files, folders, and document collections into a managed knowledge layer, so Mobile Claw can use durable knowledge beyond the current conversation without treating it as the same thing as persona or memory.

**Why this priority**: This is the first visible value of `020`. Until ingestion exists, knowledge remains a roadmap concept instead of a usable runtime layer.

**Independent Test**: Add at least one local knowledge source into the managed corpus, then verify the product can show that source as an ingested asset with recognizable availability, freshness, and source identity.

**Acceptance Scenarios**:

1. **Given** the user selects a supported local knowledge source, **When** ingestion starts, **Then** the product creates a managed knowledge asset with visible source identity and ingestion state.
2. **Given** an ingested knowledge source later changes, disappears, or becomes unavailable, **When** the user revisits it, **Then** the product shows the current knowledge-asset state instead of pretending the source is still healthy.

---

### User Story 2 - Use Retrieved Knowledge In Active Requests With Visible Provenance (Priority: P2)

As a user, I want retrieved knowledge to show up as explainable support for my current request, so I can tell what durable knowledge was used without confusing it with live context, memory, or hidden backend state.

**Why this priority**: `020` is not only about ingestion; it must also make knowledge use trustworthy and legible at request time.

**Independent Test**: Ask a request that can use ingested knowledge, then verify the current task flow exposes concise retrieval support while deeper provenance and source details remain reachable through knowledge detail views.

**Acceptance Scenarios**:

1. **Given** a request uses retrieved knowledge from the managed corpus, **When** the response or task details are shown, **Then** the product surfaces concise source-linked knowledge support rather than only an unexplained answer.
2. **Given** multiple retrieved knowledge items contribute to a request, **When** the user inspects them, **Then** the product preserves source visibility and relevance summaries instead of collapsing them into one opaque blob.

---

### User Story 3 - Manage Knowledge Freshness And Availability Without Mixing It Into Memory Or Workflow Controls (Priority: P3)

As a user, I want to inspect and manage knowledge assets from a dedicated knowledge area, so corpus health, refresh state, and retrieval availability remain understandable without turning memory management or workflow management into a catch-all bucket.

**Why this priority**: The roadmap now explicitly separates Knowledge from Now, Capabilities, Policy, and Automation. `020` needs to reinforce that separation in product behavior, not just architecture language.

**Independent Test**: Open managed knowledge assets, inspect their source, freshness, and retrieval state, adjust supported reversible availability controls, and verify those changes remain separate from memory and workflow surfaces.

**Acceptance Scenarios**:

1. **Given** a managed knowledge asset supports reversible availability controls, **When** the user opens its detail, **Then** the user can inspect its state and apply supported non-destructive management actions from the knowledge area.
2. **Given** a knowledge asset is stale, partially available, redacted, or temporarily excluded from retrieval, **When** the user inspects it, **Then** the product explains the limitation clearly instead of showing a silent failure.

---

### Edge Cases

- What happens when the same local source is ingested more than once or overlaps heavily with an existing knowledge asset?
- What happens when an ingested source is moved, deleted, or no longer readable after it was previously healthy?
- What happens when retrieval finds weak, stale, or only partially usable knowledge support?
- What happens when a knowledge asset is too large, too heterogeneous, or only partially ingested in the first pass?
- What happens when a request uses both memory and knowledge and the user needs to tell them apart?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST provide a managed local knowledge layer that is distinct from persona, conversational memory, and ephemeral request-time context.
- **FR-002**: Users MUST be able to ingest supported local knowledge sources, including file, folder, or document-based collections, into the managed knowledge layer.
- **FR-003**: Each managed knowledge asset MUST preserve source identity, ingestion state, freshness or last-known update state, and availability status in a user-inspectable form.
- **FR-004**: The knowledge layer MUST expose retrieval results through a stable retrieval contract that preserves source visibility, relevance summary, and redaction-aware request-time explanation.
- **FR-005**: The current task flow MUST show concise knowledge support when retrieval contributes to a request, while deeper corpus management and source inspection remain in the knowledge area and object detail views.
- **FR-006**: The product MUST keep durable knowledge management separate from memory management so users can distinguish long-lived ingested knowledge from memory-derived context.
- **FR-007**: Users MUST be able to inspect a managed knowledge asset’s summary, current state, provenance, recent usage, and supported management actions through a stable detail view.
- **FR-008**: Supported reversible knowledge-management actions MUST include at minimum refresh, re-include or exclude from retrieval, or comparable non-destructive availability control when appropriate for the asset.
- **FR-009**: The system MUST explain stale, missing, partially available, or redacted knowledge states rather than surfacing them as silent retrieval failures.
- **FR-010**: This milestone MUST remain local-first and MUST not require cloud indexing, remote corpus hosting, or remote retrieval services to prove value.
- **FR-011**: User-facing labels and explanations for knowledge ingestion, retrieval, freshness, and limitations MUST support English and Simplified Chinese automatically via device locale.
- **FR-012**: This milestone MUST build on the runtime contribution contracts from `019` while remaining separate from workflow authoring and automation execution concerns planned for `021`.

### Key Entities *(include if feature involves data)*

- **KnowledgeAsset**: Managed durable knowledge source with stable identity, provenance, availability state, and user-visible summary.
- **KnowledgeIngestionRecord**: User-visible record describing ingestion state, freshness, and the current known status of a knowledge asset.
- **RetrievalCitation**: Explainable reference connecting a request-time knowledge hit back to its source asset and relevance summary.
- **RetrievalSupportSummary**: Concise user-facing explanation of why specific knowledge contributed to a request and how strongly it applied.
- **KnowledgeAvailabilityState**: Healthy, stale, partial, excluded, missing, or similar user-visible state for a knowledge asset.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can ingest at least one local knowledge source and later revisit it as a managed knowledge asset with visible provenance and ingestion state.
- **SC-002**: Requests that use the managed knowledge layer surface source-linked retrieval support instead of opaque background usage.
- **SC-003**: Users can distinguish durable knowledge from memory and ephemeral context in both the active task flow and the control-center knowledge area.
- **SC-004**: Supported knowledge assets expose freshness and reversible availability state clearly enough that users can understand why an asset is or is not contributing to retrieval.

## Assumptions

- The first slice should prioritize a reliable managed corpus and understandable retrieval visibility over broad connector breadth or advanced ranking research.
- Knowledge corpus management belongs in the control center’s Knowledge area and object detail views; the conversation and session layers should only surface concise request-relevant summaries.
- Destructive purge, advanced diagnostics, and marketplace-style connector expansion can remain outside the first knowledge milestone as long as reversible availability management exists.
- This milestone depends on `019` contribution contracts being stable enough to separate request-time contribution from durable corpus management cleanly.
