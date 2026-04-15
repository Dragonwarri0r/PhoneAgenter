package com.mobileclaw.app.runtime.action

data class StructuredActionPayload(
    val actionType: StructuredActionType,
    val completenessState: PayloadCompletenessState,
    val fields: Map<String, String>,
    val evidence: List<PayloadFieldEvidence> = emptyList(),
    val warnings: List<String> = emptyList(),
) {
    fun fieldValue(name: String): String = fields[name].orEmpty()
}
