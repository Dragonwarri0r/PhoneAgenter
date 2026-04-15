# Research: Workspace Information Architecture

## Decision 1: Use a compact runtime digest instead of large permanent status cards

- **Decision**: Replace the always-expanded model/context stack with a compact digest surface that carries the active stage, headline, and a bounded set of status chips/lines.
- **Rationale**: The current workspace already has the right underlying information, but it is distributed across large vertical cards that permanently consume space. A digest keeps critical state visible while returning space to the transcript.
- **Alternatives considered**:
  - Keep the current cards and only auto-collapse them more aggressively: rejected because the base information model is still too tall and heterogeneous.
  - Hide all state behind a single sheet: rejected because key execution state would become invisible during normal use.

## Decision 2: Keep deep capability surfaces as existing sheets/dialogs

- **Decision**: Preserve model picker, context inspector, governance center, portability preview, and approval UI as on-demand surfaces instead of merging them into a new dashboard.
- **Rationale**: Those surfaces already exist and work. `013` is about information architecture, not replacing every deeper interaction. Reusing them keeps the milestone focused and reduces regression risk.
- **Alternatives considered**:
  - Create a new multi-tab workspace settings screen: rejected because it would expand scope into navigation and settings IA.
  - Inline every surface into the workspace itself: rejected because that would worsen the panel bloat problem.

## Decision 3: Move quick actions closer to the composer as a lightweight accessory rail

- **Decision**: Place quick prompts in a compact accessory rail adjacent to the composer rather than in the top diagnostic stack.
- **Rationale**: Quick prompts are composition helpers, not diagnostic context. Positioning them near the composer reinforces their role and reduces vertical competition with runtime status.
- **Alternatives considered**:
  - Leave quick actions in the top area: rejected because it makes the top stack feel like a mixed dashboard.
  - Hide quick actions behind another sheet: rejected because they should remain fast and discoverable.

## Decision 4: Attention priority should be state-driven, not panel-driven

- **Decision**: The workspace should compute an attention state that determines whether the digest emphasizes normal status, approval, failure, or empty/preparing messaging.
- **Rationale**: Approval and recoverable failure deserve more prominence than normal route metadata, but they still should not replace the conversation entirely. A state-driven emphasis model keeps this predictable.
- **Alternatives considered**:
  - Let each component independently render prominence rules: rejected because the resulting hierarchy becomes inconsistent.
  - Always prioritize error/failure banners over all other content: rejected because it risks recreating the same crowding problem in a different form.
