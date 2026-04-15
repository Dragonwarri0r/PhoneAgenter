# Contract: Workspace Information Architecture

## Base Workspace Zones

The workspace base screen should be organized into the following stable zones:

1. **Header Zone**
   - Title
   - Active session identity
   - Stable compact action entry points

2. **Digest Zone**
   - Active stage label
   - One primary headline
   - Bounded status/capability signals
   - Optional high-priority inline action such as requesting permissions or opening detail

3. **Conversation Zone**
   - Main transcript surface
   - Remains the visual center in normal operation

4. **Accessory Zone**
   - Lightweight quick actions and/or compact secondary shortcuts
   - Must not exceed the visual weight of the composer

5. **Composer Zone**
   - Persistent bottom input surface
   - Anchored above navigation/IME

## Progressive Disclosure Rules

- Detailed diagnostics do not live permanently in the digest zone.
- Model management, context inspection, governance, portability preview, and approval stay as on-demand surfaces.
- The base workspace must always allow discovery of those surfaces through compact stable entry points.

## Priority Rules

- Approval emphasis outranks normal runtime status.
- Recoverable failure emphasis outranks normal runtime status but should coexist with the transcript.
- Preparing/unavailable states may replace the transcript with empty-state content, but should still use the same top-level zone logic.
- Keyboard visibility should collapse or compress secondary content before it harms conversation/composer usability.

## Compatibility Rules

- Existing runtime flows from `001-012` must remain reachable.
- Existing bilingual strings must continue to use device locale selection.
- Existing bottom sheets and dialogs may be reused so long as the base workspace remains compact by default.
