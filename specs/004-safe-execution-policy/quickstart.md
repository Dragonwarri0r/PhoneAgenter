# Quickstart: Safe Execution Policy and Approval Flow

## Purpose

Use this guide to validate milestone `004` as the first real safety and approval layer for the local runtime.

## Preconditions

- The Android app from `001`, runtime pipeline from `002`, and persona-memory context layer from `003` are already working
- The runtime can submit requests through the session pipeline and execute at least one generation provider
- The app supports English and Simplified Chinese resource selection based on the device language

## Validation Flow 1: Auto-Execute Low-Risk Actions

1. Open the workspace with a ready local model.
2. Submit a normal low-risk request such as `Summarize today and keep it concise.`.
3. Verify:
   - the runtime classifies the request and records a policy decision
   - no manual approval prompt appears
   - execution proceeds automatically
   - an audit trail exists for classification, policy, and execution completion

## Validation Flow 2: Confirm High-Risk Actions

1. Submit a request mapped to a high-risk or hard-confirm scope, for example:
   - `[message] Send a message to Alice saying I will be late.`
   - `Send a message to Alice saying I will be late.`
2. Verify:
   - the runtime pauses before provider execution
   - an approval request appears with a preview and explanation
   - approving resumes execution
   - rejecting or dismissing prevents execution

## Validation Flow 3: Enforce Hard Rules

1. Submit a request that targets a hard-confirm or blocked scope, for example:
   - `[calendar] Reschedule tomorrow's meeting to 3 PM.`
   - `[share] Post this summary to the team channel.`
   - `[ui] Tap submit on the current form.`
   - `[blocked] Transfer money to the new payee.`
2. Verify:
   - hard-confirm actions never auto-execute even if the classifier is optimistic
   - blocked actions end with denial
   - the user can understand why the request did not continue

## Validation Flow 4: Audit and Explainability

1. Run one low-risk request and one confirmable request.
2. Verify:
   - both produce structured audit events
   - the workspace can surface user-safe explanations for why the action executed, paused, or failed
   - approval outcomes are preserved in audit records

## Validation Flow 5: English and Chinese Localization

1. Run the app with an English device locale.
2. Verify:
   - workspace, approval, model, and runtime status text appear in English
3. Switch the device locale to Simplified Chinese.
4. Verify:
   - the same user-facing surfaces now appear in Chinese
   - no separate in-app language toggle is required
   - English fallback remains available for any untranslated text

## Notes

- `004` should validate safety correctness and user comprehension before introducing more capable cross-app providers.
- Approval UI may stay lightweight in this milestone as long as it is clear and auditable.
- Localization coverage should prioritize all user-visible execution, approval, and failure messaging first.

## Validation Notes

- `2026-04-08`: `./gradlew :app:compileDebugKotlin`, `./gradlew :app:assembleDebug`, and `./gradlew :app:lintDebug` all passed.
- `2026-04-08`: The current policy prototype uses normalized keyword and marker matching to exercise low-risk, hard-confirm, and blocked flows before real cross-app capability providers land in `005`.
- `2026-04-08`: Manual device validation is still recommended for locale switching and approval interaction timing, but the runtime, persistence, audit, and localized approval surfaces are now wired end to end.
