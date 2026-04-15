# Quickstart: Sync-Ready Share and Extension Hooks

## Purpose

Use this guide to validate milestone `006` as the schema and contract layer that prepares the runtime for future sync, merge, export, and provider extensibility without enabling real sync in `v0`.

## Preconditions

- `001` through `005` are already implemented and working
- The workspace can create and inspect memory items
- The current runtime remains local-only

## Validation Flow 1: Persist Future-Ready Metadata Without Sync

1. Open the workspace and trigger at least one new memory writeback by completing a local runtime turn.
2. Inspect the stored memory item through the existing inspector or repository debug path.
3. Verify:
   - the record still defaults to local-only behavior
   - `exposurePolicy` is explicitly present
   - `syncPolicy` is explicitly present
   - future merge metadata such as logical record identity and version are present

## Validation Flow 2: Simulate Merge Input Readiness

1. Pick an existing memory item and normalize it into a merge candidate through the new domain service or debug path.
2. Verify:
   - the candidate contains stable identity, origin, and logical version metadata
   - the candidate can be built without schema rewrites or placeholder objects
   - private records expose safe summary payloads rather than raw evidence by default

## Validation Flow 3: Generate Redaction-Aware Export Bundles

1. Generate an export bundle for:
   - one `PRIVATE` record
   - one `SHAREABLE_SUMMARY` record
   - one `SHAREABLE_FULL` record
2. Verify:
   - private records do not export raw content
   - summary-shareable records export summary payload only
   - full-shareable records can still produce a summary-safe bundle variant
   - the bundle records which fields were redacted

## Validation Flow 4: Validate Extension Registration Hooks

1. Register or simulate a future extension proposal, such as a portability exporter or sync transport adapter.
2. Verify:
   - the extension declares required record fields explicitly
   - the extension fits the current runtime contracts without introducing a new core entity
   - older records can be evaluated for compatibility through schema-version and required-field checks

## Notes

- `006` should not introduce actual multi-device sync.
- The success condition is future-readiness through metadata and contracts, not remote behavior.
- Private-by-default and redaction-aware sharing must remain stronger than portability convenience.

## Validation Notes

- `2026-04-09`: `./gradlew :app:compileDebugKotlin`, `./gradlew :app:assembleDebug`, and `./gradlew :app:lintDebug` all passed.
- `2026-04-09`: `MemoryItem` now persists explicit logical identity, origin device/user, logical version, schema version, exposure policy, and sync policy while keeping local-only defaults intact.
- `2026-04-09`: The workspace context inspector now surfaces sync, merge, export, and extension-compatibility summaries for active memory items.
- `2026-04-09`: Redaction-aware export bundle generation and seeded extension compatibility evaluation are available through the local memory domain layer without enabling actual cross-device sync.
