# Data Model: Multimodal Ingress And Composer

## Entities

### ModelModalityCapabilities

- **Purpose**: Declares which multimodal inputs a selected model can accept.
- **Fields**:
  - `supportsImage`: Boolean
  - `supportsAudio`: Boolean
  - `capabilitySummary`: localized summary string

### PendingAttachment

- **Purpose**: User-visible attachment staged in the workspace composer before send.
- **Fields**:
  - `attachmentId`
  - `kind`: `IMAGE` or `AUDIO`
  - `displayName`
  - `mimeType`
  - `localPath`
  - `previewSummary`
  - `sourceLabel`
  - `addedAtEpochMillis`
- **Validation**:
  - Must have a non-empty local path
  - Must be removable before send

### RuntimeAttachment

- **Purpose**: Canonical runtime contract for multimodal media.
- **Fields**:
  - `attachmentId`
  - `kind`
  - `mimeType`
  - `localPath`
  - `displayName`
  - `sourceType`: `COMPOSER_IMPORT` or `EXTERNAL_HANDOFF`

### ExternalMediaHandoffPayload

- **Purpose**: External handoff payload with optional text plus normalized attachments.
- **Fields**:
  - `handoffId`
  - `sharedText`
  - `sharedSubject`
  - `attachments`
  - `rawSourceSummary`

## State Transitions

- Composer selected attachment -> pending attachment visible
- Pending attachment removed -> excluded from request
- Pending attachment submitted -> runtime attachment included in `RuntimeRequest`
- External shared media accepted -> normalized into runtime attachments and visible as pending/request attachments

## Validation Rules

- Unsupported media kinds must not be attachable/sendable for the current model.
- External media that cannot be copied locally must fail with a user-visible explanation.
- Requests may contain text-only, media-only, or text-plus-media input, but not a fully empty payload.
