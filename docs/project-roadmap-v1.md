# Agent Runtime Project Roadmap v1

## 1. 现在的位置

`001-006` 已经完成了 `v0` 的 runtime foundation：

- Android app shell 与本地模型工作区
- 统一 runtime session pipeline
- persona + scoped memory
- risk / policy / approval / audit
- AppFunctions-first capability bridge
- sync / merge / export / extension hooks

`007-015` 也已经把这套底座推进成第一层真正可用的产品：

- 第一个真实外部 handoff 入口
- structured action payload
- caller / scope / approval 治理中心
- contacts / calendar system source
- portability bundle 预览与分享
- real AppFunctions integration
- conversation-first workspace IA
- multimodal composer 与 request normalization
- standardized tool contract

这意味着我们现在缺的已经不是“再补一个底层抽象”，而是把现有能力继续收口成一个真正稳定、可理解、可管理的 Android Agent 产品。

## 2. v1 产品定位

`v1` 不应该被定义成一个“聊天壳”，也不应该只是一个“内部 runtime demo”。

更准确的定位应该是：

**一个 local-first、conversation-first、可治理、可扩展的 Android 个人 Agent 控制面与分发核心。**

它至少要同时成立下面四件事：

- 用户可以从 app 内外把事情交给 Mobile Claw
- 系统可以把请求转成稳定的 structured tool / approval / memory / extension 执行链路
- 用户可以在 app 内看懂系统用了什么、为什么这样做、哪里被允许或拒绝
- 用户可以在 app 内直接管理支持编辑的运行时对象，而不是到处分散找入口

### 2.1 Hub Interop 之后的产品定义

在 `024-026` 之后，Mobile Claw 不应该再被描述成“一个带 interop 的 app”。

更准确的产品定义是三件东西：

1. `Hub Interop Protocol`
开放给其他 app 接入的公共协议 / SDK / contract。它定义 discovery、authorization、capability invocation、task polling、artifact loading、compatibility diagnostics 等外部调用语义。

2. `Mobile Claw Host`
实现协议的执行中枢。它负责 host-attested caller identity、governance、scope grant、policy、approval、runtime routing、provider execution、audit、control center。

3. `Interop Probe App`
协议测试端和 conformance client。它不是 demo app，而是用来证明协议和 host 行为真的能被一个完全独立的外部 app 使用。

对应的系统链路应固定为：

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

这里的边界很重要：

- 协议层不执行能力，只定义外部 caller 如何发现、授权、调用、轮询 task、读取 artifact、处理兼容性。
- Mobile Claw Host 才执行能力，它是 runtime、policy engine、用户治理中心和审计面。
- Probe app 不验证“按钮能不能点”，而验证协议行为是否符合 contract。

因此下一阶段重心应从“继续增加 app 功能”调整为：

> 把协议变成稳定公共入口，把 Claw app 变成可信 host，把 probe app 变成协议一致性测试工具。

## 3. v1 的四个产品支柱

### 3.1 Conversation-First Control Surface

内置聊天不是附属调试入口，而是主入口。

- 它要保持可读、可用、不过度膨胀
- 它要支持多模态输入
- 它要能把用户带到更深层的治理、记忆、工具、扩展管理面

### 3.2 Governed Dispatch Core

runtime core 需要像 OpenClaw 一样成为真正的分发核心，而不是单次 prompt 执行器。

- request normalization
- structured action
- tool contract
- policy / approval / audit
- caller / scope / trust

### 3.3 Stable Interop And Extension Contracts

随着外部 caller 和内部能力持续增长，合同必须先稳定，再继续加功能。

- app-to-app / agent-to-agent interop contract
- unified extension registration / compatibility / enablement
- 避免每增加一个入口或扩展点就长出新的特例路径

这条支柱的 docs-level 上游设计已经单独冻结在 [Hub Interop Protocol Design v1](./hub-interop-protocol-design-v1.md)。

从 roadmap 视角，后续 interop 不再等同于“把 share 入口做得更完整”，而是要收口成一个 hub-grade interop family：

