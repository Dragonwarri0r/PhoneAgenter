# Data Model: Portability Bundles

## PortabilityExportRequest

- `memoryId`: target record id
- `preferFullExport`: whether the user is asking for full export when allowed
- `requestedAtEpochMillis`: timestamp for analytics/audit-friendly UI flow

**Validation**

- `memoryId` must resolve to an existing memory record
- `preferFullExport` must be ignored when full export is not allowed

## PortabilityBundlePreview

- `memoryId`: source record id
- `title`: user-visible record title
- `exportModeLabel`: localized label for current export mode
- `payloadPreview`: formatted portable payload text
- `redactionReason`: localized explanation of current redaction policy
- `includedFields`: visible list of bundle fields
- `redactedFields`: visible list of hidden fields
- `compatibilityLines`: list of previewable compatibility results
- `canShare`: whether the bundle can leave the app
- `canSwitchToFull`: whether full export is allowed
- `canSwitchToSummary`: whether summary export is allowed

**State transitions**

- `idle -> previewed`
- `previewed -> mode switched`
- `previewed -> shared`
- `previewed -> blocked`
- `previewed -> dismissed`

## PortabilityCompatibilityLine

- `title`: extension/import target label
- `detail`: compatibility explanation
- `isCompatible`: boolean

## PortabilityBundleDocument

- `bundleId`: derived export bundle id
- `documentText`: formatted outbound text
- `mimeType`: `text/plain`

**Validation**

- `documentText` must be generated from `ExportBundle`, not raw `MemoryItem`
- `documentText` must not include fields listed in `redactedFields`
