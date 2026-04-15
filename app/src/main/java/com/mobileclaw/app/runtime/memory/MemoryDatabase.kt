package com.mobileclaw.app.runtime.memory

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
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
    ],
    version = 5,
    exportSchema = false,
)
@TypeConverters(MemoryConverters::class)
abstract class MemoryDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun policyDao(): PolicyDao
    abstract fun approvalDao(): ApprovalDao
    abstract fun auditDao(): AuditDao
    abstract fun governanceDao(): GovernanceDao
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
    fun fromStringList(values: List<String>): String = values.joinToString(separator = LIST_SEPARATOR)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        return value.split(LIST_SEPARATOR)
    }
}
