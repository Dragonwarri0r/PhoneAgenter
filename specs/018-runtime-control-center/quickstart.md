# Quickstart: Runtime Control Center

## Goal

Validate that Mobile Claw now has one coherent runtime control surface reachable from the conversation flow.

## Preconditions

- Build the app successfully
- Have current workspace IA from `013`
- Have multimodal composer from `014`
- Have governance, interop, and extension contracts from `009`, `016`, and `017`

## Manual Validation Scenarios

1. **Conversation-first entry**
   - Open the app
   - Send a text request and a media-backed request
   - Confirm chat remains primary and the control center is reachable from the same flow

2. **Coherent runtime trace**
   - Trigger a normal request, an approval-gated request, and an external handoff request
   - Confirm one control surface shows source, tool path, approval, context, and extension signals together

3. **Managed artifact entries**
   - Open memory, governance, and extension-related entries from the control center
   - Confirm each entry clearly shows whether it is inspectable, editable, or unavailable

4. **Editing continuity**
   - Modify at least one supported memory/governance artifact through the control flow
   - Confirm the change persists and the base conversation remains intact

5. **Locale**
   - Run once in English and once in Simplified Chinese
   - Confirm control-center labels, trace summaries, and limitation messages localize correctly

## Follow-up Notes

- This milestone is considered complete when the app reads like one coherent runtime control surface instead of several disconnected management sheets.
- Device-side validation is strongly recommended because sheet behavior, IME interplay, and multitasking transitions affect the conversation-first experience.
- Build validation completed on 2026-04-13 with:
  - `./gradlew :app:compileDebugKotlin`
  - `./gradlew :app:assembleDebug`
  - `./gradlew :app:lintDebug`
- Current implementation keeps chat primary, replaces the old generic details entry with a runtime control-center sheet, and routes supported artifact actions back into the existing model picker, context inspector, and governance flows.
- Approval and extension state are now readable from the control center, but extension editing remains inspect-only in this slice and should be revisited in a later management milestone.
