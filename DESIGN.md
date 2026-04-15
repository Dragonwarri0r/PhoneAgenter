# Design System Specification: The Ethereal Assistant

## 1. Overview & Creative North Star
**Creative North Star: "The Digital Atrium"**

This design system moves away from the dense, dark "command center" aesthetic common in AI. Instead, we are building a "Digital Atrium"—a space characterized by light, transparency, and a rhythmic sense of calm. Our goal is to make the user feel as though they are interacting with an intelligent entity that is both high-functioning and effortlessly approachable.

To break the "template" look, we reject the rigid, boxed-in grid. We embrace **intentional asymmetry** and **tonal depth**. By utilizing oversized typography, overlapping glass layers, and generous whitespace, we create an editorial experience that feels premium and custom-built, rather than a generic dashboard.

---

## 2. Colors & Surface Philosophy

Our palette is anchored by a high-energy `primary` blue, balanced by a sophisticated suite of "Cloud" neutrals.

### The "No-Line" Rule
**Traditional 1px borders are strictly prohibited for sectioning.** To create a high-end, seamless interface, boundaries must be defined through background color shifts. Use `surface-container-low` against a `surface` background to denote change. This creates a "molded" look rather than a "sketched" look.

### Surface Hierarchy & Nesting
Treat the UI as a physical stack of semi-translucent materials.
- **Base Level:** `surface` (#F5F7F9) – The foundation.
- **Sectioning:** `surface-container-low` (#EEF1F3) – To define large content areas.
- **Interaction Layer:** `surface-container-lowest` (#FFFFFF) – Reserved for cards, inputs, and primary interaction zones to provide maximum "pop."

### The "Glass & Gradient" Rule
To inject "soul" into the AI experience:
- **Glassmorphism:** Use `surface-container-lowest` at 70% opacity with a `24px` backdrop blur for floating navigation or AI response bubbles.
- **Signature Textures:** For Hero CTAs and primary states, utilize a linear gradient: `primary` (#0057BD) to `primary_container` (#6E9FFF) at a 135° angle. This adds a "sunny" luminosity that flat hex codes cannot achieve.

---

## 3. Typography: Editorial Authority

We use a dual-typeface system to balance technical precision with human warmth.

*   **Headlines (Manrope):** Our "voice." Manrope’s geometric yet friendly curves should be used for all `display` and `headline` roles. Use `display-lg` (3.5rem) with tight letter-spacing (-0.02em) for high-impact hero moments.
*   **Body (Inter):** Our "engine." Inter is used for all `title`, `body`, and `label` roles. Its tall x-height ensures maximum legibility for AI-generated long-form text.

**Hierarchy Tip:** Pair a `headline-sm` in a bold weight with a `body-md` in a regular weight. The high contrast between the Manrope headlines and Inter body text creates an "Editorial" feel found in high-end magazines.

---

## 4. Elevation & Depth: Tonal Layering

We do not use shadows to create "distance"; we use them to create "atmosphere."

*   **The Layering Principle:** Place a `surface-container-lowest` card on top of a `surface-container-low` background. This creates a natural "lift" without a single drop shadow.
*   **Ambient Shadows:** When an element must float (e.g., a Modal or Popover), use a shadow color tinted with our `on-surface` (#2C2F31). 
    *   *Spec:* `0px 20px 40px rgba(44, 47, 49, 0.06)`. It should feel like a soft glow of occlusion, not a dark smudge.
*   **The Ghost Border:** If a container requires more definition for accessibility, use a `1px` stroke of `outline-variant` (#ABADAF) at **15% opacity**. It should be felt, not seen.

---

## 5. Components

### Buttons
*   **Primary:** Gradient (`primary` to `primary_container`), `lg` (2rem) rounded corners. White text (`on_primary`).
*   **Secondary:** `surface-container-high` background with `primary` text. No border.
*   **Tertiary:** Transparent background, `primary` text, with an underline that appears only on hover.

### AI Chat Bubbles (The Signature Component)
*   **User Bubble:** `surface-container-highest` with `md` (1.5rem) rounded corners. 
*   **AI Agent Bubble:** Glassmorphic (`surface-container-lowest` @ 80% + blur) with a subtle `primary` glow (2px inner shadow) to signify "activity."

### Input Fields
*   **Style:** `surface-container-lowest` background. 
*   **Corner Radius:** `md` (1.5rem).
*   **State:** On focus, the "Ghost Border" transitions to 100% opacity `primary` blue with a 4px soft outer glow.

### Cards & Lists
*   **Rule:** **Forbid divider lines.** Use `1.5rem` to `2rem` of vertical whitespace to separate list items. If separation is visually required, use a subtle background shift to `surface-container-low`.

---

## 6. Do’s and Don'ts

### Do:
*   **Do** use "Optical Centering." AI icons should often be slightly offset to visually feel centered within rounded containers.
*   **Do** use `display-lg` typography for empty states to make them feel like intentional design moments rather than "missing" data.
*   **Do** lean into the `lg` (2rem) and `xl` (3rem) corner radii for large containers to maintain the "sunny/friendly" atmosphere.

### Don't:
*   **Don't** use pure black (#000000) for text. Use `on-surface` (#2C2F31) to maintain the "airy" feel.
*   **Don't** use 100% opaque borders. They create "visual noise" and break the soft, futuristic atmosphere.
*   **Don't** crowd the interface. If a screen feels "busy," increase the padding using our `xl` spacing scale. Space is a luxury; use it.