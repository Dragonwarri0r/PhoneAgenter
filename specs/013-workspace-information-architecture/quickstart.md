# Quickstart: Workspace Information Architecture

## Goal

Validate that the workspace is conversation-first, keeps key runtime state visible, and uses progressive disclosure for deeper capability surfaces.

## Preconditions

- Build the app successfully
- Launch the workspace with at least one importable local model available
- Have at least one active session and one external handoff scenario available

## Manual Validation Scenarios

1. **Idle conversation-first layout**
   - Open the workspace in a ready state
   - Confirm the transcript is the dominant visual surface
   - Confirm secondary status is shown through a compact digest rather than multiple large top cards

2. **Streaming state**
   - Send a prompt and let the assistant stream
   - Confirm the digest updates stage/headline without displacing the transcript
   - Confirm the composer remains anchored and usable after streaming completes

3. **Approval emphasis**
   - Trigger a high-risk action that requires approval
   - Confirm approval is visually emphasized
   - Confirm the transcript remains visible behind the approval flow

4. **Recoverable failure emphasis**
   - Trigger a recoverable failure path
   - Confirm the failure signal is visible and understandable
   - Confirm the workspace still keeps conversation and next actions legible

5. **IME compaction**
   - Focus the composer to show the keyboard
   - Confirm secondary chrome compacts before the conversation/composer become cramped

6. **Progressive disclosure**
   - Open model picker, context inspector, governance center, and portability preview from the compact workspace
   - Dismiss each surface
   - Confirm the base workspace returns to a compact layout

7. **Locale**
   - Run once in English and once in Simplified Chinese
   - Confirm the new digest/entry labels localize correctly

## Validation Notes

- `./gradlew :app:compileDebugKotlin` passed
- `./gradlew :app:assembleDebug` passed
- `./gradlew :app:lintDebug` passed
- Lint report: `/Users/youxuezhe/StudioProjects/mobile_claw/app/build/reports/lint-results-debug.html`

## Follow-Up

- A manual device walkthrough is still recommended for keyboard compaction feel, digest readability during streaming, and the discoverability of the new secondary-entry rail.
