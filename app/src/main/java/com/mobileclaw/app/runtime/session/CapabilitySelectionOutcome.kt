package com.mobileclaw.app.runtime.session

import com.mobileclaw.app.runtime.capability.CapabilityAvailabilityState
import com.mobileclaw.app.runtime.capability.ToolSideEffectType
import com.mobileclaw.app.runtime.capability.ToolVisibilitySnapshot

enum class CapabilitySelectionSource {
    EXPLICIT_HINT,
    MODEL_PLANNER,
    FREEFORM_INFERENCE,
    REPLY_FALLBACK,
}

enum class CapabilityResolutionMode {
    REPLY_FALLBACK,
    EXPLICIT_READ,
    EXPLICIT_ACTION,
    UNAVAILABLE,
    CLARIFICATION_NEEDED,
}

data class CapabilitySelectionCandidate(
    val capabilityId: String,
    val toolId: String,
    val selectionSource: CapabilitySelectionSource,
    val confidence: Double,
    val sideEffectType: ToolSideEffectType,
    val availabilityState: CapabilityAvailabilityState,
    val selectionReason: String,
    val providerOptions: List<String> = emptyList(),
)

data class CapabilitySelectionOutcome(
    val selectedCapabilityId: String,
    val selectedToolId: String? = null,
    val selectedProviderId: String? = null,
    val selectionSource: CapabilitySelectionSource,
    val resolutionMode: CapabilityResolutionMode,
    val confidence: Double,
    val explanation: String,
    val warnings: List<String> = emptyList(),
    val visibilitySnapshot: ToolVisibilitySnapshot? = null,
    val candidateSummaries: List<String> = emptyList(),
)
