package com.mobileclaw.interop.android

import android.os.Bundle
import androidx.core.os.bundleOf
import com.mobileclaw.interop.android.bundle.InteropBundleCodec
import com.mobileclaw.interop.contract.CompatibilityEvaluator
import com.mobileclaw.interop.contract.InteropCompatibilitySignal
import com.mobileclaw.interop.contract.InteropVersion

object CompatibilityBundleAdapter {
    const val KEY_SIGNAL: String = "compatibility_signal"

    fun supportedSignal(
        requestedVersion: String = InteropVersion.CURRENT.value,
        supportedVersion: String = InteropVersion.CURRENT.value,
    ): InteropCompatibilitySignal {
        return CompatibilityEvaluator.evaluate(
            requestedVersion = requestedVersion,
            supportedVersion = supportedVersion,
        ).toSignal()
    }

    fun evaluateSignal(
        requestedVersion: String,
        supportedVersion: String = InteropVersion.CURRENT.value,
        unknownFieldCount: Int = 0,
        requiredUnknownFieldCount: Int = 0,
        optionalUnknownFieldCount: Int = 0,
        extensionFieldCount: Int = 0,
    ): InteropCompatibilitySignal {
        return CompatibilityEvaluator.evaluate(
            requestedVersion = requestedVersion,
            supportedVersion = supportedVersion,
            unknownFieldCount = unknownFieldCount,
            requiredUnknownFieldCount = requiredUnknownFieldCount,
            optionalUnknownFieldCount = optionalUnknownFieldCount,
            extensionFieldCount = extensionFieldCount,
        ).toSignal()
    }

    fun toBundle(signal: InteropCompatibilitySignal): Bundle {
        return bundleOf(KEY_SIGNAL to InteropBundleCodec.compatibilitySignalToBundle(signal))
    }

    fun fromBundle(bundle: Bundle?): InteropCompatibilitySignal? {
        return InteropBundleCodec.compatibilitySignalFromBundle(bundle?.getBundle(KEY_SIGNAL))
    }
}
