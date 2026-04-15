# Feature Specification: Portability Bundles

**Feature Branch**: `011-portability-bundles`  
**Created**: 2026-04-09  
**Status**: Draft  
**Input**: User description: "Turn the existing export hooks into a real user-facing portability experience so users can preview and share safe summary bundles from Mobile Claw."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Preview A Safe Portability Bundle (Priority: P1)

As a user, I want to preview the portability bundle for a memory record before it leaves the app, so I can understand what will be shared and what will remain private.

**Why this priority**: `006` already created the metadata contracts, but users still do not get a real export experience. Preview is the minimum product step that makes portability tangible.

**Independent Test**: Open the context inspector, choose an exportable memory item, and verify a bundle preview shows export mode, payload preview, included fields, and redacted fields.

**Acceptance Scenarios**:

1. **Given** a memory item marked `SHAREABLE_SUMMARY`, **When** I preview its portability bundle, **Then** I see a summary-only bundle with redacted full-content fields called out explicitly.
2. **Given** a memory item marked `SHAREABLE_FULL`, **When** I switch between allowed export modes, **Then** the preview updates to reflect the chosen mode and resulting redactions.
3. **Given** a memory item marked `PRIVATE`, **When** I try to export it, **Then** the app does not build a shareable bundle and instead explains that the record must stay local.

---

### User Story 2 - Share Or Export The Bundle Safely (Priority: P2)

As a user, I want to share the resulting portability bundle from Android, so I can move safe results to another app or save them for later use.

**Why this priority**: Preview alone does not complete the product value. The bundle needs an actual outbound path that still respects redaction policy.

**Independent Test**: From a previewable bundle, trigger the share action and verify Android share dispatch opens with the generated bundle text rather than raw memory internals.

**Acceptance Scenarios**:

1. **Given** a summary-safe export bundle is previewed, **When** I choose share, **Then** Android share dispatch opens with the formatted bundle text.
2. **Given** the bundle can only leave as a summary, **When** I share it, **Then** the shared payload omits redacted fields and raw private evidence.
3. **Given** the bundle cannot be exported, **When** I attempt to share, **Then** the app surfaces a clear refusal instead of silently failing.

---

### User Story 3 - Understand Future Compatibility (Priority: P3)

As a user, I want to see whether a portability bundle is compatible with future import or extension surfaces, so portability feels intentional rather than opaque.

**Why this priority**: The roadmap positions portability as a bridge to future import/sync flows, so users need a visible explanation of compatibility and limitations.

**Independent Test**: Open a bundle preview and verify the preview includes compatibility lines for known extensions plus clear reasons when something is not compatible.

**Acceptance Scenarios**:

1. **Given** a record matches the current extension contract, **When** I open the preview, **Then** the bundle shows compatible future extension targets.
2. **Given** an extension requires fields or schema the record does not satisfy, **When** I inspect compatibility, **Then** the preview explains why compatibility is missing.

---

### Edge Cases

- What happens when the context inspector shows no exportable items because all current records are private?
- What happens when a memory item allows only summary export and the user asks for full export?
- What happens when the payload is long enough that the preview must remain readable without overwhelming the sheet?
- What happens when Android share dispatch is unavailable on the device?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST expose a user-visible portability bundle preview for exportable memory records.
- **FR-002**: The preview MUST clearly show the selected export mode, the payload preview, included fields, and redacted fields.
- **FR-003**: The system MUST default to the safest allowed export mode for a record.
- **FR-004**: The system MUST prevent private records from being exported and MUST explain why export is blocked.
- **FR-005**: The system MUST allow mode switching between `summary` and `full` only when the record's exposure policy allows it.
- **FR-006**: The system MUST provide an Android outbound share/export path for allowed bundles.
- **FR-007**: The outbound bundle text MUST be derived from the redaction-aware export bundle, not from raw unfiltered memory content.
- **FR-008**: The preview MUST expose future compatibility information for known extension/import surfaces.
- **FR-009**: User-facing portability messaging MUST support English and Simplified Chinese automatically via device locale.
- **FR-010**: This milestone MUST not require cloud sync, account identity, or a generalized import engine.

### Key Entities *(include if feature involves data)*

- **PortabilityBundlePreview**: User-visible preview state built from an `ExportBundle`, redaction policy, and compatibility results.
- **PortabilityBundleDocument**: Formatted outbound text representation of the bundle intended for Android share/export.
- **PortabilityCompatibilityLine**: Human-readable compatibility result for one future extension/import target.
- **PortabilityExportRequest**: UI-triggered request to preview or share a bundle for a specific memory record and export mode.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can open the context inspector and preview a portability bundle for at least one exportable record.
- **SC-002**: Shared portability text is generated from the bundle formatter and respects redaction policy.
- **SC-003**: Private records never produce a shareable outbound payload.
- **SC-004**: Bundle preview and compatibility messaging remain understandable in both English and Simplified Chinese.

## Assumptions

- `006` remains the source of truth for export metadata, redaction rules, and extension registration.
- The first portability experience can focus on text-based outbound bundles instead of file-based archive formats.
- Android share dispatch is sufficient as the initial outbound path for this milestone.
- Import compatibility remains preview-only in this milestone; no actual import flow is required yet.
