# Spec Breakdown v1

## Purpose

This document maps the post-`v0` productization path into a set of `spec-kit` feature specs and keeps the remaining next-scope work visible now that `001-015` are already in place.

The split keeps the same two rules:

- one spec should map to one primary milestone
- one spec should still be independently demoable and valuable

## Milestone to Spec Map

| Spec | Milestone | Primary Goal | Why This Is a Good Slice | Depends On |
|---|---|---|---|---|
| `007-external-runtime-entry` | V1-M1 | Add the first trusted external handoff entry into the runtime | Turns the current workspace-only flow into the first real “send this to Mobile Claw” product path | 005 |
| `008-structured-action-payloads` | V1-M2 | Normalize key actions into structured payloads before execution | Makes message/calendar/alarm/share execution reliable and explainable | 004, 005, 007 |
| `009-permission-governance-center` | V1-M3 | Add user-manageable caller, scope, and approval governance | Gives users control over who can invoke the runtime and what they can request | 004, 005, 007 |
| `010-system-source-ingestion` | V1-M4 | Connect first real system context connectors | Makes the agent useful with real device data beyond the initial external handoff path | 003, 007, 008 |
| `011-portability-bundles` | V1-M5 | Turn export hooks into a user-visible summary portability flow | Converts `006` contracts into a real user-facing result without becoming full sync | 006, 009 |
| `012-real-appfunctions-integration` | V1-M6 | Upgrade the seeded AppFunctions boundary to framework-backed integration when platform conditions allow | Advances Android alignment without blocking earlier product milestones | 005, 007, 008 |

## Cross-Cutting Productization Specs

| Spec | Track | Primary Goal | Why This Is a Good Slice | Depends On |
|---|---|---|---|---|
| `013-workspace-information-architecture` | V1-X1 | Reshape the workspace into a conversation-first, capability-visible, progressively disclosed execution surface | Prevents later capability growth from overwhelming the UI and makes runtime state legible | 001, 002, 003, 004, 007 |
| `014-multimodal-ingress-and-composer` | V1-X2 | Add model-aware image/audio input, attachment preview, and multimodal request normalization | Uses imported multimodal models as multimodal models instead of text-only shells | 001, 005, 007, 013 |
| `015-tool-contract-standardization` | V1-X3 | Standardize tool descriptors, schemas, side-effect policy, and common productivity actions | Makes calendar/alarm/share/message tooling scalable, auditable, and explainable | 004, 005, 006, 008, 009 |
| `016-external-caller-interop-contracts` | V1-X4 | Define stable app-to-app and agent-to-agent calling contracts for Mobile Claw | Prevents external integrations from fragmenting into one-off adapters | 007, 009, 015 |
| `017-unified-extension-surface` | V1-X5 | Expand `006` portability hooks into a broader extension surface across ingress, tools, providers, context sources, and portability paths | Preserves extensibility as a system property rather than isolated hooks | 006, 015, 016 |
| `018-runtime-control-center` | V1-X6 | Consolidate Mobile Claw into a conversation-first runtime control center for reading and editing supported managed state | Closes the gap between existing runtime capabilities and one coherent in-app management surface without displacing chat as the main entry | 003, 009, 013, 014, 015, 017 |

## Post-018 Expansion Specs

| Spec | Track | Primary Goal | Why This Is a Good Slice | Depends On |
|---|---|---|---|---|
| `019-runtime-hooks-and-context-sources` | V2-A1 | Add one unified runtime hook surface plus stable context-source / knowledge-source contracts | Keeps hooks and knowledge contribution in one runtime language instead of fragmenting into callbacks, ad hoc providers, and one-off source adapters | 015, 016, 017, 018 |
| `020-knowledge-ingestion-and-retrieval` | V2-A2 | Turn knowledge into a first-class local runtime layer through ingestion, indexing metadata, retrieval, and source visibility | Big enough to be a real milestone, but still focused on one product problem: making local knowledge usable and inspectable | 003, 006, 010, 018, 019 |
| `021-workflow-graph-and-automation` | V2-A3 | Add workflow graph contracts, resumable execution, and first automation/task-flow support on top of tools, hooks, approvals, and knowledge retrieval | Keeps DAG/automation in one meaningful milestone instead of splitting graph contracts, runners, and automation into tiny specs | 004, 015, 018, 019, 020 |

## Recommended Build Order

1. `007-external-runtime-entry`
2. `013-workspace-information-architecture`
3. `008-structured-action-payloads`
4. `014-multimodal-ingress-and-composer`
5. `009-permission-governance-center`
6. `015-tool-contract-standardization`
7. `010-system-source-ingestion`
8. `011-portability-bundles`
9. `016-external-caller-interop-contracts`
10. `017-unified-extension-surface`
11. `018-runtime-control-center`
12. `012-real-appfunctions-integration`
13. `019-runtime-hooks-and-context-sources`
14. `020-knowledge-ingestion-and-retrieval`
15. `021-workflow-graph-and-automation`

## Why This Cut

This cut deliberately separates:

- `first handoff`
- `action normalization`
- `governance`
- `system context`
- `portability`
- `control-surface productization`

Then, after `018`, it deliberately **re-groups** the next stage into three broader product slices instead of continuing to split every small capability into a top-level spec:

- `hooks + context-source contracts`
- `knowledge ingestion + retrieval`
- `workflow graph + automation`

That matters because these are related but not the same engineering problems.

If they were merged into one umbrella spec, the result would be a large mixed milestone with too many moving parts:

- Android entry points
- external handoff UX
- trust and permission models
- action extraction
- new system connectors
- export behavior
- in-app management and control-surface behavior

By splitting them, each spec still produces a meaningful result:

- `007` proves real external handoff
- `008` proves structured action execution
- `009` proves user governance
- `010` proves real system context ingestion
- `011` proves portability value
- `018` proves the app itself can become the primary runtime control center

This cut also avoids a second failure mode: treating UX structure, multimodal support, tool standards, external interop, extension hooks, and in-app management as “misc follow-up” work.
Those concerns all change real contracts and product behavior, so they deserve explicit specs:

- `013` keeps the workspace legible as capability density increases
- `014` makes multimodal models actually usable
- `015` prevents tool growth from becoming ad hoc branching logic
- `016` stabilizes app-to-app and agent-to-agent calling contracts
- `017` turns extensibility into a first-class runtime surface
- `018` turns those capabilities into one readable and editable in-app control experience

For the next stage, it also avoids a third failure mode: over-splitting hooks, knowledge, ingestion, indexing, retrieval, graph contracts, runner behavior, and automation entry into too many tiny specs.
Those concerns should stay grouped by product problem:

- `019` defines how runtime extensions and context contributions plug in
- `020` makes local knowledge usable
- `021` makes multi-step automation and DAG execution usable

## Active Next Scope

Recommended immediate next spec after the current completed line:

- `019-runtime-hooks-and-context-sources`

Recommended follow-on specs right after that:

- `020-knowledge-ingestion-and-retrieval`
- `021-workflow-graph-and-automation`

## Relationship to v0

`001-006` stay valid and complete the `v0` foundation.

`007+` are not rework specs.
They are the first layer of productization on top of the runtime that `v0` already established.
