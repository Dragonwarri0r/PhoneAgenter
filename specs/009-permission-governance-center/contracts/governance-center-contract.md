# Contract: Governance Center

## Purpose

Define the internal app contract for exposing governance state to the workspace and applying governance overrides during runtime routing.

## Repository Surface

### `GovernanceRepository.observeGovernanceCenter()`

Returns a bounded, UI-ready snapshot containing:

- recent callers
- per-caller trust mode
- editable scope grants
- recent governance activity

## Mutation Surface

### `GovernanceRepository.updateTrustMode(callerId, trustMode)`

Persist a new trust mode for a caller.

### `GovernanceRepository.updateScopeGrant(callerId, scopeId, grantState)`

Persist a scope-level governance change.

## Runtime Resolution Surface

### `GovernanceRepository.resolveSnapshot(callerIdentity, capabilityId)`

Returns a `GovernanceDecisionSnapshot` consumed by caller verification / routing.

Expected outcomes:

- allow caller as trusted
- require ask/preview behavior
- deny caller entirely
- deny a specific scope even if caller is otherwise trusted

## UI Contract

The governance center must support:

- a list of recent callers
- a list of bounded editable scopes for the selected caller
- a bounded recent governance activity section
- bilingual user-visible text
