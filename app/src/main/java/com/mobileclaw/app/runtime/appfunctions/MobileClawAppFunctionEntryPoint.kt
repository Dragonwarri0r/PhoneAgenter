package com.mobileclaw.app.runtime.appfunctions

import com.mobileclaw.app.runtime.localchat.LocalChatGateway
import com.mobileclaw.app.runtime.localchat.LocalModelCatalog
import com.mobileclaw.app.runtime.memory.ExportDecisionService
import com.mobileclaw.app.runtime.memory.PortabilityBundleFormatter
import com.mobileclaw.app.runtime.memory.ScopedMemoryRepository
import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MobileClawAppFunctionEntryPoint {
    fun localModelCatalog(): LocalModelCatalog

    fun localChatGateway(): LocalChatGateway

    fun scopedMemoryRepository(): ScopedMemoryRepository

    fun exportDecisionService(): ExportDecisionService

    fun portabilityBundleFormatter(): PortabilityBundleFormatter

    fun appStrings(): AppStrings
}
