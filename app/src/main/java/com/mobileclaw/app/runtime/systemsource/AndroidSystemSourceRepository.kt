package com.mobileclaw.app.runtime.systemsource

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.strings.AppStrings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AndroidSystemSourceRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appStrings: AppStrings,
) : SystemSourceRepository {
    private val descriptorsFlow = MutableStateFlow(buildDescriptors())

    override fun observeDescriptors(): Flow<List<SystemSourceDescriptor>> = descriptorsFlow.asStateFlow()

    override suspend fun currentDescriptors(): List<SystemSourceDescriptor> = buildDescriptors()

    override fun refresh() {
        descriptorsFlow.value = buildDescriptors()
    }

    private fun buildDescriptors(): List<SystemSourceDescriptor> {
        return listOf(
            descriptor(
                sourceId = SystemSourceId.CONTACTS,
                permission = Manifest.permission.READ_CONTACTS,
                label = appStrings.get(R.string.system_source_contacts),
            ),
            descriptor(
                sourceId = SystemSourceId.CALENDAR,
                permission = Manifest.permission.READ_CALENDAR,
                label = appStrings.get(R.string.system_source_calendar),
            ),
        )
    }

    private fun descriptor(
        sourceId: SystemSourceId,
        permission: String,
        label: String,
    ): SystemSourceDescriptor {
        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        return SystemSourceDescriptor(
            sourceId = sourceId,
            displayName = label,
            permissionName = permission,
            isGranted = granted,
            availabilitySummary = if (granted) {
                appStrings.get(R.string.system_source_available, label)
            } else {
                appStrings.get(R.string.system_source_permission_missing, label)
            },
        )
    }
}
