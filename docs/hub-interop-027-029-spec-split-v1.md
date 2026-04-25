# Hub Interop 027-029 Spec Split v1

日期：2026-04-24

目的：把新的 Hub Interop roadmap 从一张过大的 `027-public-interop-host-baseline` 拆成三张更稳的 spec：

- `027-public-interop-contract-stabilization`
- `028-mobileclaw-trusted-interop-host`
- `029-interop-probe-conformance-suite`

## 1. 当前判断

`024-026` 已经证明三件事：

- `024`：共享 public protocol modules 可以独立存在。
- `025`：Mobile Claw 可以实现这个协议并接入 runtime。
- `026`：独立 probe app 可以只依赖 contract modules 调用 host。

但它们仍然是第一版可跑通闭环，不是长期公共入口。

当前最关键的风险不是“能力不够多”，而是：

- protocol status / descriptor / compatibility 还没有到稳定公共 API 的粒度。
- host 仍需要 host-attested caller identity，不能信 request Bundle 里的 caller metadata。
- task/artifact 还需要 durable record 或明确 lifecycle semantics。
- probe app 还需要从 manual demo 升级为 conformance suite。

所以新增 spec 不应该继续扩成 `027-032` 一长串，也不应该把所有硬化塞进一张 `027`。更稳的切法是把 protocol、host、probe 分成三张。

## 2. 拆分原则

1. `027` 只稳定公共协议。
它回答：第三方 app 依赖 contract modules 时，method、status、schema、descriptor、compatibility 是否足够清晰稳定？

2. `028` 只硬化 Mobile Claw Host。
它回答：Claw app 是否能可信地接住外部请求，并通过治理、授权、runtime、task、artifact、audit 完成执行？

3. `029` 只升级 Probe Conformance。
它回答：一个完全独立 app 是否能持续验证协议和 host 行为，而不是只手动点按钮看结果？

这三张 spec 的顺序必须固定：

```text
027 public contract
  -> 028 trusted host
  -> 029 probe conformance
```

## 3. 027-public-interop-contract-stabilization

### 目标

把 `:hub-interop-contract-core` 和 `:hub-interop-android-contract` 从“第一版可用”硬化成第三方 app 可以依赖的公共 contract。

### 范围

- 固定 public method family：
  `discover_surface`、`request_authorization`、`get_grant_status`、`revoke_grant`、`invoke_capability`、`get_task`、`get_artifact`
- 清理 status code taxonomy：
  `OK`、`BAD_REQUEST`、`UNAUTHORIZED`、`AUTHORIZATION_REQUIRED`、`AUTHORIZATION_PENDING`、`FORBIDDEN`、`NOT_FOUND`、`EXPIRED`、`INCOMPATIBLE_VERSION`、`UNSUPPORTED_CAPABILITY`、`PROVIDER_UNAVAILABLE`、`PERMISSION_UNAVAILABLE`、`POLICY_DENIED`、`APPROVAL_REQUIRED`、`APPROVAL_REJECTED`、`EXECUTION_FAILED`、`INTERNAL_ERROR`
- 稳定 descriptor v1：
  capability、grant、task、artifact、surface、compatibility
- 明确 version / compatibility policy：
  major mismatch、minor newer、patch newer、required unknown field、optional unknown field、extension namespace
- 区分 required unknown fields 与 optional unknown fields
- 为 Bundle request/response codecs 补 roundtrip tests
- 把 `024/025` tasks checkbox 与实现状态同步
- 对齐 docs 中 authority / method / status / descriptor 示例

### 不做

- 不实现 host-attested caller identity
- 不做 Room-backed task/artifact
- 不新增 probe conformance runner
- 不暴露新 capability
- 不做 control center UI

### 主要触达区域

- `hub-interop-contract-core/src/main/kotlin/com/mobileclaw/interop/contract/`
- `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/`
- `hub-interop-android-contract/src/main/java/com/mobileclaw/interop/android/bundle/`
- `hub-interop-contract-core/src/test/`
- `hub-interop-android-contract/src/test/`
- `docs/hub-interop-*.md`
- `specs/024-shared-interop-contract/tasks.md`
- `specs/025-mobileclaw-interop-host/tasks.md`

### 验收

- contract modules 不依赖 `:app`、Room、Hilt、Compose、policy engine 或具体 provider implementation。
- status code 能清楚区分 unauthorized、authorization required、pending、forbidden、not found、expired。
- `get_artifact(handle)` 语义明确：
  handle 不存在是 `NOT_FOUND`，已过期是 `EXPIRED`，属于其他 caller 是 `FORBIDDEN`，caller 无可信身份是 `UNAUTHORIZED`。
- compatibility diagnostics 能表达 supported、downgraded、incompatible、required unknown、optional unknown。
- request/response Bundle codecs 有 roundtrip tests。
- `024/025` checklist 不再和 implemented 状态冲突。

### `027` Validation Notes

`027` 的实现收敛点如下：

- `:hub-interop-android-contract` 固定 public status taxonomy：`ok`、`bad_request`、`unauthorized`、`authorization_required`、`authorization_pending`、`forbidden`、`not_found`、`expired`、`incompatible_version`、`unsupported_capability`、`provider_unavailable`、`permission_unavailable`、`policy_denied`、`approval_required`、`approval_rejected`、`execution_failed`、`internal_error`。
- `:hub-interop-contract-core` 的 descriptor v1 已补齐 capability schema / artifact / side-effect / sensitivity / boundedness / availability 字段，以及 grant/task/artifact lifecycle 字段。
- compatibility policy 已区分 malformed version、major mismatch、minor newer、required unknown field、optional unknown field、extension namespace field。
- Android Bundle codec 已覆盖 compatibility、surface/capability、grant、task、artifact descriptor roundtrip。
- `024` / `025` tasks checkbox 应与各自 `Status: Implemented` 对齐，避免 roadmap 继续显示“代码已实现但任务 0/N”的冲突。

