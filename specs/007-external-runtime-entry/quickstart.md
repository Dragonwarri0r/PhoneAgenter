# Quickstart: Trusted External Handoff Entry

## Purpose

Use this guide to validate milestone `007` as the first real external Android handoff into the existing Mobile Claw runtime.

## Preconditions

- `001` through `006` are already implemented and working
- The workspace can submit internal requests through the current runtime, policy, capability, and audit pipeline
- The device or emulator can launch Mobile Claw from an Android share or explicit send intent

## Validation Flow 1: Receive Text From Another App

1. Open any Android app that can share plain text, such as a browser, notes app, or messaging app draft.
2. Share a text snippet or URL to Mobile Claw using the Android Sharesheet.
3. Verify:
   - Mobile Claw appears as a visible share target for plain text
   - Mobile Claw opens and lands on the workspace
   - the incoming content appears as a new or resumed agent session input
   - source attribution is visible in runtime status, context, or audit surfaces

## Validation Flow 2: Normalize the External Request Into Runtime

1. Repeat the external handoff with supported text content.
2. Compare the resulting runtime behavior against a similar request typed directly in the workspace.
3. Verify:
   - both flows enter the same runtime pipeline after normalization
   - the external handoff carries different source metadata, but the same canonical request shape reaches planning and execution
   - Android action and MIME details do not appear inside downstream runtime planning or capability contracts

## Validation Flow 3: Reject Unsupported or Malformed Handoffs Safely

1. Trigger an unsupported or malformed entry, for example:
   - a send intent with missing `EXTRA_TEXT`
   - an unsupported MIME type
   - an inbound request with ambiguous or incomplete metadata that cannot be normalized safely
2. Verify:
   - the runtime does not start from ambiguous or malformed input
   - the user sees a safe failure or denial message
   - the failure is auditable

## Validation Flow 4: Show Source and Trust Outcome

1. Submit one external handoff with best-available source metadata.
2. Submit another handoff where the source package cannot be confidently identified, if you can reproduce that path on device or emulator.
3. Verify:
   - the accepted handoff shows source and trust outcome
   - unverified or denied caller states are visible and do not silently upgrade restricted execution
   - restricted capability paths still depend on the current trust and policy boundary from `004` and `005`

## Optional ADB Flow

If you want a deterministic validation path, use `adb`:

```bash
adb shell am start \
  -a android.intent.action.SEND \
  -t text/plain \
  --es android.intent.extra.TEXT "Summarize this link for me and keep it short." \
  com.mobileclaw.app
```

Then verify that Mobile Claw opens, normalizes the inbound content, and surfaces the external source as a visible session.

## Notes

- `007` is intentionally text-first; it does not yet support structured action payload extraction.
- Caller package identity is best-effort for this milestone; missing identity must degrade safely rather than silently expanding trust.
- This milestone is complete when Mobile Claw behaves like a real external share target without forking the runtime contract.

## Validation Notes

- `2026-04-09`: `./gradlew :app:compileDebugKotlin` passed after the external ingress, source metadata, and workspace landing flow were wired in.
- `2026-04-09`: `./gradlew :app:assembleDebug` passed with the new `ACTION_SEND` `text/plain` manifest entry and ingress services enabled.
- `2026-04-09`: `./gradlew :app:lintDebug` passed and wrote the HTML report to `/Users/youxuezhe/StudioProjects/mobile_claw/app/build/reports/lint-results-debug.html`.
- `2026-04-09`: This validation round covered compile/build/lint and static integration checks; a full device-side Android Sharesheet walkthrough and `adb am start` flow still need to be exercised manually.
