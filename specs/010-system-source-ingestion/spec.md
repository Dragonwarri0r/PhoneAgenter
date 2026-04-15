# Feature Specification: System Source Ingestion

**Feature Branch**: `010-system-source-ingestion`  
**Created**: 2026-04-09  
**Status**: Draft  
**Input**: User description: "Connect the first real Android system context sources so Mobile Claw can use contacts and calendar data as runtime context when relevant."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Use Contacts As Runtime Context (Priority: P1)

As a user, I want Mobile Claw to recognize relevant contacts from my device when a request refers to a person, so the runtime can ground message and relationship-oriented requests in real device context.

**Why this priority**: Contacts are the most immediately valuable personal system source for message and handoff flows already present in the app.

**Independent Test**: Grant contacts permission, submit a request referencing a known contact, and verify the runtime ingests a contact-backed system source record into context.

**Acceptance Scenarios**:

1. **Given** the app has contacts permission and a matching device contact exists, **When** the request references that contact, **Then** the runtime ingests a contact-backed system source memory item.
2. **Given** contacts permission is missing, **When** the runtime attempts contact ingestion, **Then** the app does not fail silently and instead exposes permission state clearly.

---

### User Story 2 - Use Calendar As Runtime Context (Priority: P2)

As a user, I want Mobile Claw to ingest relevant upcoming calendar context when my request is scheduling-related, so the runtime can ground planning and scheduling tasks in real device data.

**Why this priority**: Calendar is the next most valuable system source and directly complements the existing calendar-write flow.

**Independent Test**: Grant calendar permission, submit a schedule-related request, and verify relevant upcoming calendar context is ingested and appears in runtime context.

**Acceptance Scenarios**:

1. **Given** the app has calendar permission and relevant upcoming events exist, **When** the request is schedule-related, **Then** the runtime ingests bounded upcoming calendar records into context.
2. **Given** no relevant calendar events are found, **When** ingestion runs, **Then** the runtime remains stable and surfaces a clear “no relevant system context” outcome rather than synthetic data.

---

### User Story 3 - Expose Permission And Source Status Clearly (Priority: P3)

As a user, I want to understand which system sources are connected, permissioned, and contributing to context, so the runtime’s system grounding feels understandable rather than invisible.

**Why this priority**: Real system-source ingestion changes trust expectations; users need visibility into whether contacts/calendar are connected and used.

**Independent Test**: Open the workspace after granting or denying source permissions and verify source availability and contribution status are visible.

**Acceptance Scenarios**:

1. **Given** contacts or calendar permission is denied, **When** the workspace shows system source status, **Then** the user can see the source is unavailable because permission is missing.
2. **Given** system-source ingestion contributed records to the latest request, **When** the runtime completes context loading, **Then** the workspace shows that contacts and/or calendar were used.

---

### Edge Cases

- What happens when permissions are granted but the device has no matching contacts or no relevant upcoming events?
- What happens when a request is unrelated to contacts or calendar and no system-source ingestion should run?
- What happens when one source is available and the other is denied?
- What happens when ingested source records become stale and should not accumulate indefinitely?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST support first-party ingestion of at least `Contacts` and `Calendar` as Android system context sources.
- **FR-002**: The system MUST only ingest system-source data when the corresponding Android permission is granted.
- **FR-003**: The system MUST keep system-source ingestion local-first and store ingested records as local runtime-readable data.
- **FR-004**: The system MUST mark ingested records as `SYSTEM_SOURCE`.
- **FR-005**: The system MUST bound ingestion to relevant and recent records rather than importing unbounded raw device data.
- **FR-006**: The runtime context loader MUST be able to include ingested system-source records in runtime context assembly.
- **FR-007**: The workspace MUST expose source availability or permission state for supported system sources.
- **FR-008**: The workspace MUST expose whether contacts and/or calendar contributed to the latest runtime context.
- **FR-009**: User-facing source status and permission messaging MUST support English and Simplified Chinese automatically via device locale.
- **FR-010**: The milestone MUST not require cloud sync or a generalized connector marketplace.

### Key Entities *(include if feature involves data)*

- **SystemSourceDescriptor**: Local description of a supported source, its permission requirement, and current availability.
- **SystemSourceIngestionResult**: Result of a source-ingestion pass including source id, records written, and user-visible status.
- **SystemSourceContribution**: Runtime-facing summary of which sources contributed to the current request context.
- **SystemSourceMemoryRecord**: Memory item produced from contacts or calendar ingestion and marked as `SYSTEM_SOURCE`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The runtime can ingest real contacts and calendar records on-device when permissions are granted.
- **SC-002**: At least one runtime request can show contacts or calendar as contributing system sources in workspace-visible status.
- **SC-003**: Permission-missing and no-results states are visible and understandable in both English and Simplified Chinese.
- **SC-004**: System-source ingestion remains bounded and does not require importing the full contacts or calendar database.

## Assumptions

- The first milestone only needs `Contacts` and `Calendar`; other Android sources remain for later specs.
- On-device system-source ingestion may remain heuristic-triggered in this milestone as long as the source contribution is explicit.
- Ingested records may be materialized as `MemoryItem` entries rather than requiring a wholly separate storage stack.
- Permission prompts can stay inside the current workspace experience rather than requiring a separate settings flow.
