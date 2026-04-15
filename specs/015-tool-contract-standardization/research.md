# Research: Tool Contract Standardization

## Decision 1: Evolve the existing capability registry into a richer tool descriptor layer

- **Decision**: Build standardized tool contracts by enriching the current capability registration path instead of creating a separate parallel registry.
- **Why**: The project already has `CapabilityRegistration`, `ProviderDescriptor`, governance, routing, and approval flows. Reusing that backbone keeps `015` large enough to matter without turning it into a rewrite.
- **Alternatives considered**:
  - Create a brand-new tool system next to capability routing: rejected because it would fragment runtime semantics and duplicate bridge/provider logic.
  - Keep adding per-capability hardcoded metadata: rejected because that is exactly the scaling problem `015` is meant to solve.

## Decision 2: Use JSON-schema-first descriptor fields with typed preview models

- **Decision**: Represent tool contracts with schema-bearing descriptors, while runtime preview and UI continue to use typed Kotlin models derived from those descriptors.
- **Why**: JSON-schema-style contracts align with the project's MCP-style direction and future interop goals, but current runtime/UI code still benefits from typed preview models for safety and readability.
- **Alternatives considered**:
  - Purely typed per-tool models without a generic schema layer: rejected because it would not create a portable contract surface.
  - Raw schema objects only: rejected because it would make the current Android runtime/UI path heavier than necessary.

## Decision 3: Normalize side effects into read, write, and dispatch families

- **Decision**: Standardized tools explicitly declare whether they are `read`, `write`, or `dispatch`.
- **Why**: Existing behavior distinguishes low-risk reply/read behaviors from higher-risk actions, but the distinction is not normalized well enough for scalability. This split is simple, explainable, and adequate for the first standardized catalog.
- **Alternatives considered**:
  - Keep only coarse risk labels like low/high: rejected because risk alone does not explain the type of effect to the user.
  - Introduce a much larger taxonomy immediately: rejected because it would add detail without improving the first product slice.

## Decision 4: Tool visibility must be resolved on demand

- **Decision**: Covered tools are surfaced only when current request intent, governance state, provider availability, and policy support them.
- **Why**: This protects the `013` information architecture and follows the roadmap principle that capability growth should not make the workspace feel like a control surface.
- **Alternatives considered**:
  - Permanent tool shelves: rejected because they over-expose unavailable actions and add noise.
  - Hide all tool state until execution time: rejected because users still need preview and explainability before write/dispatch actions.

## Decision 5: First standardized tool catalog covers productivity actions only

- **Decision**: The first catalog includes `generate.reply`, `calendar.read`, `calendar.write`, `alarm.set`, `alarm.show`, `alarm.dismiss`, `message.send`, and `share.outbound`.
- **Why**: These actions are already implied by the roadmap and represent a meaningful mix of read, write, and dispatch semantics.
- **Alternatives considered**:
  - Standardize every current and future capability now: rejected because that would turn `015` into an umbrella spec.
  - Standardize only one or two tools: rejected because it would not create enough product or architectural change to justify the spec.
