# Data Model: Workflow Graph And Automation

## WorkflowDefinition

Durable managed object describing reusable multi-step work.

**Fields**
- `workflowDefinitionId`: stable workflow identity
- `title`: user-facing name
- `entrySummary`: concise trigger or entry-condition summary
- `availabilityState`: ready, disabled, blocked, degraded, or similar state
- `stepCount`
- `lastRunSummary`

## WorkflowStep

One step in a workflow definition.

**Fields**
- `workflowStepId`
- `workflowDefinitionId`
- `stepType`: action, guard, approval-sensitive, context-aware, or similar role
- `summary`
- `requiredInputs`
- `nextTransitionRule`

## WorkflowTrigger

Entry rule that starts or makes a workflow eligible to run.

**Fields**
- `workflowTriggerId`
- `workflowDefinitionId`
- `triggerType`
- `triggerSummary`
- `isEnabled`

## WorkflowRun

Explainable execution record for one workflow attempt.

**Fields**
- `workflowRunId`
- `workflowDefinitionId`
- `runState`: running, paused, awaiting-approval, completed, failed, cancelled, resumable
- `startedAt`
- `lastCheckpointSummary`
- `nextRequiredAction`

## WorkflowCheckpoint

Resumable execution marker for one workflow run.

**Fields**
- `workflowCheckpointId`
- `workflowRunId`
- `stepId`
- `checkpointState`
- `resumeSummary`
- `blockingReason`

## WorkflowRunOutcome

User-visible outcome summary for one workflow run.

**Fields**
- `workflowRunId`
- `outcomeState`
- `headline`
- `details`
- `recoveryGuidance`
- `recentActivitySummary`

## ManagedAutomationEntry

Control/detail-surface summary for one workflow definition or active run.

**Fields**
- `entryId`
- `entryType`: workflow-definition or workflow-run
- `title`
- `statusLine`
- `actionSummary`
- `detailRoute`