- `share ingress` 只保留兼容型 handoff 角色
- `callable capability` 承担显式能力调用
- `resource / context exchange` 承担受控上下文与 artifact 交换
- `task collaboration` 承担长任务与状态化协作
- `governance plane` 统一入权限、出权限、approval 与 audit

### 3.4 Local Context And Portable Results

产品不是只会执行动作，还要会解释上下文和带走结果。

- persona + memory + system source
- redaction-aware portability
- explainable runtime trace

## 4. v1 的 North-Star Flows

### Flow A：从别的 App 把内容交给 Agent

- 用户在别的 app 里发起 handoff
- Mobile Claw 接住请求
- caller / source / uri grant 可识别
- 请求进入同一条 runtime 主链路

这条路径定义了：`真实入口`

但它只是兼容入口，不是完整的 hub interop protocol。

### Flow B：把输入推进成可执行、可治理的动作

- runtime 判断请求语义
- planner / tool layer 生成更稳定的动作合同
- 低风险动作可继续推进
- 高风险动作会显示 preview、scope、approval 与结果

这条路径定义了：`结构化执行 + 安全治理`

### Flow C：用内置多模态聊天直接测试和驱动系统

- 用户可以用 text / image / audio 直接发起请求
- 聊天仍然是主工作区
- 关键执行状态、贡献来源、限制条件都能从同一工作流中查看

这条路径定义了：`内置测试入口 + 主控入口`

### Flow D：在 app 内读懂并管理系统

- 用户能看懂本次请求用了哪些 memory / tool / approval / extension
- 用户能调整支持编辑的 memory、caller governance、extension enablement 等对象
- 用户不需要在零散面板和“伪设置页”之间来回跳转

这条路径定义了：`可读 + 可编辑的控制面`

## 5. 当前路线状态

### 已完成：Foundation And First Productization

这部分已经由 `001-015` 覆盖。

它们共同证明了：

- Mobile Claw 已经能从 app 内外接住请求
- workspace 已经是 conversation-first
- 多模态输入已经进入主请求链路
- tool、governance、memory、portability 已经各自成立

但这些能力现在仍然更像“已经存在的一组产品面”，还不是“已经收束成一个统一控制面”。

### 已落地：Contract / Control / Interop First Pass

`016-018` 已经把 external caller、extension surface、runtime control center 推进到了第一版产品收口。

`024-026` 又把 Hub Interop 从 docs-level 设计推进成了真实模块和真实外部验证路径：

- `:hub-interop-contract-core`
- `:hub-interop-android-contract`
- `:app` 内的 `HubInteropProvider` host implementation
- `:interop-probe-app`

这说明第一条公共协议链路已经能跑通，但还不能直接把它当成长期开放边界。

当前最重要的新增 spec 应拆成三张，而不是继续把协议、Host 和 Probe 都塞进一张：

- `027-public-interop-contract-stabilization`
- `028-mobileclaw-trusted-interop-host`
- `029-interop-probe-conformance-suite`

这三张的目标不是扩功能，而是把 `024-026` 从“能跑通”硬化到“第三方 app 可以依赖”。

### 当前主线：Public Interop Baseline

`027-029` 对应三件产品边界：

1. `027-public-interop-contract-stabilization`
稳定 public method、status code、request/response schema、capability / grant / task / artifact descriptor、version compatibility、unknown field policy、Bundle codec roundtrip test。

2. `028-mobileclaw-trusted-interop-host`
引入 host-attested caller identity、caller fingerprint、signature digest host computation、authorization request lifecycle、durable task/artifact record、ownership checks、audit enrichment，并把 `generate.reply` + bounded `calendar.read` 跑进统一 runtime spine。

3. `029-interop-probe-conformance-suite`
把 probe app 从手动调试工具升级成 conformance client，覆盖 discovery、authorization lifecycle、spoof diagnostic、unauthorized / pending / granted / revoked invocation、task polling、artifact loading、downgrade / incompatible diagnostics、report export。

`028` 只应暴露两个 capability：

