package com.mobileclaw.app.runtime.persona

import com.mobileclaw.app.runtime.strings.AppStrings

data class PersonaProfile(
    val personaId: String = DEFAULT_PERSONA_ID,
    val displayName: String = "",
    val verbosity: PersonaVerbosity = PersonaVerbosity.LOW,
    val warmth: PersonaWarmth = PersonaWarmth.MEDIUM,
    val confirmationStyle: ConfirmationStyle = ConfirmationStyle.ASK_BEFORE_SCHEDULING,
    val avoidOvercommitment: Boolean = true,
    val askBeforeScheduling: Boolean = true,
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),
) {
    fun summaryText(strings: AppStrings): String {
        return strings.personaSummary(this)
    }

    companion object {
        const val DEFAULT_PERSONA_ID = "default_self"
    }
}

enum class PersonaVerbosity {
    LOW,
    MEDIUM,
    HIGH,
}

enum class PersonaWarmth {
    LOW,
    MEDIUM,
    HIGH,
}

enum class ConfirmationStyle {
    ASK_BEFORE_ACTION,
    ASK_BEFORE_SCHEDULING,
    DIRECT_WHEN_SAFE,
}
