package com.mobileclaw.app.runtime.workflow

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

enum class WorkflowAvailabilityState {
    READY,
    DISABLED,
    BLOCKED,
    DEGRADED,
}

enum class WorkflowStepType {
    CONTEXT_CONTRIBUTION,
    GUARD,
    APPROVAL_GATE,
    ACTION,
}

enum class WorkflowTriggerType {
    MANUAL,
    SHARE_INGRESS,
    KNOWLEDGE_REFRESH,
}

enum class WorkflowRunState {
    RUNNING,
    PAUSED,
    AWAITING_APPROVAL,
    COMPLETED,
    FAILED,
    CANCELLED,
    RESUMABLE,
}

enum class WorkflowCheckpointState {
    READY,
    RUNNING,
    WAITING_APPROVAL,
    BLOCKED,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED,
}

@Entity(tableName = "workflow_definitions")
data class WorkflowDefinitionEntity(
    @PrimaryKey val workflowDefinitionId: String,
    val templateId: String,
    val title: String,
    val entrySummary: String,
    val isEnabled: Boolean,
    val availabilityState: WorkflowAvailabilityState,
    val availabilityReason: String,
    val stepCount: Int,
    val lastRunSummary: String,
    val lastRunState: WorkflowRunState?,
    val lastRunAtEpochMillis: Long?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Entity(
    tableName = "workflow_steps",
    indices = [
        Index(value = ["workflowDefinitionId"]),
        Index(value = ["workflowDefinitionId", "ordinal"], unique = true),
    ],
)
data class WorkflowStepEntity(
    @PrimaryKey val workflowStepId: String,
    val workflowDefinitionId: String,
    val ordinal: Int,
    val stepType: WorkflowStepType,
    val title: String,
    val summary: String,
    val requiredInputs: List<String>,
    val nextTransitionRule: String,
    val actionPayload: String,
)

@Entity(
    tableName = "workflow_triggers",
    indices = [Index(value = ["workflowDefinitionId"])],
)
data class WorkflowTriggerEntity(
    @PrimaryKey val workflowTriggerId: String,
    val workflowDefinitionId: String,
    val triggerType: WorkflowTriggerType,
    val triggerSummary: String,
    val isEnabled: Boolean,
)

@Entity(
    tableName = "workflow_runs",
    indices = [
        Index(value = ["workflowDefinitionId"]),
        Index(value = ["runState"]),
        Index(value = ["updatedAtEpochMillis"]),
    ],
)
data class WorkflowRunEntity(
    @PrimaryKey val workflowRunId: String,
    val workflowDefinitionId: String,
    val title: String,
    val runState: WorkflowRunState,
    val startedAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
    val lastCheckpointSummary: String,
    val nextRequiredAction: String,
    val outcomeHeadline: String,
    val outcomeDetails: String,
    val recoveryGuidance: String,
    val recentActivityLines: List<String>,
    val provenanceLines: List<String>,
    val activeApprovalRequestId: String?,
)

@Entity(
    tableName = "workflow_checkpoints",
    indices = [
        Index(value = ["workflowRunId"]),
        Index(value = ["workflowDefinitionId"]),
    ],
)
data class WorkflowCheckpointEntity(
    @PrimaryKey val workflowCheckpointId: String,
    val workflowRunId: String,
    val workflowDefinitionId: String,
    val stepId: String,
    val stepOrdinal: Int,
    val checkpointState: WorkflowCheckpointState,
    val resumeSummary: String,
    val blockingReason: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Dao
interface WorkflowDao {
    @Query("SELECT * FROM workflow_definitions ORDER BY updatedAtEpochMillis DESC")
    fun observeDefinitions(): Flow<List<WorkflowDefinitionEntity>>

    @Query("SELECT * FROM workflow_steps ORDER BY workflowDefinitionId, ordinal ASC")
    fun observeSteps(): Flow<List<WorkflowStepEntity>>

    @Query("SELECT * FROM workflow_triggers ORDER BY workflowDefinitionId, workflowTriggerId ASC")
    fun observeTriggers(): Flow<List<WorkflowTriggerEntity>>

    @Query("SELECT * FROM workflow_runs ORDER BY updatedAtEpochMillis DESC")
    fun observeRuns(): Flow<List<WorkflowRunEntity>>

    @Query("SELECT * FROM workflow_checkpoints ORDER BY updatedAtEpochMillis DESC")
    fun observeCheckpoints(): Flow<List<WorkflowCheckpointEntity>>

    @Query("SELECT * FROM workflow_definitions WHERE workflowDefinitionId = :workflowDefinitionId LIMIT 1")
    suspend fun getDefinitionById(workflowDefinitionId: String): WorkflowDefinitionEntity?

    @Query("SELECT * FROM workflow_steps WHERE workflowDefinitionId = :workflowDefinitionId ORDER BY ordinal ASC")
    suspend fun getStepsForDefinition(workflowDefinitionId: String): List<WorkflowStepEntity>

    @Query("SELECT * FROM workflow_triggers WHERE workflowDefinitionId = :workflowDefinitionId ORDER BY workflowTriggerId ASC")
    suspend fun getTriggersForDefinition(workflowDefinitionId: String): List<WorkflowTriggerEntity>

    @Query("SELECT * FROM workflow_runs WHERE workflowRunId = :workflowRunId LIMIT 1")
    suspend fun getRunById(workflowRunId: String): WorkflowRunEntity?

    @Query(
        """
        SELECT * FROM workflow_runs
        WHERE workflowDefinitionId = :workflowDefinitionId
        ORDER BY updatedAtEpochMillis DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestRunForDefinition(workflowDefinitionId: String): WorkflowRunEntity?

    @Query(
        """
        SELECT * FROM workflow_checkpoints
        WHERE workflowRunId = :workflowRunId
        ORDER BY updatedAtEpochMillis DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestCheckpointForRun(workflowRunId: String): WorkflowCheckpointEntity?

    @Query("SELECT * FROM workflow_runs WHERE runState IN (:runStates)")
    suspend fun getRunsByStates(runStates: List<WorkflowRunState>): List<WorkflowRunEntity>

    @Query("DELETE FROM workflow_steps WHERE workflowDefinitionId = :workflowDefinitionId")
    suspend fun deleteStepsForDefinition(workflowDefinitionId: String)

    @Query("DELETE FROM workflow_triggers WHERE workflowDefinitionId = :workflowDefinitionId")
    suspend fun deleteTriggersForDefinition(workflowDefinitionId: String)

    @Upsert
    suspend fun upsertDefinition(definition: WorkflowDefinitionEntity)

    @Upsert
    suspend fun upsertDefinitions(definitions: List<WorkflowDefinitionEntity>)

    @Upsert
    suspend fun upsertSteps(steps: List<WorkflowStepEntity>)

    @Upsert
    suspend fun upsertTriggers(triggers: List<WorkflowTriggerEntity>)

    @Upsert
    suspend fun upsertRun(run: WorkflowRunEntity)

    @Upsert
    suspend fun upsertCheckpoint(checkpoint: WorkflowCheckpointEntity)
}
