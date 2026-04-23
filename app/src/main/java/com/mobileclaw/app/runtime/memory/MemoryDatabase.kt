package com.mobileclaw.app.runtime.memory

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.mobileclaw.app.runtime.knowledge.KnowledgeAvailabilityEntity
import com.mobileclaw.app.runtime.knowledge.KnowledgeAvailabilityHealth
import com.mobileclaw.app.runtime.knowledge.KnowledgeChunkEntity
import com.mobileclaw.app.runtime.knowledge.KnowledgeDao
import com.mobileclaw.app.runtime.knowledge.KnowledgeAssetEntity
import com.mobileclaw.app.runtime.knowledge.KnowledgeIngestionRecordEntity
import com.mobileclaw.app.runtime.knowledge.KnowledgeIngestionState
import com.mobileclaw.app.runtime.knowledge.KnowledgeSourceType
import com.mobileclaw.app.runtime.policy.ApprovalDao
import com.mobileclaw.app.runtime.policy.ApprovalOutcome
import com.mobileclaw.app.runtime.policy.ApprovalRequest
import com.mobileclaw.app.runtime.policy.ApprovalRequestStatus
import com.mobileclaw.app.runtime.policy.AuditDao
import com.mobileclaw.app.runtime.policy.AuditEvent
import com.mobileclaw.app.runtime.policy.AuditEventType
import com.mobileclaw.app.runtime.governance.CallerGovernanceRecord
import com.mobileclaw.app.runtime.governance.GovernanceDao
import com.mobileclaw.app.runtime.governance.GovernanceGrantState
import com.mobileclaw.app.runtime.governance.GovernanceTrustMode
import com.mobileclaw.app.runtime.governance.ScopeGrantRecord
import com.mobileclaw.app.runtime.policy.PolicyDao
import com.mobileclaw.app.runtime.policy.PolicyDecision
import com.mobileclaw.app.runtime.policy.PolicyDecisionType
import com.mobileclaw.app.runtime.policy.PolicyRuleSource
import com.mobileclaw.app.runtime.policy.RiskAssessment
import com.mobileclaw.app.runtime.policy.RiskLevel
import com.mobileclaw.app.runtime.policy.ApprovalOutcomeType
import com.mobileclaw.app.runtime.workflow.WorkflowAvailabilityState
import com.mobileclaw.app.runtime.workflow.WorkflowCheckpointEntity
import com.mobileclaw.app.runtime.workflow.WorkflowCheckpointState
import com.mobileclaw.app.runtime.workflow.WorkflowDao
import com.mobileclaw.app.runtime.workflow.WorkflowDefinitionEntity
import com.mobileclaw.app.runtime.workflow.WorkflowRunEntity
import com.mobileclaw.app.runtime.workflow.WorkflowRunState
import com.mobileclaw.app.runtime.workflow.WorkflowStepEntity
import com.mobileclaw.app.runtime.workflow.WorkflowStepType
import com.mobileclaw.app.runtime.workflow.WorkflowTriggerEntity
import com.mobileclaw.app.runtime.workflow.WorkflowTriggerType

// Migration rule:
// Any Room entity/schema change in this database must also bump the database version.
// We currently rely on destructive migration in AppModule for local-only runtime state.
@Database(
    entities = [
        MemoryItem::class,
        RiskAssessment::class,
        PolicyDecision::class,
        ApprovalRequest::class,
        ApprovalOutcome::class,
        AuditEvent::class,
        CallerGovernanceRecord::class,
        ScopeGrantRecord::class,
        KnowledgeAssetEntity::class,
        KnowledgeIngestionRecordEntity::class,
        KnowledgeAvailabilityEntity::class,
        KnowledgeChunkEntity::class,
        WorkflowDefinitionEntity::class,
        WorkflowStepEntity::class,
        WorkflowTriggerEntity::class,
        WorkflowRunEntity::class,
        WorkflowCheckpointEntity::class,
    ],
    version = 7,
    exportSchema = false,
)
@TypeConverters(MemoryConverters::class)
abstract class MemoryDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun policyDao(): PolicyDao
    abstract fun approvalDao(): ApprovalDao
    abstract fun auditDao(): AuditDao
    abstract fun governanceDao(): GovernanceDao
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun workflowDao(): WorkflowDao
}

class MemoryConverters {
    private companion object {
        const val LIST_SEPARATOR = "\u001F"
    }

    @TypeConverter
    fun fromLifecycle(value: MemoryLifecycle): String = value.name

    @TypeConverter
    fun toLifecycle(value: String): MemoryLifecycle = MemoryLifecycle.valueOf(value)

    @TypeConverter
    fun fromScope(value: MemoryScope): String = value.name

    @TypeConverter
    fun toScope(value: String): MemoryScope = MemoryScope.valueOf(value)

    @TypeConverter
    fun fromExposure(value: MemoryExposurePolicy): String = value.name

