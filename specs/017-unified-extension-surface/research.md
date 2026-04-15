# Research: Unified Extension Surface

## Decision 1: Evolve `006` registrations instead of creating a separate extension runtime

- **Decision**: Build the unified extension surface by expanding the existing `ExtensionRegistration` lineage from `006`.
- **Why**: The project already has seeded portability-oriented registrations and compatibility concepts. Reusing that foundation keeps `017` meaningful without forcing a clean-slate rewrite.
- **Alternatives considered**:
  - Create a brand-new plugin registry: rejected because it would fragment the runtime and ignore the work already done in `006`.
  - Keep extension hooks limited to export/provider paths: rejected because the roadmap explicitly calls for a broader runtime-wide surface.

## Decision 2: Support several extension families through one registration model

- **Decision**: The first unified registration model covers ingress adapters, tool providers, context sources, export adapters, import adapters, and sync transports.
- **Why**: This is broad enough to prove extensibility is now a system property, not an isolated portability feature.
- **Alternatives considered**:
  - Standardize only portability/export first: rejected because it would not materially change the current extensibility story.
  - Try to include every possible future adapter type now: rejected because the first slice still needs to remain implementable.

## Decision 3: Compatibility and enablement are first-class registration concerns

- **Decision**: Every extension declares compatibility requirements, privacy guarantees, and enablement semantics in the registration itself.
- **Why**: Extension growth without compatibility and enablement control would quickly create opaque or unsafe runtime behavior.
- **Alternatives considered**:
  - Validate compatibility only at execution time: rejected because incompatible extensions should be detectable before activation.
  - Assume every valid extension is active: rejected because some extension types should remain off by default.

## Decision 4: Discovery summaries must be human-inspectable

- **Decision**: The unified surface includes a discovery/inspection summary that describes what each extension contributes, whether it is active, and why it may be incompatible.
- **Why**: Extensibility is only manageable if runtime state can be inspected and explained.
- **Alternatives considered**:
  - Keep extension registration internal only: rejected because it would make future extension debugging and governance much harder.

## Decision 5: `017` stops at unified registration and discovery

- **Decision**: `017` intentionally does not become a distribution marketplace or remote installation system.
- **Why**: The roadmap calls for a unified extension surface, not for marketplace infrastructure.
- **Alternatives considered**:
  - Introduce remote registry or installation flows now: rejected because it would exceed the current product slice.
