# Research: Android Capability Bridge

## Decision 1: Model AppFunctions as an Adapter Boundary, Not a Compile-Time Requirement

**Decision**: Implement an `AppFunctionBridge` abstraction and treat actual Android AppFunctions framework integration as one adapter implementation rather than a hard compile-time dependency.

**Rationale**:

- The current project builds with `compileSdk = 35`, while AppFunctions targets newer Android platform APIs.
- The roadmap already treats AppFunctions as the preferred bridge rather than the runtime core itself.
- This allows `005` to ship a working AppFunctions-first routing contract now without blocking on SDK uplift.

**Alternatives considered**:

- **Wait to implement any AppFunctions path until the project moves to Android 16 APIs**: Rejected because it would block the milestone and delay bridge contract stabilization.
- **Fake AppFunctions by baking app-specific logic directly into the runtime**: Rejected because it would violate the adapter-based architecture principle.

## Decision 2: Introduce a Normalized Capability Registry Above Provider Execution

**Decision**: Add a new capability registry and router layer above the current provider registry.

**Rationale**:

- `004` already established policy and approval on the runtime path; `005` should decide *which Android bridge path* is eligible before handing off to execution.
- A registry layer can normalize capability metadata, availability, scopes, and provider preference order without rewriting individual providers.
- This keeps AppFunctions, Intent, and Share fallback logic out of the UI and orchestration edge cases.

**Alternatives considered**:

- **Reuse only the existing provider registry**: Rejected because the current provider registry only answers “which provider supports this plan” and lacks Android bridge metadata, caller trust, and fallback ordering.
- **Attach routing logic directly in `RuntimePlanner`**: Rejected because planner intent selection and Android provider routing should remain separate concerns.

## Decision 3: Verify Caller Trust Before Restricted Capability Routing

**Decision**: Add a caller verification layer that normalizes trusted internal callers first and leaves room for package/signature checks when external app entry points are added.

**Rationale**:

- `spec005` requires trusted versus untrusted caller behavior even before full external invocation is implemented.
- The current runtime requests already carry `originApp`, which can be normalized into a caller identity contract now.
- This allows `005` to enforce a real trust boundary while keeping `v0` single-user and local-first.

**Alternatives considered**:

- **Skip caller verification until external apps can invoke the runtime**: Rejected because the bridge would otherwise normalize restricted capability execution without any identity boundary.
- **Hardcode a single trusted path with no explicit model**: Rejected because it would undermine the contract and make future Android app entry points harder to add safely.

## Decision 4: Ship Ordered Fallback Routing Across AppFunctions, Intent, and Share

**Decision**: Support ordered provider preferences across `APP_FUNCTIONS -> INTENT -> SHARE`, with Accessibility reserved but not executable in this milestone.

**Rationale**:

- This matches the roadmap’s Android integration priority.
- Intent and Share can be represented today without prematurely implementing Accessibility automation.
- It gives `005` a meaningful fallback path even while actual AppFunctions availability may be simulated through seeded descriptors.

**Alternatives considered**:

- **Implement AppFunctions only**: Rejected because the spec explicitly requires graceful fallback when the preferred bridge is unavailable.
- **Add Accessibility now as an executable fallback**: Rejected because the roadmap reserves it as the last resort and current milestone scope should stay bounded.

## Decision 5: Route Bridge Outcomes Back Through the Existing Runtime and Policy Contract

**Decision**: Return normalized invocation results and route explanations back through the current runtime session and workspace status surfaces.

**Rationale**:

- The user should see one consistent runtime explanation path whether the bridge path was AppFunctions, Intent, or Share.
- `004` already added policy/audit explanation infrastructure that `005` should reuse.
- This keeps future provider changes from leaking Android-specific complexity into the UI contract.

**Alternatives considered**:

- **Expose raw Android bridge details directly to UI**: Rejected because it would make the runtime contract unstable.
- **Hide route selection entirely from the user**: Rejected because provider choice and fallback behavior are important for explainability and debugging.
