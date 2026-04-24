# Hub Interop Protocol Design v1

## 1. 文档目的

这份文档用于在进入新的 top-level spec 之前，先冻结 Hub Interop Protocol 的边界与核心能力，并说明 Mobile Claw 作为首批实现者时应满足的要求。

它解决的不是某一个 Android 入口怎么接，而是下面四件事：

- 协议本体如何以独立公共 contract 的形式被外部 app 引用
- Mobile Claw 如何作为一个可发现、可调用、可治理的 hub 对外暴露能力
- 外部 app 如何把自己的能力、上下文或任务接入 Mobile Claw
- 短时能力调用与长时任务协作如何分层，而不是混成一个入口
- 授权如何成为协议内建能力，并通过统一的入权限/出权限模型管理

在这份文档里：

- Hub Interop Protocol 是独立公共协议
- Mobile Claw 是该协议的实现者之一
- 后续的独立 probe / test app 是该协议的消费者与验证者之一

这份文档是 roadmap 与后续 spec 的上游设计基线。

## 1.1 当前实现对齐状态

截至 `025-mobileclaw-interop-host` 的首批实现，协议设计已经有一个真实 host 对齐面：

- Mobile Claw 通过独立导出的 `HubInteropProvider` 成为共享协议的实现者之一
- `discover / authorization / invoke / task / artifact` 已通过共享 Android binding 暴露
- 第一条受治理 capability 固定为 `generate.reply`
- inbound grant 当前复用现有 governance 模型，并通过现有治理中心管理，而不是再造第二套 host-only 设置面
- `ACTION_SEND` 继续保留为 compatibility ingress，不再承担主 interop contract 角色

## 2. 为什么现有 share 入口不够

当前实现已经证明了第一个真实外部入口成立：

- 其他 app 可以通过 Android share 把内容交给 Mobile Claw
- Mobile Claw 可以识别 caller、source、grant、attachment 并进入统一 runtime 主链路

但 `ACTION_SEND` / `ACTION_SEND_MULTIPLE` 这类 share contract 只适合做：

- 用户手势驱动的内容 handoff
- 低结构化、兼容型 ingress
- 明确由用户发起的一次性交付

它不适合长期承担下面这些协议目标：

- 显式 capability discovery
- 稳定的 callable contract
- 外部 app 向 Mobile Claw 暴露能力
- 长任务、多轮输入、artifact、异步完成
- 入权限/出权限的持久治理

因此，share 必须保留，但只能作为 compatibility ingress，不能继续被当作主协议。

## 3. 协议设计原则

### 3.1 Hub First

Mobile Claw 的角色不是单个被动目标 app，而是：

**一个 local-first、conversation-first、governed dispatch hub。**

它要能同时承担：

- 对外暴露能力
- 对内统一治理
- 接入外部能力
- 聚合上下文、记忆、知识与执行结果

但这不意味着协议本体附属于 Mobile Claw。

协议需要先独立成立，Mobile Claw 再作为其中一个 host / provider implementation 接入。

### 3.1.1 Protocol First, Host Agnostic

协议本体必须与任一具体 app 实现解耦。

这意味着：

- 外部 app 应能直接引用协议 contract，而不是复制文档中的魔法字符串
- Mobile Claw 不能成为协议常量、协议对象、协议版本定义的唯一宿主
- 后续的独立测试 app 应只依赖公共协议 contract 与 Android binding，而不依赖 Mobile Claw host internals
- 后续若出现第二个 host 或 provider app，也应复用同一协议本体

### 3.2 两类交互必须分开

协议必须区分：

- `capability-style interaction`
  - 更接近工具调用
  - 输入输出明确
  - 强 schema、强 scope、强 approval
- `task-style interaction`
  - 更接近 agent 协作
  - 允许多轮输入、进度、artifact、异步完成
  - 需要稳定 task identity

不能把这两类形态都压进 share payload，或都压进一次性的 callable entry。

### 3.3 授权是协议原语，不是普通能力

授权不应散落在每个 capability 的业务规则里。

协议必须内建：

- 授权请求
- grant 校验
- grant 生命周期
- grant 撤销
- 入权限与出权限的独立治理

### 3.4 兼容 Android，本体对齐 MCP / A2A

本协议不应简单复刻某个外部标准的 wire format。

更稳的方向是：

- 在 `capability plane` 上参考 MCP
- 在 `task plane` 上参考 A2A
- 在 Android 本地绑定层优先使用 AppFunctions、content URI、Intent、FileProvider 等系统机制

