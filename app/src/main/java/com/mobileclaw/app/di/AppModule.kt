package com.mobileclaw.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.mobileclaw.app.runtime.memory.DefaultScopedMemoryRepository
import com.mobileclaw.app.runtime.memory.MemoryDao
import com.mobileclaw.app.runtime.memory.MemoryDatabase
import com.mobileclaw.app.runtime.memory.MemoryRetrievalService
import com.mobileclaw.app.runtime.memory.MemoryWritebackService
import com.mobileclaw.app.runtime.memory.PersonaMemoryContextLoader
import com.mobileclaw.app.runtime.memory.ScopedMemoryRepository
import com.mobileclaw.app.runtime.knowledge.KnowledgeDao
import com.mobileclaw.app.runtime.action.StructuredActionNormalizer
import com.mobileclaw.app.runtime.ingress.ExternalEntryRegistration
import com.mobileclaw.app.runtime.ingress.ExternalEntryStatus
import com.mobileclaw.app.runtime.ingress.ExternalEntryType
import com.mobileclaw.app.runtime.governance.DefaultGovernanceRepository
import com.mobileclaw.app.runtime.governance.GovernanceDao
import com.mobileclaw.app.runtime.governance.GovernanceRepository
import com.mobileclaw.app.runtime.systemsource.AndroidSystemSourceRepository
import com.mobileclaw.app.runtime.systemsource.SystemSourceRepository
import com.mobileclaw.app.runtime.capability.AppFunctionBridge
import com.mobileclaw.app.runtime.capability.IntentFallbackBridge
import com.mobileclaw.app.runtime.capability.LocalMutationCapabilityBridge
import com.mobileclaw.app.runtime.capability.RealAppFunctionBridge
import com.mobileclaw.app.runtime.capability.RealIntentFallbackBridge
import com.mobileclaw.app.runtime.capability.LocalReadCapabilityBridge
import com.mobileclaw.app.runtime.capability.MutationCapabilityBridge
import com.mobileclaw.app.runtime.capability.RealShareFallbackBridge
import com.mobileclaw.app.runtime.capability.ReadCapabilityBridge
import com.mobileclaw.app.runtime.capability.ShareFallbackBridge
import com.mobileclaw.app.runtime.capability.CapabilityRouter
import com.mobileclaw.app.runtime.localchat.LocalChatGateway
import com.mobileclaw.app.runtime.localchat.LocalModelCatalog
import com.mobileclaw.app.runtime.localchat.ImportedLocalModelCatalog
import com.mobileclaw.app.runtime.localchat.LiteRtLocalChatGateway
import com.mobileclaw.app.runtime.persona.PersonaRepository
import com.mobileclaw.app.runtime.persona.PreferenceBackedPersonaRepository
import com.mobileclaw.app.runtime.policy.ApprovalDao
import com.mobileclaw.app.runtime.policy.ApprovalRepository
import com.mobileclaw.app.runtime.policy.AuditDao
import com.mobileclaw.app.runtime.policy.AuditRepository
import com.mobileclaw.app.runtime.policy.PendingApprovalCoordinator
import com.mobileclaw.app.runtime.policy.PolicyDao
import com.mobileclaw.app.runtime.policy.PolicyEngine
import com.mobileclaw.app.runtime.policy.PolicyRepository
import com.mobileclaw.app.runtime.policy.RiskClassifier
import com.mobileclaw.app.runtime.provider.ReadToolRequestBuilder
import com.mobileclaw.app.runtime.provider.CapabilityProviderRegistry
import com.mobileclaw.app.runtime.session.DefaultRuntimePlanner
import com.mobileclaw.app.runtime.session.RuntimeContextLoader
import com.mobileclaw.app.runtime.session.RuntimePlanner
import com.mobileclaw.app.runtime.session.RuntimeSessionFacade
import com.mobileclaw.app.runtime.session.RuntimeSessionOrchestrator
import com.mobileclaw.app.runtime.session.RuntimeSessionRegistry
import com.mobileclaw.app.runtime.strings.AppStrings
import com.mobileclaw.app.runtime.workflow.WorkflowDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideExternalEntryRegistration(): ExternalEntryRegistration = ExternalEntryRegistration(
        entryId = "android_share_text",
        entryType = ExternalEntryType.ACTIVITY_SHARE,
        supportedActions = setOf("android.intent.action.SEND"),
        supportedMimeTypes = setOf("text/plain", "image/*", "audio/*"),
        contentMode = "multimodal",
        requiresUserVisibleLanding = true,
        status = ExternalEntryStatus.ENABLED,
    )

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("mobile_claw.preferences_pb") },
    )

    @Provides
    @Singleton
    fun provideLocalModelCatalog(
        catalog: ImportedLocalModelCatalog,
    ): LocalModelCatalog = catalog

    @Provides
    @Singleton
    fun provideLocalChatGateway(
        gateway: LiteRtLocalChatGateway,
    ): LocalChatGateway = gateway

    @Provides
    @Singleton
    fun provideMemoryDatabase(
        @ApplicationContext context: Context,
    ): MemoryDatabase = Room.databaseBuilder(
        context,
        MemoryDatabase::class.java,
        "mobile_claw.memory.db",
    )
        // For now the runtime database is local-only and may be rebuilt across schema changes.
        // This only takes effect after the Room version is bumped in MemoryDatabase.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    @Provides
    fun provideMemoryDao(
        database: MemoryDatabase,
    ): MemoryDao = database.memoryDao()

    @Provides
    fun providePolicyDao(
        database: MemoryDatabase,
    ): PolicyDao = database.policyDao()

    @Provides
    fun provideApprovalDao(
        database: MemoryDatabase,
    ): ApprovalDao = database.approvalDao()

    @Provides
    fun provideAuditDao(
        database: MemoryDatabase,
    ): AuditDao = database.auditDao()

    @Provides
    fun provideGovernanceDao(
        database: MemoryDatabase,
    ): GovernanceDao = database.governanceDao()

    @Provides
    fun provideKnowledgeDao(
        database: MemoryDatabase,
    ): KnowledgeDao = database.knowledgeDao()

    @Provides
    fun provideWorkflowDao(
        database: MemoryDatabase,
    ): WorkflowDao = database.workflowDao()

    @Provides
    @Singleton
    fun providePersonaRepository(
        repository: PreferenceBackedPersonaRepository,
    ): PersonaRepository = repository

    @Provides
    @Singleton
    fun provideScopedMemoryRepository(
        repository: DefaultScopedMemoryRepository,
    ): ScopedMemoryRepository = repository

    @Provides
    @Singleton
    fun provideGovernanceRepository(
        repository: DefaultGovernanceRepository,
    ): GovernanceRepository = repository

    @Provides
    @Singleton
    fun provideSystemSourceRepository(
        repository: AndroidSystemSourceRepository,
    ): SystemSourceRepository = repository

    @Provides
    @Singleton
    fun provideRuntimeContextLoader(
        loader: PersonaMemoryContextLoader,
    ): RuntimeContextLoader = loader

    @Provides
    @Singleton
    fun provideRuntimePlanner(
        planner: DefaultRuntimePlanner,
    ): RuntimePlanner = planner

    @Provides
    @Singleton
    fun provideRuntimeSessionFacade(
        registry: RuntimeSessionRegistry,
        providerRegistry: CapabilityProviderRegistry,
        contextLoader: RuntimeContextLoader,
        planner: RuntimePlanner,
        structuredActionNormalizer: StructuredActionNormalizer,
        readToolRequestBuilder: ReadToolRequestBuilder,
        riskClassifier: RiskClassifier,
        policyEngine: PolicyEngine,
        policyRepository: PolicyRepository,
        approvalRepository: ApprovalRepository,
        auditRepository: AuditRepository,
        pendingApprovalCoordinator: PendingApprovalCoordinator,
        memoryWritebackService: MemoryWritebackService,
        appStrings: AppStrings,
        capabilityRouter: CapabilityRouter,
    ): RuntimeSessionFacade = RuntimeSessionOrchestrator(
        registry = registry,
        providerRegistry = providerRegistry,
        capabilityRouter = capabilityRouter,
        contextLoader = contextLoader,
        planner = planner,
        structuredActionNormalizer = structuredActionNormalizer,
        readToolRequestBuilder = readToolRequestBuilder,
        riskClassifier = riskClassifier,
        policyEngine = policyEngine,
        policyRepository = policyRepository,
        approvalRepository = approvalRepository,
        auditRepository = auditRepository,
        pendingApprovalCoordinator = pendingApprovalCoordinator,
        memoryWritebackService = memoryWritebackService,
        appStrings = appStrings,
    )

    @Provides
    @Singleton
    fun provideAppFunctionBridge(
        bridge: RealAppFunctionBridge,
    ): AppFunctionBridge = bridge

    @Provides
    @Singleton
    fun provideIntentFallbackBridge(
        bridge: RealIntentFallbackBridge,
    ): IntentFallbackBridge = bridge

    @Provides
    @Singleton
    fun provideReadCapabilityBridge(
        bridge: LocalReadCapabilityBridge,
    ): ReadCapabilityBridge = bridge

    @Provides
    @Singleton
    fun provideMutationCapabilityBridge(
        bridge: LocalMutationCapabilityBridge,
    ): MutationCapabilityBridge = bridge

    @Provides
    @Singleton
    fun provideShareFallbackBridge(
        bridge: RealShareFallbackBridge,
    ): ShareFallbackBridge = bridge
}
