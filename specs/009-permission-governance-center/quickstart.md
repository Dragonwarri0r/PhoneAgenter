# Quickstart: Permission Governance Center

## Purpose

Validate milestone `009` as the first user-manageable governance layer above the current policy and caller-verification stack.

## Preconditions

- `004`, `005`, `007`, and `008` are already implemented and working
- The workspace can already show runtime status, approval prompts, and recent audit
- External handoff and capability routing can produce trusted, unverified, and denied caller states

## Validation Flow 1: Review Governance State

1. Trigger a few runtime requests, including at least one approval-required request.
2. Open the governance center.
3. Verify:
   - recent callers appear
   - trust mode is visible
   - recent approval/denial activity is visible

## Validation Flow 2: Change Caller Trust

1. Pick a caller shown in the governance center.
2. Change its trust mode.
3. Verify:
   - the change persists after reopening the sheet
   - the governance center reflects the updated trust state

## Validation Flow 3: Change Scope Grant

1. Pick a caller shown in the governance center.
2. Disable one supported restricted scope, such as `message.send`.
3. Verify:
   - the scope grant persists
   - the governance center shows the overridden scope state

## Validation Flow 4: Enforce Governance Override

1. Configure a caller or scope denial.
2. Submit a matching request.
3. Verify:
   - runtime denies or downgrades the request because of governance
   - the user-visible explanation references the governance-derived restriction

## Notes

- `009` is complete when governance becomes both visible and behavior-changing.
- This milestone is not a full settings architecture; it is the first usable governance center.
- Validation completed on 2026-04-09 with:
  - `./gradlew :app:compileDebugKotlin`
  - `./gradlew :app:assembleDebug`
  - `./gradlew :app:lintDebug`
- Implementation notes:
  - The workspace now exposes a governance center sheet that shows recent callers and recent governance-related activity.
  - Caller trust mode and per-scope grant changes persist locally in Room-backed governance tables.
  - Caller verification and routing now consult governance overrides before allowing restricted capability execution.
