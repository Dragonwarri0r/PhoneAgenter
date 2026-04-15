package com.mobileclaw.app.runtime.capability

data class ToolSchemaDescriptor(
    val schemaId: String,
    val schemaJson: String,
    val requiredFields: List<String> = emptyList(),
    val previewFields: List<String> = emptyList(),
    val supportsPartial: Boolean = true,
)

enum class ToolSideEffectType {
    READ,
    WRITE,
    DISPATCH,
}

enum class ToolVisibilityState {
    VISIBLE,
    DEGRADED,
    HIDDEN,
    DENIED,
}

data class ToolBindingDescriptor(
    val bindingId: String,
    val bindingType: ProviderType,
    val androidContract: String,
    val requiredPermissions: List<String> = emptyList(),
    val primary: Boolean = false,
)

data class ToolDescriptor(
    val toolId: String,
    val legacyCapabilityId: String,
    val displayName: String,
    val description: String,
    val inputSchema: ToolSchemaDescriptor,
    val outputSchema: ToolSchemaDescriptor? = null,
    val sideEffectType: ToolSideEffectType,
    val riskLevelHint: String,
    val requiredScopes: List<String>,
    val confirmationPolicy: ConfirmationPolicy,
    val bindingDescriptors: List<ToolBindingDescriptor>,
)

data class ToolVisibilitySnapshot(
    val toolId: String,
    val state: ToolVisibilityState,
    val reason: String,
    val relevanceScore: Double,
    val allowedByGovernance: Boolean,
    val availableBindingCount: Int,
)

data class ToolExecutionPreview(
    val toolId: String,
    val displayName: String,
    val sideEffectType: ToolSideEffectType,
    val riskLevelHint: String,
    val scopeLines: List<String>,
    val previewFieldLines: List<String>,
    val warnings: List<String> = emptyList(),
    val canExecute: Boolean = true,
)
