# Quickstart: Unified Extension Surface

## Goal

Validate that multiple extension families can be registered, inspected, and compatibility-checked through one shared extension model.

## Preconditions

- Build the app successfully
- Have existing portability-oriented extension hooks from `006`
- Have tool standardization direction from `015`
- Have a seeded set of at least several extension registrations available for validation

## Manual Validation Scenarios

1. **Unified registration**
   - Inspect registered extensions across at least several extension families
   - Confirm they all use the same registration shape

2. **Compatibility**
   - Validate at least one compatible and one incompatible or degraded registration
   - Confirm missing fields, runtime-version issues, or trust requirements are explained

3. **Enablement**
   - Inspect default-enabled and default-disabled registrations
   - Confirm the runtime can distinguish active, disabled, degraded, and incompatible states

4. **Discovery summaries**
   - Open the extension inspection path
   - Confirm each extension exposes type, contribution summary, privacy guarantee, and status summary

5. **Locale**
   - Run once in English and once in Simplified Chinese
   - Confirm extension labels and compatibility explanations localize correctly

## Follow-up Notes

- This milestone is considered complete when `006` portability hooks and at least several broader extension families can be represented through one shared extension model.
- Device-side inspection is recommended if extension state is surfaced in the workspace UI.
- Implementation validation completed with:
  - `./gradlew :app:compileDebugKotlin`
  - `./gradlew :app:assembleDebug`
  - `./gradlew :app:lintDebug`
- The current implementation now exposes a shared seeded extension registry for ingress, tool provider, context source, export, import, and sync transport families, and surfaces compact discovery summaries in the workspace.
