# Quickstart: Local Runtime Session Pipeline

## Purpose

Use this guide to validate milestone `002` as the unified local runtime backbone behind the workspace.

## Preconditions

- Android project builds successfully under project root
- The `001` workspace is available as the request-entry UI
- The runtime session pipeline has replaced direct UI control of provider execution
- At least one local or mock generation provider is registered

## Validation Flow 1: Unified Session Creation

1. Launch the app and open the agent workspace.
2. Select a ready local model.
3. Submit a prompt.
4. Verify:
   - one execution session is created for the request
   - the session emits ordered non-terminal stage updates
   - the request completes through one terminal session outcome

## Validation Flow 2: Stage Visibility

1. Run a request through the runtime.
2. Confirm the context/status surface updates through stages such as:
   - loading context
   - planning
   - selecting capability
   - executing
   - completed or failed
3. Verify the surface remains compact and user-readable rather than log-like.

## Validation Flow 3: Provider Substitution Stability

1. Configure the runtime to use one mock or local provider.
2. Run a request and record the observed top-level session lifecycle.
3. Switch to another provider that satisfies the same contract.
4. Run the same class of request again.
5. Verify:
   - top-level session lifecycle remains unchanged
   - the final session contract is still stable
   - any provider differences stay behind provider-specific internals

## Validation Flow 4: Terminal Failure and Denial Handling

1. Run a request that triggers a provider failure or a denied outcome from a placeholder gate.
2. Verify:
   - the session stops in a terminal state
   - the compact status surface reflects the terminal state clearly
   - the runtime does not leave the session ambiguous or still marked active

## Notes

- This milestone may validate against local fixture providers before real Android capability bridges are introduced.
- The session pipeline should preserve explicit hooks for future memory, persona, policy, and capability routing even when they are no-op or placeholder implementations here.

## Validation Notes

- 2026-04-08: `:app:assembleDebug` completed successfully from `/Users/youxuezhe/StudioProjects/mobile_claw` using Gradle `8.10.2`.
- 2026-04-08: `:app:lintDebug` completed successfully. HTML report: `/Users/youxuezhe/StudioProjects/mobile_claw/app/build/reports/lint-results-debug.html`.
- Runtime provider substitution can be exercised by including `[mock]` in the submitted prompt, which routes generation through the mock provider while preserving the same top-level session lifecycle.
