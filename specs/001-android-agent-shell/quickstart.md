# Quickstart: Android Agent Shell and Local Model Workspace

## Purpose

Use this guide to validate milestone `001` as a usable Android agent workspace.

## Preconditions

- Android project is scaffolded under project root
- App launches on a supported Android emulator or device
- At least one local model profile is available to the workspace
- For early bring-up only, a development fixture may stand in for the local model backend
- Final milestone validation should use a real local-model-backed workspace path

## Validation Flow 1: Start a Local Agent Session

1. Launch the app and enter the agent workspace.
2. Confirm the workspace shows the expected major regions:
   - workspace header
   - model health surface
   - context window surface
   - conversation layer
   - composer dock
3. Select a ready local model if one is not already active.
4. Enter a short prompt and press send.
5. Verify:
   - the user turn appears immediately
   - the workspace enters an in-progress state
   - the assistant turn streams into the conversation layer
   - duplicate send is guarded while generation is active

## Validation Flow 2: Model Readiness and Recovery

1. Open the model picker or switch the selected model into non-ready states.
2. Validate each visible state:
   - unavailable
   - preparing
   - ready
   - failed
3. Verify the model health surface explains the state and suggests the next useful action.
4. Verify the workspace does not present the composer as sendable when the model is not ready.

## Validation Flow 2a: Model Import

1. Open the model picker from the workspace.
2. Choose the import action and select a compatible local model file.
3. Verify:
   - the workspace surfaces import progress feedback
   - a successful import updates the selected model and model health surface
   - a failed import surfaces a user-readable error instead of silent failure

## Validation Flow 3: Session Visibility and Reset

1. Send multiple prompts and confirm the transcript preserves turn order.
2. Confirm the context/status surface updates while the session is active.
3. Trigger session reset.
4. Verify:
   - the reset asks for confirmation
   - the transcript clears after confirmation
   - the workspace returns to a fresh state
   - a lightweight success or failure feedback message is shown

## Design Validation

Check the implemented screen against the intended workspace feel:

- no dense divider-line layout
- tonal surface separation and whitespace are doing most of the sectioning work
- assistant bubbles feel lighter and more atmospheric than user bubbles
- controls use large-radius approachable surfaces
- transient success and error feedback are visible without exposing raw logs

Reference inputs:

- [spec.md](./spec.md)
- [/Users/youxuezhe/StudioProjects/mobile_claw/DESIGN.md](/Users/youxuezhe/StudioProjects/mobile_claw/DESIGN.md)
- `/Users/youxuezhe/StudioProjects/mobile_claw/screen.png`

## Validation Notes

- 2026-04-08: `:app:assembleDebug` completed successfully from `/Users/youxuezhe/StudioProjects/mobile_claw` using Gradle `8.10.2`.
- 2026-04-08: `:app:lintDebug` completed successfully. HTML report: `/Users/youxuezhe/StudioProjects/mobile_claw/app/build/reports/lint-results-debug.html`.
- Current implementation uses a fixture-backed `LiteRtLocalChatGateway` so the workspace loop, model states, reset flow, and feedback surfaces can be exercised before the fuller runtime pipeline from spec `002`.
- Recommended next validation step: launch on an Android 12+ emulator or device and walk through the three validation flows above with the fixture models.
