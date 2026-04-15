package com.mobileclaw.app.runtime.ingress

data class InboundRuntimeRequest(
    val interopRequestId: String,
    val runtimeRequestId: String,
    val userInput: String,
    val selectedModelId: String,
    val originApp: String,
    val workspaceId: String,
    val subjectKey: String? = null,
    val envelope: InteropRequestEnvelope,
)
