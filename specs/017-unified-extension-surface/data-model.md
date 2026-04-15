# Data Model: Unified Extension Surface

## RuntimeExtensionRegistration

Canonical registration object for all extension types.

**Fields**
- `extensionId`: stable extension identity
- `extensionType`: ingress, tool provider, context source, export, import, or sync transport
- `displayName`: localized label
- `contributedCapabilities`: tools, routes, sources, or transport functions provided
- `requiredRecordFields`: metadata or record fields required for compatibility
- `privacyGuarantee`: declared privacy model
- `defaultEnablement`: whether the extension is enabled by default
- `trustRequirement`: whether trusted callers or privileged context are required
- `compatibilityVersionRange`: runtime versions supported

## RuntimeExtensionType

Enumerated extension family.

**Values**
- `INGRESS`
- `TOOL_PROVIDER`
- `CONTEXT_SOURCE`
- `EXPORT`
- `IMPORT`
- `SYNC_TRANSPORT`

## ExtensionCompatibilityReport

Compatibility evaluation result for one registered extension.

**Fields**
- `extensionId`: referenced registration
- `isCompatible`: whether the extension can be activated
- `reason`: compatibility explanation
- `missingFields`: unmet field or metadata dependencies
- `runtimeVersionSatisfied`: whether current runtime version is supported

## ExtensionEnablementState

Activation state for a registered extension.

**Fields**
- `extensionId`: referenced registration
- `state`: active, disabled, degraded, incompatible
- `reason`: explainable status summary

## ExtensionContributionSummary

Inspectable summary of what an extension adds to the runtime.

**Fields**
- `extensionId`: referenced registration
- `displayName`: user-facing label
- `extensionType`: extension family
- `capabilitySummary`: summarized contributions
- `privacySummary`: short privacy statement
- `statusSummary`: enablement and compatibility status
