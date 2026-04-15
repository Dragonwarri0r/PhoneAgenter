package com.mobileclaw.app.runtime.memory

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_items ORDER BY updatedAtEpochMillis DESC")
    fun observeAll(): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memory_items ORDER BY updatedAtEpochMillis DESC")
    suspend fun getAll(): List<MemoryItem>

    @Query("SELECT * FROM memory_items WHERE memoryId = :memoryId LIMIT 1")
    suspend fun getById(memoryId: String): MemoryItem?

    @Query("SELECT COUNT(*) FROM memory_items")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(items: List<MemoryItem>)

    @Upsert
    suspend fun upsert(item: MemoryItem)
}
