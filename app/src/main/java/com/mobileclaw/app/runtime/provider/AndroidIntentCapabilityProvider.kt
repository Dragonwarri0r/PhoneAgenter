package com.mobileclaw.app.runtime.provider

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.capability.ProviderDescriptor
import com.mobileclaw.app.runtime.session.RuntimePlan
import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class AndroidIntentCapabilityProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appStrings: AppStrings,
) : CapabilityProvider {
    override val providerId: String = "android_intent_dispatch"

    override fun supports(plan: RuntimePlan): Boolean {
        return plan.selectedCapabilityId in setOf(
            "message.send",
            "calendar.write",
            "external.share",
            "alarm.set",
            "alarm.show",
            "alarm.dismiss",
        )
    }

    override fun execute(
        request: CapabilityExecutionRequest,
    ): Flow<ProviderExecutionEvent> = flow {
        val descriptor = request.providerDescriptor
        if (descriptor == null) {
            emitFailure(request, appStrings.get(R.string.bridge_execution_missing_route))
            return@flow
        }

        val intent = buildIntent(
            capabilityId = request.plan.selectedCapabilityId,
            request = request,
            descriptor = descriptor,
        )
        if (intent == null) {
            emitFailure(
                request,
                appStrings.get(
                    R.string.bridge_execution_capability_not_supported,
                    request.plan.selectedCapabilityId,
                ),
            )
            return@flow
        }

        val packageManager = context.packageManager
        if (intent.resolveActivity(packageManager) == null) {
            emitFailure(request, appStrings.get(R.string.bridge_execution_target_unavailable))
            return@flow
        }

        emit(
            ProviderExecutionEvent.ExecutionStarted(
                capabilityId = request.plan.selectedCapabilityId,
                providerId = providerId,
            ),
        )

        runCatching {
            context.startActivity(intent)
        }.onSuccess {
            emit(
                ProviderExecutionEvent.ExecutionCompleted(
                    capabilityId = request.plan.selectedCapabilityId,
                    providerId = providerId,
                    outputText = appStrings.get(
                        R.string.bridge_execution_launched_target,
                        descriptor.providerLabel,
                    ),
                ),
            )
        }.onFailure { throwable ->
            emitFailure(
                request,
                throwable.message ?: appStrings.get(R.string.bridge_execution_launch_failed),
            )
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<ProviderExecutionEvent>.emitFailure(
        request: CapabilityExecutionRequest,
        message: String,
    ) {
        emit(
            ProviderExecutionEvent.ExecutionFailed(
                capabilityId = request.plan.selectedCapabilityId,
                providerId = providerId,
                userMessage = message,
            ),
        )
    }

    private fun buildIntent(
        capabilityId: String,
        request: CapabilityExecutionRequest,
        descriptor: ProviderDescriptor,
    ): Intent? {
        return when (capabilityId) {
            "message.send" -> {
                val body = request.structuredPayload?.fieldValue("body").orEmpty().ifBlank {
                    request.request.userInput
                }
                Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:")
                    putExtra("sms_body", body)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    descriptor.providerApp
                        .takeUnless { it.isBlank() || it == "android.intent.seeded" }
                        ?.let(::setPackage)
                }
            }

            "calendar.write" -> {
                val title = request.structuredPayload?.fieldValue("title").orEmpty().ifBlank {
                    request.request.userInput.take(80)
                }
                val description = request.structuredPayload?.fieldValue("description").orEmpty().ifBlank {
                    request.request.userInput
                }
                Intent(Intent.ACTION_INSERT).apply {
                    data = CalendarContract.Events.CONTENT_URI
                    putExtra(CalendarContract.Events.TITLE, title)
                    putExtra(CalendarContract.Events.DESCRIPTION, description)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    descriptor.providerApp
                        .takeUnless { it.isBlank() || it == "android.intent.seeded" }
                        ?.let(::setPackage)
                }
            }

            "external.share" -> {
                val shareText = request.structuredPayload?.fieldValue("content").orEmpty().ifBlank {
                    request.request.userInput
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    descriptor.providerApp
                        .takeUnless { it.isBlank() || it == "android.share.seeded" }
                        ?.let(::setPackage)
                }
                Intent.createChooser(
                    shareIntent,
                    appStrings.get(R.string.bridge_execution_share_chooser_title),
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            "alarm.set" -> {
                val label = request.structuredPayload?.fieldValue("label").orEmpty().ifBlank {
                    request.request.userInput.take(60)
                }
                Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_MESSAGE, label)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    descriptor.providerApp
                        .takeUnless { it.isBlank() || it == "android.intent.seeded" }
                        ?.let(::setPackage)
                }
            }

            "alarm.show" -> {
                Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    descriptor.providerApp
                        .takeUnless { it.isBlank() || it == "android.intent.seeded" }
                        ?.let(::setPackage)
                }
            }

            "alarm.dismiss" -> {
                Intent(AlarmClock.ACTION_DISMISS_ALARM).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    descriptor.providerApp
                        .takeUnless { it.isBlank() || it == "android.intent.seeded" }
                        ?.let(::setPackage)
                }
            }

            else -> null
        }
    }
}