也就是说：

- 语义对齐国际协议
- 绑定适配 Android 本地生态

## 4. 协议分层

建议把 Mobile Claw 的跨 app 协议固定成四层。

### 4.1 Surface Descriptor Layer

任何可连接 surface 都先通过一份稳定自描述对象暴露出来。

它负责回答：

- 我是谁
- 我支持哪几种交互方式
- 我有哪些 capability 或 skill
- 我需要什么授权
- 我与当前 runtime 是否兼容

建议核心对象：

```text
HubSurfaceDescriptor
- surfaceId
- displayName
- summary
- contractVersion
- supportedMethods
- capabilities
- authorizationRequirement
- supportsAttachments
- tags
```

这层更接近 A2A Agent Card 的角色，但需要更贴近 Android 本地 surface 语义。

### 4.2 Capability Plane

这层用于短时、显式、结构化能力调用。

语义上参考 MCP：

- tool-like callable capabilities
- resource-like context access
- prompt-like guided invocation templates

建议核心对象：

```text
CallableCapabilityDescriptor
- capabilityId
- displayName
- description
- inputSchemaVersion
- outputArtifactTypes
- sideEffectLevel
- dataSensitivity
- boundedness
- availability
- availabilityMessage
- requiredScopes
- approvalRequirement
- authorizationRequirement
- preferredMethods
- compatibilitySignal
```

这一层负责：

- capability discovery
- schema-backed invocation
- explicit scope / approval posture
- 短时结构化返回

### 4.3 Task Plane

这层用于长时、多轮、状态化协作。

语义上参考 A2A：

- task identity
- message / follow-up input
- progress / status
- artifacts
- streaming or async completion

建议核心对象：

```text
InteropTaskRecord
- taskId
- handle
- displayName
- status
- lifecycleState
- availability
- summary
- artifactHandles
- createdAtEpochMillis
- updatedAtEpochMillis
- expiresAtEpochMillis
```

`ArtifactDescriptor` 同样需要把 lifecycle 和 availability 放进公共合同：

```text
InteropArtifactDescriptor
- handle
- displayName
- mimeType
- artifactType
- accessMode
- lifecycleState
- availability
- contentUri
- summary
- createdAtEpochMillis
- expiresAtEpochMillis
```

`CompatibilitySignal` 在 v1 中固定支持：

- `supported`
- `minor_version_downgraded`
- `major_version_unsupported`
- `malformed_version`
- `required_unknown_fields`
- `optional_unknown_fields`
- `extension_namespace_fields`

其中 required unknown field 使请求不兼容；optional unknown field 可以降级继续；extension namespace field 应尽量保留诊断而不破坏基础互通。

这一层负责：

- 多轮输入
- input-required
- working / completed / failed
- artifact exchange
- 中断和恢复

### 4.4 Governance Plane

这层把协议对象统一接回现有 runtime 治理模型：

- caller trust
- scope grants
- approval
- audit
- memory / knowledge / attachment redaction
- extension enablement

协议接入后不能绕过现有治理面。

## 5. 协议边界

建议把协议里的 cross-app 交互明确分成 5 类。

### 5.1 Share Ingress

用途：

- 从其他 app 把内容 handoff 给 Mobile Claw

定位：

- compatibility path
- 兼容型用户手势入口

特点：

- 用户显式发起
- 低结构化
- 默认不建立持久授权关系

### 5.2 Callable Capability

用途：

- 别的 app 显式调用 Mobile Claw 的能力
- Mobile Claw 显式调用外部 app 的能力

定位：

- 主协议之一

特点：

- 强 schema
- 强 scope
- 强 auth
- 强 audit

### 5.3 Resource / Context Exchange

用途：

- 受控交换上下文、资源、知识片段、可读数据、artifact handle

定位：

- 主协议之一

特点：

- 必须带资源边界
- 必须带 privacy / redaction 语义
- 不能默认等价于“全部记忆可读”

### 5.4 Task Collaboration

用途：

- 长任务、多轮输入、异步处理、artifact 输出

定位：

- 主协议之一

特点：

- 稳定 task identity
- 支持 input-required
- 支持 progress / completion
- 允许 artifact 和回调

### 5.5 Provider Federation

用途：

- 外部 app 将 capability、context、task surface 接入 Mobile Claw

定位：

- 主协议之一

特点：

- 外部 app 不是单纯 caller，而是 provider / peer
- 必须可发现、可启停、可审计、可治理

## 6. 角色模型

### 6.1 Mobile Claw 的角色

