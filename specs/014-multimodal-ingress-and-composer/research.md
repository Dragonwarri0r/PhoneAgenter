# Research: Multimodal Ingress And Composer

## Decision 1: Represent modality capability in app-managed model metadata

- **Decision**: Extend local model profiles with coarse `supportsImage` and `supportsAudio` flags managed by the app.
- **Rationale**: Current imported model records do not expose formal runtime capability introspection. The workspace still needs a stable gating signal to decide whether to show image/audio entry points.
- **Alternatives considered**:
  - Query capability directly from the LiteRT runtime at runtime: rejected because the current integration does not expose such metadata cleanly.
  - Always show image/audio entry points: rejected because it would mislead users on text-only models.

## Decision 2: Use app-managed local file copies for pending attachments

- **Decision**: Copy selected/shared media into app-managed transient storage and reference those local paths in runtime attachments.
- **Rationale**: LiteRT-LM accepts absolute file paths for `Content.ImageFile` and `Content.AudioFile`. Copying into app-managed storage avoids relying on caller URI permissions at execution time.
- **Alternatives considered**:
  - Keep raw content URIs through the whole pipeline: rejected because URI grants may not remain stable and the backend expects local file paths.
  - Load media fully into memory and pass byte arrays: rejected for the first milestone because file-path based handling is simpler and more stable.

## Decision 3: Reuse one canonical attachment model for both composer and external handoff

- **Decision**: Define a single runtime attachment contract used by internal composer selection and external share media intake.
- **Rationale**: `014` should unify semantics, not create parallel media paths.
- **Alternatives considered**:
  - Separate “composer attachments” and “external handoff media” models: rejected because it would fragment runtime normalization and UI presentation.

## Decision 4: Keep attachment UI compact and removable

- **Decision**: Show attachments as a bounded preview rail above the composer with remove support.
- **Rationale**: The milestone goal is multimodal ingress, not turning the workspace into a media studio. Compact previews preserve the `013` conversation-first IA.
- **Alternatives considered**:
  - Full preview cards in the main conversation area: rejected because they would crowd the transcript.
  - Hide attachments entirely after selection: rejected because users need confidence about what will be sent.

## Decision 5: Pass multimodal content to LiteRT-LM using file-based `Contents`

- **Decision**: Build `Contents.of(...)` with prompt text plus `Content.ImageFile(...)` / `Content.AudioFile(...)` entries when attachments are present.
- **Rationale**: The currently installed LiteRT-LM Android dependency exposes `Content.ImageFile` and `Content.AudioFile`, so the app can forward multimodal attachments through the existing conversation API.
- **Alternatives considered**:
  - Keep text prompt only and append file descriptions into text: rejected because that would not be real multimodal execution.
