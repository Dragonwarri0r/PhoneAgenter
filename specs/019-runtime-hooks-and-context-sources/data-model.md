# Data Model: Runtime Hooks And Context Sources

## RuntimeContributionRegistration

Canonical registration for one lifecycle hook, context source, or similar request-time contributor.

**Fields**
- `contributionId`: stable contributor identity
- `displayName`: user-facing contributor label
- `contributionType`: lifecycle, context, or mixed contribution mode
- `lifecyclePoints`: request stages where the contributor may observe, attach context, gate behavior, or emit reflection metadata
- `summaryTemplate`: concise contribution summary shown in current task and management surfaces
- `eligibilityProfile`: trust, scope, privacy, and dependency conditions
- `defaultAvailabilityState`: enabled, disabled, degraded, or similar reversible state

## ContributionLifecyclePoint

Stable request-stage descriptor for runtime contribution timing.

**Fields**
- `pointId`
- `displayName`
- `category`: ingress, planning, context-attach, proposal, approval, execution, reflection
- `supportsContextAttachment`
- `supportsBehavioralObservation`

## ContextContribution

Request-time context payload attached by a contributor.

**Fields**
- `contributionId`
- `summary`
- `provenanceLabel`
- `scopeLabel`
- `privacyLabel`
- `attachedAtLifecyclePoint`
- `isRemovable`

## ContributionEligibilityProfile

Human-readable conditions controlling whether a contributor may run.

**Fields**
- `requiredTrustState`
- `requiredScopes`
- `requiredDependencies`
- `policyNotes`
- `unavailableReason`

## ContributionOutcomeRecord

Explainable outcome for one contributor during one request.

**Fields**
- `contributionId`
- `requestId`
- `lifecyclePoint`
- `outcomeState`: applied, skipped, degraded, blocked, unavailable
- `summary`
- `details`
- `policyReason`
- `provenanceSummary`

## ManagedContributorEntry

Inspectable runtime-managed contributor shown in control and detail surfaces.

**Fields**
- `contributionId`
- `title`
- `statusLine`
- `availabilityState`
- `scopeSummary`
- `supportsAvailabilityChange`
- `detailRoute`