    @TypeConverter
    fun toExposure(value: String): MemoryExposurePolicy = MemoryExposurePolicy.valueOf(value)

    @TypeConverter
    fun fromSyncPolicy(value: MemorySyncPolicy): String = value.name

    @TypeConverter
    fun toSyncPolicy(value: String): MemorySyncPolicy = MemorySyncPolicy.valueOf(value)

    @TypeConverter
    fun fromExportMode(value: ExportMode): String = value.name

    @TypeConverter
    fun toExportMode(value: String): ExportMode = ExportMode.valueOf(value)

    @TypeConverter
    fun fromSourceType(value: MemorySourceType): String = value.name

    @TypeConverter
    fun toSourceType(value: String): MemorySourceType = MemorySourceType.valueOf(value)

    @TypeConverter
    fun fromRiskLevel(value: RiskLevel): String = value.name

    @TypeConverter
    fun toRiskLevel(value: String): RiskLevel = RiskLevel.valueOf(value)

    @TypeConverter
    fun fromPolicyDecisionType(value: PolicyDecisionType): String = value.name

    @TypeConverter
    fun toPolicyDecisionType(value: String): PolicyDecisionType = PolicyDecisionType.valueOf(value)

    @TypeConverter
    fun fromPolicyRuleSource(value: PolicyRuleSource): String = value.name

    @TypeConverter
    fun toPolicyRuleSource(value: String): PolicyRuleSource = PolicyRuleSource.valueOf(value)

    @TypeConverter
    fun fromApprovalRequestStatus(value: ApprovalRequestStatus): String = value.name

    @TypeConverter
    fun toApprovalRequestStatus(value: String): ApprovalRequestStatus = ApprovalRequestStatus.valueOf(value)

    @TypeConverter
    fun fromApprovalOutcomeType(value: ApprovalOutcomeType): String = value.name

    @TypeConverter
    fun toApprovalOutcomeType(value: String): ApprovalOutcomeType = ApprovalOutcomeType.valueOf(value)

    @TypeConverter
    fun fromAuditEventType(value: AuditEventType): String = value.name

    @TypeConverter
    fun toAuditEventType(value: String): AuditEventType = AuditEventType.valueOf(value)

    @TypeConverter
    fun fromGovernanceTrustMode(value: GovernanceTrustMode): String = value.name

    @TypeConverter
    fun toGovernanceTrustMode(value: String): GovernanceTrustMode = GovernanceTrustMode.valueOf(value)

    @TypeConverter
    fun fromGovernanceGrantState(value: GovernanceGrantState): String = value.name

    @TypeConverter
    fun toGovernanceGrantState(value: String): GovernanceGrantState = GovernanceGrantState.valueOf(value)

    @TypeConverter
    fun fromKnowledgeSourceType(value: KnowledgeSourceType): String = value.name

    @TypeConverter
    fun toKnowledgeSourceType(value: String): KnowledgeSourceType = KnowledgeSourceType.valueOf(value)

    @TypeConverter
    fun fromKnowledgeIngestionState(value: KnowledgeIngestionState): String = value.name

    @TypeConverter
    fun toKnowledgeIngestionState(value: String): KnowledgeIngestionState = KnowledgeIngestionState.valueOf(value)

    @TypeConverter
    fun fromKnowledgeAvailabilityHealth(value: KnowledgeAvailabilityHealth): String = value.name

    @TypeConverter
    fun toKnowledgeAvailabilityHealth(value: String): KnowledgeAvailabilityHealth =
        KnowledgeAvailabilityHealth.valueOf(value)

    @TypeConverter
    fun fromWorkflowAvailabilityState(value: WorkflowAvailabilityState): String = value.name

    @TypeConverter
    fun toWorkflowAvailabilityState(value: String): WorkflowAvailabilityState =
        WorkflowAvailabilityState.valueOf(value)

    @TypeConverter
    fun fromWorkflowStepType(value: WorkflowStepType): String = value.name

    @TypeConverter
    fun toWorkflowStepType(value: String): WorkflowStepType = WorkflowStepType.valueOf(value)

    @TypeConverter
    fun fromWorkflowTriggerType(value: WorkflowTriggerType): String = value.name

    @TypeConverter
    fun toWorkflowTriggerType(value: String): WorkflowTriggerType = WorkflowTriggerType.valueOf(value)

    @TypeConverter
    fun fromWorkflowRunState(value: WorkflowRunState): String = value.name

    @TypeConverter
    fun toWorkflowRunState(value: String): WorkflowRunState = WorkflowRunState.valueOf(value)

    @TypeConverter
    fun fromWorkflowCheckpointState(value: WorkflowCheckpointState): String = value.name

    @TypeConverter
    fun toWorkflowCheckpointState(value: String): WorkflowCheckpointState =
        WorkflowCheckpointState.valueOf(value)

    @TypeConverter
    fun fromStringList(values: List<String>): String = values.joinToString(separator = LIST_SEPARATOR)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        return value.split(LIST_SEPARATOR)
    }
}
