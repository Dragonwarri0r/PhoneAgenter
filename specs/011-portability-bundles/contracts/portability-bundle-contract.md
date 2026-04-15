# Portability Bundle Contract

## Intent

`011` turns existing export metadata into a user-visible portability flow.

The contract is:

1. Choose a memory record from the context inspector
2. Build the safest allowed export bundle
3. Present a preview with redaction and compatibility information
4. Share the formatted bundle text through Android only if export is allowed

## Preview Input

```kotlin
data class PortabilityExportRequest(
    val memoryId: String,
    val preferFullExport: Boolean,
    val requestedAtEpochMillis: Long,
)
```

## Preview Output

```kotlin
data class PortabilityBundlePreview(
    val memoryId: String,
    val title: String,
    val exportModeLabel: String,
    val payloadPreview: String,
    val redactionReason: String,
    val includedFields: List<String>,
    val redactedFields: List<String>,
    val compatibilityLines: List<PortabilityCompatibilityLine>,
    val canShare: Boolean,
    val canSwitchToFull: Boolean,
    val canSwitchToSummary: Boolean,
)
```

## Outbound Bundle Text

The first portability format is `text/plain` and should stay human-readable plus machine-friendly.

Recommended sections:

1. bundle header
2. record id and logical record id
3. export mode
4. payload
5. included fields
6. redacted fields
7. compatibility summary

## Safety Rules

- Private records never produce a shareable outbound bundle.
- Summary-only records may not switch to full export.
- Bundle text must be generated from `ExportBundle`.
- Compatibility lines are advisory only and must not imply that import is already implemented.
