# Feature Specification: Workflow Graph And Automation

**Feature Branch**: `021-workflow-graph-and-automation`  
**Created**: 2026-04-22  
**Status**: Draft  
**Input**: User description: "Continue the roadmap after hooks and knowledge and add workflow graph contracts, resumable execution, and the first local-first automation and task-flow support so Mobile Claw can run multi-step work with approvals, context, and explainable state without jumping straight to a remote orchestration platform or a heavy graph editor."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Define Reusable Multi-Step Work Without A Heavy Graph Builder (Priority: P1)

As a user, I want to define a reusable multi-step workflow and attach a trigger or entry condition, so repeated work no longer depends on manually reconstructing the same prompt sequence each time.

**Why this priority**: The first value of `021` is turning repeatable task structure into a durable runtime object. The roadmap explicitly warns against starting with a heavy graph editor; the first slice needs a stable workflow contract before a visually complex builder.

**Independent Test**: Create a workflow definition with at least one trigger or entry rule, one action-bearing step, and one decision or gating step, then verify it can be saved, inspected, and recognized as runnable automation state.

**Acceptance Scenarios**:

1. **Given** a user defines a multi-step workflow through the supported first-slice authoring flow, **When** it is saved, **Then** the product preserves a stable workflow definition with identifiable steps, entry conditions, and current availability state.
2. **Given** a workflow definition is incomplete or depends on unavailable capabilities, **When** the user tries to activate it, **Then** the product explains why activation is blocked instead of allowing an opaque broken automation.

---

### User Story 2 - Run, Pause, Approve, And Resume Workflow Executions (Priority: P2)

As a user, I want workflow runs to pause for approvals, recover from interruptions, and resume with clear state, so multi-step execution stays governable and understandable instead of behaving like a black-box background process.

**Why this priority**: Without resumable and explainable execution, automation would only add risk and confusion. This story is what makes workflow support trustworthy enough to ship.

**Independent Test**: Start a workflow that includes at least one approval-sensitive or stateful step, interrupt or pause it mid-run, then verify the system can resume with preserved run state and readable outcomes.

**Acceptance Scenarios**:

1. **Given** a workflow run reaches a guarded or approval-sensitive step, **When** execution pauses, **Then** the user can see what is waiting, why it paused, and what action is required next.
2. **Given** a workflow run is interrupted by failure, timeout, app interruption, or deliberate pause, **When** the run is revisited, **Then** the product preserves enough state to resume or explain why it cannot resume.

---

### User Story 3 - Manage Automations And Run History Through The Existing Control Surfaces (Priority: P3)

As a user, I want to inspect, enable, pause, resume, and review automations and their run history from the existing control surfaces, so automation becomes part of the same runtime product instead of growing a separate admin console.

**Why this priority**: `021` should extend the control plane, not replace it. Automation needs stable summary/detail handling and recent activity integration, not a parallel backend UI.

**Independent Test**: Inspect workflow definitions and workflow runs from the control center and their detail views, then verify current state, recent activity, and supported management actions remain understandable without leaving the existing product model.

**Acceptance Scenarios**:

1. **Given** one or more workflows exist, **When** the user opens the automation area, **Then** the product shows summary state, current enablement, last run, and next relevant action for each supported automation.
2. **Given** a workflow run succeeds, fails, is blocked, or is cancelled, **When** the user inspects recent activity or the run detail, **Then** the product shows a traceable run outcome and related next-step guidance instead of a raw log dump.

---

### Edge Cases

- What happens when a trigger fires while the related workflow is disabled, paused, or blocked by missing dependencies?
- What happens when a workflow step produces output that the next step cannot use?
- What happens when a guarded step needs approval but the request times out, is rejected, or is never resumed?
- What happens when a workflow run is interrupted and the user returns later after runtime state, permissions, or sources have changed?
- What happens when multiple triggers or overlapping runs compete for the same workflow definition?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST define a stable workflow graph contract that represents workflow identity, steps, ordering, entry conditions, current availability, and run state without requiring a freeform graph editor for the first slice.
- **FR-002**: The first workflow slice MUST support reusable multi-step flows that can include action-bearing steps, guard or decision steps, approval-sensitive steps, and context-aware contribution steps.
- **FR-003**: Users MUST be able to save and inspect workflow definitions as durable runtime-managed objects with stable summary and detail views.
- **FR-004**: Workflow runs MUST preserve explainable execution state across running, paused, awaiting approval, completed, failed, cancelled, and resumable conditions when those states apply.
- **FR-005**: Approval-sensitive or side-effecting workflow steps MUST reuse the existing proposal, policy, approval, and result language rather than inventing a separate automation-specific approval model.
- **FR-006**: The product MUST surface current workflow-run status and required next action in the current task flow when a workflow is actively relevant, while keeping global automation management in the control center and object detail views.
- **FR-007**: Users MUST be able to inspect workflow and run details including summary, current state, provenance, recent activity, and supported management actions through stable detail views.
- **FR-008**: Supported reversible automation-management actions MUST include at minimum enable or disable, pause or resume, and comparable non-destructive run-control operations where applicable.
- **FR-009**: Workflow failures, guard blocks, timeouts, cancellations, and resume limitations MUST produce explainable activity items and recovery guidance rather than raw log-only output.
- **FR-010**: This milestone MUST remain local-first and MUST not require remote orchestration, multi-device execution, or marketplace-style flow sharing to prove value.
- **FR-011**: User-facing labels and explanations for workflow definitions, run state, approvals, automation controls, and recovery guidance MUST support English and Simplified Chinese automatically via device locale.
- **FR-012**: This milestone MUST build on the runtime contribution contracts from `019` and the knowledge layer from `020` while preserving the updated product rule that conversation handles task progression and the control center handles global automation management.

### Key Entities *(include if feature involves data)*

- **WorkflowDefinition**: Durable managed object describing reusable multi-step work, entry conditions, current availability, and supported actions.
- **WorkflowStep**: One step in a workflow definition, including its role, required inputs, and transition intent.
- **WorkflowTrigger**: Entry rule or activation condition that causes a workflow definition to start or become eligible to run.
- **WorkflowRun**: Explainable execution record for one attempt to execute a workflow definition.
- **WorkflowCheckpoint**: Resumable state marker that records where a paused, failed, or approval-gated run currently stands.
- **WorkflowRunOutcome**: User-visible summary of whether a run completed, failed, paused, was blocked, or was cancelled and what should happen next.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create at least one reusable workflow definition with a trigger or entry condition and inspect it as a durable managed runtime object.
- **SC-002**: A workflow run that pauses for approval or interruption can later be revisited with preserved state and understandable next-step guidance.
- **SC-003**: Workflow definitions and run history can be managed through the existing control-center and detail-view model without requiring a separate automation console.
- **SC-004**: The first automation slice proves multi-step local execution, approval reuse, and resumable run state without depending on remote orchestration or a heavy graph editor.

## Assumptions

- The first slice should prove stable workflow contracts, run state, and explainable automation management before attempting a full visual graph builder.
- Workflow and automation management belong in the control center’s Automation area and object detail views; conversation and session layers should only surface task-relevant run summaries and approvals.
- Existing tool, policy, approval, activity, and runtime contribution contracts remain the foundation for workflow execution semantics rather than being duplicated.
- Marketplace sharing, distributed execution, and advanced orchestration can remain outside this milestone as long as the local-first workflow model is coherent and resumable.
