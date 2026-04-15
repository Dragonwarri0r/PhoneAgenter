# Contract: Multimodal Runtime Request

## Model Capability Surface

Each `LocalModelProfile` must expose coarse modality capability flags:

- `supportsImage`
- `supportsAudio`

These flags drive composer gating and explainability.

## Runtime Attachment Contract

Each `RuntimeRequest` may include zero or more `RuntimeAttachment` records:

- `attachmentId`
- `kind`
- `mimeType`
- `localPath`
- `displayName`
- `sourceType`

## Workspace Composer Contract

The composer base surface should support:

- optional image import action
- optional audio import action
- bounded pending attachment preview rail
- remove action per attachment

These actions must remain compact and not replace the conversation as the main surface.

## External Handoff Contract

External media share handling should accept:

- `ACTION_SEND` with `image/*`
- `ACTION_SEND` with `audio/*`
- optional `EXTRA_TEXT`
- optional `EXTRA_STREAM`

Accepted external media must map into the same runtime attachment model used by internal composer imports.

## Backend Contract

When runtime attachments exist:

- image attachments should be forwarded as `Content.ImageFile(localPath)`
- audio attachments should be forwarded as `Content.AudioFile(localPath)`
- text prompt should remain part of the same `Contents` payload
