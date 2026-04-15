# Quickstart: Android Capability Bridge

## Purpose

Use this guide to validate milestone `005` as the first Android capability bridge over the existing runtime and policy stack.

## Preconditions

- `001` through `004` are already implemented and working
- The runtime can execute low-risk and confirmable flows through the current provider pipeline
- The workspace shows runtime status and recent audit details

## Validation Flow 1: AppFunctions-First Routing

1. Open the workspace with a ready local model.
2. Submit a request that maps to a capability with a seeded AppFunctions-style provider, for example `Summarize today and keep it concise.`
3. Verify:
   - the capability registry resolves the requested capability
   - routing prefers the AppFunctions-style provider
   - execution result returns through the normal runtime contract
   - the route explanation is visible in the workspace status or audit surface

## Validation Flow 2: Ordered Fallback Routing

1. Submit a request that maps to a capability whose AppFunctions route is unavailable but an approved fallback exists, for example:
   - `[fallback] Summarize today and keep it concise.`
   - `[noappfunc] [message] Send a message to Alice saying I will be late.`
2. Verify:
   - the runtime does not pretend the AppFunctions provider executed
   - the router chooses the next eligible fallback
   - the route explanation indicates why fallback was used
   - `message.send`, `calendar.write`, or `external.share` can open a real Android target activity instead of only returning mock output

## Validation Flow 3: No Eligible Provider

1. Submit a request for a capability with no eligible provider, for example:
   - `[noprovider] [share] Post this summary to the team channel.`
2. Verify:
   - the runtime fails cleanly
   - the user sees a normalized failure reason
   - the session does not claim the action succeeded

## Validation Flow 4: Caller Trust Enforcement

1. Submit one request from the trusted workspace caller.
2. Submit another request with an explicitly untrusted origin marker, for example:
   - `[untrusted] [message] Send a message to Alice saying I will be late.`
3. Verify:
   - the trusted request reaches routing and policy evaluation
   - the untrusted request is denied before restricted capability execution
   - the denial reason is visible and auditable
4. If you can invoke the runtime with a real package name as `originApp`, verify:
   - installed packages are inspected through Android package metadata
   - restricted capabilities only proceed when package verification and signing checks succeed

## Notes

- `005` keeps AppFunctions as a seeded adapter boundary while the project remains on SDK 35.
- Intent and share fallback execution should now use real Android activity launches where the device has a compatible target app.

## Validation Notes

- `2026-04-08`: `./gradlew :app:compileDebugKotlin`, `./gradlew :app:assembleDebug`, and `./gradlew :app:lintDebug` all passed.
- `2026-04-08`: The current implementation uses seeded AppFunctions-style, Intent fallback, and Share fallback descriptors to stabilize the bridge contract while staying compatible with SDK 35.
- `2026-04-08`: Caller trust can be exercised from the workspace using the `[untrusted]` marker, and fallback/no-provider flows can be exercised with `[fallback]`, `[noappfunc]`, and `[noprovider]`.
- `2026-04-08`: Intent/share fallback discovery now queries real device handlers through `PackageManager`, and fallback execution launches Android activities with `startActivity(...)`.
- `2026-04-08`: Restricted caller verification now checks installed package presence and SHA-256 signing digest matches for package-name callers.
