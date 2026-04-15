package com.mobileclaw.app.runtime.memory

import com.mobileclaw.app.runtime.extension.DefaultRuntimeExtensionRegistrations
import com.mobileclaw.app.runtime.extension.ExtensionCompatibilityReport
import com.mobileclaw.app.runtime.extension.RuntimeExtensionRegistration
import com.mobileclaw.app.runtime.extension.RuntimeExtensionType

typealias ExtensionType = RuntimeExtensionType
typealias ExtensionRegistration = RuntimeExtensionRegistration
typealias ExtensionCompatibility = ExtensionCompatibilityReport

object DefaultExtensionRegistrations {
    fun seeded(): List<ExtensionRegistration> = DefaultRuntimeExtensionRegistrations.seeded()
}
