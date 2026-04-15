# Implementation Plan: Unified Extension Surface

**Branch**: `017-unified-extension-surface` | **Date**: 2026-04-13 | **Spec**: [/Users/youxuezhe/StudioProjects/mobile_claw/specs/017-unified-extension-surface/spec.md](/Users/youxuezhe/StudioProjects/mobile_claw/specs/017-unified-extension-surface/spec.md)
**Input**: Feature specification from `/Users/youxuezhe/StudioProjects/mobile_claw/specs/017-unified-extension-surface/spec.md`

## Summary

Evolve the portability-oriented hooks from `006` into a runtime-wide unified extension surface covering ingress adapters, tool providers, context sources, export/import adapters, and sync transports. Add shared registration, enablement, compatibility, and discovery contracts so new extensions can plug into Mobile Claw without repeated core refactors.

## Roadmap Fit

`017` is the second contract-hardening milestone after `016`.
Its job is to generalize extension registration, compatibility, and enablement across runtime surfaces without reopening the external caller contract work from `016` or prematurely turning the app into the control-center UX planned for `018`.

## Technical Context

**Language/Version**: Kotlin 2.2.x on Android with Java 11 toolchain  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing runtime/memory/capability/session/governance layers, Room, DataStore, current portability and provider contracts  
**Storage**: Reuse existing local runtime state and seeded extension registrations; persist compatibility or enablement metadata only if required by the current local architecture  
**Testing**: Build/lint plus quickstart walkthroughs for extension registration, compatibility validation, and discovery summaries  
**Target Platform**: Android application rooted at `/Users/youxuezhe/StudioProjects/mobile_claw/app`  
**Project Type**: Android application with local-first runtime and extensibility hooks  
**Performance Goals**: Extension enumeration and compatibility checks should stay lightweight enough for startup and on-demand inspection flows  
**Constraints**: Must remain local-first, preserve backward compatibility with `006` portability hooks, avoid introducing a second plugin runtime, and keep extension growth from leaking into ad hoc core branches  
**Scale/Scope**: First unified extension registration surface plus compatibility and enablement semantics across several extension families

## Constitution Check

- `local-first`: Pass. Extension registration and discovery remain local and do not require remote registries.
- `private-by-default`: Pass. Registrations explicitly carry privacy guarantees and dependency metadata.
- `policy-overrides-convenience`: Pass. Extensions declare trust/enablement semantics instead of bypassing runtime policy.
- `android-first`: Pass. The extension surface supports Android-facing ingress, provider, and export/import paths while preserving common runtime contracts.

## Project Structure

### Feature Artifacts

```text
specs/017-unified-extension-surface/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/
│   └── unified-extension-surface.md
├── quickstart.md
└── tasks.md
```

### Code Touch Points

```text
app/src/main/java/com/mobileclaw/app/runtime/memory/
app/src/main/java/com/mobileclaw/app/runtime/capability/
app/src/main/java/com/mobileclaw/app/runtime/ingress/
app/src/main/java/com/mobileclaw/app/runtime/systemsource/
app/src/main/java/com/mobileclaw/app/runtime/session/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/model/
app/src/main/res/values/
app/src/main/res/values-zh/
```

## Phase 0: Research

Generate `research.md` to lock:

- how to evolve `ExtensionRegistration` from `006` into a broader runtime-wide registration shape
- what the minimum viable unified extension types should be in the first slice
- how enablement and compatibility should be represented without creating a marketplace-level plugin system
- how discovery summaries should be surfaced to keep runtime extension state inspectable
- how to preserve backward compatibility with existing portability-oriented registrations

## Phase 1: Design & Contracts

Generate:

- `data-model.md`
- `contracts/unified-extension-surface.md`
- `quickstart.md`

Then refresh agent context with:

```bash
SPECIFY_FEATURE=017-unified-extension-surface ./.specify/scripts/bash/update-agent-context.sh codex
```

## Implementation Strategy

### MVP First

1. Introduce unified runtime extension registration and type models
2. Add compatibility and enablement state models
3. Evolve seeded `006` registrations into the unified surface
4. Add discovery summaries for multiple extension families
5. Align wording and validation

### Incremental Delivery

1. Add unified registration, type, enablement, and compatibility models
2. Adapt existing portability registrations into the new model
3. Register at least several extension families through the same surface
4. Add extension discovery and inspection summaries
5. Refine bilingual wording and validation

## Risks

- If `017` is too abstract, it can become a contract-only milestone without enough product or runtime change.
- If `017` is too broad, it can swallow the more concrete interop work from `016`.
- Backward compatibility with `006` must be preserved carefully or current portability logic will fork into old and new extension systems.
