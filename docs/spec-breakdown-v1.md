# Spec Breakdown v1

## Purpose

This document maps the post-`v0` productization path into a set of `spec-kit` feature specs and keeps the remaining next-scope work visible now that `001-015` are already in place.

The split keeps the same two rules:

- one spec should map to one primary milestone
- one spec should still be independently demoable and valuable

For interop-related work, the upstream docs-level design baseline now lives in [Hub Interop Protocol Design v1](./hub-interop-protocol-design-v1.md).
That means future interop specs should treat Android share as a compatibility ingress, not as the long-term primary contract for hub-to-app communication.

The agreed protocol docs set is now indexed in [Hub Interop Docs Index v1](./hub-interop-docs-index-v1.md).
`022-hub-interop-protocol` remains an exploratory draft context, while implementation work starts from `024`.

After the first `024-026` implementation pass, the product definition is now stricter:

- `Hub Interop Protocol` is the public protocol / SDK / contract that other apps can depend on.
- `Mobile Claw Host` is the governed execution center that authorizes, routes, executes, audits, and exposes user control.
- `Interop Probe App` is the external conformance client that proves the protocol and host behavior can be used by an app with no `:app` dependency.

This changes the next planning priority from "add more app features" to "stabilize the public protocol entry, harden Mobile Claw as a trusted host, and turn the probe into a repeatable conformance tool."

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

## Capability Inference Specs

| Spec | Track | Primary Goal | Why This Is a Good Slice | Depends On |
|---|---|---|---|---|
| `023-capability-inference-read-tools` | V2-A4 | Route clear read/write intents to explicit capabilities instead of defaulting every freeform request to `generate.reply` | Provides the first real read-tool baseline, especially `calendar.read`, that `027` can safely expose through Hub Interop | 010, 015, 018, 019 |

## Hub Interop Delivery Specs

| Spec | Track | Primary Goal | Why This Is a Good Slice | Depends On |
|---|---|---|---|---|
| `024-shared-interop-contract` | V2-I1 | Extract the shared public Hub Interop protocol contract and Android binding into separately reusable modules | Makes the protocol independently consumable by Mobile Claw, external apps, and the probe app instead of leaving it host-bound | 015, 016, 017, 018 |
| `025-mobileclaw-interop-host` | V2-I2 | Implement Mobile Claw as one governed host/provider of the shared Hub Interop protocol | Turns the docs-level contract into a real host implementation with governed discovery, authorization, invocation, task, and artifact behavior | 024 |
| `026-interop-probe-app` | V2-I3 | Build a separate protocol consumer app that validates Hub Interop against Mobile Claw through the shared public contract only | Proves the protocol is externally consumable and catches host-specific leakage early | 024, 025 |

## Hub Interop Public Baseline Specs

| Spec | Track | Primary Goal | Why This Is a Good Slice | Depends On |
|---|---|---|---|---|
| `027-public-interop-contract-stabilization` | V2-I4 | Stabilize the public Hub Interop protocol contract, Android binding, status semantics, descriptors, compatibility behavior, and codec validation | Locks the contract before host hardening and conformance testing depend on it as a public API | 024, 025, 026 |
| `028-mobileclaw-trusted-interop-host` | V2-I5 | Harden Mobile Claw as the trusted host with host-attested caller identity, authorization lifecycle, durable task/artifact semantics, and `generate.reply` + bounded `calendar.read` execution | Proves the Claw app can safely execute public interop requests through the governed runtime spine | 023, 025, 027 |
| `029-interop-probe-conformance-suite` | V2-I6 | Upgrade the probe app into a repeatable conformance client with manual mode, conformance mode, spoof/authorization/task/artifact diagnostics, and shareable reports | Proves an independent app can validate protocol and host behavior without depending on `:app` internals | 026, 027, 028 |

This replaces the earlier broader `027-032` split. Read expansion, control-center object detail, workflow runner, resource/knowledge exchange, and side-effect capability exposure stay deferred until the public protocol, trusted host, and conformance loop are stable.

