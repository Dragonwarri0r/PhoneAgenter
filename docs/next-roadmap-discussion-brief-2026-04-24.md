# Mobile Claw 下一步路线讨论 Brief

日期：2026-04-24

仓库：`/Users/youxuezhe/StudioProjects/mobile_claw`

当前分支：`codex/025-mobileclaw-interop-host`

依据：当前 working tree，不只是 `HEAD`。当前分支里有一批尚未提交的 Hub Interop、host、probe app、roadmap/spec 文档改动。

## 1. 这份文档的用途

这份 brief 是为了拿去问 GPT-5.5 Pro 或其他强模型，帮助我们判断下一步路线和优化重点。

它重点回答四件事：

- 现在 roadmap 走到哪里了
- 当前代码真实实现了什么
- 哪些地方已经能证明方向成立
- 哪些地方应该优先硬化，而不是继续堆新功能

## 2. 一句话现状

Mobile Claw 现在已经不是“本地聊天壳”了，而是一个正在成型的 Android local-first agent runtime：

- conversation-first workspace 是主入口
- runtime session pipeline 已经打通
- capability selection、tool contract、provider routing 已经形成
- risk / policy / approval / audit 已经是统一执行链路的一部分
- persona / memory / system source / knowledge / contribution 能进入上下文
- workflow / automation 已有 Room-backed 定义、运行、checkpoint 和 approval gate
- governance center 已能管理 caller trust 和 scope grant
- multimodal composer、attachment、external handoff 已经接入主链路
- Hub Interop 已经拆出共享协议模块、Android binding、Mobile Claw host 和独立 probe app

现在的核心问题已经从：

> 能不能做一个 agent runtime？

变成：

> 怎么把这个已经很宽的 runtime 硬化成一个可治理、可解释、可扩展、能长期演进的 Android agent 控制面？

## 2.1 最新路线表述

经过 `024-026` 的实现验证后，Mobile Claw 的路线应重新表述为三件东西：

1. `Hub Interop Protocol`
开放给其他 app 接入的公共协议 / SDK / contract。

2. `Mobile Claw Host`
实现协议的执行中枢，负责治理、授权、路由、执行、审计。

3. `Interop Probe App`
协议测试端 / conformance client，用来验证协议和 host 是否真的可被外部 app 调用。

所以下一步重心应从“做更多 app 功能”调整为：

> 把协议变成稳定公共入口，把 Claw app 变成可信 host，把 probe app 变成协议一致性测试工具。

准确链路是：

```text
Third-party App / Probe App
        ↓
Hub Interop Android Contract
        ↓
Mobile Claw HubInteropProvider
        ↓
Authorization / Governance / Policy / Approval
        ↓
Runtime Session Orchestrator
        ↓
Capability Router
        ↓
Provider Execution
        ↓
Task / Artifact / Audit / Control Center
```

边界原则：

- 协议层不执行，只定义 caller 如何 discover、authorize、invoke、poll task、load artifact、handle compatibility。
- Claw app 才执行，它是 host、runtime、policy engine 和用户治理中心。
- Probe app 不只是 demo，而是协议行为是否符合 contract 的外部验证器。

## 3. Roadmap 当前状态

### 3.1 已完成的 foundation：`001-006`

这部分是 v0 底座：

- `001-android-agent-shell`
- `002-runtime-session-pipeline`
- `003-persona-memory-fabric`
- `004-safe-execution-policy`
- `005-android-capability-bridge`
- `006-sync-extension-hooks`

已经具备：

- Android app shell
- 本地模型 workspace
- runtime session 生命周期
- persona + scoped memory
- policy / approval / audit
- capability bridge
- portability / sync / extension hook 的第一层合同

### 3.2 已完成的 v1 产品化：`007-015`

这部分把底座推进成第一层产品：

- `007-external-runtime-entry`
- `008-structured-action-payloads`
- `009-permission-governance-center`
- `010-system-source-ingestion`
- `011-portability-bundles`
- `012-real-appfunctions-integration`
- `013-workspace-information-architecture`
- `014-multimodal-ingress-and-composer`
- `015-tool-contract-standardization`

已经具备：

- 外部 share handoff
- structured action preview
- governance center
- contacts/calendar system source
- portability preview/share
- AppFunctions 对齐
- conversation-first workspace IA
- 多模态输入与附件
- 标准化 tool descriptor

