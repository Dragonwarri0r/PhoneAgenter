# Quickstart: Workflow Graph And Automation

## Goal

Validate that Mobile Claw can now define durable local workflows, run them with resumable state and approval-aware pauses, and manage automation summaries through the existing control surfaces without requiring a heavy graph editor or remote orchestration.

## Preconditions

- Build the app successfully
- Have runtime contribution contracts from `019`
- Have knowledge-layer support from `020` where needed
- Have an Automation area in the control-center model that can surface workflow definitions and runs

## Manual Validation Scenarios

1. **Workflow definition**
   - Create or save a supported first-slice workflow definition
   - Confirm the workflow appears as a durable managed automation object with trigger or entry summary and current availability state

2. **Approval-gated or resumable run**
   - Trigger a workflow run that pauses for approval, interruption, or similar guarded state
   - Confirm the run preserves checkpoint state and next-step guidance instead of failing opaquely

3. **Automation management**
   - Open workflow definitions and workflow runs from the Automation area
   - Confirm summaries, availability state, run history, and supported reversible actions are understandable

4. **Run outcome visibility**
   - Complete, fail, or cancel a workflow run
   - Confirm recent activity and detail views show a traceable run outcome with recovery guidance

5. **Locale**
   - Run once in English and once in Simplified Chinese
   - Confirm workflow, checkpoint, run-state, and recovery wording localize correctly

## Follow-up Notes

- This milestone is considered complete when multi-step local work no longer depends on manually replaying prompt sequences and when paused or blocked runs remain understandable.
- A heavy visual graph editor, remote orchestration, distributed execution, and marketplace flow sharing remain outside this first automation slice.
- Workflow management should stay inside the Automation area and object detail model rather than becoming a separate console.
- Validation completed with:
  - `./gradlew :app:compileDebugKotlin`
  - `./gradlew :app:lintDebug`
  - `./gradlew :app:assembleDebug`
- First-slice authoring is template-based rather than freeform graph editing:
  - `Knowledge briefing loop`
  - `Follow-up loop`
- Active workflow state is surfaced in three places:
  - workspace bottom banner for task-relevant runs
  - runtime control center Automation artifact and trace section
  - Automation center list/detail sheet with definition and run history
