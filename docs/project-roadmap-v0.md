# Agent Runtime Project Roadmap v0

## 1. 项目目标

本项目的 `v0` 目标是构建一个 `Android + 单用户 + 本地优先` 的个人 Agent Runtime。

系统需要在单机上完成以下核心能力：

- 接收并解析 agent 请求
- 基于本地 memory 和 persona 形成执行上下文
- 通过独立的风险分类 agent 判断动作风险
- 对低风险动作自动执行
- 对高风险动作弹出确认
- 优先通过 `AppFunctions` 接入 Android 跨 App 能力
- 为未来的 `sync / merge / provider extension` 预留接口

### 1.1 产品目标

项目希望先落地一个真正可用的本地个人 Agent Runtime，而不是只停留在概念验证。

v0 的产品目标包括：

- 让用户可以在 Android 本机上发起 agent 请求并获得执行结果
- 让系统具备基本的上下文理解能力，而不是只做一次性问答
- 让低风险操作可以自动完成，高风险操作仍保持用户控制权
- 让跨 App 能力接入有统一路径，减少后续能力扩展成本

### 1.2 系统目标

项目需要建立一套可扩展的 runtime 主干，使后续能力增长不会推翻当前设计。

v0 的系统目标包括：

- 建立清晰的 `request -> decision -> execution -> audit` 主链路
- 建立 `memory / persona / capability / policy` 的独立边界
- 建立统一的风险分类与审批机制
- 建立 Android 能力桥与内部能力模型的映射层

### 1.3 架构目标

虽然 v0 不实现所有高级能力，但架构上需要从第一天开始预留扩展空间。

v0 的架构目标包括：

- 预留 `sync / merge` 接口，避免未来多设备支持时推倒重来
- 预留 provider 扩展机制，避免能力接入只能写特例
- 预留 memory 的共享与隔离策略，支持后续公共/私密治理
- 保证 Runtime Core 不被 Android 单一平台能力模型绑死

### 1.4 v0 完成目标

当以下目标成立时，可以认为项目的 `v0` 主体成立：

- 本地 agent 请求可以进入统一执行链路
- 系统可以基于本地 context 做出能力调用决策
- 风险分类可以驱动自动执行或确认执行
- `AppFunctions` 可以作为首选 Android capability bridge
- memory 已具备生命周期、作用域、共享性和同步预留字段
- 审计与解释能力可以支撑用户理解系统行为

## 2. v0 范围定义

### 2.1 In Scope

- Android 平台优先
- 单用户
- 本地优先
- 单设备先跑通主链路
- Human-in-the-loop 默认开启
- 低风险自动执行，高风险确认执行
- 以 `AppFunctions` 为 Android 能力桥主路径
- Memory 模型预留同步、共享、合并能力

### 2.2 Out of Scope

- v0 不做真正的多设备同步
- v0 不做全量原始私密上下文跨设备共享
- v0 不做开放给任意第三方的完整插件生态
- v0 不做 iOS / Desktop 的统一落地实现
- v0 不追求完全自治的高风险操作

## 3. 核心设计方向

### 3.1 运行时总原则

- `persona != memory`
- `risk classification != final authorization`
- `AppFunctions is adapter, not runtime core`
- `local-first`
- `private by default`

### 3.2 风险执行原则

- 通过独立的 `risk classification agent` 先判断动作风险
- 当分类结果明显为低风险时，允许自动执行
- 当分类结果明显为高风险时，要求用户确认
- 最终执行决策仍由 `policy engine` 结合 scope、caller、上下文进行裁决

### 3.3 Memory 建模原则

Memory 不只按内容主题区分，还要同时考虑：

- 生命周期：长期、短期、临时
- 作用域：全局、应用级、联系人级、设备级
- 共享属性：公共、可分享摘要、私密
- 同步策略：仅本地、摘要同步、完整同步

这意味着某条 memory 可以同时具有如下属性：

- 是短期 memory
- 只属于某个 app
- 默认私密
- 后续只允许摘要同步

## 4. 高层架构

### 4.1 Runtime Core

负责：

- Persona
- Memory
- Risk Classification
- Policy Decision
- Capability Broker
- Audit

### 4.2 Android Integration Layer

负责：

- AppFunctions Bridge
- Android System APIs
- Caller Verification
- Package / Signature 校验

### 4.3 Fallback / Extension Layer

负责：

- Intent
- Deep Link
- Share Entry
- Accessibility
- Future Providers

## 5. 项目级实施路线

### 阶段 A：项目框架冻结

目标：先把不会轻易变的边界固定下来。

需要敲定：

- v0 产品范围
- 三层架构
- 核心实体模型
- Memory 一级生命周期分类
- Capability 抽象方式
- Risk Classifier 与 Policy Engine 的职责边界
- Android 集成优先级
- Sync / Share 预留策略

阶段产物：

- `Project Overview`
- `Architecture v0`
- `Domain Model v0`
- `Execution Flow v0`
- `Roadmap v0`

验收标准：

- 对系统边界没有关键歧义
- 后续 schema 和模块划分可以基于本阶段输出继续推进

