# Data Model: Tool Contract Standardization

## ToolDescriptor

Canonical descriptor for a runtime-callable tool.

**Fields**
- `toolId`: stable tool identity such as `calendar.write`
- `displayName`: localized display label
- `description`: concise user-facing explanation
- `inputSchemaJson`: schema contract for structured input
- `outputSchemaJson`: optional schema contract for structured output
- `sideEffectType`: `read`, `write`, or `dispatch`
- `riskLevel`: coarse risk label used by policy and UI
- `requiredScopes`: scopes required for execution
- `confirmationPolicy`: none, preview-first, or require-confirmation
- `visibilityPolicy`: descriptor of on-demand surfacing rules
- `bindingDescriptors`: Android execution bindings

**Relationships**
- Owns one or more `ToolBindingDescriptor`
- Produces `ToolExecutionPreview`
- Produces request-scoped `ToolVisibilitySnapshot`

## ToolSchemaDescriptor

Schema-bearing description of input or output fields.

**Fields**
- `schemaId`: stable schema identifier
- `schemaJson`: JSON-schema-like document or canonical string representation
- `requiredFields`: required field names
- `previewFields`: ordered fields suitable for preview and approval
- `supportsPartial`: whether incomplete values can still be previewed safely

## ToolBindingDescriptor

Concrete binding metadata connecting a standardized tool to Android execution.

**Fields**
- `bindingId`: stable binding identifier
- `toolId`: owning tool id
- `bindingType`: app function, intent, share dispatch, provider, picker, or other Android-native binding
- `providerId`: runtime provider identity
- `requiredPermissions`: Android permission or provider prerequisites
- `availabilityState`: available, degraded, unavailable, or restricted
- `routeMetadata`: optional execution notes
- `primary`: whether the binding is the preferred route

## ToolVisibilitySnapshot

Request-scoped tool visibility result.

**Fields**
- `toolId`: referenced descriptor
- `state`: visible, hidden, degraded, denied
- `reason`: explainable state summary
- `relevanceScore`: coarse relevance signal for planner/UI sorting
- `allowedByGovernance`: whether current governance allows the tool
- `availableBindingCount`: number of usable bindings

## ToolExecutionPreview

Preview data shown before tool execution.

**Fields**
- `toolId`: referenced descriptor
- `displayName`: localized label
- `sideEffectType`: read/write/dispatch
- `riskLevel`: low/medium/high/blocking style label
- `scopeLines`: user-visible scope lines
- `previewFields`: ordered field/value lines
- `warnings`: validation or completeness warnings
- `canExecute`: whether current arguments and bindings allow execution

## StandardToolCatalogEntry

Catalog entry tying standardized tool identity to current runtime concepts.

**Fields**
- `toolId`: standardized identity
- `legacyCapabilityId`: existing capability identifier for transition compatibility
- `defaultStructuredActionType`: upstream structured action family
- `defaultScopeId`: normalized scope id
- `family`: reply, calendar, alarm, message, share
