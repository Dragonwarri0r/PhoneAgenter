# Data Model: Workspace Information Architecture

## Overview

`013` is primarily a presentation-layer milestone, so its “data model” is a set of UI state groupings rather than new durable storage entities.

## Entities

### WorkspaceAttentionState

- **Purpose**: Describes the current priority mode of the workspace so the UI can elevate the right information without replacing the transcript.
- **Fields**:
  - `mode`: `NORMAL`, `AWAITING_APPROVAL`, `RECOVERABLE_FAILURE`, `PREPARING`, `UNAVAILABLE`
  - `headline`: primary user-facing headline for the current state
  - `supportingText`: optional supporting line
  - `showsInlineFailure`: Boolean
  - `showsApprovalEmphasis`: Boolean
- **Relationships**:
  - Derived from `WorkspaceScreenState`, `pendingApproval`, and `runtimeStatus`

### WorkspaceStatusDigest

- **Purpose**: Compact summary visible in the base workspace without opening a deep sheet.
- **Fields**:
  - `stageLabel`
  - `headline`
  - `supportingText`
  - `primarySignals`: list of compact user-facing lines or chips
  - `secondarySignals`: optional bounded list for less critical metadata
  - `primaryActionLabel`
  - `showsPermissionAction`
  - `isEmphasized`
- **Relationships**:
  - Derived from `RuntimeStatusUiModel`, `recentAudit`, and current attention state

### WorkspaceSecondaryEntry

- **Purpose**: Stable compact entry point to a deeper surface.
- **Fields**:
  - `entryId`: model / context / governance / portability / reset
  - `label`
  - `supportingValue`
  - `isHighlighted`
  - `actionKind`
- **Relationships**:
  - Driven by existing workspace data and UI actions

### WorkspaceCapabilityVisibilitySnapshot

- **Purpose**: Normalized presentation grouping for runtime/capability signals.
- **Fields**:
  - `sourceLine`
  - `trustLine`
  - `routeLine`
  - `callerLine`
  - `structuredActionLine`
  - `systemSourceLine`
  - `auditLine`
- **Relationships**:
  - Derived from `RuntimeStatusUiModel`, `ContextInspectorUiModel`, and `AuditUiModel`

## State Transitions

- `PREPARING` / `UNAVAILABLE` -> digest becomes availability-focused and empty-state content remains primary
- `READY_IDLE` / `STREAMING` -> digest remains compact and conversation stays primary
- `AWAITING_APPROVAL` -> digest and approval surface become emphasized, but transcript remains visible
- `RECOVERABLE_FAILURE` -> inline failure messaging is visible while digest still provides route/source context

## Validation Rules

- Digest content must remain bounded; it should not render every available metadata line by default.
- Secondary entry affordances must remain available even when conversation is empty.
- Approval/failure emphasis must not remove access to the conversation or composer.
