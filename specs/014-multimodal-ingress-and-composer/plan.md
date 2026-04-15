# Implementation Plan: Multimodal Ingress And Composer

**Branch**: `014-multimodal-ingress-and-composer` | **Date**: 2026-04-10 | **Spec**: [/Users/youxuezhe/StudioProjects/mobile_claw/specs/014-multimodal-ingress-and-composer/spec.md](/Users/youxuezhe/StudioProjects/mobile_claw/specs/014-multimodal-ingress-and-composer/spec.md)
**Input**: Feature specification from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/014-multimodal-ingress-and-composer/spec.md`

## Summary

Extend Mobile Claw from text-only workspace input to model-aware multimodal input by adding image/audio attachment support in the composer, normalizing attachments into the runtime request contract, and forwarding them to the LiteRT-LM backend as image/audio content. Reuse the same attachment model for external media share handoffs.

## Technical Context

**Language/Version**: Kotlin 2.2.x on Android with Java 11 toolchain  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing runtime/workspace layers, Android Activity Result APIs, LiteRT-LM Android runtime  
**Storage**: Reuse existing local storage plus app-managed on-device transient attachment copies under internal/external app storage  
**Testing**: Build/lint plus quickstart walkthroughs for composer attachment, external media share, and multimodal model gating  
**Target Platform**: Android application rooted at `/Users/youxuezhe/StudioProjects/mobile_claw/app`  
**Project Type**: Android application with local-first runtime and workspace UI  
**Performance Goals**: Attachment preview and copying should be lightweight enough for small local media selections and should not degrade normal text-only chat performance  
**Constraints**: Must preserve local-first behavior, avoid adding a full media library UI, keep composer compact, and remain compatible with existing policy/runtime/session flows  
**Scale/Scope**: First multimodal slice with bounded image/audio attachments, local file copies, request normalization, and external share media support

## Constitution Check

- `local-first`: Pass. Attachments are copied and used on-device only.
- `private-by-default`: Pass. Attachment files are stored in app-managed space and only forwarded through local runtime execution.
- `policy-overrides-convenience`: Pass. This milestone adds ingress/normalization, not bypasses to policy behavior.
- `android-first`: Pass. The feature is explicitly about Android composer, Android intents, and LiteRT multimodal input.

## Project Structure

### Feature Artifacts

```text
specs/014-multimodal-ingress-and-composer/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/
│   └── multimodal-runtime-contract.md
├── quickstart.md
└── tasks.md
```

### Code Touch Points

```text
app/src/main/java/com/mobileclaw/app/runtime/localchat/
app/src/main/java/com/mobileclaw/app/runtime/session/
app/src/main/java/com/mobileclaw/app/runtime/provider/
app/src/main/java/com/mobileclaw/app/runtime/ingress/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/
app/src/main/res/values/
app/src/main/res/values-zh/
app/src/main/AndroidManifest.xml
```

## Phase 0: Research

Generate `research.md` to lock:

- how to represent model modality capability in app-managed metadata
- how to store transient attachment copies safely inside the app
- how to map image/audio attachments into runtime request contracts
- how to pass multimodal content into LiteRT-LM `Contents` using `Content.ImageFile` and `Content.AudioFile`
- how to extend external share handling from text-only into media-aware handoff

## Phase 1: Design & Contracts

Generate:

- `data-model.md`
- `contracts/multimodal-runtime-contract.md`
- `quickstart.md`

Then refresh agent context with:

```bash
SPECIFY_FEATURE=014-multimodal-ingress-and-composer ./.specify/scripts/bash/update-agent-context.sh codex
```

## Implementation Strategy

### MVP First

1. Add model modality capability metadata and composer gating
2. Add compact pending attachment preview and removal in the workspace
3. Normalize attachments into `RuntimeRequest`
4. Forward attachments through local generation to LiteRT-LM
5. Extend external handoff to support shared media through the same attachment contract

### Incremental Delivery

1. Introduce model capability and runtime attachment models
2. Add attachment staging/import service and composer UI
3. Update runtime/provider/gateway for multimodal execution
4. Extend external share text/media parsing and mapping
5. Refine bilingual messaging and validation

## Risks

- Imported models may not expose formal capability introspection, so the app needs a coarse but honest capability declaration strategy.
- Media URI handling can fail across callers; the app needs safe copy/import behavior and graceful fallbacks.
- Multimodal support can easily bloat the composer if previews and actions are not kept compact.
