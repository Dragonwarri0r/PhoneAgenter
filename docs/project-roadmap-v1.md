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

### 下一阶段：Contract Hardening

当前最应该继续推进的是两张合同型 spec：

- `016-external-caller-interop-contracts`
- `017-unified-extension-surface`

这两张 spec 的目标不是继续堆功能，而是先把外部调用与内部扩展的边界固定下来。

### 紧接着：Runtime Control Consolidation

在 `016` 和 `017` 之后，需要补上一张真正的产品收口 spec：

- `018-runtime-control-center`

它解决的不是“再加一个管理页面”，而是把已经存在的 runtime / memory / approval / tool / extension 能力收束成一个 app 内可读、可编辑、可治理的控制面，同时保持多模态聊天仍然是主入口。

## 6. 现在仍然缺的产品收口

从当前完成度看，最明显的缺口已经不是 `007-015` 当初定义的那些基础能力，而是下面五个收口问题：

1. `external interop 仍然偏入口导向`
现有外部入口已经存在，但 caller identity、grant、callable surface 还没有完全稳定成统一 interop family。

2. `extension 还是 hook-first，不是 system-first`
`006` 和现有能力已经证明扩展方向成立，但统一 registration / compatibility / enablement 还没有封口。

3. `管理面仍然分散`
memory、governance、approval、tool、extension 各自有自己的入口或半成品视图，用户仍要自己拼出系统全貌。

4. `内置聊天还没有成为完整的“管理我们”的入口`
它已经能做多模态测试和执行，但还没有把“查看和管理系统自身状态”彻底吸进同一主工作流。

5. `explainability 仍然分片`
来源、工具、批准、上下文、扩展贡献都已经部分可见，但还没有形成稳定、连续、可回看的 runtime trace。

## 7. 018 之后的主线

`016-018` 完成之后，下一段主线不应该继续按“一个小扩展点一个 spec”去拆。

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

从 `019` 开始，拆分建议固定成这三个规则：

1. 一个 spec 要能完成一个真实里程碑，不只是一层抽象合同
2. 一个 spec 可以覆盖一组强相关能力，但不要跨越两个不同产品问题
3. 不再因为一个小入口、一个小 connector、一个小 hook 类型就单独起一张 spec

也就是说，后面优先避免这种拆法：

- “仅新增一种 hook 类型”
- “只做一个 connector adapter”
- “只做一个 DAG 节点类型”

这些更适合成为已有 spec 里的子任务，而不是新的顶层 spec。

`012-real-appfunctions-integration` 现在已经完成，因此它不再是后续主线的阻塞项，而是已落地的 Android 对齐能力。

## 8. 新增横向轨道：Runtime Control Center

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

## 8.5 下一段新增轨道：Hooks / Knowledge / Workflow

这条轨道是 `018` 之后最自然的产品扩展方向。

它解决的不是“再多接几个工具”，而是：

- 让 runtime 在生命周期中可被安全扩展
- 让外部知识与本地 memory 分层共存
- 让更复杂的多步任务不再只能靠单轮 prompt 拼接

这条轨道的三张 spec 就是：

- `019-runtime-hooks-and-context-sources`
- `020-knowledge-ingestion-and-retrieval`
- `021-workflow-graph-and-automation`

当这三张完成后，Mobile Claw 才会真正从“可治理的 agent app”进入“可扩展、可组合、可编排的本地 agent runtime”。

## 9. v1 完成态

当下面这些成立时，可以认为 `v1` 真正成立：

- 用户可以从工作区外把内容交给 Mobile Claw
- 内置多模态聊天是主入口，也是测试和驱动系统的主入口
- 关键 action 拥有稳定的 structured contract、risk annotation、approval semantics
- caller、source、uri grant、tool、extension 都有统一合同而不是特例路径
- 用户可以在 app 内看懂一次请求用了什么、为什么这样做、哪里被允许或拒绝
- 用户可以在 app 内管理支持编辑的 runtime artifact
- 至少两类系统源进入 runtime context，且仍受 local-first / privacy / policy 约束
- portability 输出成为真实功能，而不是只停留在 schema

到那时，Mobile Claw 才算真正从“可运行的 Agent Runtime”进入“可使用、可治理、可扩展的 Android 个人 Agent 产品”。

## 10. 019-021 的完成态

当下面这些成立时，可以认为下一段 roadmap 成立：

- runtime 拥有稳定的 event hook 和 context-source 合同
- 外部知识源不再和 memory 混在同一层
- 本地知识摄取、检索、预览成为正式能力
- 多步任务可以通过 workflow/DAG 稳定执行，而不是靠临时 prompt 拼接
- hooks、knowledge、workflow 的关键状态可以被当前 control center 吸收，而不是重新长出另一套管理面
