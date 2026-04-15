package com.mobileclaw.app.runtime.action

enum class StructuredActionType(
    val capabilityId: String,
) {
    MESSAGE_SEND("message.send"),
    CALENDAR_WRITE("calendar.write"),
    EXTERNAL_SHARE("external.share"),
    ;

    companion object {
        fun fromCapabilityId(capabilityId: String): StructuredActionType? {
            return entries.firstOrNull { it.capabilityId == capabilityId }
        }
    }
}
