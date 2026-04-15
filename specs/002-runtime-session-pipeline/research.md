# Research: Local Runtime Session Pipeline

## Decision 1: Implement `002` Directly Inside the Existing Android App

**Decision**: Build the runtime session pipeline inside `app` and use it to replace the current direct `AgentWorkspaceViewModel -> LocalChatGateway` path.

**Rationale**:

- `001` already established the Android app shell and workspace interaction loop.
- The next risk is contract shape, not module packaging.
- Integrating directly into the app ensures the session pipeline is exercised by a real UI path immediately.
- A later extraction into a library or deeper runtime module remains possible once the contract is stable.

**Alternatives considered**:

- **Create a standalone pure-Kotlin runtime module now**: Rejected because it adds separation cost before the orchestration contract has been validated in the app.
- **Leave `001` untouched and build a parallel runtime path**: Rejected because it would preserve two overlapping request-entry contracts.

## Decision 2: Model the Runtime Around a Stable `ExecutionSession` Contract

**Decision**: Represent every accepted request as an `ExecutionSession` with ordered stages, structured updates, and one terminal `SessionOutcome`.

**Rationale**:

- The spec requires one consistent lifecycle for both generation-only and provider-backed requests.
- A top-level execution session creates a stable contract for UI, future audit, policy, and capability layers.
- It also prevents provider-specific details from leaking into the UI or request entry APIs.

**Alternatives considered**:

- **Continue using provider-specific event contracts**: Rejected because it would keep the runtime opaque and tightly coupled to each provider.
- **Use only a final result callback without stages**: Rejected because later approval and explainability need intermediate visibility.

## Decision 3: Treat Planning, Memory, Persona, and Policy as Explicit Hooks in `002`

**Decision**: Add explicit orchestration stages and extension hooks for context loading, planning, policy gating, and capability routing, but do not implement their full logic in this milestone.

**Rationale**:

- `002` should define the runtime backbone without prematurely swallowing the scope of `003`, `004`, and `005`.
- Explicit hooks preserve future compatibility and avoid redesigning the session contract later.
- Placeholder implementations allow current UI and tests to validate lifecycle sequencing now.

**Alternatives considered**:

- **Skip these stages until later milestones**: Rejected because the runtime contract would then need a breaking redesign when those features arrive.
- **Implement real memory/policy now**: Rejected because it would collapse multiple milestone boundaries and slow validation.

## Decision 4: Use Replaceable Local/Mock Providers First

**Decision**: Introduce a provider registry and a normalized provider interface, then validate `002` with local or mock providers before real cross-app capability bridges land.

**Rationale**:

- The runtime contract must be stable before `005` connects Android capability providers.
- Mock/local providers are enough to test session lifecycle, stage ordering, substitution, and outcome handling.
- The current local generation path from `001` can be wrapped as the first provider implementation.

**Alternatives considered**:

- **Tie `002` directly to AppFunctions now**: Rejected because that belongs to the Android bridge milestone and would prematurely hard-wire provider behavior.
- **Keep only one hard-coded provider**: Rejected because the feature explicitly requires stable contracts across provider changes.

## Decision 5: Expose a Compact `RuntimeStatusSummary` for UI Consumption

**Decision**: Emit both detailed ordered session events and a compact derived status model that the workspace can render in its context/status surface.

**Rationale**:

- The spec calls out a compact user-facing status window that should not depend on raw logs.
- Session stages may change quickly; the UI needs a reduced summary that remains understandable.
- This makes `002` directly useful to the `001` workspace rather than only future audit features.

**Alternatives considered**:

- **Let the UI derive everything from low-level events**: Rejected because it spreads orchestration knowledge into presentation code.
- **Expose only one coarse in-progress flag**: Rejected because it is too weak for approval, execution, and denial states.

