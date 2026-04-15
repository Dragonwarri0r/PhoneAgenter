# Data Model: System Source Ingestion

## Entities

### SystemSourceDescriptor

Describes a supported Android system source and its runtime availability.

**Fields**

- `sourceId: String`
- `displayName: String`
- `permissionName: String`
- `isGranted: Boolean`
- `availabilitySummary: String`

### SystemSourceIngestionResult

Represents one ingestion pass for a source.

**Fields**

- `sourceId: String`
- `recordsWritten: Int`
- `recordsSkipped: Int`
- `statusMessage: String`

### SystemSourceContribution

Represents source usage in the current runtime context.

**Fields**

- `sourceId: String`
- `displayName: String`
- `recordCount: Int`
- `summary: String`

### SystemSourceMemoryRecord

Derived `MemoryItem` record representing a contact or calendar result.

**Derived properties**

- `sourceType = SYSTEM_SOURCE`
- `scope = CONTACT_SCOPED` or `DEVICE_SCOPED`
- bounded `expiresAtEpochMillis`

## Relationships

- One `SystemSourceDescriptor` can produce many `SystemSourceMemoryRecord`
- One runtime request can have many `SystemSourceContribution`
- `SystemSourceIngestionResult` is a transient execution result, not necessarily a persisted table
