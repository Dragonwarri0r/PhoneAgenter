# Research: Capability Inference and Read Tools

## Decision 1: Replace workspace hard-default reply behavior with conservative capability selection

- **Decision**: Introduce a request-scoped capability-selection step for freeform workspace input instead of always forcing direct workspace requests into reply generation.
- **Rationale**: The current behavior blocks explicit read capabilities from ever becoming reachable through the main conversation surface. A conservative selection layer allows clear low-risk read intents to route into real tools while preserving benign reply fallback for ambiguous prompts.
- **Alternatives considered**:
  - Keep the current reply-only default and add calendar-specific shortcuts: rejected because it would not scale beyond one-off cases.
  - Infer capabilities only after provider routing: rejected because the workspace needs a user-visible explanation before execution.
  - Treat all inferred capabilities equally: rejected because read, write, and dispatch actions require different safety behavior.

## Decision 2: Model explicit read tools separately from passive system-context ingestion

- **Decision**: Keep passive system-source ingestion as contextual enrichment, but define explicit read-tool execution as a separate runtime path with its own identity, preview, route explanation, and result contract.
- **Rationale**: Passive context ingestion and explicit user-requested lookup have different trust, visibility, and privacy expectations. Users should be able to tell when the runtime is enriching context versus when it is performing a direct lookup on their behalf.
- **Alternatives considered**:
  - Continue treating system-source ingestion as the read path: rejected because it hides execution and limits previewability.
  - Merge explicit read lookup into `calendar.write`: rejected because read and write semantics must remain distinct.
  - Bypass the tool contract for read capabilities: rejected because it would fragment routing, audit, and extension behavior.

## Decision 3: Introduce a unified read-provider discovery surface inside the existing capability bridge

- **Decision**: Extend the existing capability bridge so provider discovery can represent explicit read providers through the same registration family as AppFunctions, intent fallbacks, and future external-app providers.
- **Rationale**: The repository already has the right architectural spine: tool descriptors, provider descriptors, approval/audit integration, and unified extension registration. Adding a separate read-only registry would undo the value of that standardization.
- **Alternatives considered**:
  - Hardcode a special calendar provider directly in capability routing: rejected because it would duplicate the same anti-pattern the extension surface was meant to remove.
  - Build a separate read-capability subsystem: rejected because future app capabilities need one consistent discovery and explanation path.
  - Treat read providers as hidden internal helpers instead of registered capabilities: rejected because that would make availability and route reasoning inconsistent.

## Decision 4: Use `calendar.read` as the first complete explicit read capability

- **Decision**: Deliver `calendar.read` as the first fully executable read capability, with bounded query semantics and conversationally sized results.
- **Rationale**: Calendar lookup is already close to the runtime because the app has permission visibility, passive ingestion, and adjacent scheduling flows. Completing it first proves the read-tool abstraction without needing a brand-new domain model.
- **Alternatives considered**:
  - Start with contacts lookup instead: rejected because calendar requests are the clearest immediate product gap in workspace behavior.
  - Start with a generic read-provider API before any concrete capability: rejected because the abstraction would be harder to validate without a real capability.
  - Expand to multiple read capabilities in the same slice: rejected because the first milestone should validate one full path end-to-end.

## Decision 5: Surface capability-selection reasoning as part of the normal workspace runtime status

- **Decision**: The selected capability, fallback reason, and provider route should be visible in the same runtime status path already used for planning, approval, and execution.
- **Rationale**: The workspace already has a user-visible route explanation surface. Reusing it keeps read-tool behavior honest and makes freeform inference feel intentional rather than magical.
- **Alternatives considered**:
  - Hide inference and only show final answers: rejected because users need to understand why the runtime chose a tool path.
  - Create a separate inference-only debug panel: rejected because users need explanation in the normal flow, not in a secondary engineering surface.
  - Explain only failures and not successful selections: rejected because trust depends on understanding both cases.
