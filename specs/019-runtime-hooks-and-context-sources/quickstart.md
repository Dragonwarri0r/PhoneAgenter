# Quickstart: Runtime Hooks And Context Sources

## Goal

Validate that Mobile Claw now has one unified runtime contribution surface for lifecycle hooks and request-time context sources without collapsing that work into full knowledge-corpus or workflow management.

## Preconditions

- Build the app successfully
- Have the runtime control-center foundation from `018`
- Have extension and interop contracts from `016` and `017`
- Have at least one lifecycle-oriented contributor and one context-oriented contributor represented through the new runtime contribution language

## Manual Validation Scenarios

1. **Unified contributor registration**
   - Inspect covered contributor families
   - Confirm both lifecycle-oriented and context-oriented contributors share one registration language and availability model

2. **Current task visibility**
   - Trigger a request that receives request-time context or lifecycle influence from covered contributors
   - Confirm the active task flow shows concise contribution summaries and explains blocked or skipped contributors when relevant

3. **Contributor management**
   - Open a managed contributor entry from the existing control surfaces
   - Confirm supported contributors expose reversible availability state and clear limitation messaging

4. **Separation from later milestones**
   - Confirm no durable knowledge corpus management is required to validate contributor contracts
   - Confirm no workflow authoring or automation runner is required to validate contributor contracts

5. **Locale**
   - Run once in English and once in Simplified Chinese
   - Confirm contributor summaries, availability labels, and limitation messaging localize correctly

## Follow-up Notes

- This milestone is considered complete when runtime contributors are no longer hidden behind scattered callbacks or source-specific branches.
- A short `018.x` control-surface hardening pass may improve the session/control/detail presentation of contributors, but it should not redefine the contribution contracts introduced here.
- Full knowledge ingestion belongs to `020`, and workflow/DAG execution belongs to `021`.
- Validation note (2026-04-22): `./gradlew :app:assembleDebug :app:lintDebug` completed successfully after wiring unified contribution registrations, request-time context outcomes, task-flow summaries, reversible contributor availability toggles, and governance-facing trust/scope/privacy/policy explanations for managed contributors.