### 3.3 已完成的控制面与扩展收口：`016-018`

- `016-external-caller-interop-contracts`
- `017-unified-extension-surface`
- `018-runtime-control-center`

意义：

- 外部 caller contract 不再只是 share intent
- extension 从 hook-first 变成 runtime-wide surface
- control center 开始把 runtime state 收束到一个可读、可管理的控制面

### 3.4 已完成的 v2 runtime expansion：`019-021`

- `019-runtime-hooks-and-context-sources`
- `020-knowledge-ingestion-and-retrieval`
- `021-workflow-graph-and-automation`

已经实现：

- runtime contribution / context source 表达
- local knowledge asset / chunk / retrieval
- workflow definition / run / checkpoint / approval gate

注意：

- knowledge 现在是本地关键词检索和文本 chunk，尚不是 embedding/vector 级知识库。
- workflow 现在有 durable run state，但 action step 还没有完全变成通用 tool graph runner。

### 3.5 最新已完成能力：`023-capability-inference-read-tools`

`023` 已经完成，解决了一个重要问题：

> workspace freeform 输入不再全部默认走 `generate.reply`。

当前策略：

- explicit capability hint 优先
- external structured request 优先
- blocked / ambiguous prompt 保守 fallback 到 reply
- 清晰低风险 read intent 可以选择 explicit read tool
- 清晰 side-effect intent 可以选择 action tool，然后由 policy / approval 接管风险

已落地能力：

- `calendar.read`：真实 Calendar Provider 读取
- `calendar.write`：真实 Calendar Provider 写入
- `calendar.delete`：真实 Calendar Provider 删除
- `contacts.read`：已作为 read provider 注册，但当前执行仍不可用

### 3.6 最新协议路线：`024-026`

这是当前最关键的新轨道：

- `024-shared-interop-contract`
- `025-mobileclaw-interop-host`
- `026-interop-probe-app`

当前代码已经有四层：

- `:hub-interop-contract-core`
- `:hub-interop-android-contract`
- `:app` 里的 Mobile Claw host implementation
- `:interop-probe-app`

重要状态不一致：

- `specs/024-shared-interop-contract/spec.md` 写的是 `Status: Implemented`
- `specs/025-mobileclaw-interop-host/spec.md` 写的是 `Status: Implemented`
- `docs/project-roadmap-v1.md` 也把 024/025/026 描述为首批落地
- 但 `specs/024-shared-interop-contract/tasks.md` 仍是 `0/21`
- `specs/025-mobileclaw-interop-host/tasks.md` 仍是 `0/22`
- `specs/026-interop-probe-app/tasks.md` 是 `20/20`

结论：

> 024/025 的代码和高层文档已经领先于 tasks checkbox。下一步应该清理这个状态，否则后续规划会被旧 checkbox 误导。

## 4. 当前工程结构

当前 Gradle modules：

- `:app`
- `:hub-interop-contract-core`
- `:hub-interop-android-contract`
- `:interop-probe-app`

当前主要版本：

- Kotlin `2.2.0`
- Android Gradle Plugin `8.9.3`
- compileSdk `36`
- minSdk `31`
- Room `2.7.1`
- Hilt `2.57`
- Compose BOM `2025.04.01`
- AppFunctions `1.0.0-alpha08`
- LiteRT-LM `0.10.0`

注意：

- `AGENTS.md` 仍提到 Android Gradle Plugin `8.8.x`，但代码已经是 `8.9.3`。

## 5. 当前代码实现总览

### 5.1 Runtime session 主链路

核心文件：

- `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionOrchestrator.kt`
- `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeRequest.kt`
- `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionEvent.kt`
- `app/src/main/java/com/mobileclaw/app/runtime/session/RuntimeSessionRegistry.kt`

当前执行链路：

1. 接收 request，创建 execution session
2. 如果是 external handoff，记录 source metadata 和 audit
3. 通过 `PersonaMemoryContextLoader` 加载上下文
4. `DefaultRuntimePlanner` 做 capability planning
5. `StructuredActionNormalizer` 生成 structured payload
6. `ReadToolRequestBuilder` 生成 explicit read request
7. 发出 capability selection 事件
8. `CapabilityRouter` 路由 provider
9. `CallerVerifier` 和 governance repository 处理 caller / scope
10. `RiskClassifier` 分类风险
11. `PolicyEngine` 生成 policy decision
12. 必要时创建 approval request 并等待结果
13. 调用 provider 执行
14. 记录 audit 和 memory writeback
15. session 进入 success / failure / denied / cancelled

