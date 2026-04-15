# Research: Runtime Control Center

## Decision 1: Use one primary control-center sheet instead of many top-level peer sheets

**Decision**: Introduce a dedicated runtime control center as the main deep-inspection surface, and route existing detail entry points through it.

**Rationale**: The current workspace already has multiple sheets for details, context, governance, portability, and approval. A separate peer sheet for each concern keeps the system legible in isolation, but not as one product.

**Alternatives considered**:
- Keep the current sheet set and only improve labels: too little product change
- Build a separate full-screen dashboard: violates the conversation-first direction

## Decision 2: The first slice should prioritize readable trace + artifact entry, not full editor unification

**Decision**: `018` should unify reading and entry routing first, while reusing existing supported editors and actions for memory, governance, portability, and approval.

**Rationale**: Existing artifact actions already work. The product gap is that users cannot see them as one system. Reusing those actions through a control-center frame delivers a meaningful milestone without re-implementing all editors.

**Alternatives considered**:
- Rewrite all artifact editing in one new screen: too broad
- Only show runtime trace and defer artifact entry: not enough management value

## Decision 3: Model the trace as one coherent snapshot with grouped signals

**Decision**: Introduce a `RuntimeTraceSnapshot` that groups:
- request source and caller trust
- selected tool/action path
- approval status
- major context contributors
- extension contributors

**Rationale**: These signals already exist, but they are distributed across runtime status, audit, context inspector, and governance data. The control center needs one readable synthesis object.

**Alternatives considered**:
- Show raw audit list only: too low-level and incomplete
- Show every runtime event inline: too noisy for a user-facing control surface

## Decision 4: Preserve chat-first layout and use progressive disclosure

**Decision**: Keep the current workspace shell and use the control center as a modal/bottom-sheet deep view reachable from digest and secondary entries.

**Rationale**: This preserves the current `013` conversation-first IA and avoids turning the app into a settings-style control dashboard.

**Alternatives considered**:
- Replace the main screen with a tabbed dashboard: breaks established interaction model
- Hide control state behind secondary screens only: too fragmented

## Decision 5: Extension and approval state should appear in the same management language as memory and governance

**Decision**: Control-center sections should use one shared “artifact entry” language for inspectable/editable runtime-managed objects.

**Rationale**: `016` and `017` stabilized contracts; `018` should unify their presentation and entry points, not invent a special UI language for each family.

**Alternatives considered**:
- Keep approvals only in the approval sheet and extensions only in the context/detail surfaces: maintains fragmentation
