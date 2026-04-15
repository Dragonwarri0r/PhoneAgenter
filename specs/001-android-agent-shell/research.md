# Research: Android Agent Shell and Local Model Workspace

## Decision 1: Use a Single Android App Module for the First Milestone

**Decision**: Build `001` as a single Android app module under `app`, with feature-first packages inside the module.

**Rationale**:

- The repository is still greenfield.
- The milestone only needs one primary screen and a few supporting surfaces.
- A single module keeps the first implementation fast to scaffold and easy to reason about.
- Package-level separation is enough for the first cut while still leaving a clean handoff to later runtime work.

**Alternatives considered**:

- **Multiple Android feature modules**: Rejected for `001` because it adds build and dependency overhead before the screen contract stabilizes.
- **Flat package layout**: Rejected because it would make later runtime extraction harder.

## Decision 2: Use Compose + Material 3 + Custom Theme Tokens for the Workspace

**Decision**: Build the workspace using Jetpack Compose and Material 3, then layer the `Digital Atrium` design language through custom theme tokens and workspace components.

**Rationale**:

- Compose is the clearest fit for rapid iteration on a screen with multiple dynamic surfaces.
- Material 3 provides a stable foundation for accessibility, navigation, and state-aware UI.
- The design system calls for tonal surfaces, whitespace, glass-like layers, and large-radius controls, which fit better as theme and component rules than as one-off view styling.

**Alternatives considered**:

- **XML Views**: Rejected because state-heavy chat surfaces and screen-level iteration are slower to evolve there.
- **Pure bespoke styling without Material 3**: Rejected because it raises accessibility and consistency cost too early.

## Decision 3: Introduce a Thin LocalChatGateway Instead of the Full Runtime Pipeline

**Decision**: Power `001` through a thin `LocalChatGateway` contract plus a local session store, while leaving the full execution session pipeline to `002`.

**Rationale**:

- The first milestone needs a real local chat loop, but not the full runtime orchestration system.
- A UI-facing gateway lets the workspace stream assistant output and expose model health now.
- The later runtime session pipeline can wrap or replace this adapter without invalidating the screen contract.

**Alternatives considered**:

- **Build directly on the future runtime pipeline now**: Rejected because it would collapse milestones `001` and `002` together.
- **Use a fake-only chat backend**: Rejected because this milestone should validate a real local-model interaction path.

## Decision 4: Borrow Patterns from `gallery`, Not Its Task Structure

**Decision**: Reuse ideas from `gallery` for model lifecycle, streaming chat UI, and screen composition, but do not copy its task abstractions or product-specific flows into `mobile_claw`.

**Rationale**:

- `gallery` already demonstrates solid local model and streaming chat patterns.
- `mobile_claw` has a different product goal and should not inherit unrelated features or task hierarchies.
- Using `gallery` as a reference reduces exploration cost while preserving our own runtime boundaries.

**Alternatives considered**:

- **Reinvent the shell from scratch**: Rejected because it discards useful local-model UX lessons already available.
- **Direct code parity with gallery**: Rejected because the product scope and future runtime shape are different.

## Decision 5: Keep Active Session State In Memory, Persist Only Lightweight Preferences

**Decision**: Keep the current chat session in memory for `001`, while persisting only lightweight workspace preferences such as selected model or last-used UI options.

**Rationale**:

- The first milestone needs a fresh session model, not long-term history.
- In-memory state keeps the implementation small and lowers migration cost before the runtime contract is finalized.
- Lightweight persistence still improves usability for repeated workspace entry.

**Alternatives considered**:

- **Persist full transcript history immediately**: Rejected because long-term session strategy is not yet settled.
- **Persist nothing**: Rejected because selected model and simple workspace preferences are worth keeping.