这条链路现在是项目最重要的资产。外部入口、工作区自由输入、read tool、write/dispatch tool、approval、audit 都汇入同一条 execution spine。

### 5.2 Capability selection

核心文件：

- `WorkspaceCapabilitySelector`
- `LocalCapabilityPlanner`
- `RuntimeIntentHeuristics`
- `DefaultRuntimePlanner`

当前行为：

- explicit hint 优先
- blocked operation 保守处理
- workspace freeform 只有在 capability-relevant 时才调用本地 planner
- heuristic + model proposal 双层判断
- ambiguous request fallback 到 `generate.reply`
- read、write、dispatch 分开建模

Planner 当前认识的 capability：

- `generate.reply`
- `calendar.read`
- `calendar.write`
- `calendar.delete`
- `alarm.show`
- `alarm.set`
- `alarm.dismiss`
- `message.send`
- `external.share`
- `ui.act`
- `sensitive.write`

### 5.3 Tool contract 和 provider routing

核心文件：

- `StandardToolCatalog`
- `ToolContracts`
- `CapabilityRegistry`
- `CapabilityRouter`
- `runtime/provider/*`
- `runtime/capability/*Bridge.kt`

当前 tool families：

- `generate.reply`
- `calendar.read`
- `contacts.read`
- `calendar.write`
- `calendar.delete`
- `alarm.set`
- `alarm.show`
- `alarm.dismiss`
- `message.send`
- `share.outbound`

Provider 类型：

- local generation
- content resolver
- AppFunctions
- intent fallback
- sharesheet fallback

一个重要细节：

- `CapabilityRegistry` 会选择 `AVAILABLE` 或 `DEGRADED` provider。
- 这让权限缺失或 fallback path 仍能进入解释/执行流程，但也可能让“degraded”和“incompatible”的语义变模糊。

### 5.4 Calendar read/write/delete

核心文件：

- `CalendarReadCapabilityProvider`
- `CalendarMutationCapabilityProvider`
- `ReadToolRequestBuilder`
- `CalendarCapabilityParser`

已实现：

- `calendar.read` 查询 `CalendarContract.Instances`
- 支持 today / tomorrow / this afternoon / this week 等 bounded scope
- no-results 会返回真实无结果，不会编造
- permission unavailable 会返回恢复提示
- `calendar.write` 能通过 `CalendarContract.Events` 插入事件
- `calendar.delete` 会在时间窗口内查找候选，只有单一匹配时删除

限制：

- 日期时间解析仍很窄
- delete 的 fuzzy matching 较简单
- writable calendar 选择很朴素
- `contacts.read` 目前仍只是注册了 read provider，执行不可用

### 5.5 Policy / approval / audit

核心文件：

- `RiskClassifier`
- `PolicyEngine`
- `ApprovalRepository`
- `PendingApprovalCoordinator`
- `AuditRepository`

当前规则：

- reply 和低风险 read 可 auto execute
- side-effect / high-risk 基本 require confirmation
- approval timeout 是 120 秒
- approval rejected / abandoned / timed out 都会有记录
- audit 覆盖 handoff、routing、risk、policy、approval、execution

### 5.6 Governance

核心文件：

- `CallerVerifier`
- `DefaultGovernanceRepository`
- `CallerGovernanceRecord`
- `ScopeGrantRecord`
- `GovernanceCenterSheet`

当前模型：

- caller record 保存 trust mode、package、signature digest、last decision
- scope grant 保存 `ALLOW / ASK / DENY`
- governance center 可以调整 caller trust 和 scope grant
- interop authorization 复用这套 governance model，而不是新建第二套 grant store

需要警惕：

- 当前 `HubInteropProvider.call()` 路径主要消费 request Bundle 中的 `callerIdentity`。
- 代码里暂时没有看到 provider boundary 上基于实际 calling UID/package 的 host-attested caller resolution。
- 也就是说，外部 caller 身份存在 self-report/spoof 风险。

