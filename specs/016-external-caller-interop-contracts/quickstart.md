# Quickstart: External Caller Interop Contracts

## Goal

Validate that multiple external entry styles normalize into the same canonical caller contract, trust semantics, and URI grant explainability model.

## Preconditions

- Build the app successfully
- Have existing external share entry support from `007`
- Have governance and tool standardization flows available from `009` and `015`
- Have one external text share source and one external media share source available for manual validation

## Manual Validation Scenarios

1. **Canonical inbound normalization**
   - Trigger a text share handoff
   - Trigger a media share handoff
   - Confirm both produce the same canonical caller/source/trust structure before runtime execution

2. **Shared caller semantics**
   - Use two entry styles from the same external app if possible
   - Confirm source label, package identity, trust state, and trust reason stay consistent

3. **URI grant explainability**
   - Share media that requires cross-app URI access
   - Confirm the runtime preserves an explainable grant summary instead of dropping it

4. **Future callable compatibility**
   - Validate that a structured external request can be described against the callable surface descriptor
   - Confirm it maps cleanly into canonical runtime request fields and scope evaluation

5. **Locale**
   - Run once in English and once in Simplified Chinese
   - Confirm caller/trust/grant wording localizes correctly

## Follow-up Notes

- This milestone is considered complete when external caller handling no longer depends on entry-specific trust semantics for covered flows.
- Device-side validation is still recommended because URI grant behavior depends on real Android caller flows.
- Implementation validation completed with:
  - `./gradlew :app:compileDebugKotlin`
  - `./gradlew :app:assembleDebug`
  - `./gradlew :app:lintDebug`
- The current implementation now routes text share and media share through one `InteropRequestEnvelope`, preserves URI grant summaries in runtime source metadata, and includes a callable-surface mapper for future structured callers.
