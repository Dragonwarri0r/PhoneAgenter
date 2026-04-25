package com.mobileclaw.interop.contract

enum class InteropHandleFamily(val wireName: String) {
    SURFACE("surface"),
    GRANT_REQUEST("grant_request"),
    CONNECTED_APP("connected_app"),
    TASK("task"),
    ARTIFACT("artifact"),
    RESOURCE("resource"),
    CAPABILITY("capability"),
}

data class InteropHandle(
    val family: InteropHandleFamily,
    val value: String,
) {
    val opaqueValue: String = "${family.wireName}:$value"

    companion object {
        fun parse(raw: String): InteropHandle? {
            val familyName = raw.substringBefore(':', missingDelimiterValue = "")
            val value = raw.substringAfter(':', missingDelimiterValue = "")
            if (familyName.isBlank() || value.isBlank()) return null
            val family = InteropHandleFamily.entries.firstOrNull { it.wireName == familyName } ?: return null
            return InteropHandle(family = family, value = value)
        }
    }
}
