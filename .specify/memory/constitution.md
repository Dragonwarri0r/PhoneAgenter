# mobile_claw Constitution

## Core Principles

### I. Local-First, User-Controlled
The product is designed first for `Android + single user + local-first` operation.
Core request handling, memory access, risk evaluation, and execution decisions must work without requiring cloud sync or remote orchestration.
Automation must preserve explicit user control, especially for high-impact actions.

### II. Persona and Memory Are Separate Systems
Persona defines stable behavioral constraints.
Memory stores dynamic facts and context.
Specifications and implementations must not collapse persona and memory into a single prompt blob or single profile record.

### III. Safety Gates Override Convenience
Risk classification informs automation, but final authorization belongs to policy.
Low-risk actions may auto-execute.
High-risk actions must require confirmation.
Hard-confirm and deny rules take precedence over model confidence.

### IV. Adapter-Based Capability Integration
Runtime Core must remain platform-agnostic enough to survive future expansion.
Android integration should prefer `AppFunctions` first, then fall back to `Intent / Deep Link / Share / Accessibility`.
Platform bridges must adapt into common runtime capability contracts rather than define the core architecture.

### V. Privacy, Scope, and Audit by Default
Memory is private by default.
New inferred application memory is app-scoped unless explicitly promoted.
Every execution path must be explainable and auditable.
Future sync and sharing support must be enabled through explicit metadata and policies, not implicit data leakage.

## Product Constraints

- `v0` targets Android only.
- `v0` supports a single user on a single device.
- `v0` does not implement multi-device sync.
- `v0` must still reserve interfaces for sync, merge, shareability, and provider extension.
- Capability access must be modeled with fine-grained scopes rather than blanket full-access permissions.
- Memory modeling must include lifecycle, scope, exposure policy, and sync policy from the start.

## Workflow and Quality Gates

- Roadmap defines milestone sequence.
- Each major milestone should map to one primary feature spec whenever possible.
- Specs must be small enough to be independently planned, implemented, demonstrated, and tested.
- Specs should describe user value and system behavior, not implementation-specific code structure.
- Design and implementation references to external projects belong in roadmap or design artifacts, not in user-facing requirements.
- Planning may only proceed when a spec has clear scope boundaries, testable requirements, and measurable success criteria.

## Governance

This constitution governs roadmap refinement, spec generation, planning, and implementation.
If a later spec conflicts with these principles, the constitution wins unless it is formally amended.
Amendments require:

- an explicit rationale,
- an updated roadmap or design reference when scope changes,
- and a migration note if the change affects existing specs or data contracts.

**Version**: 0.1.0 | **Ratified**: 2026-04-08 | **Last Amended**: 2026-04-08
