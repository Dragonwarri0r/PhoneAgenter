# Issue: Layout & Keyboard Handling

**Area**: `ui/agentworkspace`
**Severity**: High — degrades core chat interaction
**Specs affected**: spec1 (FR-010, FR-012, FR-013, SC-001)

---

## Problem Summary

Three related layout problems exist in `AgentWorkspaceScreen`:

1. **Keyboard squeezes the input field** — when the soft keyboard opens, all fixed-height cards (header, model health, context, quick actions) continue to occupy their full height. Only the `ConversationLayer` (which has `Modifier.weight(1f)`) shrinks. On smaller screens there is not enough room left for the conversation and the composer, so they both get compressed into a tiny strip.

2. **Conversation is not full-screen** — the transcript is buried below ~280dp of stacked cards. The spec calls the conversation "the visual center of the screen" but it only gets whatever vertical space remains after the other regions are satisfied.

3. **Floating panels are not collapsible** — the model health card, context window card, and quick action strip are always visible at full height with no way to collapse or dismiss them. This compounds both problems above.

---

## Root Cause

`AgentWorkspaceScreen` uses a single vertical `Column` with `Arrangement.spacedBy(16.dp)`. Every region stacks in the same fixed flow:

```
Column(
    modifier = Modifier
        .fillMaxSize()
        .navigationBarsPadding()
        .imePadding()           // shrinks the column when keyboard appears
        .padding(vertical = 28.dp),  // 28dp lost at top AND bottom
    verticalArrangement = Arrangement.spacedBy(16.dp),
)
```

When `imePadding()` shrinks the available height, the column has no mechanism to hide or scroll the fixed cards above. Only `weight(1f)` on the conversation card absorbs the collapse — and at some point even that hits zero.

Additional contributing factors:

- `WorkspaceFeedbackHost` sits in the normal column flow instead of being overlaid, consuming space even when invisible (due to `AnimatedVisibility`).
- The outer column has `padding(vertical = 28.dp)`, wasting 56dp total that could have been conversation space.
- `ComposerDock` has `heightIn(min = 56.dp)`, which at minimum still occupies 56dp + 24dp row padding inside the dock Surface.
- `AndroidManifest.xml` has no explicit `windowSoftInputMode` set. With `enableEdgeToEdge()`, insets are expected to be handled manually — but there is no fallback if inset delivery fails on a device.

---

## What the Spec Expects

From spec1:

- **FR-012**: "clearly legible workspace regions for model health, context visibility, conversation flow, and message composition" — regions should be legible, not necessarily always visible.
- **FR-013**: "large-radius, approachable interaction surfaces" — the composer should feel open, not cramped.
- **Visual Behavior Constraints**: "Keep model health and context surfaces readable without overpowering the transcript."
- **Screen State — Streaming**: "transcript visible, assistant turn actively updating, composer guarded against conflicting duplicate sends" — transcript must remain visible during generation.

---

## Required Changes

### 1. Switch the root layout from `Column` to `Box`

Make the conversation the primary full-size layer. Overlay everything else on top of it.

```
Box(
    modifier = Modifier
        .fillMaxSize()
        .navigationBarsPadding()
        .imePadding(),
)
```

- `ConversationLayer` fills the `Box` at full size.
- `WorkspaceHeader` is pinned to the top edge of the `Box`.
- `ComposerDock` is pinned to the bottom edge of the `Box`.
- The panel group (ModelHealthCard + ContextWindowCard + QuickActionStrip) is overlaid below the header and can be collapsed/expanded.

### 2. Make the top panel group collapsible

Wrap `ModelHealthCard`, `ContextWindowCard`, and `QuickActionStrip` in a collapsible container (e.g. `AnimatedVisibility` or `animateContentSize`). A toggle in the header (or a drag handle on the panel itself) should collapse the group to a one-line summary strip.

When the keyboard opens, the panel group should auto-collapse so the conversation and composer have maximum vertical room.

### 3. Move `WorkspaceFeedbackHost` out of the column flow

`WorkspaceFeedbackHost` should be a floating overlay using `Box` alignment rather than a column child. This prevents it from participating in vertical measurement even when not visible.

### 4. Add `windowSoftInputMode` to the manifest

```xml
<activity
    android:name=".MainActivity"
    android:windowSoftInputMode="adjustResize">
```

With `enableEdgeToEdge()` and Compose inset handling this should be `adjustResize` so the window reports the correct available height to the inset APIs.

---

## Affected Files

- [AgentWorkspaceScreen.kt](app/src/main/java/com/mobileclaw/app/ui/agentworkspace/AgentWorkspaceScreen.kt)
- [ComposerDock.kt](app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/ComposerDock.kt)
- [WorkspaceFeedbackHost.kt](app/src/main/java/com/mobileclaw/app/ui/agentworkspace/components/WorkspaceFeedbackHost.kt)
- [AndroidManifest.xml](app/src/main/AndroidManifest.xml)
