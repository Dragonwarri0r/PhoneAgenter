package com.mobileclaw.app.runtime.provider

import com.mobileclaw.app.runtime.capability.ProviderDescriptor
import com.mobileclaw.app.runtime.session.RuntimePlan
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapabilityProviderRegistry @Inject constructor(
    localGenerationProvider: LocalGenerationProvider,
    mockCapabilityProvider: MockCapabilityProvider,
    androidIntentCapabilityProvider: AndroidIntentCapabilityProvider,
) {
    private val providers = linkedMapOf(
        localGenerationProvider.providerId to localGenerationProvider,
        mockCapabilityProvider.providerId to mockCapabilityProvider,
        androidIntentCapabilityProvider.providerId to androidIntentCapabilityProvider,
    )

    fun getProvider(plan: RuntimePlan): CapabilityProvider? {
        return providers.values.firstOrNull { provider ->
            provider.supports(plan) &&
                (plan.providerHint == null || provider.providerId == plan.providerHint)
        }
    }

    fun getProvider(providerId: String): CapabilityProvider? = providers[providerId]

    fun getProvider(descriptor: ProviderDescriptor): CapabilityProvider? {
        return providers[descriptor.executorProviderId]
    }
}