这应该是下一步 P0 级硬化点。

### 5.7 Memory / knowledge / contribution

核心文件：

- `PersonaMemoryContextLoader`
- `MemoryRetrievalService`
- `ManagedKnowledgeService`
- `KnowledgeRetrievalService`
- `RuntimeContributionRegistry`
- `RuntimeExtensionRegistry`

当前上下文加载会合并：

- persona / scoped memory
- system source ingestion
- knowledge retrieval
- contribution outcome
- context contribution

Knowledge 当前能力：

- 通过 Android `ContentResolver` 读取本地文档 URI
- Room 存储 knowledge asset、ingestion record、availability record、chunk
- 文本按窗口 chunk
- 本地关键词 overlap 检索
- 支持 retrieval inclusion toggle
- citation 支持 excerpt / summary-only

限制：

- 没有 embedding/vector index
- 文档类型基本偏文本
- 没有后台重建索引
- 没有知识库同步或冲突合并

### 5.8 Workflow / automation

核心文件：

- `ManagedWorkflowService`
- `WorkflowDao`
- `WorkflowModels`
- workspace automation UI models/components

已实现：

- workflow definition / step / trigger / run / checkpoint 都是 Room-backed
- run 支持 running、paused、awaiting approval、completed、failed、cancelled、resumable
- approval gate 复用 approval repository/coordinator
- app 启动时 running/awaiting approval 的 run 会恢复为 resumable
- UI 可以 create from template、start、pause、resume、cancel

限制：

- action step 目前更像 runtime-local simulation，还没有完全通过统一 capability provider spine 执行
- 没有真正 DAG branch/merge
- 没有 visual graph editor
- 没有外部 scheduler/trigger

### 5.9 Workspace / control center

核心文件：

- `AgentWorkspaceViewModel`
- `AgentWorkspaceScreen`
- `RuntimeControlCenterSheet`
- `GovernanceCenterSheet`
- `KnowledgeCenterSheet`
- `AutomationCenterSheet`

当前 UI 已有：

- conversation-first 主工作区
- text + image/audio composer
- model import / selection / health
- approval sheet
- context inspector
- governance center
- knowledge center
- automation center
- portability preview
- runtime control center

Runtime control center 当前 tab：

- Overview
- Context
- Automation
- Manage

它已经开始把 trace sections 和 managed artifact entries 汇总起来，但还没完全形成统一 object detail page 体系。

## 6. Hub Interop 当前实现

### 6.1 `:hub-interop-contract-core`

核心内容：

- `InteropVersion`
- `CompatibilitySignal`
- `InteropIds`
- `InteropHandles`
- `HubSurfaceDescriptor`
- `InteropCapabilityDescriptor`
- `InteropGrantDescriptor`
- `InteropTaskDescriptor`
- `InteropArtifactDescriptor`
- `InteropContractValidator`
- `HubSurfaceDescriptorFactory`

当前协议版本：

- `1.0`

兼容性语义：

- supported
- downgraded
- incompatible
- unknown fields 当前视为 incompatible

### 6.2 `:hub-interop-android-contract`

核心内容：

- `HubInteropAndroidContract`
- `HubInteropMethod`
- `HubInteropStatus`
- `HubInteropStatusMapper`
- `HubInteropUriBuilder`
- `HubInteropRequestFactory`
- `HubInteropCaller`
- discovery / authorization / invocation / task / artifact Bundle codecs
- ContentResolver call helpers

当前 authority：

- `HubInteropAndroidContract.authorityFor(packageName)` 生成 `${packageName}.hubinterop`
- `:app` manifest 使用 `${applicationId}.hubinterop`

注意：

- 部分 docs 里还有 `content://com.mobileclaw.app.interop` 之类早期示例，建议统一改成 `.hubinterop`，避免公共协议文档和代码不一致。

### 6.3 Mobile Claw host

核心文件：

- `HubInteropProvider`
- `HubInteropMethodDispatcher`
- `HubDiscoveryService`
- `HubSurfaceDescriptorAssembler`
- `HubInteropAuthorizationService`
- `HubCapabilityInvocationService`
- `HubInteropTaskService`
- `HubInteropCompatibilityService`

已支持 provider methods：