Mobile Claw 需要能同时扮演：

- `Hub Host`
  - 聚合 connected apps
  - 管理入/出权限
  - 统一治理与控制面
- `Capability Provider`
  - 对外暴露 model-backed/internal capabilities
- `Task Peer`
  - 参与长任务协作
- `Federation Client`
  - 连接外部 provider 与 agent-like peer

### 6.2 外部 app 的角色

外部 app 至少可能是：

- `Caller`
  - 只调用 Mobile Claw
- `Provider`
  - 暴露能力给 Mobile Claw
- `Peer`
  - 参与更长的状态化任务协作
- `Hybrid`
  - 既调用也被调用

协议不能把这些角色混成单一 caller 模型。

### 6.3 独立验证 app 的角色

在首批 Android 落地中，建议明确保留一个 `Protocol Consumer / Probe App` 角色。

这个 app 的职责不是复制 Mobile Claw，而是：

- 引用公共协议模块
- 调用 Mobile Claw 暴露的 public surface
- 走完 discovery、authorization、invoke、task、artifact 这些路径
- 验证协议 contract 是否足够清晰、稳定、可外部消费

## 7. 授权模型

### 7.1 授权不是普通 tool

授权虽然可以通过协议对象暴露，但语义上不应被建模成普通业务 capability。

它是协议保留能力，用于：

- 发起授权请求
- 查看 grant
- 校验 grant
- 撤销 grant
- 刷新 grant 状态

### 7.2 默认授权策略

建议所有 surface 默认：

- `authPolicy = require_auth`

只有极少数受限公开元数据允许：

- `authPolicy = none`

推荐最小策略枚举：

```text
AuthPolicyMode
- none
- user_consent
- trusted_app
- system_only
```

### 7.3 入权限与出权限分离

协议治理必须明确区分：

- `Inbound Permissions`
  - 谁可以发现我们
  - 谁可以调用我们的哪些 capability
  - 谁可以读取哪些资源
  - 谁可以创建哪些 task
- `Outbound Permissions`
  - 我们可以连接哪些外部 app
  - 我们可以调用它的哪些 capability
  - 我们可以把哪些数据发出去
  - 我们可以把哪些 artifact / memory / knowledge 暴露出去

一个 app 被允许调我们，不代表我们也可以调它。

### 7.4 Grant 维度

建议 grant 至少包含三组维度：

```text
InteropGrant
- direction: inbound | outbound
- lifetime: once | session | persistent
- scopeSet
- approvalMode
- sourceSurfaceId
- targetSurfaceId
- grantedBy
- grantedAt
- revokedAt
```

scope 建议至少覆盖：

- discover
- invoke capability
- read resource
- write resource
- create task
- resume task
- receive artifact
- push callback

### 7.5 权限管理面

协议必须在产品上有对应控制面。

建议统一落在现有 runtime control center 下，新增一个权限域，而不是单独再做“设置页孤岛”。

最小结构建议：

- `Connected Apps`
- `Inbound Permissions`
- `Outbound Permissions`
- `Pending Grants`
- `Audit & History`

最小授权动作建议：

- `Allow once`
- `Allow for session`
- `Always allow`
- `Deny`
- `Revoke`

### 7.6 协议保留授权能力

授权虽然不是普通业务 capability，但协议仍然需要一组稳定对象来表达授权行为。

建议固定一个保留 surface：

```text
AuthorizationSurfaceDescriptor
- authSurfaceId
- displayName
- supportedGrantDirections
- supportedGrantLifetimes
- supportedApprovalModes
- permissionCenterRoute
- auditEnabled
```

它负责承载下面这些保留动作：

- 请求授权
- 查询现有 grant
- 校验某次调用是否已满足授权条件
- 撤销既有 grant
- 打开对应权限管理入口

这个 surface 不参与普通 tool ranking，也不应该被 planner 当作常规业务能力调用。

### 7.7 连接关系记录

除了 grant，本协议还需要一层更稳定的连接关系记录，用于承载 connected app 的身份、兼容性与治理状态。

建议核心对象：

```text
ConnectedAppRecord
- connectedAppId
- packageName
- signingIdentity
- displayName
- surfaceDescriptorVersion
- trustLevel
- enabled
- inboundDefaultPolicy
- outboundDefaultPolicy
- lastSeenAt
- lastGrantedAt
- lastRevokedAt
```

这一层不等于授权结果。

它回答的是：

- 这个 app 是谁
- 我们是否认识它
- 它当前是否可连接
- 它默认应该按什么策略处理请求

