# Contract: Tool Contract Standardization

## Purpose

Define the first stable tool contract surface for Mobile Claw so planner, router, governance, preview, and audit all reference the same tool identity.

## Standardized Tool Contract

Every covered tool must provide:

- `toolId`
- `displayName`
- `description`
- `inputSchemaJson`
- `outputSchemaJson` or `null`
- `sideEffectType`
- `riskLevel`
- `requiredScopes`
- `confirmationPolicy`
- `visibilityPolicy`
- `bindingDescriptors`

## Covered Tool Families In `015`

- `generate.reply`
- `calendar.read`
- `calendar.write`
- `alarm.set`
- `alarm.show`
- `alarm.dismiss`
- `message.send`
- `share.outbound`

## Side-Effect Classification

- `read`
  - Queries or gathers information without mutating device or app state
- `write`
  - Mutates device or app state
- `dispatch`
  - Sends or hands content to another app, destination, or user-visible system action

## Visibility Contract

Tool visibility must be request-scoped and determined by:

- request relevance
- governance allowance
- provider/binding availability
- current model/runtime context
- current policy constraints

The result must be representable as:

- `visible`
- `degraded`
- `hidden`
- `denied`

## Preview Contract

Covered tools that are `write` or `dispatch` must produce a preview object before execution containing:

- tool display name
- side-effect type
- risk level
- required scopes
- ordered preview field lines
- warnings or completeness notes

## Governance And Audit Contract

For covered tools, governance, approval, route explanation, and audit must all reference:

- the same `toolId`
- the same primary display name
- the same normalized scope id
- the same side-effect classification

## Transitional Compatibility

`015` may continue to map standardized tools onto existing legacy capability ids and providers during transition, but covered flows must resolve through the tool contract first and only then map to execution bindings.
