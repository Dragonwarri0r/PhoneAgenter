package com.mobileclaw.app.runtime.provider

import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.session.RuntimePlan
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class MockCapabilityProvider @Inject constructor(
    private val appStrings: AppStrings,
) : CapabilityProvider {
    private val supportedCapabilities = setOf(
        "generate.reply",
        "message.send",
        "calendar.write",
        "external.share",
        "ui.act",
        "sensitive.write",
    )

    override val providerId: String = "mock_generation"

    override fun supports(plan: RuntimePlan): Boolean {
        return plan.selectedCapabilityId in supportedCapabilities &&
            plan.providerHint == providerId
    }

    override fun execute(
        request: CapabilityExecutionRequest,
    ): Flow<ProviderExecutionEvent> = flow {
        emit(
            ProviderExecutionEvent.ExecutionStarted(
                capabilityId = request.plan.selectedCapabilityId,
                providerId = providerId,
            ),
        )
        val chunks = listOf(
            appStrings.get(R.string.mock_chunk_selected),
            appStrings.get(R.string.mock_chunk_validates),
            appStrings.get(R.string.mock_chunk_contract),
        )
        for (chunk in chunks) {
            delay(40)
            emit(
                ProviderExecutionEvent.OutputChunk(
                    capabilityId = request.plan.selectedCapabilityId,
                    providerId = providerId,
                    chunk = chunk,
                ),
            )
        }
        emit(
            ProviderExecutionEvent.ExecutionCompleted(
                capabilityId = request.plan.selectedCapabilityId,
                providerId = providerId,
                outputText = chunks.joinToString(separator = ""),
            ),
        )
    }
}
