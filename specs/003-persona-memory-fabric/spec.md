# Feature Specification: Persona and Scoped Memory Fabric

**Feature Branch**: `003-persona-memory-fabric`  
**Created**: 2026-04-08  
**Status**: Draft  
**Input**: User description: "Design persona and scoped memory context fabric"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Use Stable Persona and Relevant Context (Priority: P1)

As a user, the agent should respond and decide in a way that reflects my stable preferences while also using the most relevant current context.

**Why this priority**: The project stops being a real personal agent if it cannot distinguish stable behavior from dynamic context.

**Independent Test**: Can be tested independently by defining persona traits and a small set of memory items, then verifying that later runtime outputs use both without merging them into one undifferentiated profile.

**Acceptance Scenarios**:

1. **Given** the user has saved stable persona preferences, **When** the runtime builds a response context, **Then** those traits are applied as behavior constraints rather than treated as ordinary factual memory.
2. **Given** relevant context exists in memory, **When** the runtime retrieves context for a request, **Then** it includes in-scope relevant memory and excludes unrelated memory.

---

### User Story 2 - Keep Memory Safely Scoped (Priority: P2)

As a user, memory that belongs only to one application or one sensitive context should not leak into unrelated requests.

**Why this priority**: Scope isolation is required for privacy, correctness, and future sync safety.

**Independent Test**: Can be tested independently by writing memory with different scopes and verifying that retrieval returns only entries allowed for the current request scope.

**Acceptance Scenarios**:

1. **Given** a memory item is app-scoped and private, **When** a request from another app asks for context, **Then** that memory item is not returned.
2. **Given** a memory item is global and allowed for the current request, **When** retrieval runs, **Then** the item is eligible for context assembly.

---

### User Story 3 - Promote, Edit, and Expire Memory (Priority: P3)

As a user, I can keep durable preferences and important facts while letting temporary or stale context fade out over time.

**Why this priority**: Memory without lifecycle management either becomes noisy or unsafe.

**Independent Test**: Can be tested independently by creating durable, working, and ephemeral memory items and verifying that promotion, manual editing, and expiration follow the expected lifecycle.

**Acceptance Scenarios**:

1. **Given** a temporary memory becomes important, **When** it is promoted, **Then** it becomes available through the durable memory path without losing provenance.
2. **Given** an ephemeral memory has expired, **When** retrieval runs, **Then** it is no longer returned as active context.

### Edge Cases

- What happens when two memory items conflict and come from different trust levels?
- How does the system handle memory that is globally useful but originated from one application?
- What happens when a private raw evidence source exists but only a shareable summary should be exposed?
- How should expired working memory be treated if the user manually pinned it as important?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST store persona separately from memory and treat persona as stable behavioral constraints.
- **FR-002**: The system MUST support memory lifecycle classes for `durable`, `working`, and `ephemeral` context.
- **FR-003**: The system MUST support memory scopes that distinguish at least global, app-scoped, contact-scoped, and device-scoped context.
- **FR-004**: The system MUST support exposure policies that distinguish private, shareable summary, and shareable full memory.
- **FR-005**: The system MUST support sync policies that distinguish local-only, summary-sync-ready, and full-sync-ready states, even though actual sync is out of scope for `v0`.
- **FR-006**: The system MUST track provenance, confidence, timestamps, and source type for stored memory items.
- **FR-007**: The system MUST default newly inferred application-specific memory to `app-scoped` and `private` unless a policy explicitly promotes it.
- **FR-008**: The system MUST allow context retrieval to filter memory by lifecycle, scope, exposure policy, and relevance.
- **FR-009**: The system MUST allow durable persona traits and important durable memory to be manually edited or pinned by the user.
- **FR-010**: The system MUST support promotion, demotion, and expiration behavior for memory items without erasing provenance.
- **FR-011**: The system MUST support a safe, user-visible context summary representation so later UI layers can explain active context without exposing private raw evidence by default.

### Key Entities *(include if feature involves data)*

- **PersonaProfile**: The stable set of behavioral preferences and guardrails that shape how the agent behaves.
- **MemoryItem**: A stored fact, summary, event, or inferred context unit with lifecycle, scope, and policy metadata.
- **MemoryScope**: The visibility boundary that determines where a memory item may be used.
- **MemoryPolicy**: The combined exposure and sync rules attached to a memory item.
- **RetrievalQuery**: The request-time filter used to choose which memory items can participate in context assembly.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Persona traits and factual memory remain separately editable and separately retrievable in all milestone validation flows.
- **SC-002**: App-scoped private memory is excluded from out-of-scope retrieval in 100% of tested isolation scenarios.
- **SC-003**: Durable, working, and ephemeral memory classes can each be created and retrieved with distinct lifecycle behavior in all test cases.
- **SC-004**: Users can manually edit or pin durable context without losing provenance and confidence metadata.

## Assumptions

- `v0` does not implement true multi-device sync.
- Raw evidence may remain local even when a summary is shareable.
- The first memory milestone focuses on retrieval correctness and governance before advanced summarization strategies.
- System sources and user edits will later outrank inferred model conclusions, even if that ranking is not fully exercised in the first implementation slice.
- A later context window UI may expose selected context to the user, so this milestone must preserve enough metadata to support safe explanation.
