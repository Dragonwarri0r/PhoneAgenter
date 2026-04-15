# Contract: Sync Extension and Portability Hooks

## Purpose

This contract defines the minimum shapes that future sync, merge, export, and extension work must be able to rely on after milestone `006`.
It is a runtime/domain contract, not a network API.

## 1. Persisted Record Metadata Contract

Every memory record created or updated after this milestone must expose these fields through the persisted model:

| Field | Required | Notes |
|---|---|---|
| `memoryId` | yes | Concrete persisted id |
| `logicalRecordId` | yes | Stable cross-version identity |
| `exposurePolicy` | yes | `PRIVATE`, `SHAREABLE_SUMMARY`, `SHAREABLE_FULL` |
| `syncPolicy` | yes | `LOCAL_ONLY`, `SUMMARY_SYNC_READY`, `FULL_SYNC_READY` |
| `originApp` | yes | Existing source field remains valid |
| `originDeviceId` | yes | Device attribution for future merge logic |
| `originUserId` | yes | User attribution placeholder |
| `logicalVersion` | yes | Monotonic version used for future merge inputs |
| `schemaVersion` | yes | Record schema compatibility marker |
| `evidenceRef` | optional | Must remain local-only unless future policy says otherwise |

## 2. Merge Candidate Normalization Contract

The runtime must be able to normalize any current memory record into a merge-ready shape:

```kotlin
data class MergeCandidate(
    val logicalRecordId: String,
    val recordId: String,
    val originDeviceId: String,
    val originUserId: String?,
    val logicalVersion: Long,
    val updatedAtEpochMillis: Long,
    val exposurePolicy: MemoryExposurePolicy,
    val syncPolicy: MemorySyncPolicy,
    val summaryPayload: String,
    val rawPayloadRef: String?,
)
```

Requirements:
- normalization must not require a new core entity
- private records may still produce merge candidates, but only with share-safe summary payload
- merge candidate generation must not trigger remote sync

## 3. Export Bundle Contract

The runtime must be able to produce a redaction-aware portability bundle:

```kotlin
data class ExportBundle(
    val bundleId: String,
    val recordId: String,
    val logicalRecordId: String,
    val exportMode: ExportMode,
    val payloadText: String,
    val includedFields: List<String>,
    val redactedFields: List<String>,
    val exposurePolicy: MemoryExposurePolicy,
    val syncPolicy: MemorySyncPolicy,
    val generatedAtEpochMillis: Long,
)
```

Rules:
- `PRIVATE` records may not export full raw content
- `SHAREABLE_SUMMARY` records export summary payload only
- `SHAREABLE_FULL` records may export full content if explicitly requested by the bundle mode
- raw evidence references are excluded by default

## 4. Redaction Decision Contract

Before any export bundle is produced, the runtime must evaluate:

```kotlin
data class DataRedactionPolicy(
    val recordId: String,
    val exposurePolicy: MemoryExposurePolicy,
    val allowFullExport: Boolean,
    val allowSummaryExport: Boolean,
    val mustRedactEvidence: Boolean,
    val reason: String,
)
```

This decision layer is required even though `v0` does not perform real sync.

## 5. Extension Registration Contract

Future providers and portability paths must register against a stable extension shape:

```kotlin
data class ExtensionRegistration(
    val extensionId: String,
    val extensionType: ExtensionType,
    val displayName: String,
    val supportedPayloadModes: List<String>,
    val requiredRecordFields: List<String>,
    val minimumSchemaVersion: Int,
    val privacyGuarantee: String,
    val enabledByDefault: Boolean,
)
```

Rules:
- new extensions must declare which metadata fields they depend on
- extensions must not require direct access to private raw evidence by default
- the registration contract must fit current runtime entities without introducing a new runtime core abstraction

## 6. Non-Goals for This Milestone

This contract does **not** require:
- real cross-device synchronization
- conflict resolution UX
- background merge executors
- cloud transport credentials
- user-facing export/import screens