- `discover_surface`
- `invoke_capability`
- `request_authorization`
- `get_grant_status`
- `revoke_grant`
- `get_task`
- `get_artifact`

当前暴露能力：

- `generate.reply`

当前行为：

- discovery 返回 public `HubSurfaceDescriptor`
- authorization 写入/读取现有 governance scope grants
- `generate.reply` 默认要求 inbound grant
- accepted invocation 创建 in-memory interop task
- runtime session events 推动 task state
- completed task 可以生成 text artifact descriptor

限制：

- task/artifact 目前是内存态，进程死后丢失
- 没有 authorization Activity / PendingIntent ceremony
- 目前只暴露一条 callable capability
- 没有 resource/context exchange plane
- 没有 provider federation

### 6.4 `:interop-probe-app`

当前能力：

- discover host
- request authorization
- refresh grant status
- revoke grant
- invoke `generate.reply`
- poll task
- load artifact
- run contract drift diagnostics：`1.1` 和 `2.0`
- timeline + shareable summary

最重要的正向信号：

- probe app 只依赖 `:hub-interop-contract-core` 和 `:hub-interop-android-contract`
- 它不依赖 `:app`

这说明协议边界已经开始从“文档上的公共协议”变成真实可消费的公共 contract。

## 7. 当前存储状态

`MemoryDatabase.version = 7`

Room entities 覆盖：

- memory items
- risk assessments
- policy decisions
- approval requests/outcomes
- audit events
- caller governance records
- scope grant records
- knowledge assets / ingestion / availability / chunks
- workflow definitions / steps / triggers / runs / checkpoints

当前迁移策略：

- `fallbackToDestructiveMigration(dropAllTables = true)`
- 文档要求任何 Room schema 变化都必须同步 bump version

判断：

- 对当前 local-first prototype 可以接受
- 一旦用户知识库、workflow、governance 成为真实资产，就需要 explicit migrations 和 schema export

## 8. 当前 working tree 状态

当前分支不是干净状态。

已有 modified tracked files 包括：

- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- appfunctions / governance / ingress / strings / workspace UI 文件
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle/libs.versions.toml`
- roadmap/spec 相关 docs

新增 untracked areas 包括：

- `app/src/main/java/com/mobileclaw/app/runtime/interop/`
- `app/src/test/java/com/mobileclaw/app/runtime/interop/`
- `hub-interop-contract-core/`
- `hub-interop-android-contract/`
- `interop-probe-app/`
- `docs/hub-interop-*.md`
- `specs/024-shared-interop-contract/`
- `specs/025-mobileclaw-interop-host/`
- `specs/026-interop-probe-app/`
- `.tmp-usability/`

这份 brief 反映的是当前 working tree 的真实状态。

## 9. 最重要的问题与优化机会

### P0：Interop caller identity 必须 host-attested

当前最大风险是：

- `HubInteropProvider` 是 exported provider
- request Bundle 里携带 `callerIdentity`
- host 当前主要消费这个自报身份

下一步需要明确：

- provider boundary 如何拿到真实 calling package / UID
- request-provided identity 是否只能作为 display metadata
- signature digest 应该由 host 计算，而不是 caller 自报
- spoofed caller 是否能被测试覆盖

这是下一步最值得优先做的安全硬化。

### P0：024/025 tasks 状态需要清理

当前 spec/docs/code 都说 024/025 已实现，但 tasks checkbox 没跟上。

建议：

- 更新 `specs/024-*/tasks.md`
- 更新 `specs/025-*/tasks.md`
- 或至少加明确 note：implementation ahead of task checklist

否则后续模型会误判实现状态。

### P0：建立固定 validation baseline

本次已运行基础验证，结果通过。

命令：

```bash
./gradlew :hub-interop-contract-core:test \
  :hub-interop-android-contract:testDebugUnitTest \
  :interop-probe-app:testDebugUnitTest \
  :app:testDebugUnitTest \
  :app:compileDebugKotlin \
  --no-daemon
