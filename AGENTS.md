# mobile_claw Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-04-23

## Active Technologies
- Kotlin 2.2.x with Java 11 toolchain + Android Gradle Plugin 8.8.x, Jetpack Compose Material 3, Hilt, Kotlin coroutines and Flow, Preferences DataStore, Room (003-persona-memory-fabric)
- Preferences DataStore for persona profile and lightweight persona settings, Room-backed local database for memory items and retrieval metadata (003-persona-memory-fabric)
- Room for risk assessments, policy decisions, approval requests, approval outcomes, and audit events; existing DataStore and Room layers from `003` remain in use (004-safe-execution-policy)
- Kotlin 2.2.x with Java 11 toolchain + Android Gradle Plugin 8.8.x, Jetpack Compose Material 3, Hilt, Kotlin coroutines and Flow, Room, existing runtime/provider/policy layers (005-android-capability-bridge)
- In-memory registry plus Room-backed audit/policy state already added in `004`; no durable capability sync layer in this milestone (005-android-capability-bridge)
- Kotlin 2.2.x with Java 11 toolchain + Android Gradle Plugin 8.8.x, Room, Hilt, Kotlin coroutines and Flow, existing `runtime/memory`, `runtime/policy`, and `runtime/capability` layers (006-sync-extension-hooks)
- Room-backed `MemoryItem` persistence plus in-memory domain services for export simulation, merge candidate evaluation, and extension registration (006-sync-extension-hooks)
- Kotlin 2.2.x with Java 11 toolchain + Android Gradle Plugin 8.8.x, Jetpack Compose Material 3, Hilt, Kotlin coroutines and Flow, existing runtime/session, runtime/policy, runtime/capability, and runtime/memory layers (007-external-runtime-entry)
- Existing Room-backed memory/policy/audit persistence plus lightweight in-memory inbound handoff coordination at the app boundary (007-external-runtime-entry)
- Kotlin 2.2.x with Java 11 toolchain + Android Gradle Plugin 8.8.x, Jetpack Compose Material 3, Hilt, Kotlin coroutines and Flow, existing runtime/session, runtime/provider, runtime/policy, runtime/capability, and runtime/intent layers (008-structured-action-payloads)
- Existing Room-backed policy/audit persistence plus in-memory structured payload normalization and preview state during runtime execution (008-structured-action-payloads)
- Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, Room (009-permission-governance-center)
- Extend existing Room database with governance tables (009-permission-governance-center)
- Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, Room, Android `ContentResolver` APIs (010-system-source-ingestion)
- Reuse Room-backed `MemoryItem` as the persistent system-source record store (010-system-source-ingestion)
- Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing memory/export services, Android share intents (011-portability-bundles)
- Reuse existing `MemoryItem` and `ExportBundle`; no new durable storage required (011-portability-bundles)
- Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, AndroidX AppFunctions, KSP, existing runtime/capability layers (012-real-appfunctions-integration)
- Reuse existing runtime/policy/memory storage; no new durable storage required (012-real-appfunctions-integration)
- Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing runtime/policy/memory/capability layers (013-workspace-information-architecture)
- Reuse existing DataStore and Room-backed runtime state; no new durable storage required (013-workspace-information-architecture)
- Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing runtime/workspace layers, Android Activity Result APIs, LiteRT-LM Android runtime (014-multimodal-ingress-and-composer)
- Reuse existing local storage plus app-managed on-device transient attachment copies under internal/external app storage (014-multimodal-ingress-and-composer)
- Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing runtime/session/capability/policy/governance layers, Room, DataStore, current Android bridge providers (015-tool-contract-standardization)
- Reuse existing Room-backed governance/audit state and in-memory capability registry; add descriptor metadata in runtime-managed catalogs or services without introducing remote storage (015-tool-contract-standardization)
- Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing runtime ingress/session/capability/governance layers, current external handoff parser and share targets (016-external-caller-interop-contracts)
- Reuse existing Room-backed governance/audit state and runtime-local request models; add contract metadata without introducing remote persistence (016-external-caller-interop-contracts)
- Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing runtime extension/interop/governance/workspace layers (017-unified-extension-surface)
- Reuse existing runtime-local extension registrations and workspace state; no new durable storage required (017-unified-extension-surface)
- Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing conversation-first workspace, runtime trace, governance, approval, and extension surfaces (018-runtime-control-center)
- Reuse existing Room/DataStore-backed runtime state and consolidate control views in-app without adding remote storage (018-runtime-control-center)
- Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, Room, existing runtime/memory/systemsource/session/contribution layers, Android local file/content access, current runtime control-center surfaces (020-knowledge-ingestion-and-retrieval)
- Reuse local Room-backed runtime storage and app-managed local file access; add durable knowledge asset, ingestion, and retrieval metadata locally; if `MemoryDatabase` schema changes, bump `MemoryDatabase.version` in the same patch (020-knowledge-ingestion-and-retrieval)
- Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, Room, existing runtime/session/policy/approval/contribution/knowledge layers, current runtime control-center surfaces (021-workflow-graph-and-automation)
- Reuse local Room/DataStore-backed runtime state; add durable workflow definitions, run records, and resumable checkpoints locally; if `MemoryDatabase` schema changes, bump `MemoryDatabase.version` in the same patch (021-workflow-graph-and-automation)
- Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing runtime/extension/session/systemsource/memory/policy/governance layers, current runtime control-center surfaces from `018` (019-runtime-hooks-and-context-sources)
- Reuse existing local Room/DataStore-backed runtime state and audit paths; add runtime-local contributor descriptors and outcome summaries without introducing remote persistence or durable knowledge-corpus storage (019-runtime-hooks-and-context-sources)
- Kotlin 2.2.x with Java 11 toolchain + Android Gradle Plugin 8.8.x, Jetpack Compose Material 3, Hilt, Kotlin coroutines and Flow, Room, existing runtime capability/policy/extension/session layers (023-capability-inference-read-tools)
- Reuse local Room/DataStore-backed runtime state and audit history; no remote storage (023-capability-inference-read-tools)

- (006-sync-extension-hooks)

## Project Structure

```text
src/
tests/
```

## Commands

# Add commands for 

## Code Style

: Follow standard conventions

## Recent Changes
- 023-capability-inference-read-tools: Added Kotlin 2.2.x with Java 11 toolchain + Android Gradle Plugin 8.8.x, Jetpack Compose Material 3, Hilt, Kotlin coroutines and Flow, Room, existing runtime capability/policy/extension/session layers
- 020-knowledge-ingestion-and-retrieval: Added Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, Room, existing runtime/memory/systemsource/session/contribution layers, Android local file/content access, current runtime control-center surfaces
- 019-runtime-hooks-and-context-sources: Added Kotlin 2.2.x on Android with Java 11 toolchain + Jetpack Compose Material 3, Hilt, Coroutines/Flow, existing runtime/extension/session/systemsource/memory/policy/governance layers, current runtime control-center surfaces from `018`


<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