- `generate.reply`：证明基本 invocation 链路。
- `calendar.read`：证明 Claw Host 可以通过真实 Android Calendar Provider 完成低风险 read capability，并覆盖权限缺失、bounded query、空结果、artifact、audit。

`027-029` 不应包含大量新 capability、workflow runner、knowledge exchange、side-effect tools interop exposure，或完整 visual control center 重构。

详细拆解见 [Hub Interop 027-029 Spec Split v1](./hub-interop-027-029-spec-split-v1.md)。

## 6. 现在仍然缺的产品收口

从当前完成度看，最明显的缺口已经不是 `007-015` 当初定义的那些基础能力，而是下面这些收口问题：

1. `external interop 仍然偏入口导向`
现有外部入口已经存在，但 caller identity、grant、callable surface、connected app relationship、task-style collaboration 还没有完全稳定成统一 interop family；这部分现在以上游协议文档为收口基线，而不再以 `ACTION_SEND` 本身为主定义。

2. `extension 还是 hook-first，不是 system-first`
`006` 和现有能力已经证明扩展方向成立，但统一 registration / compatibility / enablement 还没有封口。

3. `管理面仍然分散`
memory、governance、approval、tool、extension 各自有自己的入口或半成品视图，用户仍要自己拼出系统全貌。

4. `内置聊天还没有成为完整的“管理我们”的入口`
它已经能做多模态测试和执行，但还没有把“查看和管理系统自身状态”彻底吸进同一主工作流。

5. `explainability 仍然分片`
来源、工具、批准、上下文、扩展贡献都已经部分可见，但还没有形成稳定、连续、可回看的 runtime trace。

6. `共享公共协议层已经立住第一条可验证主线，但还没有成为稳定公共入口`
`024-026` 已经把 docs-level Hub Interop Protocol、Android IPC 与模块边界落成了真实模块、host implementation 与独立 probe app，因此“协议是否能被外部 app 真正消费”这件事已经有了第一条可验证闭环。当前剩下的不是从零到一，而是按 `027` public protocol、`028` trusted host、`029` probe conformance 三步把边界硬化。

7. `host 信任边界还不能依赖 request payload`
下一步必须把 grant lookup、task ownership、artifact access、audit identity 都切到 host-attested caller identity。Bundle 里的 caller metadata 只能用于 display、diagnostics 和 mismatch warning，不能作为可信身份。

8. `task / artifact handle 需要公共协议级生命周期语义`
如果 task/artifact 仍只存在内存里，外部 caller 在 host 进程重启后会得到不可解释的 not found。`027` 先定义公共生命周期语义，`028` 再落 host durable record 或明确区分 available、expired、deleted、not_found、forbidden。

## 7. Runtime Expansion Track (`019-021`)

`019-021` 描述的是 runtime hooks、knowledge、workflow 这条横向能力扩展轨道。

在 `027-029` 的 interop 重新定位之后，这条轨道仍然重要，但不再是当前唯一“下一步主线”。它应作为 host execution spine 的内部能力继续演进，而公共协议开放的优先级要先由 `027-public-interop-contract-stabilization`、`028-mobileclaw-trusted-interop-host`、`029-interop-probe-conformance-suite` 接住。

这条轨道也不应该继续按“一个小扩展点一个 spec”去拆。

如果我们后面希望参考 OpenClaw、Claude Code hooks 这类能力，并逐步接入：

- runtime hooks
- context source / knowledge source
- ingestion / retrieval
- DAG / task flow / automation

那更稳的做法是把它们收成 **三张更有分量、彼此边界清晰的 spec**。

### 7.1 `019-runtime-hooks-and-context-sources`

目标：
先把“可插拔 runtime”真正立住，但不急着做完整 DAG。

这一张应该一起解决：

- runtime event bus
- hook contract
- hook registration / filtering / audit
- context source / knowledge source contract
- request-time context contribution shape

为什么要合在一张：

