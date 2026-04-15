# Implementation Plan: Real AppFunctions Integration

**Branch**: `012-real-appfunctions-integration` | **Date**: 2026-04-09 | **Spec**: [/Users/youxuezhe/StudioProjects/mobile_claw/specs/012-real-appfunctions-integration/spec.md](/Users/youxuezhe/StudioProjects/mobile_claw/specs/012-real-appfunctions-integration/spec.md)

## Summary

Upgrade the seeded AppFunctions boundary into a real AndroidX AppFunctions integration. This includes build-system support, real AppFunction service exposure, framework-backed availability checks for mapped capabilities, and honest workspace-visible status while preserving existing fallbacks.

## Technical Context

**Language/Version**: Kotlin 2.2.x on Android with Java 11 toolchain  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Coroutines/Flow, AndroidX AppFunctions, KSP, existing runtime/capability layers  
**Storage**: Reuse existing runtime/policy/memory storage; no new durable storage required  
**Testing**: Build/lint plus quickstart walkthroughs and generated-source verification  
**Target Platform**: Android application rooted at `/Users/youxuezhe/StudioProjects/mobile_claw/app`  
**Project Type**: Android application with local-first runtime  
**Performance Goals**: AppFunctions discovery should add negligible overhead compared with current capability resolution  
**Constraints**: Must preserve current fallback behavior, bilingual messaging, and local-first runtime boundaries  
**Scale/Scope**: First real AppFunctions exposure and self-discovery only; not a full third-party AppFunctions marketplace

## Constitution Check

- `local-first`: Pass. Exposed functions and discovery remain on-device.
- `private-by-default`: Pass. AppFunctions exposure is intentionally narrow and still respects existing runtime privacy controls.
- `policy-overrides-convenience`: Pass. Real AppFunctions availability does not bypass the current policy and fallback model.
- `android-first`: Pass. This milestone explicitly deepens Android integration through the platform-aligned Jetpack stack.

## Project Structure

### Feature Artifacts

```text
specs/012-real-appfunctions-integration/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/
│   └── real-appfunctions-contract.md
├── quickstart.md
└── tasks.md
```

### Code Touch Points

```text
build.gradle.kts
app/build.gradle.kts
gradle/libs.versions.toml
app/src/main/AndroidManifest.xml
app/src/main/java/com/mobileclaw/app/runtime/capability/
app/src/main/java/com/mobileclaw/app/runtime/appfunctions/
app/src/main/java/com/mobileclaw/app/ui/agentworkspace/
app/src/main/res/values/
app/src/main/res/values-zh/
```

## Phase 0: Research

Generate `research.md` to lock:

- which AndroidX AppFunctions artifacts and compiler path the project needs
- what the minimum viable exposed Mobile Claw functions should be
- how to safely bridge framework availability into current capability resolution
- how to keep unsupported devices on honest fallback wording

## Phase 1: Design & Contracts

Generate:

- `data-model.md`
- `contracts/real-appfunctions-contract.md`
- `quickstart.md`

Then refresh agent context with:

```bash
SPECIFY_FEATURE=012-real-appfunctions-integration ./.specify/scripts/bash/update-agent-context.sh codex
```

## Implementation Strategy

### MVP First

1. Add AppFunctions build/runtime dependencies
2. Register a real AppFunction service and expose a small Mobile Claw function set
3. Replace the seeded bridge with framework-backed status checks where supported
4. Surface honest AppFunctions status in the workspace

### Incremental Delivery

1. Upgrade Gradle/tooling for AndroidX AppFunctions
2. Add service exposure and generated metadata
3. Add bridge availability probing and self-package mapping
4. Refine workspace and bilingual wording

## Risks

- AndroidX AppFunctions is still alpha, so API assumptions need tight compile-time validation.
- Build-system uplift may introduce plugin/version friction because AppFunctions requires KSP.
- Real AppFunctions support varies by platform level; unsupported devices must stay stable and truthful.