## 4. 028-mobileclaw-trusted-interop-host

### 目标

把 Mobile Claw 从“实现协议的 app”硬化成可信 host：外部 caller 只能通过协议边界进入 runtime，host 自己确认身份、授权、ownership 和审计。

### 范围

- 引入 `HostAttestedCallerIdentity`
- 从 `ContentProvider` boundary 获取真实 caller package / UID，并计算 signing certificate digest
- 把 request Bundle 里的 caller 信息降级为 `ClaimedCallerMetadata`
- grant lookup 使用 host-attested caller fingerprint
- task ownership 使用 host-attested caller fingerprint
- artifact access 使用 host-attested caller fingerprint
- audit identity 使用 host-attested caller fingerprint
- 新增 interop authorization request lifecycle record，继续复用 `CallerGovernanceRecord` / `ScopeGrantRecord` 作为授权真相
- task/artifact 做 durable record，或明确 restart 后的 expired / not-found 语义
- `get_task` 和 `get_artifact` 做 ownership check
- `generate.reply` 保持 baseline invocation
- 新增 bounded `calendar.read`
- provider unavailable / permission unavailable / empty result 明确返回
- audit 和 minimal control-center visibility 能展示 host-attested caller identity、capability、grant、task、artifact 摘要

### 不做

- 不开放 `contacts.read`
- 不开放 `knowledge.search`
- 不开放 `calendar.write` / `calendar.delete`
- 不开放 `message.send`
- 不做 workflow runner
- 不做完整 object detail UI 重构

### 主要触达区域

- `app/src/main/java/com/mobileclaw/app/runtime/interop/`
- `app/src/main/java/com/mobileclaw/app/runtime/governance/`
- `app/src/main/java/com/mobileclaw/app/runtime/session/`
- `app/src/main/java/com/mobileclaw/app/runtime/capability/`
- `app/src/main/java/com/mobileclaw/app/runtime/provider/` 或现有 calendar provider 相关路径
- `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
- `app/src/test/java/com/mobileclaw/app/runtime/interop/`

### 验收

- spoofed caller metadata 不能复用其他 caller 的 grant。
- revoked caller 不能继续 invoke。
- cross-caller task/artifact access 返回 `FORBIDDEN` 或等价明确状态。
- caller 无法 host-attest 时返回 `UNAUTHORIZED`，不进入 runtime。
- accepted task 进程重启后可恢复，或返回明确 `EXPIRED` / `NOT_FOUND` 语义。
- bounded `calendar.read` 能从 interop 调用进入 runtime，并产生 calendar summary artifact。
- calendar permission 缺失返回 `PERMISSION_UNAVAILABLE`。
- audit/control center 至少能展示 host-attested caller、capability、grant、task/artifact 摘要。

## 5. 029-interop-probe-conformance-suite

### 目标

把 `:interop-probe-app` 从手动 demo app 升级成协议一致性测试工具，形成可重复执行、可分享报告、可发现 host drift 的 conformance baseline。

### 范围

- Manual mode：
  Discover、Request Authorization、Refresh Grant、Invoke、Poll Task、Load Artifact、Revoke、Export Report
- Conformance mode：
  compatibility、authorization lifecycle、caller spoof、unauthorized invoke、pending grant invoke、granted invoke、revoked invoke、task lifecycle、artifact lifecycle、malformed request、downgraded version、incompatible version
- 支持 claimed identity mismatch diagnostics
- 支持 bounded `calendar.read` conformance path
- 支持 report export
- report 包含 host package、authority、protocol version、supported methods、supported capabilities、test matrix、pass/fail、failure reason、raw status codes、timeline
- probe app 继续只依赖 `:hub-interop-contract-core` 和 `:hub-interop-android-contract`

### 不做

- 不把 probe 做成完整第三方 client 产品
- 不依赖 `:app`
- 不修复 host 行为本身
- 不新增 `027/028` 没有稳定过的协议能力

### 主要触达区域

- `interop-probe-app/src/main/java/com/mobileclaw/interop/probe/`
- `interop-probe-app/src/test/java/com/mobileclaw/interop/probe/`
- `hub-interop-android-contract/src/test/` 里需要被 probe 共享验证的 helper 覆盖
- 可选：新增 report model / conformance runner model

### 验收

- Probe manual mode 能跑完整 external lifecycle。
- Probe conformance mode 能自动跑一组 pass/fail matrix。
- Probe 能发送 claimed identity mismatch，用来验证 host spoof 防护。
- Probe 能验证 unauthorized、pending、granted、revoked 的状态转换。
- Probe 能验证 downgraded minor 和 incompatible major version。
- Probe 能验证 task polling 和 artifact loading。
- Probe report 能直接用于讨论 host 行为问题。

## 6. 后续暂缓

这些能力先不要压进 `027-029`：

- expanded read capability protocol
- host control center object details 全量重构
- workflow capability runner
- resource and knowledge exchange
- side-effect capability protocol

原因是它们都会扩大公共协议、安全、治理和 UI 复杂度。`027-029` 的价值是先把公共入口、可信 host、conformance 闭环变稳。
