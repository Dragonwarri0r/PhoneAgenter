# Implementation Plan: Sync-Ready Share and Extension Hooks

**Branch**: `006-sync-extension-hooks` | **Date**: 2026-04-09 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/006-sync-extension-hooks/spec.md`

## Summary

Prepare the current local-first runtime for future sync, merge, and portability work without implementing real multi-device synchronization in `v0`.
This milestone extends the existing memory schema and repository layer with future-ready metadata, adds redaction-aware export and merge-ready contracts, and reserves extension registration hooks so future providers and portability paths can plug into the runtime without redesigning the core model.

The feature must preserve all current local-only behavior as the default while making new metadata explicit and inspectable.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain  
**Primary Dependencies**: Android Gradle Plugin 8.8.x, Room, Hilt, Kotlin coroutines and Flow, existing `runtime/memory`, `runtime/policy`, and `runtime/capability` layers  
**Storage**: Room-backed `MemoryItem` persistence plus in-memory domain services for export simulation, merge candidate evaluation, and extension registration  
**Testing**: Build, lint, and manual quickstart validation using inspector-visible metadata and local export/merge simulation flows  
**Target Platform**: Android 12+ phones and emulators, single-user local-first runtime  
**Project Type**: Single-module mobile app (Android)  
**Performance Goals**: Metadata enrichment and export evaluation should remain lightweight enough to preserve current workspace responsiveness and context loading behavior  
**Constraints**: `v0` must remain fully local-only by default; no remote sync transport, account system, or background merge executor is added in this milestone; private raw evidence must stay non-shareable by default; the current memory schema should evolve without forcing a breaking rewrite later  
**Scale/Scope**: Extend the existing memory model, add one export/redaction decision layer, add one merge-input representation layer, and add one extension registration contract layer

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: PASS. The feature explicitly preserves local-only defaults and does not add remote orchestration.
- **Persona and Memory Are Separate Systems**: PASS. Only memory, export, and extension metadata are changing.
- **Safety Gates Override Convenience**: PASS. No execution bypass is introduced; this milestone is metadata and contract work only.
- **Adapter-Based Capability Integration**: PASS. New portability and provider hooks are extension-facing adapters rather than new runtime-core ownership models.
- **Privacy, Scope, and Audit by Default**: PASS. Private-by-default and redaction-aware sharing are first-class goals of this milestone.

Post-design expectation: still passes, provided sync remains non-executing in `v0`, redaction applies before export bundle generation, and extension hooks do not bypass scope or privacy rules.

## Project Structure

### Documentation (this feature)

```text
specs/006-sync-extension-hooks/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── sync-extension-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
└── src/
    └── main/
        ├── java/com/mobileclaw/app/
        │   ├── di/
        │   │   └── AppModule.kt
        │   ├── runtime/
        │   │   ├── memory/
        │   │   │   ├── MemoryItem.kt
        │   │   │   ├── MemoryDao.kt
        │   │   │   ├── MemoryDatabase.kt
        │   │   │   ├── ScopedMemoryRepository.kt
        │   │   │   ├── MemoryWritebackService.kt
        │   │   │   ├── MemoryFixtures.kt
        │   │   │   ├── MemoryRetrievalService.kt
        │   │   │   ├── ActiveContextSummary.kt
        │   │   │   ├── SyncEnvelope.kt
        │   │   │   ├── MergeCandidate.kt
        │   │   │   ├── ExportBundle.kt
        │   │   │   ├── DataRedactionPolicy.kt
        │   │   │   ├── ExportDecisionService.kt
        │   │   │   └── ExtensionRegistration.kt
        │   │   ├── capability/
        │   │   │   ├── CapabilityRegistration.kt
        │   │   │   └── ProviderDescriptor.kt
        │   │   ├── strings/
        │   │   │   └── AppStrings.kt
        │   └── ui/
        │       └── agentworkspace/
        │           ├── AgentWorkspaceViewModel.kt
        │           ├── model/
        │           │   └── ContextInspectorUiModel.kt
        │           └── components/
        │               └── ContextInspectorSheet.kt
        └── res/
            ├── values/
            │   └── strings.xml
            └── values-zh/
                └── strings.xml
```

**Structure Decision**: Build `006` primarily inside `runtime/memory`, because the current future-sync metadata already begins on `MemoryItem` through `exposurePolicy` and `syncPolicy`. The milestone should enrich that layer with origin/version metadata, add redaction/export and merge-input domain models beside it, and only minimally touch capability/UI code where extension and explainability surfaces need to consume the new metadata.

## Complexity Tracking

No constitution violations are expected for this feature.
