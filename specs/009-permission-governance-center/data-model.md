# Data Model: Permission Governance Center

## Entities

### CallerGovernanceRecord

Represents persistent local governance state for a caller.

**Fields**

- `callerId: String`
- `originApp: String`
- `displayLabel: String`
- `packageName: String?`
- `signatureDigest: String?`
- `trustMode: GovernanceTrustMode`
- `trustReason: String`
- `lastSeenAtEpochMillis: Long`
- `lastDecisionSummary: String`

**Validation**

- `callerId` must be stable and unique.
- `displayLabel` must remain user-readable even when package metadata is missing.

### ScopeGrantRecord

Represents a persisted per-caller scope policy.

**Fields**

- `grantId: String`
- `callerId: String`
- `scopeId: String`
- `grantState: GovernanceGrantState`
- `updatedAtEpochMillis: Long`

**Validation**

- `scopeId` must map to a known `ActionScope` in this milestone.
- One `(callerId, scopeId)` pair should resolve to one current record.

### GovernanceActivityItem

Represents a bounded user-visible recent governance event.

**Fields**

- `activityId: String`
- `callerLabel: String`
- `headline: String`
- `details: String`
- `scopeLabel: String`
- `timestamp: Long`

### GovernanceDecisionSnapshot

Resolved runtime governance view used during caller verification/routing.

**Fields**

- `callerId: String`
- `effectiveTrustMode: GovernanceTrustMode`
- `allowedScopes: Set<String>`
- `deniedScopes: Set<String>`
- `decisionExplanation: String`

## Enums

### GovernanceTrustMode

- `TRUSTED`
- `ASK_EACH_TIME`
- `DENIED`

### GovernanceGrantState

- `ALLOW`
- `ASK`
- `DENY`

## Relationships

- One `CallerGovernanceRecord` has many `ScopeGrantRecord`
- One `CallerGovernanceRecord` can produce many `GovernanceActivityItem`
- `GovernanceDecisionSnapshot` is a derived runtime view, not necessarily a persisted table
