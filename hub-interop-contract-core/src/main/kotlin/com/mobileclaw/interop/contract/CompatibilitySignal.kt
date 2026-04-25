package com.mobileclaw.interop.contract

enum class CompatibilityState {
    SUPPORTED,
    DOWNGRADED,
    INCOMPATIBLE,
}

enum class CompatibilityReasonCode(val wireValue: String) {
    SUPPORTED("supported"),
    REQUIRED_UNKNOWN_FIELDS("required_unknown_fields"),
    OPTIONAL_UNKNOWN_FIELDS("optional_unknown_fields"),
    EXTENSION_NAMESPACE_FIELDS("extension_namespace_fields"),
    MALFORMED_VERSION("malformed_version"),
    MAJOR_VERSION_UNSUPPORTED("major_version_unsupported"),
    MINOR_VERSION_DOWNGRADED("minor_version_downgraded"),
}

data class CompatibilityEvaluation(
    val requestedVersion: String,
    val supportedVersion: String,
    val state: CompatibilityState,
    val reasonCode: CompatibilityReasonCode,
    val unknownFieldCount: Int = 0,
    val requiredUnknownFieldCount: Int = 0,
    val optionalUnknownFieldCount: Int = 0,
    val extensionFieldCount: Int = 0,
) {
    val isCompatible: Boolean = state != CompatibilityState.INCOMPATIBLE

    fun toSignal(reason: String = reasonCode.wireValue): InteropCompatibilitySignal {
        return InteropCompatibilitySignal(
            interopVersion = requestedVersion,
            isCompatible = isCompatible,
            compatibilityReason = reason,
            unknownFieldCount = unknownFieldCount,
            compatibilityState = state,
            supportedVersion = supportedVersion,
            reasonCode = reasonCode,
            requiredUnknownFieldCount = requiredUnknownFieldCount,
            optionalUnknownFieldCount = optionalUnknownFieldCount,
            extensionFieldCount = extensionFieldCount,
        )
    }
}

data class InteropCompatibilitySignal(
    val interopVersion: String = InteropVersion.CURRENT.value,
    val isCompatible: Boolean = true,
    val compatibilityReason: String = CompatibilityReasonCode.SUPPORTED.wireValue,
    val unknownFieldCount: Int = 0,
    val compatibilityState: CompatibilityState = if (isCompatible) {
        CompatibilityState.SUPPORTED
    } else {
        CompatibilityState.INCOMPATIBLE
    },
    val supportedVersion: String = InteropVersion.CURRENT.value,
    val requiredUnknownFieldCount: Int = 0,
    val optionalUnknownFieldCount: Int = 0,
    val extensionFieldCount: Int = 0,
    val reasonCode: CompatibilityReasonCode = when {
        requiredUnknownFieldCount > 0 ||
            (unknownFieldCount > 0 && optionalUnknownFieldCount == 0 && extensionFieldCount == 0) -> {
            CompatibilityReasonCode.REQUIRED_UNKNOWN_FIELDS
        }
        optionalUnknownFieldCount > 0 -> CompatibilityReasonCode.OPTIONAL_UNKNOWN_FIELDS
        extensionFieldCount > 0 -> CompatibilityReasonCode.EXTENSION_NAMESPACE_FIELDS
        compatibilityState == CompatibilityState.DOWNGRADED -> CompatibilityReasonCode.MINOR_VERSION_DOWNGRADED
        compatibilityState == CompatibilityState.INCOMPATIBLE -> CompatibilityReasonCode.MAJOR_VERSION_UNSUPPORTED
        else -> CompatibilityReasonCode.SUPPORTED
    },
)

object CompatibilityEvaluator {
    fun evaluate(
        requestedVersion: String,
        supportedVersion: String = InteropVersion.CURRENT.value,
        unknownFieldCount: Int = 0,
        requiredUnknownFieldCount: Int = 0,
        optionalUnknownFieldCount: Int = 0,
        extensionFieldCount: Int = 0,
    ): CompatibilityEvaluation {
        val requested = InteropVersion.parse(requestedVersion)
        val supported = InteropVersion.parse(supportedVersion)
        if (requested == null || supported == null) {
            return CompatibilityEvaluation(
                requestedVersion = requestedVersion,
                supportedVersion = supportedVersion,
                state = CompatibilityState.INCOMPATIBLE,
                reasonCode = CompatibilityReasonCode.MALFORMED_VERSION,
            )
        }

        val effectiveRequiredUnknownFieldCount = unknownFieldCount + requiredUnknownFieldCount
        val totalUnknownFieldCount = effectiveRequiredUnknownFieldCount +
            optionalUnknownFieldCount +
            extensionFieldCount
        if (effectiveRequiredUnknownFieldCount > 0) {
            return CompatibilityEvaluation(
                requestedVersion = requested.value,
                supportedVersion = supported.value,
                state = CompatibilityState.INCOMPATIBLE,
                reasonCode = CompatibilityReasonCode.REQUIRED_UNKNOWN_FIELDS,
                unknownFieldCount = totalUnknownFieldCount,
                requiredUnknownFieldCount = effectiveRequiredUnknownFieldCount,
                optionalUnknownFieldCount = optionalUnknownFieldCount,
                extensionFieldCount = extensionFieldCount,
            )
        }

        return when {
            requested.major != supported.major -> CompatibilityEvaluation(
                requestedVersion = requested.value,
                supportedVersion = supported.value,
                state = CompatibilityState.INCOMPATIBLE,
                reasonCode = CompatibilityReasonCode.MAJOR_VERSION_UNSUPPORTED,
            )

            requested.minor > supported.minor -> CompatibilityEvaluation(
                requestedVersion = requested.value,
                supportedVersion = supported.value,
                state = CompatibilityState.DOWNGRADED,
                reasonCode = CompatibilityReasonCode.MINOR_VERSION_DOWNGRADED,
            )

            optionalUnknownFieldCount > 0 -> CompatibilityEvaluation(
                requestedVersion = requested.value,
                supportedVersion = supported.value,
                state = CompatibilityState.DOWNGRADED,
                reasonCode = CompatibilityReasonCode.OPTIONAL_UNKNOWN_FIELDS,
                unknownFieldCount = totalUnknownFieldCount,
                optionalUnknownFieldCount = optionalUnknownFieldCount,
                extensionFieldCount = extensionFieldCount,
            )

            extensionFieldCount > 0 -> CompatibilityEvaluation(
                requestedVersion = requested.value,
                supportedVersion = supported.value,
                state = CompatibilityState.SUPPORTED,
                reasonCode = CompatibilityReasonCode.EXTENSION_NAMESPACE_FIELDS,
                unknownFieldCount = totalUnknownFieldCount,
                extensionFieldCount = extensionFieldCount,
            )

            else -> CompatibilityEvaluation(
                requestedVersion = requested.value,
                supportedVersion = supported.value,
                state = CompatibilityState.SUPPORTED,
                reasonCode = CompatibilityReasonCode.SUPPORTED,
            )
        }
    }
}
