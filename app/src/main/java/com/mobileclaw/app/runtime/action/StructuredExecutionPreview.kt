package com.mobileclaw.app.runtime.action

data class StructuredExecutionPreview(
    val title: String,
    val summary: String,
    val fieldLines: List<String>,
    val warnings: List<String>,
    val completenessState: PayloadCompletenessState,
)
