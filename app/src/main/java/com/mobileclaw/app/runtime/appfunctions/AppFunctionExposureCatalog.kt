package com.mobileclaw.app.runtime.appfunctions

object AppFunctionExposureCatalog {
    const val DRAFT_REPLY_METHOD = "draftReply"
    const val EXPORT_PORTABILITY_METHOD = "exportPortableSummary"

    fun functionHintForCapability(capabilityId: String): String? = when (capabilityId) {
        "generate.reply" -> DRAFT_REPLY_METHOD
        "external.share" -> EXPORT_PORTABILITY_METHOD
        else -> null
    }
}