`028` should intentionally stay narrow on exposed capabilities: keep `generate.reply` for basic invocation and add `calendar.read` as the first real Android read capability. Do not include contacts, knowledge search, workflow run, calendar write/delete, message send, file read, or broader side-effect tools in this baseline.

The detailed rationale for this split lives in [Hub Interop 027-029 Spec Split v1](./hub-interop-027-029-spec-split-v1.md).

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
16. `023-capability-inference-read-tools`
17. `024-shared-interop-contract`
18. `025-mobileclaw-interop-host`
19. `026-interop-probe-app`
20. `027-public-interop-contract-stabilization`
21. `028-mobileclaw-trusted-interop-host`
22. `029-interop-probe-conformance-suite`

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

For the protocol track, it also avoids a fourth failure mode: treating the shared public contract, the Mobile Claw host implementation, and the independent consumer validation app as one umbrella milestone.
Those three concerns should stay split because they answer different questions:

- `024` proves the protocol is independently consumable
- `025` proves Mobile Claw can implement it
- `026` proves an external app can really use it
- `027` proves the public contract is stable enough to depend on
- `028` proves Mobile Claw can be a trusted governed host for that contract
- `029` proves an independent probe can continuously validate the contract and host behavior

## Active Next Scope

Current priority implementation track:

- `027-public-interop-contract-stabilization`

Recently completed protocol track:

- `024-shared-interop-contract`
- `025-mobileclaw-interop-host`
- `026-interop-probe-app`

Relevant completed runtime/capability prerequisites:

- `019-runtime-hooks-and-context-sources`
- `020-knowledge-ingestion-and-retrieval`
- `021-workflow-graph-and-automation`
- `023-capability-inference-read-tools`

Recommended follow-on specs right after:

- `028-mobileclaw-trusted-interop-host`
- `029-interop-probe-conformance-suite`

Parallel backlog:

- `018.x` control-surface hardening inside the existing runtime-control-center track

That hardening should stay focused on:

- a `session tray` for active context, pending approval, running actions, and recent activity
- regrouping the control center into `Now / Capabilities / Policy / Knowledge / Automation`
- a shared summary/detail template for core runtime objects
- basic reversible extension management such as enable/disable, visibility, pinning, and default approval mode

`027` acceptance should prove the public contract is stable:

- public method families remain fixed to discovery, authorization, invocation, task, artifact, and revoke/status flows
- status codes distinguish bad request, unauthorized, authorization required, pending, forbidden, not found, expired, incompatible, unsupported capability, provider unavailable, permission unavailable, policy denied, approval required/rejected, execution failed, and internal error where applicable
- capability, grant, task, and artifact descriptors have stable v1 fields with host-internal details excluded
- version compatibility handles supported, downgraded, incompatible, required unknown fields, optional unknown fields, and extension namespaces explicitly
- request/response Bundle codecs have roundtrip tests in the public Android binding
- `024/025` task checklists are reconciled with their implemented status

`028` acceptance should prove Mobile Claw is a trusted governed host:

- host-attested caller identity, grant lookup, task ownership, artifact access, and audit all use host-derived identity
- spoofed caller metadata is display/diagnostic only
- unauthorized, pending, granted, revoked, incompatible, downgraded, not-found, forbidden, and expired states are externally observable
- accepted invocations create durable task/artifact records or explicitly documented lifecycle semantics
- `generate.reply` remains available and bounded `calendar.read` is added as the first real Android read capability
- calendar permission unavailable, empty result, bounded query, completed artifact, audit, revoke, and cross-caller access behavior are explicit

`029` acceptance should prove the probe is a conformance tool:

- third-party caller depends only on contract modules
- manual mode covers discover, request authorization, refresh grant, invoke, poll task, load artifact, revoke, and export report
- conformance mode covers compatibility, spoof diagnostics, unauthorized, pending, granted, revoked, task lifecycle, artifact lifecycle, malformed request, and downgrade/incompatible diagnostics
- report output includes host package, authority, protocol version, supported methods, supported capabilities, pass/fail matrix, raw status codes, failure reason, and timeline

## Relationship to v0

`001-006` stay valid and complete the `v0` foundation.

`007+` are not rework specs.
They are the first layer of productization on top of the runtime that `v0` already established.
