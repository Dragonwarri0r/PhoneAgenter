package com.mobileclaw.app.runtime.knowledge

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "knowledge_assets")
data class KnowledgeAssetEntity(
    @PrimaryKey val knowledgeAssetId: String,
    val title: String,
    val sourceType: KnowledgeSourceType,
    val provenanceLabel: String,
    val sourceUris: List<String>,
    val sourceLabels: List<String>,
    val documentCount: Int,
    val indexedDocumentCount: Int,
    val indexedChunkCount: Int,
    val lastKnownFreshnessEpochMillis: Long?,
    val lastRetrievedAtEpochMillis: Long?,
    val retrievalCount: Int,
    val lastRetrievalSummary: String,
    val lastCitationLabels: List<String>,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Entity(tableName = "knowledge_ingestion_records")
data class KnowledgeIngestionRecordEntity(
    @PrimaryKey val knowledgeAssetId: String,
    val ingestionState: KnowledgeIngestionState,
    val ingestionSummary: String,
    val lastIngestedAtEpochMillis: Long?,
    val lastErrorSummary: String,
    val currentScopeSummary: String,
)

@Entity(tableName = "knowledge_availability_records")
data class KnowledgeAvailabilityEntity(
    @PrimaryKey val knowledgeAssetId: String,
    val baseState: KnowledgeAvailabilityHealth,
    val retrievalIncluded: Boolean,
    val supportsRefresh: Boolean,
    val supportsRetrievalInclusionChange: Boolean,
    val reasonIfUnavailable: String,
    val lastCheckedAtEpochMillis: Long,
)

@Entity(
    tableName = "knowledge_chunks",
    indices = [Index(value = ["knowledgeAssetId"])],
)
data class KnowledgeChunkEntity(
    @PrimaryKey val chunkId: String,
    val knowledgeAssetId: String,
    val sourceLabel: String,
    val ordinal: Int,
    val text: String,
    val previewText: String,
)

@Dao
interface KnowledgeDao {
    @Query("SELECT * FROM knowledge_assets ORDER BY updatedAtEpochMillis DESC")
    fun observeAssets(): Flow<List<KnowledgeAssetEntity>>

    @Query("SELECT * FROM knowledge_assets ORDER BY updatedAtEpochMillis DESC")
    suspend fun getAllAssets(): List<KnowledgeAssetEntity>

    @Query("SELECT * FROM knowledge_assets WHERE knowledgeAssetId = :knowledgeAssetId LIMIT 1")
    suspend fun getAssetById(knowledgeAssetId: String): KnowledgeAssetEntity?

    @Query("SELECT * FROM knowledge_ingestion_records")
    fun observeIngestionRecords(): Flow<List<KnowledgeIngestionRecordEntity>>

    @Query("SELECT * FROM knowledge_ingestion_records")
    suspend fun getAllIngestionRecords(): List<KnowledgeIngestionRecordEntity>

    @Query("SELECT * FROM knowledge_ingestion_records WHERE knowledgeAssetId = :knowledgeAssetId LIMIT 1")
    suspend fun getIngestionRecordById(knowledgeAssetId: String): KnowledgeIngestionRecordEntity?

    @Query("SELECT * FROM knowledge_availability_records")
    fun observeAvailabilityRecords(): Flow<List<KnowledgeAvailabilityEntity>>

    @Query("SELECT * FROM knowledge_availability_records")
    suspend fun getAllAvailabilityRecords(): List<KnowledgeAvailabilityEntity>

    @Query("SELECT * FROM knowledge_availability_records WHERE knowledgeAssetId = :knowledgeAssetId LIMIT 1")
    suspend fun getAvailabilityById(knowledgeAssetId: String): KnowledgeAvailabilityEntity?

    @Query("SELECT * FROM knowledge_chunks WHERE knowledgeAssetId = :knowledgeAssetId ORDER BY ordinal ASC")
    suspend fun getChunksForAsset(knowledgeAssetId: String): List<KnowledgeChunkEntity>

    @Query("SELECT * FROM knowledge_chunks WHERE knowledgeAssetId IN (:knowledgeAssetIds) ORDER BY knowledgeAssetId, ordinal ASC")
    suspend fun getChunksForAssets(knowledgeAssetIds: List<String>): List<KnowledgeChunkEntity>

    @Query("DELETE FROM knowledge_chunks WHERE knowledgeAssetId = :knowledgeAssetId")
    suspend fun deleteChunksForAsset(knowledgeAssetId: String)

    @Upsert
    suspend fun upsertAsset(asset: KnowledgeAssetEntity)

    @Upsert
    suspend fun upsertIngestionRecord(record: KnowledgeIngestionRecordEntity)

    @Upsert
    suspend fun upsertAvailabilityRecord(record: KnowledgeAvailabilityEntity)

    @Upsert
    suspend fun upsertChunks(chunks: List<KnowledgeChunkEntity>)
}
