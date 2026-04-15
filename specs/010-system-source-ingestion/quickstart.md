# Quickstart: System Source Ingestion

## Purpose

Validate milestone `010` as the first real Android system context ingestion layer above the local runtime.

## Preconditions

- `003`, `007`, `008`, and `009` are already implemented and working
- The workspace can already show runtime status and governance surfaces
- The app is installed on a device or emulator where contacts/calendar permissions can be granted or denied

## Validation Flow 1: Contacts Ingestion

1. Grant contacts permission.
2. Submit a request that references a known contact.
3. Verify:
   - contacts source shows as available
   - a contacts contribution appears in runtime-visible source status
   - relevant system-source memory is ingested

## Validation Flow 2: Calendar Ingestion

1. Grant calendar permission.
2. Submit a schedule-related request.
3. Verify:
   - calendar source shows as available
   - upcoming relevant calendar context contributes to runtime status

## Validation Flow 3: Permission Missing State

1. Deny one or both system-source permissions.
2. Verify:
   - the workspace shows that source as unavailable because permission is missing
   - runtime remains stable and does not claim unavailable context was used

## Validation Flow 4: No Relevant Results

1. Submit a request unrelated to contacts or calendar, or use an empty data set.
2. Verify:
   - runtime stays stable
   - source status remains visible
   - no fake context is injected

## Notes

- `010` is complete when at least contacts and calendar can contribute real device context to runtime in a bounded, explainable way.
- This milestone is not a generalized connector marketplace.
- Validation completed on 2026-04-09 with:
  - `./gradlew :app:compileDebugKotlin`
  - `./gradlew :app:assembleDebug`
  - `./gradlew :app:lintDebug`
- Implementation notes:
  - Contacts and calendar are now permission-aware first-party system sources.
  - Relevant source records are materialized as bounded `SYSTEM_SOURCE` memory items and fed into runtime context loading.
  - The workspace now shows source availability, missing-permission state, and current-request source contribution summaries.
