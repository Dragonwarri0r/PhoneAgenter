package com.mobileclaw.app.runtime.capability

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StandardToolCatalog @Inject constructor(
    private val appStrings: AppStrings,
) {
    fun descriptorForCapability(capabilityId: String): ToolDescriptor {
        return when (capabilityId) {
            "generate.reply" -> descriptor(
                toolId = "generate.reply",
                capabilityId = capabilityId,
                displayNameRes = R.string.tool_generate_reply_name,
                descriptionRes = R.string.tool_generate_reply_description,
                sideEffectType = ToolSideEffectType.READ,
                invocationKind = ToolInvocationKind.REPLY,
                freeformSelectionPolicy = FreeformSelectionPolicy.FALLBACK_TO_REPLY,
                riskLevelHint = "low",
                requiredScopes = listOf("reply.generate"),
                confirmationPolicy = ConfirmationPolicy.NONE,
                bindings = listOf(
                    ToolBindingDescriptor(
                        bindingId = "local.generate.reply",
                        bindingType = ProviderType.LOCAL,
                        androidContract = "local_generation",
                        primary = true,
                    ),
                ),
                inputSchema = ToolSchemaDescriptor(
                    schemaId = "tool.generate.reply.input",
                    schemaJson = """{"type":"object","required":["input"],"properties":{"input":{"type":"string"}}}""",
                    requiredFields = listOf("input"),
                    previewFields = listOf("input"),
                    supportsPartial = false,
                ),
                selectionExamples = listOf("help me draft a reply", "summarize this"),
            )

            "calendar.read" -> descriptor(
                toolId = "calendar.read",
                capabilityId = capabilityId,
                displayNameRes = R.string.tool_calendar_read_name,
                descriptionRes = R.string.tool_calendar_read_description,
                sideEffectType = ToolSideEffectType.READ,
                invocationKind = ToolInvocationKind.EXPLICIT_READ,
                freeformSelectionPolicy = FreeformSelectionPolicy.SAFE_AUTO,
                riskLevelHint = "low",
                requiredScopes = listOf("calendar.read"),
                confirmationPolicy = ConfirmationPolicy.NONE,
                bindings = listOf(
                    ToolBindingDescriptor(
                        bindingId = "system.calendar.provider.read",
                        bindingType = ProviderType.CONTENT_RESOLVER,
                        androidContract = "calendar_provider_read",
                        primary = true,
                    ),
                ),
                inputSchema = ToolSchemaDescriptor(
                    schemaId = "tool.calendar.read.input",
                    schemaJson = """{"type":"object","properties":{"query":{"type":"string"}}}""",
                    previewFields = listOf("query"),
                ),
                defaultResultLimit = 5,
                selectionExamples = listOf("what's on my calendar today", "show my schedule this afternoon"),
            )

            "contacts.read" -> descriptor(
                toolId = "contacts.read",
                capabilityId = capabilityId,
                displayNameRes = R.string.tool_contacts_read_name,
                descriptionRes = R.string.tool_contacts_read_description,
                sideEffectType = ToolSideEffectType.READ,
                invocationKind = ToolInvocationKind.EXPLICIT_READ,
                freeformSelectionPolicy = FreeformSelectionPolicy.SAFE_AUTO,
                riskLevelHint = "low",
                requiredScopes = listOf("contacts.read"),
                confirmationPolicy = ConfirmationPolicy.NONE,
                bindings = listOf(
                    ToolBindingDescriptor(
                        bindingId = "system.contacts.provider.read",
                        bindingType = ProviderType.CONTENT_RESOLVER,
                        androidContract = "contacts_provider_read",
                        primary = true,
                    ),
                ),
                inputSchema = ToolSchemaDescriptor(
                    schemaId = "tool.contacts.read.input",
                    schemaJson = """{"type":"object","properties":{"query":{"type":"string"}}}""",
                    previewFields = listOf("query"),
                ),
                defaultResultLimit = 3,
            )

            "calendar.write" -> descriptor(
                toolId = "calendar.write",
                capabilityId = capabilityId,
                displayNameRes = R.string.tool_calendar_write_name,
                descriptionRes = R.string.tool_calendar_write_description,
                sideEffectType = ToolSideEffectType.WRITE,
                invocationKind = ToolInvocationKind.SIDE_EFFECT,
                freeformSelectionPolicy = FreeformSelectionPolicy.POLICY_GATED,
                riskLevelHint = "high",
                requiredScopes = listOf("calendar.write"),
                confirmationPolicy = ConfirmationPolicy.REQUIRE_CONFIRMATION,
                bindings = listOf(
                    ToolBindingDescriptor(
                        bindingId = "system.calendar.provider.write",
                        bindingType = ProviderType.CONTENT_RESOLVER,
                        androidContract = "calendar_provider_write",
                        primary = true,
                    ),
                    ToolBindingDescriptor(
                        bindingId = "intent.calendar.write",
                        bindingType = ProviderType.INTENT,
                        androidContract = "Intent.ACTION_INSERT(CalendarContract.Events)",
                    ),
                ),
                inputSchema = ToolSchemaDescriptor(
                    schemaId = "tool.calendar.write.input",
                    schemaJson = """{"type":"object","properties":{"title":{"type":"string"},"time":{"type":"string"},"description":{"type":"string"}}}""",
                    requiredFields = listOf("title"),
                    previewFields = listOf("title", "time", "description"),
                ),
            )

            "calendar.delete" -> descriptor(
                toolId = "calendar.delete",
                capabilityId = capabilityId,
                displayNameRes = R.string.tool_calendar_delete_name,
                descriptionRes = R.string.tool_calendar_delete_description,
                sideEffectType = ToolSideEffectType.WRITE,
                invocationKind = ToolInvocationKind.SIDE_EFFECT,
                freeformSelectionPolicy = FreeformSelectionPolicy.POLICY_GATED,
                riskLevelHint = "high",
                requiredScopes = listOf("calendar.delete"),
                confirmationPolicy = ConfirmationPolicy.REQUIRE_CONFIRMATION,
                bindings = listOf(
                    ToolBindingDescriptor(
                        bindingId = "system.calendar.provider.delete",
                        bindingType = ProviderType.CONTENT_RESOLVER,
                        androidContract = "calendar_provider_delete",
                        primary = true,
                    ),
                ),
                inputSchema = ToolSchemaDescriptor(
                    schemaId = "tool.calendar.delete.input",
                    schemaJson = """{"type":"object","properties":{"title":{"type":"string"},"time":{"type":"string"}}}""",
                    requiredFields = listOf("title"),
                    previewFields = listOf("title", "time"),
                ),
                selectionExamples = listOf("delete my lunch with Bob tomorrow", "删除明天下午和 Bob 的会议"),
            )

            "alarm.set" -> descriptor(
                toolId = "alarm.set",
                capabilityId = capabilityId,
                displayNameRes = R.string.tool_alarm_set_name,
                descriptionRes = R.string.tool_alarm_set_description,
                sideEffectType = ToolSideEffectType.WRITE,
                invocationKind = ToolInvocationKind.SIDE_EFFECT,
                freeformSelectionPolicy = FreeformSelectionPolicy.POLICY_GATED,
                riskLevelHint = "high",
                requiredScopes = listOf("alarm.set"),
                confirmationPolicy = ConfirmationPolicy.REQUIRE_CONFIRMATION,
                bindings = listOf(
                    ToolBindingDescriptor(
                        bindingId = "intent.alarm.set",
                        bindingType = ProviderType.INTENT,
                        androidContract = "AlarmClock.ACTION_SET_ALARM",
                        primary = true,
                    ),
                ),
                inputSchema = ToolSchemaDescriptor(
                    schemaId = "tool.alarm.set.input",
                    schemaJson = """{"type":"object","properties":{"time":{"type":"string"},"label":{"type":"string"}}}""",
                    requiredFields = listOf("time"),
                    previewFields = listOf("time", "label"),
                ),
            )

            "alarm.show" -> descriptor(
                toolId = "alarm.show",
                capabilityId = capabilityId,
                displayNameRes = R.string.tool_alarm_show_name,
                descriptionRes = R.string.tool_alarm_show_description,
                sideEffectType = ToolSideEffectType.READ,
                invocationKind = ToolInvocationKind.EXPLICIT_READ,
                freeformSelectionPolicy = FreeformSelectionPolicy.SAFE_AUTO,
                riskLevelHint = "low",
                requiredScopes = listOf("alarm.show"),
                confirmationPolicy = ConfirmationPolicy.NONE,
                bindings = listOf(
                    ToolBindingDescriptor(
                        bindingId = "intent.alarm.show",
                        bindingType = ProviderType.INTENT,
                        androidContract = "AlarmClock.ACTION_SHOW_ALARMS",
                        primary = true,
                    ),
                ),
                inputSchema = ToolSchemaDescriptor(
                    schemaId = "tool.alarm.show.input",
                    schemaJson = """{"type":"object"}""",
                    supportsPartial = false,
                ),
            )

            "alarm.dismiss" -> descriptor(
                toolId = "alarm.dismiss",
                capabilityId = capabilityId,
                displayNameRes = R.string.tool_alarm_dismiss_name,
                descriptionRes = R.string.tool_alarm_dismiss_description,
                sideEffectType = ToolSideEffectType.WRITE,
                invocationKind = ToolInvocationKind.SIDE_EFFECT,
                freeformSelectionPolicy = FreeformSelectionPolicy.POLICY_GATED,
                riskLevelHint = "high",
                requiredScopes = listOf("alarm.dismiss"),
                confirmationPolicy = ConfirmationPolicy.REQUIRE_CONFIRMATION,
                bindings = listOf(
                    ToolBindingDescriptor(
                        bindingId = "intent.alarm.dismiss",
                        bindingType = ProviderType.INTENT,
                        androidContract = "AlarmClock.ACTION_DISMISS_ALARM",
                        primary = true,
                    ),
                ),
                inputSchema = ToolSchemaDescriptor(
                    schemaId = "tool.alarm.dismiss.input",
                    schemaJson = """{"type":"object","properties":{"label":{"type":"string"}}}""",
                    previewFields = listOf("label"),
                ),
            )

            "message.send" -> descriptor(
                toolId = "message.send",
                capabilityId = capabilityId,
                displayNameRes = R.string.tool_message_send_name,
                descriptionRes = R.string.tool_message_send_description,
                sideEffectType = ToolSideEffectType.DISPATCH,
                invocationKind = ToolInvocationKind.SIDE_EFFECT,
                freeformSelectionPolicy = FreeformSelectionPolicy.POLICY_GATED,
                riskLevelHint = "high",
                requiredScopes = listOf("message.send"),
                confirmationPolicy = ConfirmationPolicy.REQUIRE_CONFIRMATION,
                bindings = listOf(
                    ToolBindingDescriptor(
                        bindingId = "intent.message.send",
                        bindingType = ProviderType.INTENT,
                        androidContract = "Intent.ACTION_SENDTO(smsto:)",
                        primary = true,
                    ),
                ),
                inputSchema = ToolSchemaDescriptor(
                    schemaId = "tool.message.send.input",
                    schemaJson = """{"type":"object","properties":{"recipient":{"type":"string"},"body":{"type":"string"}}}""",
                    requiredFields = listOf("body"),
                    previewFields = listOf("recipient", "body"),
                ),
            )

            "external.share" -> descriptor(
                toolId = "share.outbound",
                capabilityId = capabilityId,
                displayNameRes = R.string.tool_share_outbound_name,
                descriptionRes = R.string.tool_share_outbound_description,
                sideEffectType = ToolSideEffectType.DISPATCH,
                invocationKind = ToolInvocationKind.SIDE_EFFECT,
                freeformSelectionPolicy = FreeformSelectionPolicy.POLICY_GATED,
                riskLevelHint = "high",
                requiredScopes = listOf("external.share"),
                confirmationPolicy = ConfirmationPolicy.REQUIRE_CONFIRMATION,
                bindings = listOf(
                    ToolBindingDescriptor(
                        bindingId = "share.outbound.dispatch",
                        bindingType = ProviderType.SHARE,
                        androidContract = "Intent.ACTION_SEND chooser",
                        primary = true,
                    ),
                ),
                inputSchema = ToolSchemaDescriptor(
                    schemaId = "tool.share.outbound.input",
                    schemaJson = """{"type":"object","properties":{"destination":{"type":"string"},"content":{"type":"string"}}}""",
                    requiredFields = listOf("content"),
                    previewFields = listOf("destination", "content"),
                ),
            )

            else -> descriptor(
                toolId = capabilityId,
                capabilityId = capabilityId,
                displayNameRes = R.string.tool_generic_name,
                descriptionRes = R.string.tool_generic_description,
                sideEffectType = ToolSideEffectType.WRITE,
                invocationKind = ToolInvocationKind.SIDE_EFFECT,
                freeformSelectionPolicy = FreeformSelectionPolicy.POLICY_GATED,
                riskLevelHint = "unknown",
                requiredScopes = emptyList(),
                confirmationPolicy = ConfirmationPolicy.PREVIEW_FIRST,
                bindings = emptyList(),
                inputSchema = ToolSchemaDescriptor(
                    schemaId = "tool.generic.input",
                    schemaJson = """{"type":"object"}""",
                ),
            )
        }
    }

    private fun descriptor(
        toolId: String,
        capabilityId: String,
        displayNameRes: Int,
        descriptionRes: Int,
        sideEffectType: ToolSideEffectType,
        invocationKind: ToolInvocationKind,
        freeformSelectionPolicy: FreeformSelectionPolicy,
        riskLevelHint: String,
        requiredScopes: List<String>,
        confirmationPolicy: ConfirmationPolicy,
        bindings: List<ToolBindingDescriptor>,
        inputSchema: ToolSchemaDescriptor,
        defaultResultLimit: Int = 0,
        selectionExamples: List<String> = emptyList(),
    ): ToolDescriptor {
        return ToolDescriptor(
            toolId = toolId,
            legacyCapabilityId = capabilityId,
            displayName = appStrings.get(displayNameRes),
            description = appStrings.get(descriptionRes),
            inputSchema = inputSchema,
            sideEffectType = sideEffectType,
            invocationKind = invocationKind,
            freeformSelectionPolicy = freeformSelectionPolicy,
            riskLevelHint = riskLevelHint,
            requiredScopes = requiredScopes,
            confirmationPolicy = confirmationPolicy,
            bindingDescriptors = bindings,
            defaultResultLimit = defaultResultLimit,
            selectionExamples = selectionExamples,
        )
    }
}
