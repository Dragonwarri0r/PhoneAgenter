# Contract: Workflow Graph And Automation

## Purpose

Define one durable local-first workflow and automation layer that supports reusable multi-step work, resumable execution, and control-plane management without introducing a second execution system or a separate orchestration console.

## Workflow Definition Contract

Every workflow definition must provide:

- stable workflow identity
- user-facing title
- step list
- entry or trigger summary
- current availability state
- supported management actions

The first slice does not require a full freeform graph editor.

## Step Contract

Workflow steps may represent:

- action-bearing execution
- guard or decision logic
- approval-sensitive operations
- context-aware contribution

Covered steps must reuse the runtime’s existing tool, contribution, proposal, and approval language rather than inventing automation-only semantics.

## Run State Contract

Every workflow run must preserve explainable run state, including when relevant:

- running
- paused
- awaiting approval
- completed
- failed
- cancelled
- resumable

## Checkpoint Contract

Paused, blocked, or interrupted workflow runs must preserve checkpoint state sufficient to:

- show where the run stopped
- explain why it stopped
- indicate whether it can resume
- describe what action is required next

## Management Contract

Automation management must surface through the existing control/detail model and support reversible actions such as:

- enable
- disable
- pause
- resume
- inspect

## Scope Boundary Contract

`021` must not require:

- remote orchestration
- multi-device execution
- marketplace-style workflow sharing
- a heavy visual graph editor
