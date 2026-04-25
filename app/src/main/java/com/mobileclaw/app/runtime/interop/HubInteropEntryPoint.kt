package com.mobileclaw.app.runtime.interop

import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HubInteropEntryPoint {
    fun methodDispatcher(): HubInteropMethodDispatcher

    fun appStrings(): AppStrings
}