- hooks 如果没有稳定的 context/source 合同，很快会退化成临时 callback
- knowledge source 如果没有 hooks/event 语义，也会变成另一套平行接入面
- 这两者本质上都在定义“谁可以在 runtime 生命周期里贡献额外行为或上下文”

这一张不应该做：

- 完整 ingestion pipeline
- 各类 connector 全量接入
- workflow graph runner

### 7.2 `020-knowledge-ingestion-and-retrieval`

目标：
把“知识库能力”做成 runtime 的正式一层，而不是 memory 的旁支。

这一张应该一起解决：

- file / folder / document ingestion
- chunk / index / source metadata
- retrieval contract
- redaction-aware retrieval summary
- workspace 内的 knowledge preview / source visibility

为什么单独成一张：

- 它会真正引入数据量、更新、重建、检索质量这些问题
- 这类问题和 hooks 合同相关，但实现复杂度已经足够成为独立 milestone
- 如果把它塞进 `019`，spec 会膨胀；如果拆得过碎，又会在 ingestion/index/retrieval 之间来回抖动

这一张不应该做：

- 完整 DAG
- automation runner
- 大而全的 connector marketplace

### 7.3 `021-workflow-graph-and-automation`

目标：
在已有 hooks、tool contract、knowledge retrieval、approval/policy 基础上，正式引入 DAG / task flow / automation。

这一张应该一起解决：

- workflow graph contract
- node / edge / trigger / guard
- tool node / hook node / context-source node / approval gate
- resumable execution
- audit / replay / failure explanation
- first automation / task-flow UI entry

为什么把 DAG 和 automation 放在同一张：

- 在这个项目里，它们最终都会依赖同一套 runtime execution graph
- 如果先做 graph contract，再单独做 runner，再单独做 automation surface，会形成过碎的 spec 链
- 把它们收在一张 milestone 里，变化足够大，也更接近真实用户价值

这一张不应该做：

- 完整远程 orchestration
- 多设备分布式 workflow
- marketplace 级别的 flow sharing

### 7.4 Spec 拆分原则

对这条 runtime expansion 轨道，拆分建议固定成这三个规则：

1. 一个 spec 要能完成一个真实里程碑，不只是一层抽象合同
2. 一个 spec 可以覆盖一组强相关能力，但不要跨越两个不同产品问题
3. 不再因为一个小入口、一个小 connector、一个小 hook 类型就单独起一张 spec

## 8. Hub Interop Delivery Track

在当前协议边界下，Hub Interop 的真正实现不再继续沿用 `022` 这张探索性 spec。

这一条现在是当前明确启动的 interop 实施主线。

`022` 保留 docs-level 聚合与问题收口的意义，但真正实现从 `024` 开始，拆成三张 spec：

### 8.1 `024-shared-interop-contract`

目标：

- 把协议本体做成与 host 实现隔离的共享公共 contract
- 把 Android `v1` binding 做成外部 app 可直接引用的 contract 层

为什么单独成一张：

- 如果没有独立公共协议模块，后面的 host 和 probe app 都会重新长出 host-specific 依赖
- 这张 spec 回答的是“协议本体是否真正独立成立”

当前已落地的首批 contract slice：

- `:hub-interop-contract-core` 已作为共享协议语义核心落地
- `:hub-interop-android-contract` 已作为 Android `v1` binding contract 落地
- `HubInteropCaller`、public Bundle codec、status / compatibility adapter 已对外部 caller 可用
- `:app` 与 `:interop-probe-app` 均已通过同一套公共 contract 接入，而不是复制 host 内部常量

### 8.2 `025-mobileclaw-interop-host`

目标：

- 让 Mobile Claw 成为共享协议的实现者之一
- 提供 governed discovery、authorization、invoke、task、artifact 等首批互通能力

为什么单独成一张：

- 这张 spec 回答的是“Mobile Claw 能不能正确实现协议”
- 它不应和公共协议模块的边界设计混在一起

当前已落地的首批 host slice：

