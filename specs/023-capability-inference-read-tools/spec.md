# Feature Specification: Capability Inference and Read Tools

**Feature Branch**: `023-capability-inference-read-tools`  
**Created**: 2026-04-23  
**Status**: Draft  
**Input**: User description: "Introduce safe workspace capability inference and a unified extensible read-tool surface. Replace the current agent_workspace freeform default-to-generate.reply behavior with conservative capability selection that prefers explicit hints, preserves low-risk reply fallback, and only escalates into tool execution when confidence, scope, and policy allow it. Deliver calendar.read as the first fully executable, previewable, explainable read capability, separate from calendar.write and from passive system-source ingestion. Ensure the design fits the existing standardized tool contract, approval/audit flow, and unified extension surface so future Android and external app capabilities can register through the same abstraction instead of adding core special cases."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ask The Workspace To Read My Calendar (Priority: P1)

As a user, I want to ask the main workspace about my calendar in natural language and receive a bounded, explainable result, so I can use the conversation surface for real schedule lookup instead of only generic replies.

**Why this priority**: This is the first end-to-end proof that freeform workspace input can safely reach a real read capability and return useful device-grounded results.

**Independent Test**: Grant calendar access, ask a calendar lookup question from the workspace, and verify the runtime selects the calendar lookup path, shows an understandable execution explanation, and returns a bounded result or a clear no-results outcome.

**Acceptance Scenarios**:

1. **Given** calendar access is available and matching upcoming events exist, **When** the user asks the workspace a clear lookup request such as what is on the calendar today, **Then** the runtime selects calendar lookup instead of plain reply generation and returns matching schedule information through the conversation.
2. **Given** calendar access is available but no matching events exist, **When** the user asks a calendar lookup question, **Then** the runtime completes successfully with a truthful no-results outcome rather than fabricated information.
3. **Given** calendar access is unavailable, **When** the user asks a calendar lookup question, **Then** the workspace explains that the lookup path is unavailable and shows how the user can recover or retry later.

---

### User Story 2 - Keep Freeform Workspace Input Safe And Predictable (Priority: P2)

As a user, I want the workspace to infer useful capabilities from what I type without unexpectedly jumping into the wrong action path, so freeform conversation remains trustworthy.

**Why this priority**: Capability inference only improves the product if it stays conservative, understandable, and safe for ordinary conversational use.

**Independent Test**: Submit a mix of conversational prompts, ambiguous prompts, clear low-risk read requests, and higher-risk action requests from the workspace and verify that the runtime either selects the correct path or safely falls back without accidental execution.

**Acceptance Scenarios**:

1. **Given** the user types a normal conversational prompt with no clear device-action intent, **When** the runtime evaluates the request, **Then** it stays on the reply path instead of forcing an unrelated tool.
2. **Given** the user types an ambiguous prompt that could mean conversation or action, **When** the runtime cannot confidently choose a safe capability, **Then** it falls back to reply behavior or asks for clearer intent rather than executing the wrong path.
3. **Given** the user types a clear low-risk read request, **When** the runtime can confidently match an allowed read capability, **Then** it selects that capability and surfaces why it was chosen.

---

### User Story 3 - Add New Read Capabilities Through One Extension Surface (Priority: P3)

As a platform builder, I want explicit read capabilities to enter through the same capability and extension surface used by other runtime actions, so future Android and external-app integrations do not require new core-specific branches.

**Why this priority**: The first read capability should establish the reusable abstraction for future capabilities rather than becoming another one-off exception.

**Independent Test**: Define a second read-oriented capability or provider registration through the same contract family and verify that discovery, availability, selection, and explanation can represent it without inventing a new registration pattern.

**Acceptance Scenarios**:

1. **Given** a new read capability is registered through the shared extension surface, **When** the runtime enumerates relevant capability options, **Then** the capability appears through the same discovery and availability model as existing tools.
2. **Given** multiple read providers or capability families are present, **When** the runtime explains the chosen route, **Then** the explanation uses the same stable tool identity, scope language, and availability reasoning as other covered capabilities.

