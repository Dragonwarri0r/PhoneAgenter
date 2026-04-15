# Quickstart: Persona and Scoped Memory Fabric

## Purpose

Use this guide to validate milestone `003` as the first real persona-and-memory context layer for the local runtime.

## Preconditions

- The Android app from `001` and runtime pipeline from `002` are already working
- The runtime now uses a persona-and-memory-backed context loader instead of the no-op context loader
- A default persona profile exists for the local user
- The local memory store contains at least a few seeded memory items across different scopes and lifecycles

## Validation Flow 1: Stable Persona and Relevant Context

1. Open the workspace and load a ready local model.
2. Configure or inspect the current persona traits.
3. Seed or create a few memory items relevant to the current request.
4. Submit a prompt.
5. Verify:
   - the runtime reports that persona and memory context were loaded
   - the active context summary distinguishes persona constraints from retrieved memory
   - unrelated memory does not appear in the active context set

## Validation Flow 2: Scope Isolation

1. Create one private app-scoped memory item for the current workspace.
2. Create one global durable memory item.
3. Run one retrieval flow from the current workspace scope.
4. Verify:
   - the global item is eligible when relevant
   - the private app-scoped item is only eligible inside its own scope
   - out-of-scope retrieval excludes the private item rather than leaking it into another request context

## Validation Flow 3: Promotion, Editing, and Expiration

1. Create one ephemeral memory item and one working memory item.
2. Promote one of them to durable or pin it from the context inspector surface.
3. Edit a persona trait and one durable memory entry.
4. Expire the remaining temporary item or simulate time passing beyond its expiration threshold.
5. Verify:
   - promoted or pinned memory remains eligible with provenance intact
   - expired non-pinned memory drops out of active retrieval
   - persona edits remain separate from memory edits

## Validation Flow 4: Safe Context Summary

1. Create a memory item with private raw evidence and a shareable summary.
2. Trigger context retrieval for a request where the item is relevant.
3. Verify:
   - the workspace summary uses the safe summary text
   - the UI shows that some private context may be hidden
   - raw evidence is not exposed directly in the context window or inspector by default

## Notes

- This milestone should validate retrieval correctness and governance before advanced semantic retrieval work.
- Demo fixtures are acceptable for validation as long as the runtime still goes through the same repository, retrieval, and context-loader path used in production code.
- The context inspector should stay lightweight; it exists to make persona and scoped memory explainable, not to become a full knowledge-base UI.

## Validation Notes

- 2026-04-08: `:app:assembleDebug` completed successfully from `/Users/youxuezhe/StudioProjects/mobile_claw`.
- 2026-04-08: `:app:lintDebug --stacktrace` completed successfully. HTML report: `/Users/youxuezhe/StudioProjects/mobile_claw/app/build/reports/lint-results-debug.html`.
- Current implementation uses a `Room`-backed scoped memory store, a `Preferences DataStore` persona profile, a runtime-backed context loader, and a lightweight workspace context inspector with persona verbosity and memory lifecycle actions.
- The runtime now forwards assembled persona and active-memory context into the local generation prompt, and `LiteRtLocalChatGateway` uses a real LiteRT-LM backend for imported `.litertlm` models instead of fixture output.
- Built-in starter models are now honest placeholders; to validate end-to-end generation you should import a runtime-ready `.litertlm` model file and select it in the workspace.
