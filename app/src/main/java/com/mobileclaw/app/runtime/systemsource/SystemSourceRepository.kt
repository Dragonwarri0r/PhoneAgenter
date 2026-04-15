package com.mobileclaw.app.runtime.systemsource

import kotlinx.coroutines.flow.Flow

interface SystemSourceRepository {
    fun observeDescriptors(): Flow<List<SystemSourceDescriptor>>

    suspend fun currentDescriptors(): List<SystemSourceDescriptor>

    fun refresh()
}
