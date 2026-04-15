# Research: Structured Action Payloads

## Decision 1: Add a Dedicated Structured Action Normalization Layer Above Providers

**Decision**: Introduce a new `runtime/action` normalization layer that turns supported runtime plans into structured payloads before policy and provider execution continue.

**Rationale**:

- The current system already has runtime planning and provider execution, but structured execution should become a stable contract above both.
- Keeping normalization above providers prevents each provider from reparsing the user’s natural-language request differently.
- This matches the roadmap goal of making action execution more reliable and explainable without redefining the whole runtime.

**Alternatives considered**:

- **Let each provider parse raw text independently**: Rejected because it duplicates logic and undermines auditability.
- **Put structured extraction into `RuntimePlanner` only**: Rejected because planner intent selection and field extraction should remain separate concerns.

## Decision 2: Keep the First Slice Small and Action-Specific

**Decision**: Limit the first structured payload milestone to `message.send`, `calendar.write`, and `external.share`.

**Rationale**:

- These are the most visible high-impact actions already present in the capability bridge.
- They have clear downstream provider paths that currently depend too much on raw request text.
- Keeping the slice small makes the feature independently demoable and prevents it from turning into a generic NLP project.

**Alternatives considered**:

- **Normalize every action type immediately**: Rejected because it would over-expand the milestone.
- **Only handle one action type**: Rejected because it would be too narrow to prove the new contract layer.

## Decision 3: Represent Completeness Explicitly as `COMPLETE`, `PARTIAL`, or `INSUFFICIENT`

**Decision**: Every structured action payload must carry an explicit completeness state.

**Rationale**:

- The current runtime needs a reliable way to distinguish “safe to continue”, “needs preview/confirmation”, and “not safe to execute”.
- Explicit completeness is more stable than relying on scattered null checks across policy or provider code.
- This gives the UI and audit layer a single explainability hook.

**Alternatives considered**:

- **Infer completeness ad hoc from missing fields**: Rejected because it spreads policy logic across multiple layers.
- **Use only `valid` versus `invalid`**: Rejected because `partial` extraction is a meaningful state for preview-first behavior.

## Decision 4: Preserve Original Natural Language for Explainability, But Use Structured Fields for Execution

**Decision**: Keep the original request text visible in runtime/approval surfaces while making structured fields the primary execution payload for supported actions.

**Rationale**:

- Users still need to understand what they originally asked for.
- Providers and execution previews should stop depending on brittle raw text once structured fields exist.
- This balances explainability with execution reliability.

**Alternatives considered**:

- **Discard original text once structured payload exists**: Rejected because it weakens explainability.
- **Keep executing directly from raw text and only show structured preview**: Rejected because it would create a dangerous mismatch between preview and real execution.

## Decision 5: Use Heuristic Field Extraction First, Then Let Policy React to Completeness

**Decision**: The first milestone can use heuristic extraction for recipient/content/title/time hints as long as completeness and evidence are explicit and safety behavior is preserved.

**Rationale**:

- The current runtime already uses heuristics for intent classification, so extending that style is practical.
- A heuristic first pass is enough to establish the normalized contract and UI surfaces.
- Policy already exists and can consume completeness as another decision input.

**Alternatives considered**:

- **Require full semantic parsing or LLM-backed extraction first**: Rejected because it would raise complexity too early.
- **Refuse any partial payloads**: Rejected because preview-first flows benefit from partial but explicit extraction.