### 7.8 权限管理页的信息结构

为了让授权真正可治理，权限管理页至少要能同时展示：

- `Connected Apps`
  - 连接状态、信任状态、最近使用、默认策略
- `Inbound Permissions`
  - 哪些 app 可以发现或调用我们，范围到 capability / resource / task
- `Outbound Permissions`
  - 我们可以连接哪些外部 surface，以及可向外暴露哪些数据或 artifact
- `Pending Grants`
  - 尚未完成授权、等待确认、等待审批的请求
- `Audit & History`
  - grant、revoke、deny、auto-expire、policy override 的历史

权限管理页的最小操作粒度不应只到 app，还应至少支持：

- app
- direction
- surface
- capability or task type
- lifetime
- approval mode

## 8. 与现有 runtime 的映射

### 8.1 现有可复用基础

当前代码已经具备做 hub 协议的核心底座：

- `CapabilityRegistry`
  - 已经是 capability plane 的统一调度骨架
- `ToolDescriptor` / tool catalog
  - 已经接近 MCP-style capability contract
- `RuntimeExtensionRegistration`
  - 已经接近 provider federation 的注册骨架
- `RuntimeContribution` / `Knowledge`
  - 已经接近 resource/context plane
- `Governance` / `Approval` / `Audit`
  - 已经是治理底座
- `AppFunctions`
  - 已经是 Android-native outward callable surface 的起点

### 8.2 现有不足

当前仍缺：

- 对外统一 surface descriptor
- 显式 inbound/outbound grant model
- resource/context exchange contract
- 长任务 task contract
- connected app record 与管理面
- share 之外的第一类主调用协议

### 8.3 Share 的新定位

当前 share ingress 应该继续保留，但重新定义为：

- `Compatibility Ingress`
- `User Gesture Entry`
- `Low-Structure Handoff`

而不是长期的统一 interop protocol。

## 9. Android v1 绑定决策

Android 第一版绑定细节已经单独冻结在 [Hub Interop Android IPC Design v1](./hub-interop-android-ipc-v1.md)。

模块边界与公共分发方式已经单独冻结在 [Hub Interop Module Packaging v1](./hub-interop-module-packaging-v1.md)。

这里先保留结论级摘要：

- Android `v1` 不采用单一 IPC，而采用混合绑定
- `AppFunctions` 是显式 callable capability 的首选 adapter
- `HubInteropProvider` 应成为 Android `v1` 的必选基线 transport
- `content://` URI grants / `FileProvider` 承担 resource 与 artifact 交换
- `ACTION_SEND` / `ACTION_SEND_MULTIPLE` 继续保留为 compatibility ingress
- 长任务先固定成 task handle + polling，不把 Binder callback 设为首批必选能力
- 公共 contract 必须与 host app 实现分层，Android 集成方与独立 probe app 消费的是独立公共协议模块，而不是整包 host runtime

## 10. 分阶段落地建议

### Phase 1: Surface + Capability

先建立：

- `HubSurfaceDescriptor`
- `CallableCapabilityDescriptor`
- inbound/outbound auth model
- connected app 基础记录

目标：

- 让 Mobile Claw 首次成为可显式发现、可显式调用的 hub

### Phase 2: Federation

再建立：

- 外部 provider / context source / peer surface 接入
- compatibility / enablement / trust 管理

目标：

- 让 Mobile Claw 首次能把别的 app 纳入自己的 governed runtime

### Phase 3: Task Plane

最后建立：

- task identity
- progress
- input-required
- artifact model
- async completion

目标：

- 让长任务协作不再退化成一次性 tool 调用

## 11. 与后续 spec 的关系

这份文档先于新的 interop spec。

建议顺序：

1. 先冻结这份 docs-level protocol design
2. 再修改或重写下一张 interop/hub spec
3. 再做验证 app 或 connected app demo

也就是说：

- **先定协议**
- **再定 spec**
- **最后定验证与实现切片**

## 12. 参考方向

这份设计主要参考了三类外部方向，但做了本地 Android hub 化收口：

- MCP：
  - capability discovery
  - tools / resources / prompts
  - capability negotiation
  - host-centered governance
- A2A：
  - surface self-description
  - skill/task/artifact model
  - stateful collaboration
  - versioned task interaction
- Android AppFunctions：
  - Android-native callable capability exposure
  - 本地 app-to-app capability binding

这不是对任何单一标准的原样复刻，而是：

**以 MCP 管能力，以 A2A 管协作，以 Android 原生绑定落地。**
