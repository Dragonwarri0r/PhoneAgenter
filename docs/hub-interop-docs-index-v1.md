# Hub Interop Docs Index v1

## 1. 文档目的

这份文档用于把当前已经协商完成的 Hub Interop Protocol 相关设计集中整理成一套可引用的 docs 索引。

它回答三个问题：

- 当前协议相关设计分别落在哪些文档里
- 这些文档之间是什么上下游关系
- 后续实现为什么要从 `024-026` 三张 spec 开始，而不是继续沿用 `022`

## 2. 当前文档清单

### 2.1 协议总设计

[Hub Interop Protocol Design v1](./hub-interop-protocol-design-v1.md)

用途：

- 定义 Hub Interop Protocol 的总体边界、分层、角色模型、授权模型
- 明确协议本体是独立公共协议
- 明确 Mobile Claw 只是协议实现者之一
- 明确独立 probe / test app 是协议消费者与验证者之一

### 2.2 Android IPC 绑定

[Hub Interop Android IPC Design v1](./hub-interop-android-ipc-v1.md)

用途：

- 冻结 Android `v1` 的 IPC 绑定方式
- 明确 `Preferred Adapter` 与 `Baseline Transport`
- 固定 Android 公共 authority、URI family、method family、status、handle 语义
- 给外部 app 与 probe app 提供统一的 Android 调用方式说明

### 2.3 模块与分发边界

[Hub Interop Module Packaging v1](./hub-interop-module-packaging-v1.md)

用途：

- 冻结协议本体、Android binding、host implementation、probe app 的模块边界
- 明确协议本体必须进行独立模块隔离
- 明确外部 app 需要直接引用公共协议模块
- 明确 Android 集成方最终消费的是独立 contract 分发层，而不是 `:app` 内部实现

### 2.4 相关上游与对齐文档

[Hub Interop 027-029 Spec Split v1](./hub-interop-027-029-spec-split-v1.md)

[Agent Runtime Project Roadmap v1](./project-roadmap-v1.md)

[Spec Breakdown v1](./spec-breakdown-v1.md)

[Tool, Capability, and Extension Standards v1](./tool-capability-and-extension-standards-v1.md)

用途：

- 让协议设计进入 roadmap
- 让 spec 编排指向新的协议路径
- 让 `027-029` 的新增 spec 拆分有明确依据
- 让 capability / extension 标准与 Android interop 绑定保持一致

## 3. 推荐阅读顺序

建议按下面顺序阅读：

1. `hub-interop-protocol-design-v1`
2. `hub-interop-android-ipc-v1`
3. `hub-interop-module-packaging-v1`
4. `hub-interop-027-029-spec-split-v1`
5. `project-roadmap-v1`
6. `spec-breakdown-v1`

这样读的原因是：

- 先看协议本体是什么
- 再看 Android 第一版怎么落 transport
- 再看模块如何隔离与分发
- 最后再看 roadmap 和 spec 如何承接

## 4. 当前共识摘要

目前已经冻结的核心共识是：

- Hub Interop Protocol 是独立公共协议，不附属于 Mobile Claw
- Mobile Claw 是该协议的首批实现者之一
- Android `v1` 不采用单一 IPC，而采用混合绑定
- `AppFunctions` 是显式 callable capability 的首选 adapter
- `HubInteropProvider` 是 Android `v1` 的必选基线 transport
- 协议本体必须独立模块隔离
- Android 调用方与独立 probe app 都应通过共享公共协议模块接入
- 独立 probe app 是正式验证对象，不是 host 内部测试页面

补充现状：

- `:hub-interop-contract-core` 和 `:hub-interop-android-contract` 已经在仓库中建立为真实模块骨架
- `:app` 已开始引用这两层公共 contract，而不再继续把 interop 公共定义只放在宿主内部
- `:interop-probe-app` 已作为独立 Android app 模块落地，只通过共享公共 contract 验证 discovery、authorization、invoke、task、artifact 与 contract drift
- `027-public-interop-contract-stabilization` 已把 status taxonomy、descriptor v1 lifecycle / availability 字段、unknown-field compatibility policy 和 Bundle codec roundtrip 纳入公共 contract 稳定范围

## 5. 为什么不继续使用 `022`

`022-hub-interop-protocol` 在当前上下文里保留的是：

- 一轮探索性 spec 收口
- 对问题域的初步聚合

但在当前新的边界下，它不再适合作为真正实现切片的编号，原因是：

- `023` 已经存在，继续沿用 `022` 会让时间线和实现顺序混乱
- 当前任务已经从“单张 interop 大 spec”变成“协议本体、host 实现、独立 probe app”三个可交付切片
- 现有 docs 已经足够承担 `022` 的探索与上游设计角色

因此，后续真正实现从 `024` 开始。

## 6. 从 `024` 开始的三张实现 spec

### 6.1 `024-shared-interop-contract`

目标：

- 建立共享公共协议 contract
- 建立 Android binding contract
- 让 host 与外部 app 可以引用同一套公共定义

当前实现状态：

- `:hub-interop-contract-core` 已承载共享版本、兼容性、descriptor、handle 与 capability 语义；`027` 进一步补齐 descriptor v1 lifecycle、availability、schema、side-effect、sensitivity、boundedness 与 unknown-field compatibility policy
- `:hub-interop-android-contract` 已承载 authority、method、status、Bundle codec 与 caller helper；`027` 固定 public status taxonomy 并补齐 descriptor Bundle roundtrip 覆盖
- `:app` 与 `:interop-probe-app` 已通过同一套公共 contract 编译和运行，不再复制 host-owned interop 常量

### 6.2 `025-mobileclaw-interop-host`

目标：

- 让 Mobile Claw 成为协议实现者之一
- 通过共享公共 contract 暴露 governed discovery、authorization、invoke、task、artifact 流程

当前实现状态：

- `HubInteropProvider` 已作为导出的 host boundary 接入
- 已实现 `discover / invoke / request authorization / get grant status / revoke grant / get task / get artifact`
- `generate.reply` 作为第一条受治理 capability 暴露，默认要求 inbound grant
- governance center 与 runtime control center 已暴露 connected caller grant 与最近 interop task 状态

### 6.3 `026-interop-probe-app`

目标：

- 建立独立协议消费者 app
- 只通过共享公共 contract 与 Mobile Claw 互通
- 验证协议是否真的可被外部 app 消费

当前实现状态：

- `:interop-probe-app` 已完成独立安装与运行的 Compose 验证壳
- probe app 只依赖 `:hub-interop-contract-core` 与 `:hub-interop-android-contract`
- 已覆盖 discovery、grant request / refresh / revoke、governed invoke、task polling、artifact lookup、contract drift diagnostics
- 已提供英文与简体中文的验证输出与分享摘要

## 7. 后续实施顺序

建议顺序：

1. `024-shared-interop-contract`
2. `025-mobileclaw-interop-host`
3. `026-interop-probe-app`
4. `027-public-interop-contract-stabilization`
5. `028-mobileclaw-trusted-interop-host`
6. `029-interop-probe-conformance-suite`

这个顺序的原因很直接：

- 先有独立公共协议模块
- 再有 Mobile Claw 这个实现者
- 最后再用独立 probe app 证明协议真的成立
- 然后先稳定公共 contract
- 再硬化可信 host
- 最后把 probe 升级成可回归的 conformance suite