- 导出的 `HubInteropProvider` 已作为 Android `v1` 基线 transport 接入
- `discover_surface`、`invoke_capability`、`request/get/revoke authorization`、`get_task`、`get_artifact` 已接入共享 contract
- `generate.reply` 已通过统一治理模型要求 inbound grant，而不再默认无授权放行
- 现有 governance center 与 runtime control center 已能看到 `reply.generate` grant 和最近 interop host task 摘要

### 8.3 `026-interop-probe-app`

目标：

- 建立独立协议消费者 app
- 只通过共享公共协议模块与 Mobile Claw 互通

为什么单独成一张：

- 这张 spec 回答的是“外部 app 能不能真的用起来”
- 它是对协议与 host 实现的外部验证，而不是 host 内部测试

当前已落地的首批 probe slice：

- `:interop-probe-app` 已作为独立 Android app 模块接入
- probe app 只依赖共享公共 contract，不依赖 `:app` 实现类
- 已验证 discovery、authorization request / refresh / revoke、governed invocation、task continuation、artifact lookup
- 已通过固定 minor / major 版本探测显式暴露 downgrade 与 incompatible 信号

### 8.4 `027-public-interop-contract-stabilization`

目标：

- 把 Hub Interop 公共协议从“能跑通”升级成“第三方 app 可以依赖的 contract”
- 稳定 public method、status code、Bundle schema、descriptor v1、compatibility behavior、unknown field policy
- 清理 `024/025` tasks checkbox 与实现状态不一致的问题

这一张 spec 的验收重点不是执行能力，而是 contract 稳定性：

- 固定公开 method：`discover_surface`、`request_authorization`、`get_grant_status`、`revoke_grant`、`invoke_capability`、`get_task`、`get_artifact`
- 明确 status code 语义，尤其区分 `UNAUTHORIZED`、`AUTHORIZATION_REQUIRED`、`PENDING`、`FORBIDDEN`、`NOT_FOUND`、`EXPIRED`
- 稳定 capability / grant / task / artifact descriptor v1
- 明确 version compatibility：major mismatch、minor newer、patch newer、required unknown field、optional unknown field、extension namespace
- public Bundle codec 有 roundtrip tests
- contract modules 继续不依赖 `:app`、Room、Hilt、Compose、policy engine 或具体 provider implementation

`027` 不做：

- host-attested identity implementation
- Room-backed task/artifact persistence
- probe conformance runner
- 新 capability 暴露
- control center UI

### 8.5 `028-mobileclaw-trusted-interop-host`

目标：

- 把 Mobile Claw Host 从“协议实现者”硬化成可信执行中枢
- 引入 host-attested caller identity 和 caller fingerprint
- 让 authorization、task ownership、artifact access、audit identity 都使用 host-derived identity
- 把 `generate.reply` 和 bounded `calendar.read` 作为首批公开可执行 capability

这一张 spec 的真实验收链路应该是：

```text
Probe App
  -> discover Mobile Claw Host
  -> request calendar.read grant
  -> Mobile Claw shows inbound grant request
  -> user approves
  -> Probe invokes bounded calendar.read
  -> Claw Host creates durable interop task
  -> runtime session executes calendar.read
  -> task completes
  -> artifact descriptor becomes available
  -> Probe loads artifact
  -> Mobile Claw audit/control center shows invocation
  -> user revokes grant
  -> Probe invocation fails after revoke
```

`028` 的主要范围：

- `HostAttestedCallerIdentity`
- claimed caller metadata 降级为 display / diagnostics
- grant request lifecycle record
- reuse `CallerGovernanceRecord` / `ScopeGrantRecord` as authorization truth
- durable `InteropTaskRecord` / `InteropArtifactRecord` 或明确 lifecycle semantics
- `get_task` / `get_artifact` ownership check
- `generate.reply` baseline
- bounded `calendar.read` baseline
- permission unavailable / provider unavailable explicit status
- audit enrichment and minimal control-center visibility

`028` 暴露的 capability 应限制为：

- `generate.reply`
- `calendar.read`

