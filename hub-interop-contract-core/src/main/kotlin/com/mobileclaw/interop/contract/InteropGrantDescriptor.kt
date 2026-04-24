package com.mobileclaw.interop.contract

enum class InteropGrantDirection {
    INBOUND,
    OUTBOUND,
    BIDIRECTIONAL,
}

enum class InteropGrantLifetime {
    ONCE,
    SESSION,
    PERSISTENT,
}

enum class InteropGrantState {
    ACTIVE,
    PENDING,
    DENIED,
    REVOKED,
    EXPIRED,
}

data class InteropGrantDescriptor(
    val handle: InteropHandle,
    val direction: InteropGrantDirection,
    val lifetime: InteropGrantLifetime,
    val scopes: List<String>,
    val authorizationRequirement: InteropAuthorizationRequirement = InteropAuthorizationRequirement.USER_CONSENT,
    val isActive: Boolean = true,
    val expiresAtEpochMillis: Long? = null,
    val state: InteropGrantState = if (isActive) InteropGrantState.ACTIVE else InteropGrantState.PENDING,
    val requestedAtEpochMillis: Long? = null,
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),
)
