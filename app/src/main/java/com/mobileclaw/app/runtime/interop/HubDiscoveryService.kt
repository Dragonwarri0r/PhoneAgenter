package com.mobileclaw.app.runtime.interop

import com.mobileclaw.interop.android.HubInteropStatus
import com.mobileclaw.interop.android.HubInteropStatusMapper
import com.mobileclaw.interop.android.bundle.DiscoveryBundles
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HubDiscoveryService @Inject constructor(
    private val surfaceDescriptorAssembler: HubSurfaceDescriptorAssembler,
    private val compatibilityService: HubInteropCompatibilityService,
) {
    fun discover(request: DiscoveryBundles.Request): DiscoveryBundles.Response {
        val compatibility = compatibilityService.evaluate(request.requestedVersion)
        val status = HubInteropStatusMapper.merge(
            baseStatus = HubInteropStatus.OK,
            compatibilitySignal = compatibility,
        )
        return DiscoveryBundles.Response(
            status = status,
            compatibilitySignal = compatibility,
            surfaceDescriptor = surfaceDescriptorAssembler.assemble(),
        )
    }
}
