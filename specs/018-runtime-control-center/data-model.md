# Data Model: Runtime Control Center

## RuntimeControlCenterState

User-visible state connecting the active conversation, current trace, and managed artifact entries.

**Fields**
- `sessionId`
- `headline`
- `supportingText`
- `traceSnapshot`
- `artifactEntries`
- `attentionMode`
- `isBusy`
- `isEditable`

## RuntimeTraceSnapshot

Readable summary of how one request moved through the runtime.

**Fields**
- `requestSourceLine`
- `toolPathLine`
- `approvalLine`
- `contextLines`
- `extensionLines`
- `constraintLines`
- `recentAuditLines`

## ManagedArtifactEntry

Inspectable runtime-managed object surfaced from the control center.

**Fields**
- `artifactId`
- `artifactType`
- `title`
- `summary`
- `statusLine`
- `editCapability`
- `entryAction`

## ArtifactEditCapability

Description of whether and how an artifact can be changed.

**Fields**
- `isEditable`
- `supportedActions`
- `reasonIfUnavailable`

## ControlCenterSection

Section-level grouping in the control center.

**Fields**
- `sectionId`
- `title`
- `items`
- `emptyState`
- `supportsExpansion`
