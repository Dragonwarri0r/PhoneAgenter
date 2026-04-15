# Feature Specification: Tool Contract Standardization

**Feature Branch**: `015-tool-contract-standardization`  
**Created**: 2026-04-13  
**Status**: Draft  
**Input**: User description: "Continue with the next roadmap spec and standardize tool descriptors, schema contracts, side-effect policy, and common productivity actions so the runtime can scale without ad hoc branches."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Surface Relevant Tools Only When They Are Actually Usable (Priority: P1)

As a user, I want Mobile Claw to surface only the tool actions that are relevant, allowed, and currently executable, so the workspace does not feel like a permanent control panel full of unavailable actions.

**Why this priority**: This is the first user-visible outcome of `015`. Without on-demand tool visibility, every future capability makes the workspace noisier and less trustworthy.

**Independent Test**: Trigger requests that imply different actions such as reply generation, calendar updates, alarm setup, and outbound sharing, and verify the runtime resolves a consistent tool descriptor only when model capability, provider availability, governance state, and policy allow it.

**Acceptance Scenarios**:

1. **Given** the request implies a low-risk reply, **When** the runtime resolves candidate actions, **Then** it exposes the reply generation tool rather than every registered device action.
2. **Given** the request implies an alarm action but no valid binding is available, **When** tool discovery runs, **Then** the tool is hidden or clearly degraded instead of pretending it can execute.

---

### User Story 2 - Preview Common Productivity Actions Through Standardized Contracts (Priority: P2)

As a user, I want common actions such as calendar write, alarm set/show/dismiss, message send, and share outbound to use the same structured contract style, so previews and confirmations feel consistent instead of feature-specific.

**Why this priority**: `008` introduced structured payloads, but common actions still depend too much on one-off capability rules. This milestone makes those actions predictable and scalable.

**Independent Test**: Trigger at least three standardized tool families and verify the runtime produces a stable tool descriptor, schema-shaped fields, side-effect preview, and confirmation metadata before execution.

**Acceptance Scenarios**:

1. **Given** a request to create a calendar event, **When** the system prepares execution, **Then** the user sees a tool-backed preview with stable field names, risk level, and required scope instead of an opaque action label.
2. **Given** a request to send a message or share content, **When** the system prepares execution, **Then** write/dispatch side effects are shown explicitly and the approval flow uses the tool contract rather than a tool-specific special case.

---

### User Story 3 - Keep Governance, Audit, And Routing Aligned To The Same Tool Identity (Priority: P3)

As a user, I want approval history, denial explanations, and routing outcomes to reference the same stable tool identity and side-effect semantics, so I can understand what Mobile Claw was trying to do and manage it consistently.

**Why this priority**: A unified tool contract only matters if routing, governance, and audit all speak the same language. Otherwise the system stays fragmented internally and confusing externally.

**Independent Test**: Execute or deny multiple standardized tools and verify governance, approval, route explanation, and audit records all reference the same stable tool id, display name, scope, and side-effect classification.

**Acceptance Scenarios**:

1. **Given** a high-risk standardized tool is denied, **When** the user inspects the outcome, **Then** the denial explanation references the same tool identity and scope that were shown in preview.
2. **Given** a standardized tool is allowed and routed to a binding, **When** the audit trail is written, **Then** the audit record references the same tool id, display name, and execution family used in planning and preview.

---

### Edge Cases

- What happens when a request maps to a known tool family but required structured fields are incomplete?
- What happens when a tool is semantically relevant but all bindings are degraded or unavailable?
- What happens when governance allows a caller generally but denies a specific tool scope?
- What happens when two bindings exist for the same tool but only one satisfies current side-effect and confirmation requirements?
- What happens when a legacy capability id does not yet have a standardized tool descriptor?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST define a stable standardized tool descriptor for callable runtime actions instead of relying only on ad hoc capability ids.
- **FR-002**: Each standardized tool descriptor MUST include stable identity, display name, description, input contract, risk classification, scope requirements, side-effect classification, confirmation metadata, and binding metadata.
- **FR-003**: The runtime MUST resolve tool visibility on demand based on current request intent, model capability, governance state, provider availability, and policy constraints.
- **FR-004**: The first standardized tool families MUST include `generate.reply`, `calendar.read`, `calendar.write`, `alarm.set`, `alarm.show`, `alarm.dismiss`, `message.send`, and `share.outbound`.
- **FR-005**: The runtime MUST produce structured preview metadata from the standardized tool contract before write or dispatch execution proceeds.
- **FR-006**: Approval, routing, and audit flows MUST reference the same stable tool identity and side-effect classification used during planning.
- **FR-007**: Standardized tool contracts MUST distinguish read, write, and dispatch semantics rather than treating all non-reply capabilities the same.
- **FR-008**: The planner and router MUST prefer standardized tool metadata over scattered hardcoded mappings when choosing execution paths for covered tool families.
- **FR-009**: User-facing labels and explanations for standardized tools MUST support English and Simplified Chinese automatically via device locale.
- **FR-010**: This milestone MUST stay local-first and MUST reuse the existing runtime, governance, and capability bridge infrastructure instead of introducing a separate execution stack.

### Key Entities *(include if feature involves data)*

- **ToolDescriptor**: Canonical runtime-facing description of a callable tool, including identity, schema contract, risk, visibility policy, and Android binding references.
- **ToolSchemaDescriptor**: Structured definition of expected input and optional output fields for a tool family.
- **ToolVisibilitySnapshot**: Request-scoped representation of whether a tool is relevant, allowed, available, degraded, or hidden and why.
- **ToolExecutionPreview**: User-visible preview model derived from the tool descriptor and structured arguments before execution.
- **ToolBindingDescriptor**: Execution binding metadata that maps a standardized tool onto App Functions, intents, providers, share dispatch, or other Android bindings.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: At least five common productivity tool families are represented by standardized descriptors with stable ids, scopes, risk metadata, and schema-backed preview fields.
- **SC-002**: Covered tool families no longer require one-off display/risk/confirmation mappings in multiple runtime layers to reach preview and execution.
- **SC-003**: Governance, approval, and audit surfaces show consistent tool identity and side-effect language for covered tools.
- **SC-004**: Tool visibility stays on-demand so irrelevant or unavailable device actions are not permanently surfaced in the workspace.

## Assumptions

- This milestone standardizes tool contracts and the first tool catalog; it does not attempt to cover every future capability family.
- Existing structured action extraction from `008` remains the upstream source of candidate arguments for covered actions.
- Android binding execution may still rely on the existing bridge/provider path; `015` focuses on contract standardization rather than replacing all execution code in one step.
