package com.mobileclaw.interop.android

import android.os.Bundle
import com.mobileclaw.interop.android.bundle.ArtifactBundles
import com.mobileclaw.interop.android.bundle.AuthorizationBundles
import com.mobileclaw.interop.android.bundle.DiscoveryBundles
import com.mobileclaw.interop.android.bundle.InvocationBundles
import com.mobileclaw.interop.android.bundle.TaskBundles

object HubInteropRequestFactory {
    fun discovery(args: DiscoveryBundles.Request = DiscoveryBundles.Request()): Bundle {
        return DiscoveryBundles.toBundle(args)
    }

    fun authorization(args: AuthorizationBundles.Request): Bundle {
        return AuthorizationBundles.toBundle(args)
    }

    fun invocation(args: InvocationBundles.Request): Bundle {
        return InvocationBundles.toBundle(args)
    }

    fun task(args: TaskBundles.Request): Bundle {
        return TaskBundles.toBundle(args)
    }

    fun artifact(args: ArtifactBundles.Request): Bundle {
        return ArtifactBundles.toBundle(args)
    }
}
