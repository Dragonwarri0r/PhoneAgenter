# Contract: Unified Extension Surface

## Purpose

Define one shared registration and discovery surface for runtime extensions so ingress, tools, providers, context sources, export/import paths, and sync transports can join the runtime without custom core patterns.

## Unified Registration Contract

Every registered extension must provide:

- `extensionId`
- `extensionType`
- `displayName`
- `contributedCapabilities`
- `requiredRecordFields`
- `privacyGuarantee`
- `defaultEnablement`
- `trustRequirement`
- `compatibilityVersionRange`

## Covered Extension Types In `017`

- ingress adapters
- tool providers
- context sources
- export adapters
- import adapters
- sync transport adapters

## Compatibility Contract

The runtime must be able to evaluate whether an extension is compatible based on:

- runtime version support
- required record fields or metadata
- trust requirements
- any missing dependencies

Compatibility results must be explainable before activation.

## Enablement Contract

Each extension must have an explicit enablement state such as:

- `active`
- `disabled`
- `degraded`
- `incompatible`

## Discovery Contract

The runtime must be able to enumerate registered extensions and present:

- extension identity
- extension type
- contribution summary
- privacy guarantee
- compatibility state
- enablement state

## Backward Compatibility Contract

Existing `006` portability-oriented extension registrations must remain representable through the unified registration model rather than requiring a second extension system.
