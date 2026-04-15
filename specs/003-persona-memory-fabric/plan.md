# Implementation Plan: Persona and Scoped Memory Fabric

**Branch**: `003-persona-memory-fabric` | **Date**: 2026-04-08 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-persona-memory-fabric/spec.md`

## Summary

Build the first real persona and memory layer for the local runtime by introducing a stable persona profile, a scoped memory store, deterministic context retrieval, runtime writeback hooks, and a safe user-visible context summary.

This milestone should deepen the `002` runtime pipeline rather than bypass it.
Persona and memory stay separate at the storage and retrieval layers, while `RuntimeContextLoader` becomes the integration point that assembles user-safe context for execution and explanation.

## Technical Context

**Language/Version**: Kotlin 2.2.x with Java 11 toolchain  
**Primary Dependencies**: Android Gradle Plugin 8.8.x, Jetpack Compose Material 3, Hilt, Kotlin coroutines and Flow, Preferences DataStore, Room  
**Storage**: Preferences DataStore for persona profile and lightweight persona settings, Room-backed local database for memory items and retrieval metadata  
**Testing**: JUnit4 for repository, retrieval, and lifecycle rules; AndroidX instrumentation only where workspace integration needs verification  
**Target Platform**: Android 12+ phones and emulators, single-user local-first runtime  
**Project Type**: Single-module mobile app (Android)  
**Performance Goals**: Context assembly should complete inside one request orchestration loop and surface a compact summary before provider execution begins  
**Constraints**: Local-first, private-by-default, no real sync in `v0`, user-safe summaries must avoid raw evidence leakage, retrieval should be deterministic and explainable rather than embedding-dependent  
**Scale/Scope**: One default persona profile, one local memory store, scoped retrieval integrated into the existing runtime, and one lightweight workspace management surface for context visibility and manual edits

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Local-First, User-Controlled**: PASS. Persona and memory stay fully local and do not require remote services.
- **Persona and Memory Are Separate Systems**: PASS. The design stores persona and memory independently and only combines them during context assembly.
- **Safety Gates Override Convenience**: PASS. This milestone prepares scoped retrieval and safe summaries but does not bypass later policy decisions.
- **Adapter-Based Capability Integration**: PASS. Context assembly plugs into the runtime contract created in `002` rather than creating a side channel.
- **Privacy, Scope, and Audit by Default**: PASS. Memory defaults to scoped, private handling, and safe summaries explicitly redact raw evidence.

Post-design re-check expectation: still passes, provided persona editing remains separate from memory mutation and writeback defaults stay private for inferred app memory.

## Project Structure

### Documentation (this feature)

```text
specs/003-persona-memory-fabric/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── persona-memory-context-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
app/
└── src/
    └── main/
        └── java/com/mobileclaw/app/
            ├── di/
            │   └── AppModule.kt
            ├── runtime/
            │   ├── persona/
            │   │   ├── PersonaProfile.kt
            │   │   ├── PersonaFixtures.kt
            │   │   ├── PersonaRepository.kt
            │   │   └── PreferenceBackedPersonaRepository.kt
            │   ├── memory/
            │   │   ├── MemoryItem.kt
            │   │   ├── MemoryFixtures.kt
            │   │   ├── MemoryDao.kt
            │   │   ├── MemoryDatabase.kt
            │   │   ├── ScopedMemoryRepository.kt
            │   │   ├── MemoryRetrievalService.kt
            │   │   ├── MemoryWritebackService.kt
            │   │   ├── ActiveContextSummary.kt
            │   │   └── PersonaMemoryContextLoader.kt
            │   └── session/
            │       ├── RuntimeRequest.kt
            │       └── RuntimeSessionOrchestrator.kt
            └── ui/
                └── agentworkspace/
                    ├── AgentWorkspaceScreen.kt
                    ├── AgentWorkspaceViewModel.kt
                    ├── model/
                    │   └── ContextInspectorUiModel.kt
                    └── components/
                        ├── ContextWindowCard.kt
                        └── ContextInspectorSheet.kt
```

**Structure Decision**: Keep persona and memory in dedicated runtime packages and integrate them through the existing `RuntimeContextLoader` seam from `002`. Use Room for queryable memory metadata, DataStore for the smaller stable persona profile, and only add lightweight workspace surfaces needed to explain and manually adjust active context.

## Complexity Tracking

No constitution violations are expected for this feature.