`calendar.read` 的价值在于它已经接入真实 Android Calendar Provider，能验证本地数据读取、权限缺失、bounded query、无结果、artifact、audit。它比 `contacts.read` 更成熟，比 `knowledge.search` 更容易收敛，也比 `calendar.write/delete` 风险低。

`028` 不做：

- 大量新 capability
- workflow runner
- knowledge / resource exchange
- side-effect tools interop exposure
- 完整 visual control center 重构

### 8.6 `029-interop-probe-conformance-suite`

目标：

- 把 probe app 从“手动测试 app”升级成协议一致性测试工具
- 证明一个完全独立的 app 可以持续验证 protocol 和 host 行为
- 形成可分享、可回归、可用于开放协议的 conformance report

`029` 应有两个模式：

1. `Manual mode`
用于人工调试：Discover、Request Authorization、Refresh Grant、Invoke、Poll Task、Load Artifact、Revoke、Export Report。

2. `Conformance mode`
一键跑测试：protocol compatibility、authorization lifecycle、caller identity mismatch、unauthorized invoke、pending grant invoke、granted invoke、revoked invoke、task lifecycle、artifact lifecycle、downgraded version、incompatible version、malformed request。

报告应输出：

- host package
- host authority
- protocol version
- supported methods
- supported capabilities
- test matrix
- pass/fail
- failure reason
- raw status codes
- timeline

`029` 不做：

- 变成完整第三方 client 产品
- 依赖 `:app`
- 修复 host 行为本身
- 新增未在 `027/028` 稳定过的协议能力

### 8.7 `030+` 后续候选

`029` 跑稳之后，后续路线先不固定成 `030-032`，但候选仍是：

- expanded read capability protocol
- host control center object details
- workflow capability runner
- resource and knowledge exchange
- side-effect capability protocol

这些不应该压进 `027-029`，否则会把公共协议、安全、治理、测试闭环和新功能一起放大。

也就是说，后面优先避免这种拆法：

- “仅新增一种 hook 类型”
- “只做一个 connector adapter”
- “只做一个 DAG 节点类型”

这些更适合成为已有 spec 里的子任务，而不是新的顶层 spec。

`012-real-appfunctions-integration` 现在已经完成，因此它不再是后续主线的阻塞项，而是已落地的 Android 对齐能力。

## 9. Runtime Control Center Track

目标：
把 app 收束成一个真正的 `conversation-first runtime control center`。

重点应放在：

- 内置多模态聊天继续作为主入口
- 当前请求的 runtime trace 清晰可见
- memory / approval / governance / tool / extension 的支持编辑对象可以从同一控制面进入
- 深层管理能力可发现，但默认不把主界面挤成 dashboard

对应 spec：

- `018-runtime-control-center`

完成标志：

- 用户能从 active conversation 进入统一 control surface
- 用户能看懂一次请求中 tool、approval、memory、extension 的主要贡献
- 支持编辑的运行时对象可以在 app 内直接管理
- 页面仍然保持可读、可用、不会因为能力增长而持续失控

### 9.1 产品界面总原则

这一段之后，产品界面应固定成一句总原则：

**聊天是前台，control center 是控制平面，detail page 是对象真相页。**

它意味着：

- 聊天页负责任务推进，不承担系统配置
- control center 负责全局能力、治理与状态，不变成第二个工作区
- detail page 负责单个对象的完整真相，不把对象语义拆散到多个零碎面板

### 9.2 界面分层

从 `018` 开始，建议把主产品面固定成五层，而不是继续让 runtime 信息自然扩散：

#### L0：Conversation Layer

唯一主入口，也是默认停留层。

这里应该承载：

- 用户意图与回复
- 当前任务最相关的上下文提示
- 即将发生或刚发生的动作卡片
- 本次任务的 proposal / approval / result

这里不应该承载：

- 大量 source 配置
- extension 结构化编辑
- workflow 定义器
- knowledge corpus 管理

#### L1：Session Layer

这是当前会话的运行时面板，建议保持 `session tray` 心智，而不是另一张完整页面。

这里应该承载当前会话的：

- active context
- pending approval
- running actions
- recent activity
- session-level constraints