### Edge Cases

- What happens when a prompt partially resembles a calendar lookup but is actually a drafting or planning question?
- What happens when the user has granted access but the underlying read provider becomes temporarily unavailable at execution time?
- What happens when multiple providers could satisfy the same read capability but only one is currently eligible?
- What happens when passive system context is available but the explicit read tool path is unavailable?
- What happens when the lookup would return too many results to present clearly in a conversational answer?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The workspace MUST evaluate freeform requests against relevant capability candidates instead of always treating direct workspace input as plain reply generation.
- **FR-002**: Explicit capability hints, externally supplied capability requests, and other already-structured inputs MUST take precedence over inferred capability selection.
- **FR-003**: The runtime MUST keep reply generation as the default fallback when freeform input is ambiguous, low-confidence, or not eligible for safe capability execution.
- **FR-004**: The capability-selection flow MUST distinguish read, write, and dispatch actions so that low-risk reads do not inherit the same behavior as higher-risk side effects.
- **FR-005**: The system MUST support explicit read-tool execution as a separate path from passive system-context ingestion.
- **FR-006**: The system MUST provide `calendar.read` as an explicit read capability that can execute bounded calendar lookup when access and provider availability permit.
- **FR-007**: `calendar.read` MUST remain distinct from `calendar.write` in identity, preview, execution semantics, and user-facing explanation.
- **FR-008**: Before or alongside execution, the workspace MUST surface a user-understandable explanation of the selected capability path, including why it was chosen and what scope it will use.
- **FR-009**: When an explicit read capability returns no matching data, the runtime MUST return a truthful no-results outcome rather than fabricated or stale information.
- **FR-010**: When an explicit read capability is unavailable because of permissions, provider state, or policy limits, the runtime MUST surface an honest unavailable outcome and recovery guidance.
- **FR-011**: Capability selection, preview, routing, approval behavior, audit records, and runtime status MUST reference the same stable tool identity for covered capabilities.
- **FR-012**: The system MUST allow explicit read capabilities and their providers to register through the common capability and extension contract family without requiring new core-specific registration patterns.
- **FR-013**: The first read-capability slice MUST preserve future expansion for additional first-party and external-app read capabilities through the same abstraction.

### Key Entities *(include if feature involves data)*

- **Capability Selection Outcome**: The request-scoped decision describing which path the workspace chose, why it chose it, and whether it stayed in reply mode or selected an explicit capability.
- **Read Tool Request**: The normalized representation of an explicit read action, including the requested query scope, any bounded filters, and the user-visible explanation of what will be read.
- **Read Tool Result**: The normalized result of executing a read capability, including matched records, no-results outcomes, unavailable outcomes, and route explanation.
- **Capability Extension Registration**: The shared registration entry that describes what a read capability or provider contributes, what it requires, and how it participates in discovery and availability.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In milestone validation, a user can complete a calendar lookup from the main workspace in a single request without opening a secondary control surface whenever access is available.
- **SC-002**: In a curated validation set covering clear read requests, normal conversation, and ambiguous prompts, the runtime chooses the intended capability path or benign reply fallback in at least 90% of cases.
- **SC-003**: In tested ambiguous prompt scenarios, the runtime performs no accidental write or dispatch execution.
- **SC-004**: In tested unavailable and no-results scenarios for calendar lookup, the workspace returns a truthful explanation in 100% of cases.
- **SC-005**: At least one additional non-calendar read capability or provider registration can be described through the same extension and capability contract family without adding a new core registration type.

## Assumptions

- `v0` still targets a single-user, local-first Android workspace.
- Calendar lookup is the first fully executable explicit read capability in this slice, but the abstraction must not be calendar-specific.
- Existing write and dispatch capabilities continue using current policy and confirmation rules unless this feature explicitly changes their selection behavior.
- Passive system-source ingestion for calendar and contacts remains available as contextual enrichment, but it is not considered the same thing as explicit read-tool execution.
- Bounded conversational results are sufficient for the first read-capability experience; exhaustive record browsing and editing workflows remain out of scope for this slice.
