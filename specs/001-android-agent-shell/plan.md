# Implementation Plan: Android Agent Shell and Local Model Workspace

**Branch**: `001-android-agent-shell` | **Date**: 2026-04-08 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-android-agent-shell/spec.md`

## Summary

Build the first usable Android agent workspace as a single-screen local chat shell with a clear `Digital Atrium` visual language, a selectable local model, streamed assistant output, and explicit session reset and feedback behavior.

The implementation will use a single Android app module with Jetpack Compose, Material 3 theming, Hilt-based dependency wiring, and a thin `LocalChatGateway` abstraction inspired by the `gallery` project's local chat patterns.
This milestone intentionally stops short of the full runtime orchestration planned in `002`; it delivers the UI shell and local-session interaction loop without forcing the later runtime contract.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain  
**Primary Dependencies**: Android Gradle Plugin 8.8.x, Jetpack Compose Material 3, Navigation Compose, Hilt, DataStore, local-model chat adapter patterned after LiteRTLM-style streaming  
**Storage**: In-memory active chat session plus DataStore for lightweight selected-model and workspace preferences  
**Testing**: JUnit4, AndroidX instrumentation, Compose UI testing  
**Target Platform**: Android 12+ phones and emulators for workspace validation, with a forward-compatible path to Android 16+ capability features in later specs  
**Project Type**: Single-module mobile app (Android)  
**Performance Goals**: Visible input acknowledgment within 100ms of send, smooth transcript interaction at 60 fps on reference devices, and immediate in-progress state before first streamed assistant chunk arrives  
**Constraints**: Local-first, offline-capable chat path, text-first composer, single active session in `v0`, low-noise UI with tonal layering instead of dense borders, and no dependency on the later full runtime pipeline  
**Scale/Scope**: One primary workspace screen, four supporting surfaces, one selected local model at a time, one active local conversation session

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: PASS. The milestone is local-device only and does not depend on cloud orchestration.
- **Persona and Memory Are Separate Systems**: PASS. This feature does not introduce persona-memory coupling; later runtime context work stays deferred to `003`.
- **Safety Gates Override Convenience**: PASS. This milestone is chat-shell focused and does not bypass later approval or policy requirements.
- **Adapter-Based Capability Integration**: PASS. The local chat backend is introduced as a UI-facing adapter contract rather than as the final runtime core.
- **Privacy, Scope, and Audit by Default**: PASS. The milestone keeps only active session state and lightweight local preferences; no cross-device sync or broad sharing is introduced.

Post-design re-check expectation: still passes, provided the local chat adapter remains isolated from later cross-app capability and policy flows.

## Project Structure

### Documentation (this feature)

```text
specs/001-android-agent-shell/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── local-chat-gateway.md
└── tasks.md
```

### Source Code (repository root)

```text
.
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   └── libs.versions.toml
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/mobileclaw/app/
        │   │   ├── MainActivity.kt
        │   │   ├── MobileClawApplication.kt
        │   │   ├── di/
        │   │   │   └── AppModule.kt
        │   │   ├── runtime/localchat/
        │   │   ├── ui/navigation/
        │   │   ├── ui/theme/
        │   │   └── ui/agentworkspace/
        │   │       ├── AgentWorkspaceScreen.kt
        │   │       ├── AgentWorkspaceViewModel.kt
        │   │       ├── model/
        │   │       └── components/
        │   └── res/
        └── androidTest/
            └── java/com/mobileclaw/app/
```

**Structure Decision**: Use a single Android app module for `001` to minimize setup complexity and make the first milestone shippable as a focused workspace shell. Keep the local chat adapter and workspace state in their own packages so they can later be connected to the richer runtime pipeline without rewriting the screen.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No constitution violations are expected for this feature.