### 阶段 B：Runtime Core 骨架搭建

目标：把本地 runtime 主链路跑起来。

优先实现：

- `request ingress`
- `execution session`
- `capability broker`
- `risk assessment pipeline`
- `policy decision`
- `audit event`
- 基础 `persona` / `memory` 存储接口

主链路固定为：

`request -> classify -> decide -> execute/confirm -> audit -> writeback`

验收标准：

- 一次请求可完整穿过主链路
- 系统能区分自动执行和待确认执行
- 每次执行都有审计记录

### 阶段 C：Memory System v0

目标：把 memory 做成可扩展、可隔离、可治理的基础能力。

优先实现：

- `memory_item` 模型
- 生命周期分类：`durable / working / ephemeral`
- 作用域：`global / app_scoped / contact_scoped / device_scoped`
- 共享策略：`private / shareable_summary / shareable_full`
- 同步策略：`local_only / sync_summary / sync_full`
- 基础检索、写回、清理和提升规则

核心要求：

- Memory 默认 `private`
- 新写入的 app memory 默认 `app_scoped`
- 同一个用户下不同 app 的 memory 可以隔离
- 数据模型从一开始就预留同步与合并所需字段

验收标准：

- Agent 可以读写本地 memory
- Memory 支持作用域隔离
- Memory 支持标记可分享与不可分享
- Memory schema 已具备未来同步扩展位

### 阶段 D：Risk Classification + Policy

目标：把“是否危险”与“是否允许执行”拆成两个层次。

优先实现：

- `risk classification agent`
- `policy engine`
- 风险到执行动作的映射规则
- 高风险确认机制

建议风险结果：

- `low`
- `medium`
- `high`
- `blocked`

建议执行结果：

- `auto_execute`
- `preview_first`
- `require_confirmation`
- `deny`

验收标准：

- 低风险动作可自动执行
- 高风险动作必须确认
- 最终裁决不会只依赖 classifier 单点判断

### 阶段 E：Android Integration v0

目标：让 runtime 真正接上 Android 能力层。

能力接入优先级：

`AppFunctions > Intent / Deep Link > Share Entry > Accessibility`

优先实现：

- `AppFunctions adapter`
- capability registration
- provider routing
- caller verification
- Android package / signature 基础校验
- system-native capability 接入

验收标准：

- Runtime 能统一发现和路由一批 Android capability
- `AppFunctions` 成为首选 capability bridge
- Android 集成不会反向污染 runtime core

### 阶段 F：用户确认、解释与审计

目标：让系统可控、可解释、可回看。

优先实现：

- 预览和确认模型
- why-this-action 解释能力
- 审计日志结构化记录
- 执行结果说明
- 拒绝原因说明

验收标准：

- 用户能理解系统为什么要执行某动作
- 用户能区分建议、待确认、已执行
- 每次执行和拒绝都有清晰记录

### 阶段 G：Future Hooks 预留

目标：虽然 v0 不实现同步和复杂扩展，但架构上提前预留接口。

优先预留：

- `sync interface`
- `merge interface`
- `share / export policy hooks`
- provider extension hooks
- memory promotion / demotion hooks

未来目标：

- 多设备摘要同步
- 冲突合并
- 更复杂的 capability provider
- 更精细的审批策略
- 更强的自动化模式

验收标准：

- 核心模型无需大改即可进入 `v1`
- 新增 sync/provider 不需要推翻 memory 和 capability schema

## 6. 项目并行主线

整个项目建议按以下 5 条主线并行推进：

- `Architecture Track`：边界、模型、状态机、结构文档
- `Runtime Track`：core services 和 execution pipeline
- `Memory Track`：schema、检索、隔离、share policy
- `Android Track`：AppFunctions 和 provider integration
- `Safety Track`：risk classification、policy、approval、audit

## 7. 里程碑建议

- `M1`：架构与模型冻结
- `M2`：本地 runtime 主链路跑通
- `M3`：memory v0 跑通
- `M4`：risk + policy 跑通
- `M5`：Android capability bridge 跑通
- `M6`：确认、审计、解释能力补齐
- `M7`：为 sync / merge / extension 预留接口

## 8. v0 完成态定义

当下面这些条件成立时，可以认为 `v0` 达标：

- 系统能在 Android 本地接收 agent 请求
- 能基于本地 memory 和 persona 形成执行上下文
- 能通过独立 classifier 区分风险等级
- 能自动执行低风险动作
- 能对高风险动作要求确认
- 能通过 `AppFunctions` 为主的方式调度能力
- 能记录完整 audit
- Memory 已具备 `scope / shareability / sync hooks`

## 9. 下一步文档拆解建议

建议接下来按以下顺序继续细化：

1. `Architecture & Domain Model`
2. `Execution Model`
3. `Memory Model`
4. `Capability / Policy Model`
5. `Android Integration Model`
6. `Sync / Merge Hooks`

这份 roadmap 的作用是先固定项目级路线、边界和阶段目标，后续详细设计文档都应在此基础上展开。
