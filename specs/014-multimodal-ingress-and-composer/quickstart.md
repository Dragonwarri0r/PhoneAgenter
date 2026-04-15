# Quickstart: Multimodal Ingress And Composer

## Goal

Validate model-aware image/audio input, multimodal request normalization, and external media handoff reuse.

## Preconditions

- Build the app successfully
- Have at least one imported `.litertlm` model available
- Have one image file and one audio file available on-device

## Validation Status

- `./gradlew :app:compileDebugKotlin` passed on 2026-04-10
- `./gradlew :app:assembleDebug` passed on 2026-04-10
- `./gradlew :app:lintDebug` passed on 2026-04-10
- Lint report: `/Users/youxuezhe/StudioProjects/mobile_claw/app/build/reports/lint-results-debug.html`

## Manual Validation Scenarios

1. **Model-aware composer gating**
   - Select a model with image/audio capability enabled
   - Confirm image/audio actions appear in the composer
   - Switch to a model without one or both capabilities
   - Confirm unsupported actions disappear or disable honestly

2. **Composer attachment preview**
   - Attach an image
   - Attach an audio file
   - Confirm compact previews appear above the composer
   - Remove one attachment and confirm it disappears cleanly

3. **Runtime normalization**
   - Send a request with text plus at least one attachment
   - Confirm the runtime continues through the normal session pipeline
   - Confirm the request does not silently fall back to text-only behavior

4. **External media handoff**
   - Share an image or audio item into Mobile Claw
   - Confirm the workspace opens with normalized attachment previews
   - Confirm source metadata still appears

5. **Locale**
   - Run once in English and once in Simplified Chinese
   - Confirm multimodal labels and limitations localize correctly

## Follow-up Notes

- Build, packaging, and lint validation are complete for `014`
- Manual device-side validation is still recommended for real image/audio picker behavior and end-to-end LiteRT-LM multimodal generation with imported models
- External media handoff now supports `text/plain`, `image/*`, and `audio/*` through the same canonical attachment path
