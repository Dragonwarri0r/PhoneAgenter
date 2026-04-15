# Feature Specification: Workspace Information Architecture

**Feature Branch**: `013-workspace-information-architecture`  
**Created**: 2026-04-10  
**Status**: Draft  
**Input**: User description: "Reshape the workspace into a conversation-first, capability-visible, progressively disclosed execution surface so key runtime state stays legible without permanently crowding the screen."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Keep Conversation As The Primary Workspace Surface (Priority: P1)

As a user, I want the conversation and composer to remain the visual center of the workspace, so I can keep interacting with the agent without secondary panels constantly taking over the screen.

**Why this priority**: This is the defining product outcome of `013`. If conversation is still visually subordinate, the rest of the information architecture improvements do not matter.

**Independent Test**: Open the workspace in idle, streaming, and awaiting-approval states and verify the transcript remains the dominant surface while header and status controls stay compact.

**Acceptance Scenarios**:

1. **Given** the workspace is in a normal conversation state, **When** the screen renders, **Then** the transcript occupies the primary vertical space and the composer stays anchored without being displaced by large persistent cards.
2. **Given** the keyboard appears, **When** the user starts typing, **Then** secondary workspace chrome compresses or collapses before the conversation and composer become cramped.

---

### User Story 2 - See Key Runtime State At A Glance (Priority: P2)

As a user, I want key runtime status, source, trust, route, and pending action information summarized in a compact stable area, so I can understand what the agent is doing without opening multiple detail sheets.

**Why this priority**: The runtime already has richer status information, but it is spread across large cards and deep surfaces. This story makes capability and execution state legible.

**Independent Test**: Trigger local, external, approval, and structured-action flows and verify the workspace shows a compact, glanceable digest for the active session state without expanding a full detail panel.

**Acceptance Scenarios**:

1. **Given** the runtime has source, trust, route, or structured-action metadata, **When** the workspace shows the active session, **Then** the user can see a compact digest of those signals in a stable location.
2. **Given** the runtime enters a sensitive or exceptional state such as awaiting approval or recoverable failure, **When** the workspace renders, **Then** the most relevant execution signal becomes visually prominent without replacing the transcript.

---

### User Story 3 - Use Progressive Disclosure For Secondary Capability Surfaces (Priority: P3)

As a user, I want context inspection, model management, governance, portability, and similar secondary capabilities to stay discoverable but not always expanded, so the workspace remains calm even as the product grows.

**Why this priority**: `007-012` added several product surfaces. Without progressive disclosure, every new capability permanently increases workspace complexity.

**Independent Test**: From the main workspace, discover and open deeper capability surfaces without relying on permanently expanded model/context panels, then verify the base screen remains compact when those surfaces are dismissed.

**Acceptance Scenarios**:

1. **Given** secondary surfaces such as model management, context inspection, and governance are available, **When** the workspace loads, **Then** their entry points are discoverable from a compact surface rather than requiring large always-open cards.
2. **Given** the user dismisses a secondary surface, **When** the workspace returns to its base state, **Then** the information architecture returns to a compact conversation-first layout without losing access to those features.

---

### Edge Cases

- What happens when the workspace is in `PREPARING` or `UNAVAILABLE` state and there is no active conversation yet?
- What happens when runtime status has little metadata and the compact digest would otherwise appear empty?
- What happens when a pending approval and a recoverable failure are both relevant in the same session?
- What happens when system-source permission warnings exist but should not permanently dominate the workspace?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The workspace MUST present the conversation transcript and composer as the primary interactive surfaces in ready, streaming, and awaiting-approval states.
- **FR-002**: The workspace MUST provide a compact, stable runtime digest surface that summarizes stage, headline, and the most relevant execution metadata for the active session.
- **FR-003**: The runtime digest MUST support visibility for source, trust, route, structured action, system source, and audit-adjacent status without requiring a large always-expanded card.
- **FR-004**: Secondary capability surfaces including model management, context inspection, governance, and portability MUST remain discoverable from the main workspace while defaulting to a compact presentation.
- **FR-005**: The workspace MUST use progressive disclosure so detailed capability and diagnostic information is reachable on demand rather than permanently occupying the top of the screen.
- **FR-006**: The information architecture MUST preserve existing runtime actions and flows introduced in earlier milestones, including quick prompts, approvals, failures, model import, governance, context inspection, portability preview, and permission requests.
- **FR-007**: The workspace MUST remain usable when the IME is visible by preferring compaction of secondary UI before reducing transcript/composer usability.
- **FR-008**: Workspace information architecture labels and user-facing descriptions MUST support English and Simplified Chinese and follow the device locale automatically.
- **FR-009**: This milestone MUST remain local-first and MUST NOT add cloud sync or remote layout dependencies.
- **FR-010**: This milestone MUST NOT introduce new runtime capability semantics; it only reorganizes visibility, prioritization, and access patterns for existing workspace information.

### Key Entities *(include if feature involves data)*

- **WorkspaceStatusDigest**: Compact user-facing summary of the current runtime stage, headline, and the most important execution metadata.
- **WorkspaceSecondaryEntry**: Discoverable entry point for a deeper surface such as model management, context inspection, governance, or portability.
- **WorkspaceAttentionState**: UI priority state describing whether the workspace should emphasize normal conversation, approval, failure, or empty/preparing conditions.
- **WorkspaceCapabilityVisibilitySnapshot**: Presentation-layer grouping of route, source, trust, structured action, system source, and recent audit signals.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In normal ready and streaming states, the transcript occupies the majority of vertical workspace area and remains the clear visual center.
- **SC-002**: Users can identify active runtime stage plus at least source/trust/route or structured-action state from a compact digest without opening a detail sheet.
- **SC-003**: Existing secondary surfaces remain discoverable while the default workspace layout no longer depends on permanently expanded model/context cards.
- **SC-004**: Workspace IA labels and descriptions render correctly in both English and Simplified Chinese.

## Assumptions

- This milestone targets the existing single-screen workspace rather than a new multi-screen navigation structure.
- Existing sheets and dialogs can be preserved if their entry points and default prominence are reorganized.
- The most effective first pass is to compress the always-visible top stack and move richer detail into on-demand surfaces instead of rewriting every component from scratch.
- The workspace continues to build on the `Digital Atrium` visual direction established in `001`.
