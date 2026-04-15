package com.mobileclaw.app.ui.agentworkspace.model

import com.mobileclaw.app.runtime.localchat.ModelAvailabilityStatus

data class ModelHealthUiModel(
    val modelId: String = "",
    val displayName: String = "",
    val availabilityStatus: ModelAvailabilityStatus = ModelAvailabilityStatus.UNAVAILABLE,
    val headline: String = "",
    val supportingText: String = "",
    val primaryActionLabel: String? = null,
    val supportsImage: Boolean = false,
    val supportsAudio: Boolean = false,
)