```

结果：

- `BUILD SUCCESSFUL`
- 98 actionable tasks
- 1 executed, 97 up-to-date

还缺：

- 真机或 emulator 上同时安装 `:app` 和 `:interop-probe-app` 的 smoke test

### P1：Interop authorization UX 还不够明确

当前 authorization 已经能写入 governance grant，但用户体验还像“借用 governance sheet 管理 grant”。

建议补：

- connected app detail page
- inbound grant request detail
- explicit approve/reject/revoke actions
- authorization request deep link 或 Activity route
- probe app 能展示 pending -> granted -> revoked 的完整闭环

### P1：Interop task/artifact 应从内存态升级

当前 `HubInteropTaskService` 用 in-memory map 保存 task/artifact。

这对第一 slice 足够，但作为协议会弱：

- app 进程死后 task 丢失
- 外部 caller polling 会得到 not found
- 没有 expiry / ownership / cleanup policy

建议讨论：

- 是否立即做 Room-backed `InteropTaskRecord`
- artifact 是否需要持久 record
- 哪些 artifact 只是 summary，哪些需要 file/content URI
- 是否需要 caller-scoped access check

### P1：Extension compatibility 和 provider availability 语义需对齐

当前 `RuntimeExtensionRegistry.availableRuntimeMetadata` 有 `provider.read.local`，但没有 `provider.mutation.local`。

而 calendar write/delete extension 需要 `provider.mutation.local`。

结果可能是：

- extension 层显示 mutation provider incompatible
- capability routing 仍可能把 provider 当 degraded path 执行

建议：

- 如果 mutation provider 已正式支持，就补 runtime metadata
- 如果没有正式支持，就避免它在 routing 上表现得像可执行路径
- 更清楚地区分 disabled / degraded / incompatible / permission missing

### P1：Control center 需要 object detail 体系

当前 control center 已经有 tabs 和 managed artifact entries，但还没有形成稳定对象真相页。

下一步可以统一这些对象：

- connected app
- grant
- task
- artifact
- knowledge asset
- workflow definition
- workflow run
- extension
- approval ticket

建议每个对象详情页共享骨架：

1. summary
2. current state
3. scope / policy
4. provenance / source
5. recent activity
6. available actions

### P1：Knowledge retrieval 质量需要升级

当前 knowledge 已经可用，但只是第一版：

- 文本 chunk
- 关键词 overlap
- 简单 freshness boost
- citation/excerpt

后续可以讨论：

- 是否引入 embedding / hybrid retrieval
- 是否支持 PDF / docx / markdown / HTML
- 是否做后台 reindex
- citation 和 redaction 规则如何加强

### P1：Workflow action step 应进入统一 capability spine

workflow 当前最大缺口：

- run / checkpoint / approval 都已 durable
- 但 action step 还没有真正调用统一 tool/capability provider spine

下一步可以做：

- workflow action node -> RuntimeRequest / RuntimePlan
- tool node input/output contract
- approval gate node contract
- retry / resume / failure policy
- later 再做 visual graph editor

### P2：扩展更多 explicit read tools

`calendar.read` 已证明 read-tool path。

下一批候选：

- `contacts.read`
- `knowledge.search`
- `workflow.read`
- `interop.connected_apps.read`
- `file.metadata.read`

关键问题：

- 哪些 read 可以 auto-execute？
- 哪些 read 因为泄露本地敏感数据，需要 preview / inspect / approval？

## 10. 下一步路线候选

### Option A：Public Interop Host Baseline

目标：

- 把 `024-026` 从“能跑通”硬化到“第三方 app 可以依赖”
- 建立 public protocol、trusted host、probe conformance 的稳定闭环

范围：

- protocol status / method / descriptor cleanup
- host-attested caller identity
- authorization lifecycle
- durable task/artifact record
- `generate.reply` invocation baseline
- `calendar.read` real capability baseline
- docs/tasks 状态清理
- probe app 覆盖 spoof / unauthorized / pending / granted / revoked / incompatible / downgraded

优点：

- 刚刚建立公共协议边界，现在硬化成本最低
- 后续 provider federation、resource exchange、更多 capability 都依赖这个边界
- `calendar.read` 能证明 Claw Host 作为 Android 本地执行中枢的真实价值

缺点：

- 对普通用户来说新功能感不强

### Option B：Runtime Control Center Hardening

目标：

- 把已有能力收成一个真正可理解的产品控制面

范围：

- session tray
- object detail page 骨架
- connected app / grant / task / artifact detail
- extension / knowledge / workflow detail 一致性

优点：

- 现在能力很多，用户理解成本已经是主要瓶颈

缺点：

- 如果不限定对象范围，容易变成大 UI 重构

### Option C：Knowledge + Workflow Integration

目标：

- 让 knowledge 和 workflow 变成真实日用能力，而不是第一版 surface

范围：

- 更好的 ingestion/index
- workflow action node 调用 capability spine
- workflow 节点消费 knowledge citation
- workflow run trace / recovery

优点：

- 明显提升 agent utility

缺点：

- 如果 interop/security/control 还没硬化，会继续放大管理复杂度

### Option D：Capability Surface Expansion

目标：

- 增加更多实用 read tools 和少量安全 side-effect tools

范围：

- `contacts.read`
- calendar query parser 扩展
- `knowledge.search`
- 通过 interop discovery 暴露更多 read capability

优点：

- 用户价值直观

缺点：

- 工具越多，对 governance/control center 的压力越大

## 11. 推荐新增 spec 拆解

我建议把原本较大的 `027-public-interop-host-baseline` 拆成三张：

```text
027-public-interop-contract-stabilization
028-mobileclaw-trusted-interop-host
029-interop-probe-conformance-suite
```

这三张都不应该是大功能扩张，而应该是稳定化 milestone。

### 027：Public Interop Contract Stabilization

目标：

- public protocol contract cleanup
- method / status / descriptor semantics 固定
- compatibility / unknown field policy 明确
- Bundle codec roundtrip tests
- `024/025` tasks 状态对齐

不做：

- host-attested identity implementation
- durable task/artifact implementation
- probe conformance runner
- 新 capability 暴露

### 028：Mobile Claw Trusted Interop Host

目标：

- `HubInteropProvider` 做 host-attested caller identity
- interop grant request 在 Mobile Claw 里可见、可批准、可撤销
- interop task/artifact 有 durable record 或明确 expiry/not-found 语义
- 保留 `generate.reply`，新增 bounded `calendar.read`
- audit / minimal control-center visibility

不做：

- 大量新 capability
- workflow runner
- knowledge / resource exchange
- side-effect tools interop exposure
- 完整 visual control center 重构

### 029：Interop Probe Conformance Suite

目标：

- probe app 覆盖 unauthorized、pending、granted、revoked、downgraded、incompatible、task、artifact
- manual mode 覆盖 discover / grant / invoke / task / artifact / revoke / export report
- conformance mode 覆盖 spoof / malformed / compatibility / lifecycle matrix
- 建立必须通过的 validation baseline
- 输出 shareable report

为什么这个最合适：

- 项目刚刚引入 public protocol boundary
- 公共边界一旦扩散，后续修改成本会迅速上升
- 当前代码已经证明架构能跑，但 trust、grant、task lifecycle 还需要变硬
- 做完这张之后，再扩 read tools、resource exchange、provider federation 会稳很多

之后候选再接：

- expanded read capability protocol
- host control center object details
- workflow capability runner
- resource and knowledge exchange
- side-effect capability protocol

## 12. 建议问 GPT-5.5 Pro 的问题

可以把这份文档给 5.5 Pro，然后直接问：

1. 基于当前代码状态，把原 `027-public-interop-host-baseline` 拆成 `027/028/029` 是否比一张大 spec 更稳？
2. `027-public-interop-contract-stabilization` 是否应该只做 contract/status/descriptor/compatibility/codec/docs 对齐，不碰 host implementation？
3. Android exported `ContentProvider` 协议里，最小可接受的 host-attested caller identity 模型是什么？
4. `HubInteropProvider` 是否需要自定义 Android permission？还是 package/signature verification + protocol grant 就够？
5. interop grant 应继续复用 `ScopeGrantRecord`，还是应新增 `InteropGrantRecord` 并桥接到 governance？
6. task/artifact handle 是否应立即 Room-backed？还是在更多 task collaboration 之前保持 in-memory 可以接受？
7. `028-mobileclaw-trusted-interop-host` 的最小 durable task/artifact 模型应该做到 Room-backed，还是先定义 restart 后的 expired/not-found 语义？
8. `028` 是否只暴露 `generate.reply` 和 bounded `calendar.read`？还是应该等 authorization UX 硬化后再暴露 `calendar.read`？
9. workflow runner 最小下一步是不是把 action step 路由到现有 capability provider spine？
10. 每次 interop protocol 改动的最低测试 gate 应该是什么？

## 13. `027-029` 建议验收标准

### `027-public-interop-contract-stabilization`

- public methods 固定为 discovery / authorization / invocation / task / artifact / revoke/status flows
- status code 能区分 bad request、unauthorized、authorization required、pending、forbidden、not found、expired、incompatible、unsupported capability、provider unavailable、permission unavailable、policy denied、approval required/rejected、execution failed、internal error
- capability / grant / task / artifact descriptor v1 不暴露 host-only provider internals
- version compatibility 明确 supported / downgraded / incompatible / required unknown / optional unknown / extension namespace
- public Bundle codecs 有 roundtrip tests
- `024/025` tasks checkbox 与实现状态一致

### `028-mobileclaw-trusted-interop-host`

- host 能从 provider boundary 解析真实 caller package/UID/signature
- request Bundle 中 spoof 的 caller identity 不会被当作可信身份
- 未授权 caller 不能通过 spoof 调用 `generate.reply`
- authorization request 在 Mobile Claw 里有用户可见状态
- grant 可以 approve / reject / revoke
- probe app 能展示 unauthorized -> pending -> granted -> invoked -> task -> artifact -> revoked 的闭环
- bounded `calendar.read` 能从 probe app 触发，并产生 calendar summary artifact
- incompatible major version 和 downgraded minor version 都能被 probe app 明确展示
- task/artifact handle 进程重启后可恢复，或有明确 expired/not found 语义

### `029-interop-probe-conformance-suite`

- probe app manual mode 覆盖 discover / request authorization / refresh grant / invoke / poll task / load artifact / revoke / export report
- probe app conformance mode 覆盖 compatibility、spoof diagnostics、unauthorized、pending、granted、revoked、task lifecycle、artifact lifecycle、malformed request、downgrade / incompatible diagnostics
- report 输出 host package、authority、protocol version、supported methods、supported capabilities、pass/fail matrix、raw status codes、failure reason、timeline
- probe app 仍只依赖 `:hub-interop-contract-core` 和 `:hub-interop-android-contract`
- validation baseline 通过

## 14. 关键源码入口

Roadmap/docs：

- `README.md`
- `docs/project-roadmap-v1.md`
- `docs/spec-breakdown-v1.md`
- `docs/tool-capability-and-extension-standards-v1.md`
- `docs/hub-interop-protocol-design-v1.md`
- `docs/hub-interop-android-ipc-v1.md`
- `docs/hub-interop-module-packaging-v1.md`
- `docs/hub-interop-docs-index-v1.md`

近期 specs：

- `specs/023-capability-inference-read-tools`
- `specs/024-shared-interop-contract`
- `specs/025-mobileclaw-interop-host`
- `specs/026-interop-probe-app`

Runtime：

- `app/src/main/java/com/mobileclaw/app/runtime/session`
- `app/src/main/java/com/mobileclaw/app/runtime/capability`
- `app/src/main/java/com/mobileclaw/app/runtime/provider`
- `app/src/main/java/com/mobileclaw/app/runtime/policy`
- `app/src/main/java/com/mobileclaw/app/runtime/governance`
- `app/src/main/java/com/mobileclaw/app/runtime/knowledge`
- `app/src/main/java/com/mobileclaw/app/runtime/workflow`

Interop：

- `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract`
- `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android`
- `app/src/main/java/com/mobileclaw/app/runtime/interop`
- `interop-probe-app/src/main/java/com/mobileclaw/interop/probe`

UI：

- `app/src/main/java/com/mobileclaw/app/ui/agentworkspace`

## 15. 本次验证状态

已运行：

```bash
./gradlew :hub-interop-contract-core:test \
  :hub-interop-android-contract:testDebugUnitTest \
  :interop-probe-app:testDebugUnitTest \
  :app:testDebugUnitTest \
  :app:compileDebugKotlin \
  --no-daemon
```

结果：

- `BUILD SUCCESSFUL`
- 98 actionable tasks
- 1 executed, 97 up-to-date

尚未覆盖：

- 真机/emulator 安装 `:app` 和 `:interop-probe-app` 的端到端 smoke test
- probe app 对真实 Mobile Claw provider 的手动授权/调用/轮询完整演示
