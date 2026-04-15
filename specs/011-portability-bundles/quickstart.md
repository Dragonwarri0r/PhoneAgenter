# Quickstart: Portability Bundles

## Goal

Verify that export metadata from `006` has become a real user-facing portability experience.

## Preconditions

- Build the app successfully
- Launch the workspace with seeded memory available
- Ensure at least one memory item is `SHAREABLE_SUMMARY` or `SHAREABLE_FULL`

## Walkthrough

1. Open the workspace.
2. Open the context inspector.
3. Choose an exportable memory record and open its portability preview.
4. Verify the preview shows:
   - export mode
   - payload preview
   - included fields
   - redacted fields
   - compatibility lines
5. If the record allows full export, switch modes and verify the preview changes.
6. Share the bundle and confirm Android share dispatch opens with formatted bundle text.
7. Attempt export for a private record and verify the app refuses clearly.

## Validation Notes

- Summary-only records must not reveal full content or raw evidence.
- Full export must only be available for `SHAREABLE_FULL`.
- Compatibility messaging should stay explanatory, not promise actual import.
- English and Simplified Chinese wording should follow device locale.

## Implementation Validation

- `./gradlew :app:compileDebugKotlin` passed
- `./gradlew :app:assembleDebug` passed
- `./gradlew :app:lintDebug` passed
- Lint report: `/Users/youxuezhe/StudioProjects/mobile_claw/app/build/reports/lint-results-debug.html`