这一层的目标是：用户不离开当前任务，也能看懂系统当前带着什么上下文、卡在哪一步、接下来要批准什么。

#### L2：Control Center Layer

这是全局控制平面，不是执行平面。

这里应该承载：

- sources / tools / extensions 的全局状态
- approval policy
- memory / knowledge 的总体入口
- workflow / automation 的总体入口
- model / governance / constraints
- 全局 recent activity 汇总

这一层适合回答：

- 系统有哪些能力
- 哪些能力当前打开
- 默认怎么审批
- 哪些来源可用
- 哪些自动化已启用

但它不应该承担单次任务的过程展开。

#### L3：Object Detail Layer

每个重要对象都应该有自己的对象真相页。

优先覆盖：

- extension
- context source
- knowledge asset
- workflow
- approval ticket
- activity item

对象真相页建议共享同一骨架：

1. Summary
2. Current state
3. Scope / policy
4. Provenance / source
5. Recent activity
6. Available actions

#### L4：Advanced / Diagnostics Layer

这层不进入默认主导航，只在高级路径暴露。

适合放：

- trace
- raw payload
- redaction preview
- test hooks
- import / export debug
- retry / recover / orphan cleanup

### 9.3 功能分级

除了界面分层，还要固定功能分级。建议以后按 `风险 + 可逆性 + 用户负担` 分成四级常规能力和一级高级能力。

#### G0：Ambient

系统自动提供，只做轻提示。

典型场景：

- 当前附加了 calendar / contacts context
- 当前命中了某个 knowledge asset

#### G1：Inspect

用户可看，但默认不要求立即处理。

典型场景：

- context 来源
- tool contract 摘要
- approval reason
- workflow run status
- source freshness

#### G2：Editable But Reversible

可以编辑，但应尽量可撤销、可回退。

典型场景：

- enable / disable extension
- 调整默认 approval mode
- attach / detach 某个 context
- pause / resume workflow
- pin / unpin source

#### G3：Guarded Execution

会产生 side effect，必须显式确认或受 policy 约束。

典型场景：

- 发消息
- 写日历
- 调 AppFunction
- 执行 external handoff
- 自动化执行到有副作用的节点

这一层应固定成统一手感：

- proposal
- diff / preview
- approval
- execution result
- rollback hint（能做则做）

#### G4：Privileged / Debug

对系统影响大，或易误伤的能力默认不进入普通路径。

典型场景：

- 删除 knowledge corpus
- 重建索引
- 清空 activity ledger
- 修改 redaction policy
- 修改 provider schema
- 进入高级诊断

### 9.4 统一对象流转

后续功能衔接应尽量统一成一条固定链路：

**Intent -> Context Attach -> Proposal -> Approval -> Execution -> Reflection**

它分别对应：

1. 用户在聊天里表达意图
2. 系统自动或半自动附加上下文，并可见地说明附加了什么、为什么附加、是否可移除
3. 任何可能有影响的动作，都先变成 proposal，而不是直接执行
4. approval 入口尽量留在聊天页和 session layer，不埋到 control center
5. execution 必须形成可追踪的 activity item，而不是只给 toast
6. reflection 必须把执行结果回流到会话里，让用户看见做了什么、结果如何、下一步能干什么

这条链路的意义是：继续保持 conversation-first，但不让系统在后台“默默做完再回来报一句 done”。

### 9.5 统一对象视图合同

重要对象应尽量固定成两种主视图，而不是在五个地方各长一套 UI：

- `summary view`：出现在 conversation inline、session tray、control center list
- `detail view`：出现在 object detail page

summary view 只回答：

- 它是什么
- 现在什么状态
- 能不能点

detail view 负责回答：

- 它从哪来
- 规则是什么
- 最近怎么变过
- 能做哪些操作

后续 spec 最好对每类对象显式声明：

- 出现在 L0 / L1 / L2 / L3 哪几层
- 默认属于 G0 / G1 / G2 / G3 / G4 哪一级
- 是否能 inline approve
- 是否进入 recent activity
- 是否进入 export
- 是否需要 redaction preview

