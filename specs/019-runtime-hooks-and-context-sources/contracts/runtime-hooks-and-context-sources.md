# Contract: Runtime Hooks And Context Sources

## Purpose

Define one shared runtime contribution surface so lifecycle hooks, context sources, and future knowledge-source contributors can participate in request handling without creating parallel callback systems or hidden execution paths.

## Registration Contract

Every runtime contributor must be representable through one shared registration shape containing:

- stable contributor identity
- contributor type
- supported lifecycle point or points
- contribution summary
- eligibility profile
- default availability state

## Lifecycle Contract

The runtime must be able to evaluate contributors against stable lifecycle points such as:

- ingress
- planning
- context attachment
- proposal
- approval
- execution
- reflection

Covered contributors may observe, attach context, gate behavior, or emit explainability metadata at supported lifecycle points.

## Request-Time Context Contract

Contributors that attach context must provide a stable request-time context shape carrying:

- contribution summary
- provenance
- scope or privacy hint
- lifecycle point
- removability or fixed-attachment semantics

This contract is for request-time context only and does not imply durable corpus management.

## Outcome And Provenance Contract

For each request, the runtime must preserve whether a covered contributor was:

- applied
- skipped
- degraded
- blocked
- unavailable

Each outcome must remain explainable through user-facing summary plus deeper provenance details.

## Visibility Contract

The product must surface:

- concise contribution summaries in the current task flow
- deeper contributor truth in existing control-center and detail surfaces
- clear limitation messaging when contributors do not run normally

`019` must not create a second task workspace or backend console to display contributions.

## Management Contract

Compatible contributors may expose reversible availability controls such as:

- enable
- disable
- inspect

Richer contributor configuration, provider schema editing, and durable corpus operations are outside this milestone.
