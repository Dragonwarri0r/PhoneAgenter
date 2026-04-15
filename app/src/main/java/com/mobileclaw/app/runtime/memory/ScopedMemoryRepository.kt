package com.mobileclaw.app.runtime.memory

import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface ScopedMemoryRepository {
    fun observeAll(): Flow<List<MemoryItem>>

    suspend fun listAll(): List<MemoryItem>

    suspend fun get(memoryId: String): MemoryItem?

    suspend fun upsert(item: MemoryItem)

    suspend fun update(memoryId: String, transform: (MemoryItem) -> MemoryItem): MemoryItem?

    suspend fun queryEligibleItems(query: RetrievalQuery): List<MemoryItem>

    suspend fun toMergeCandidate(memoryId: String): MergeCandidate?
}

@Singleton
class DefaultScopedMemoryRepository @Inject constructor(
    private val memoryDao: MemoryDao,
    private val appStrings: AppStrings,
) : ScopedMemoryRepository {
    private val seedMutex = Mutex()
    private var hasSeeded = false

    override fun observeAll(): Flow<List<MemoryItem>> = memoryDao.observeAll()

    override suspend fun listAll(): List<MemoryItem> {
        ensureSeedData()
        return memoryDao.getAll()
    }

    override suspend fun get(memoryId: String): MemoryItem? {
        ensureSeedData()
        return memoryDao.getById(memoryId)
    }

    override suspend fun upsert(item: MemoryItem) {
        ensureSeedData()
        val existing = memoryDao.getById(item.memoryId)
        val now = System.currentTimeMillis()
        val prepared = if (existing == null) {
            item.copy(
                logicalRecordId = item.logicalRecordId.ifBlank { item.memoryId },
                originDeviceId = item.originDeviceId ?: "local_device",
                originUserId = item.originUserId ?: "single_user",
                logicalVersion = item.logicalVersion.coerceAtLeast(1L),
                schemaVersion = item.schemaVersion.coerceAtLeast(1),
                createdAtEpochMillis = item.createdAtEpochMillis.takeIf { it > 0 } ?: now,
                updatedAtEpochMillis = now,
            )
        } else {
            item.copy(
                logicalRecordId = existing.logicalRecordId,
                originDeviceId = item.originDeviceId ?: existing.originDeviceId ?: "local_device",
                originUserId = item.originUserId ?: existing.originUserId ?: "single_user",
                logicalVersion = maxOf(existing.logicalVersion + 1, item.logicalVersion),
                schemaVersion = maxOf(existing.schemaVersion, item.schemaVersion, 1),
                createdAtEpochMillis = existing.createdAtEpochMillis,
                updatedAtEpochMillis = now,
            )
        }
        memoryDao.upsert(prepared)
    }

    override suspend fun update(
        memoryId: String,
        transform: (MemoryItem) -> MemoryItem,
    ): MemoryItem? {
        ensureSeedData()
        val existing = memoryDao.getById(memoryId) ?: return null
        val updated = transform(existing).copy(
            logicalRecordId = existing.logicalRecordId,
            originDeviceId = existing.originDeviceId ?: "local_device",
            originUserId = existing.originUserId ?: "single_user",
            logicalVersion = existing.logicalVersion + 1,
            schemaVersion = maxOf(existing.schemaVersion, 1),
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
        memoryDao.upsert(updated)
        return updated
    }

    override suspend fun queryEligibleItems(query: RetrievalQuery): List<MemoryItem> {
        ensureSeedData()
        return memoryDao.getAll().filter { item ->
            !item.isExpired(query.nowEpochMillis) &&
                when (item.scope) {
                    MemoryScope.GLOBAL -> true
                    MemoryScope.APP_SCOPED -> item.originApp == query.originApp
                    MemoryScope.CONTACT_SCOPED -> item.subjectKey != null && item.subjectKey == query.subjectKey
                    MemoryScope.DEVICE_SCOPED -> item.subjectKey != null && item.subjectKey == query.deviceId
                } &&
                (query.allowPrivate || item.exposurePolicy != MemoryExposurePolicy.PRIVATE)
        }
    }

    override suspend fun toMergeCandidate(memoryId: String): MergeCandidate? {
        ensureSeedData()
        return memoryDao.getById(memoryId)?.toMergeCandidate()
    }

    private suspend fun ensureSeedData() = seedMutex.withLock {
        if (hasSeeded) return
        if (memoryDao.count() == 0) {
            memoryDao.upsert(MemoryFixtures.seedItems(appStrings))
        }
        hasSeeded = true
    }
}