### 9.6 `018.x` 的近端收口

在 `018` 完成之后，建议先允许一段不单独起顶层 spec 的 control-surface hardening。

这段收口应优先做三件事：

1. 加一个 `session tray`
当前任务内一键拉起，承载 active context、pending approval、running actions、recent activity。

2. 把 control center 重排成五组

- `Now`
- `Capabilities`
- `Policy`
- `Knowledge`
- `Automation`

其中：

- `Now` 放 pending approvals、running actions、active context、recent activity
- `Capabilities` 放 sources、tools、extensions
- `Policy` 放 approval rules、constraints、governance、model defaults
- `Knowledge` 放 memory、knowledge corpus、ingestion / retrieval 入口
- `Automation` 放 workflows、triggers、run history

3. 给核心对象统一 detail 模板
优先覆盖 approval、extension、source、context contribution、activity item。

这一段还应明确 extension editable management 的边界：

- `018.x` 先做 `Basic Editable`
  - enable / disable
  - 默认 approval mode
  - 可见性
  - pinning
  - 简单权限摘要查看
- `019` 再做 runtime 相关编辑
  - provider config schema
  - activation lifecycle
  - config 更新后的重挂载
  - 与 context contribution 的 attach / detach 联动

这部分的原则是：先把 extension 做到“可管理”，再把它做成“可编排”。

## 10. 后续横向轨道：Hooks / Knowledge / Workflow

这条轨道是 `018` 以及其近端 control-surface hardening 之后最自然的产品扩展方向，但在当前 interop 重新定义下，它应排在 `027-029` 的 public protocol、trusted host、probe conformance 闭环之后继续推进。

它解决的不是“再多接几个工具”，而是：

- 让 runtime 在生命周期中可被安全扩展
- 让外部知识与本地 memory 分层共存
- 让更复杂的多步任务不再只能靠单轮 prompt 拼接

这条轨道的三张 spec 就是：

- `019-runtime-hooks-and-context-sources`
- `020-knowledge-ingestion-and-retrieval`
- `021-workflow-graph-and-automation`

当这三张完成后，Mobile Claw 才会真正从“可治理的 agent app”进入“可扩展、可组合、可编排的本地 agent runtime”。

## 11. v1 完成态

当下面这些成立时，可以认为 `v1` 真正成立：

- 用户可以从工作区外把内容交给 Mobile Claw
- 内置多模态聊天是主入口，也是测试和驱动系统的主入口
- 用户可以在不离开当前任务的前提下看懂 session-level context、approval、running actions 与 recent activity
- 关键 action 拥有稳定的 structured contract、risk annotation、approval semantics
- caller、source、uri grant、tool、extension 都有统一合同而不是特例路径
- control center 回答的是全局能力与治理，而不是把当前任务流程再复制一遍
- 用户可以在 app 内看懂一次请求用了什么、为什么这样做、哪里被允许或拒绝
- 用户可以在 app 内管理支持编辑的 runtime artifact
- 重要对象拥有稳定的 summary view 和 detail view，而不是分散在多个不一致的面板中
- 至少两类系统源进入 runtime context，且仍受 local-first / privacy / policy 约束
- portability 输出成为真实功能，而不是只停留在 schema

到那时，Mobile Claw 才算真正从“可运行的 Agent Runtime”进入“可使用、可治理、可扩展的 Android 个人 Agent 产品”。

## 12. 019-021 的完成态

当下面这些成立时，可以认为下一段 roadmap 成立：

- runtime 拥有稳定的 event hook 和 context-source 合同
- 外部知识源不再和 memory 混在同一层
- 本地知识摄取、检索、预览成为正式能力
- 多步任务可以通过 workflow/DAG 稳定执行，而不是靠临时 prompt 拼接
- 新增 hooks、knowledge、workflow 仍复用 L0-L3 分层和 G0-G4 分级，不重新长出并行后台
- hooks、knowledge、workflow 的关键状态可以被当前 control center 吸收，而不是重新长出另一套管理面
