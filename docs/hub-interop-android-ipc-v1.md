# Hub Interop Android IPC Design v1

## 1. 文档目的

这份文档用于冻结 Mobile Claw 跨 app 协议在 Android `v1` 上的第一版 IPC 绑定方式。

它不重新定义协议语义；协议本体仍以上游文档 [Hub Interop Protocol Design v1](./hub-interop-protocol-design-v1.md) 为准。

这里解决的是另一层问题：

- Android 第一版到底用哪种 IPC 作为主通道
- 哪些 Android 机制是主协议，哪些只是兼容入口或 UI 辅助
- 授权、任务、资源交换分别落在哪种 Android transport 上
- 如何让 Android 绑定不把跨平台协议本体锁死在 Binder 语义里

## 2. 设计前提

- 协议语义必须保持跨平台，不把 Android transport 直接写进协议对象
- 当前工程 `minSdk = 31`、`targetSdk = 36`，且已经具备 share ingress、AppFunctions、capability registry 等基础
- Android `v1` 既要支持用户手势驱动入口，也要支持显式 app-to-app 调用
- 授权应由 Mobile Claw 自己的治理模型主导，而不是完全依赖 Android 安装时权限
- 资源与 artifact 交换需要最小授权、可撤销、可审计

## 3. 决策摘要

Android `v1` 不采用单一 IPC，而采用一套分层混合绑定。

冻结结论如下：

- `AppFunctions`
  - 作为显式 callable capability 的首选 Android-native adapter
- `HubInteropProvider`（自定义导出的 `ContentProvider`）
  - 作为 Android `v1` 的**必选基线 transport**
  - 负责 descriptor discovery、授权请求、grant 查询、task 记录、resource / artifact handle 暴露，以及 AppFunctions 不可用时的能力调用回退
- `content://` URI grants
  - 作为资源与 artifact 交换主路径
- `FileProvider`
  - 作为文件型 artifact 的实现辅助，不作为协议主控制面
- 显式 `Activity` / `PendingIntent`
  - 作为授权确认页、连接管理页、需要用户介入时的 UI 路由
- `ACTION_SEND` / `ACTION_SEND_MULTIPLE`
  - 只作为 compatibility ingress
- `AIDL` / `Messenger` / `BroadcastReceiver`
  - 不作为 Android `v1` 的公开主协议

### 3.1 当前 `025` host 实现对齐状态

当前工程中的首批落地实现已经对齐到这套绑定：

- `HubInteropProvider.call()` 已承载 `discover_surface`、`invoke_capability`、`request_authorization`、`get_grant_status`、`revoke_grant`、`get_task`、`get_artifact`
- `generate.reply` 作为第一条 governed callable capability 通过共享 contract 暴露
- 授权结果当前先写回现有 governance grant 模型，并通过治理中心管理
- 长任务当前先以 in-memory task handle + polling 语义提供给外部 caller，应用内则通过 control center 摘要显示最近 interop host task

## 4. 为什么不是单一 IPC

### 4.1 为什么不是只用 AppFunctions

`AppFunctions` 很适合 capability-style interaction，因为它天然对齐“可发现、可调用、可编排的工具”语义。

但它不适合单独承担整个 `v1` interop family，原因至少有三点：

- 平台可用性受限，当前官方文档仍标注为 experimental preview
- 调用方需要 `EXECUTE_APP_FUNCTIONS` 权限，生态接入面受平台和授权条件约束
- 它更适合 callable capability，不适合单独承载 task record、grant record、artifact handle 这类持久对象

因此，`AppFunctions` 应该是**首选 adapter**，而不是**唯一 transport**。

### 4.2 为什么不是只用 Intent

显式 `Intent` 与 share contract 很适合：

- 用户手势驱动的 handoff
- 打开授权确认页
- 打开某个连接详情或任务详情 UI

但它们不适合做完整主协议，因为它们缺少：

- 稳定的 discovery 面
- 强 schema 的 request / response
- 持久 task handle
- 资源与 artifact 的长期引用语义

所以 `Intent` 只能承担入口与 UI 路由，不应承担协议主数据面。

### 4.3 为什么不是只用 AIDL 或 Messenger

Android 官方文档把 `Messenger` 定位为较简单的 IPC 方式，把 `AIDL` 定位为在跨进程且需要并发处理时再使用的更底层方案。

它们的问题不是“不能用”，而是“不适合作为对外公开协议主形态”：

- 过于 Binder / Android-specific
- 需要客户端理解 Android 服务绑定生命周期
- `AIDL` 通常要求共享接口定义或 SDK，增加接入门槛
- 不天然表达 descriptor、grant、task、artifact 这些协议对象

因此，Android `v1` 不公开导出 Binder 服务作为主 interop contract。以后如果需要高性能 companion-channel，可以在同一协议语义下增加私有 adapter，但不把它定义成公共主路径。

### 4.4 为什么选择 ContentProvider 作为基线 transport

自定义 `ContentProvider` 更适合作为 Android `v1` 的稳定基线，原因是：

- 它不要求调用方预先集成 Binder stub
- 它天然支持 `content://` 句柄与 URI grants
- 它可以同时承载：
  - `query()` 风格的只读发现
  - `call()` 风格的命令调用
  - `openFile()` / `openTypedAssetFile()` 风格的 artifact 访问
- 它比 share 更结构化，比 AIDL 更容易保持跨平台抽象

因此，Android `v1` 的 transport 设计应以：

**`HubInteropProvider` + `content://` handles + `AppFunctions` adapter**

为中心，而不是以单一的 Activity、share 或 Binder service 为中心。

## 5. Android v1 IPC 栈

| 协议关注点 | Android v1 主绑定 | 说明 |
|---|---|---|
| `surface discovery` | `HubInteropProvider.query()` / `call()` | 返回最小公开 descriptor，不默认暴露敏感 capability 细节 |
| `callable capability invoke` | `AppFunctions` 优先，`HubInteropProvider.call()` 回退 | 两条路径最终汇入同一 capability plane |
| `authorization request / status / revoke` | `HubInteropProvider.call()` + 显式授权 Activity | 授权结果写回统一 grant 模型 |
| `resource / context exchange` | `content://` URI + URI grants | 默认最小权限，按资源单独授权 |
| `artifact exchange` | `content://` URI + `FileProvider` 辅助 | 文件型 artifact 可以走 `FileProvider`，协议句柄仍以 interop URI 表达 |
| `task create / status / resume / cancel` | `HubInteropProvider.call()` + `content://.../tasks/{id}` | `v1` 先采用 handle + polling |
| `user gesture ingress` | `ACTION_SEND` / `ACTION_SEND_MULTIPLE` / 显式 Activity | 只作为兼容入口或 UI 路由 |

## 6. HubInteropProvider 的职责

建议新增一个导出的 `HubInteropProvider`，例如：

```text
content://com.mobileclaw.app.hubinterop
```

它不需要一次性暴露所有数据表，但建议至少稳定这些对象域：

```text
/surface
/capabilities
/grants
/tasks
/artifacts
/connected-apps
```

建议职责划分如下：

- `query()`
  - 读取公开 descriptor
  - 读取 task 状态
  - 读取 connected app 可见摘要
- `call()`
  - 发起 capability invoke
  - 发起授权请求
  - 查询 grant 状态
  - 撤销 grant
  - 创建、恢复、取消 task
- `openFile()` / `openTypedAssetFile()`
  - 打开 artifact 或资源句柄

## 7. Android v1 的方法级约定

为了让 Android 绑定保持稳定，建议先冻结一组 method family，而不是过早冻结所有字段细节。

建议最小方法集：

```text
discover_surface
invoke_capability
request_authorization
get_grant_status
revoke_grant
get_task
get_artifact
```

建议每个请求至少携带：

```text
request_id
caller_identity
contract_version
capability_id
input
subject
requested_scopes
handle
```

建议每个响应至少返回：

```text
status
message
compatibility_signal
surface_descriptor
grant_descriptor
task_descriptor
artifact_descriptor
```

其中 `status` 建议至少支持：

- `ok`
- `bad_request`
- `unauthorized`
- `authorization_required`
- `authorization_pending`
- `forbidden`
- `not_found`
- `expired`
- `incompatible_version`
- `unsupported_capability`
- `provider_unavailable`
- `permission_unavailable`
- `policy_denied`
- `approval_required`
- `approval_rejected`
- `execution_failed`
- `internal_error`

## 8. 授权流绑定

Android `v1` 授权建议固定为下面这条链路：

1. 调用方先读取最小公开 descriptor
2. 调用 capability 或主动请求 grant
3. 如果当前未授权，`HubInteropProvider` 返回：
   - `status = authorization_required`
   - `grant_descriptor`
   - 可选 `PendingIntent` 或显式 Activity route
4. 用户进入 Mobile Claw 的授权确认页
5. Mobile Claw 写入 `InteropGrant`
6. 调用方重试调用，或继续读取 grant 状态

这条链路意味着：

- AppFunctions 路径和 Provider 路径共享同一套授权语义
- 授权 UI 通过显式 Activity 打开
- grant 存储与审计不依赖 Android 安装期 permission

## 9. 任务流绑定

长任务不应继续压在单次 share 或单次 AppFunction 返回里。

Android `v1` 先固定成：

- capability 调用可以返回 inline result
- 也可以返回 `taskUri`
- 调用方通过 `content://.../tasks/{id}` 轮询状态
- 需要更多输入时返回 `input_required`
- 产物通过 artifact URI 读取

`v1` 暂不把 push callback、broadcast callback、binder callback 定为必选能力。

后续如果需要更强实时性，可以在不改变协议语义的前提下增加：

- `PendingIntent` callback
- provider-backed observer
- 私有 Binder adapter

## 10. 资源与 artifact 绑定

资源与 artifact 建议分开看待：

- `resource`
  - 表示可读上下文、知识片段、可检索对象
- `artifact`
  - 表示某次调用或任务产出的结果

Android `v1` 的统一要求：

- 对外暴露时都使用 `content://` 句柄
- 默认只授予最小读权限
- 共享文件时可由 `FileProvider` 承担实际文件暴露
- 但上层协议仍然记录为 interop resource / artifact handle，而不是裸文件路径

## 11. Android v1 公共定义

为了避免外部 app 直接依赖内部实现，Android `v1` 先冻结下面这些公共术语。

### 11.1 Public Surface

`Public Surface` 指一个可以被其他 app 发现和交互的受管入口。

在 Android `v1` 中，它至少包含：

- `surfaceId`
- `displayName`
- `providerPackage`
- `protocolVersion`
- `interactionModes`
- `callableCapabilities`
- `taskSupport`
- `resourceSupport`
- `authPolicySummary`

### 11.2 Capability Invocation

`Capability Invocation` 指一次短时、显式、结构化的调用请求。

它的目标是：

- 执行一个明确 capability
- 返回 inline result，或
- 返回 task handle 进入长任务流

它不等价于：

- share handoff
- 打开 UI
- 普通 deep link

### 11.3 Authorization Request

`Authorization Request` 指调用方请求建立或刷新 grant 的协议动作。

它至少要回答：

- 谁在请求
- 请求哪种 direction
- 请求哪些 scopes
- grant 希望持续多久
- 是否还需要 approval

### 11.4 Task Handle

`Task Handle` 是长任务在 Android 绑定层的稳定地址，通常表现为：

```text
content://<authority>/tasks/<taskId>
```

它用于：

- 轮询状态
- 恢复任务
- 取消任务
- 读取结果摘要

### 11.5 Artifact Handle

`Artifact Handle` 是某次调用或某个任务产物的稳定地址，通常表现为：

```text
content://<authority>/artifacts/<artifactId>
```

它用于：

- 读取文本结果
- 打开文件型结果
- 读取结构化结果摘要

### 11.6 Auth Request Handle

`Auth Request Handle` 是一次待确认授权请求的稳定地址，通常表现为：

```text
content://<authority>/grants/requests/<requestId>
```

它用于：

- 打开授权确认页
- 查询授权进度
- 在授权完成后重试调用

### 11.7 Preferred Adapter 与 Baseline Transport

Android `v1` 需要区分两种角色：

- `Preferred Adapter`
  - 指最适合某类交互的 Android-native 暴露方式
  - 在当前方案里主要是 `AppFunctions`
- `Baseline Transport`
  - 指无论首选 adapter 是否可用，都需要存在的基础协议通道
  - 在当前方案里是 `HubInteropProvider`

这两个角色不能混为一谈。

## 12. Android v1 公共调用约定

### 12.1 Provider Authority

建议固定一个稳定 authority：

```text
com.mobileclaw.app.hubinterop
```

实际 authority 是否需要带品牌前缀，可以在实现阶段微调，但 `v1` 必须保持唯一稳定，不能让外部调用方依赖临时字符串。

### 12.2 Public URI Family

建议 Android `v1` 至少公开下面这些 URI family：

```text
content://<authority>/surface
content://<authority>/capabilities
content://<authority>/grants
content://<authority>/tasks
content://<authority>/artifacts
content://<authority>/connected-apps
```

细分句柄建议至少支持：

```text
content://<authority>/tasks/<taskId>
content://<authority>/artifacts/<artifactId>
content://<authority>/grants/requests/<requestId>
content://<authority>/connected-apps/<connectedAppId>
```

### 12.3 Public Method Family

`ContentResolver.call()` 的 method name 建议冻结为：

```text
discover_surface
invoke_capability
request_authorization
get_grant_status
revoke_grant
get_task
get_artifact
```

### 12.4 Request Bundle Keys

Android `v1` 建议先冻结一组公共 request keys：

```text
request_id
caller_identity
contract_version
capability_id
input
subject
requested_scopes
handle
```

### 12.5 Response Bundle Keys

Android `v1` 建议先冻结一组公共 response keys：

```text
status
message
compatibility_signal
surface_descriptor
grant_descriptor
task_descriptor
artifact_descriptor
```

### 12.6 Status Enum

公共状态建议固定为：

- `ok`
- `bad_request`
- `unauthorized`
- `authorization_required`
- `authorization_pending`
- `forbidden`
- `not_found`
- `expired`
- `incompatible_version`
- `unsupported_capability`
- `provider_unavailable`
- `permission_unavailable`
- `policy_denied`
- `approval_required`
- `approval_rejected`
- `execution_failed`
- `internal_error`

其中：

- `ok`
  - 请求成功；如果返回 `task_descriptor`，调用方继续轮询 task
- `authorization_required`
  - 需要先完成授权
- `authorization_pending`
  - 授权请求已建立，但用户或 host 尚未放行
- `forbidden`
  - 调用方已知，但不能访问该 capability、task 或 artifact
- `not_found`
  - handle 不存在
- `expired`
  - handle 曾存在，但生命周期已过期
- `incompatible_version`
  - caller 与 host 的协议版本不能安全互通
- `approval_required`
  - 已有 grant，但当前请求仍需审批
- `execution_failed`
  - host provider 执行失败
- `input_required`
  - 当前任务缺少继续执行所需输入
- `rejected`
  - 请求被拒绝，且通常不会继续重试
- `error`
  - 发生异常或兼容问题

## 13. Android 调用方式与示例

### 13.1 发现 Mobile Claw Surface

调用方应先读取最小公开 surface，而不是直接猜测 capability。

在实现层面，调用方应优先通过独立公共 Android contract 模块提供的 helper 访问这些接口，而不是手写 authority、URI 和 key。

推荐路径：

1. 使用 `ContentResolver.call()` 调用 `discover_surface`
2. 读取 `contract_version`、`supportedMethods`、`capabilities`
3. 如果需要更细粒度 capability 信息，读取 `surface_descriptor.capabilities`

示例：

```kotlin
val response = context.contentResolver.call(
    Uri.parse("content://com.mobileclaw.app.hubinterop/surface"),
    "discover_surface",
    null,
    bundleOf(
        "contract_version" to "1.0",
    ),
)
```

### 13.2 调用显式 Capability

调用 capability 时，调用方应优先使用官方 Android contract helpers，而不是手写 method name 和 key。

推荐路径：

1. 读取 capability descriptor
2. 组织 `input`
3. 调用 `invoke_capability`
4. 根据 `status` 分支处理：
   - `ok`
   - `authorization_required`
   - `authorization_pending`
   - `approval_required`
   - `unsupported_capability`

示例：

```kotlin
val response = context.contentResolver.call(
    Uri.parse("content://com.mobileclaw.app.hubinterop/surface"),
    "invoke_capability",
    null,
    bundleOf(
        "request_id" to "sample-request-1",
        "contract_version" to "1.0",
        "capability_id" to "generate.reply",
        "input" to "Summarize this thread",
        "requested_scopes" to arrayListOf("reply.generate"),
    ),
)
```

### 13.3 处理 `authorization_required`

如果响应为 `authorization_required`，调用方不应把它当成执行失败。

推荐处理方式：

1. 读取 `grant_descriptor`
2. 打开 Mobile Claw 的授权确认页，或调用 `request_authorization`
3. 等待用户完成授权
4. 重新调用原请求，或轮询 `get_grant_status`

### 13.4 处理长任务

如果响应为 `ok` 且包含 `task_descriptor`，调用方应转入 task 模式。

推荐处理方式：

1. 读取 `task_descriptor.handle`
2. 通过 `get_task` 读取任务状态
3. 如果任务状态为 `INPUT_REQUIRED`，等待 host 侧用户输入或后续协议扩展
4. 如果状态为 `COMPLETED`，读取 `artifactHandles`

### 13.5 读取 Artifact

文件型和非文件型结果都不应直接暴露内部文件路径。

推荐处理方式：

1. 读取 `artifactHandles`
2. 对文本或结构化摘要，优先走 `query()` / `call()`
3. 对文件型结果，走 `openInputStream()`、`openFileDescriptor()` 或 typed asset access

示例：

```kotlin
val input = context.contentResolver.openInputStream(
    Uri.parse("content://com.mobileclaw.app.hubinterop/artifacts/artifact-123")
)
```

### 13.6 AppFunctions 的使用位置

如果调用方设备、平台和权限条件允许，`AppFunctions` 仍然应该作为显式 capability invoke 的首选 adapter。

但调用方仍应保留对 baseline transport 的理解，因为下面这些动作不应只依赖 AppFunctions：

- 读取 surface descriptor
- 授权请求与 grant 状态
- task handle 轮询
- artifact handle 访问
- connected app 管理

独立 probe app 也应遵循同一原则：

- 可以测试 `AppFunctions` path
- 但不能把整个互通验证建立在只支持 `AppFunctions` 的假设上
- 必须同时验证 baseline transport 是否可独立完成互通

## 14. 安全与治理约束

Android `v1` 至少应固定这些约束：

- 不使用隐式 `bindService()` 作为公开主入口
- 不导出公共 AIDL service 作为 `v1` 主协议
- 公开 descriptor 默认只暴露最小元数据
- 敏感方法必须同时校验：
  - caller package
  - signing identity
  - inbound / outbound grant
  - approval / policy
- 资源 URI 默认最小权限、最小时长
- 所有 grant / revoke / deny / invoke / task outcome 都进入 audit

## 15. 对当前代码的影响

这份决策与现有工程是连续的，不是推倒重来。

可以直接复用的基础：

- `CapabilityRegistry`
  - 继续作为 capability plane 路由核心
- `MobileClawAppFunctions`
  - 继续作为 AppFunctions adapter 的起点
- `ExternalHandoffParser`
  - 继续作为 share compatibility ingress
- 现有 governance / approval / audit / memory / knowledge 基础
  - 继续承担治理与上下文底座

后续 Android `v1` 应新增的重点：

- `HubInteropProvider`
- connected app / grant / task 的 Android-facing adapter
- 授权确认 Activity 与连接详情入口
- resource / artifact URI handle 管理

## 16. 明确不在 Android v1 首批冻结范围内的内容

- 公共 AIDL SDK
- Messenger-based 公共协议
- push-first callback 机制
- 远程网络传输层
- 多设备分布式协作 transport

## 17. 参考方向

- [Overview of AppFunctions](https://developer.android.com/ai/appfunctions)
- [AppFunctionManager reference](https://developer.android.com/reference/android/app/appfunctions/AppFunctionManager)
- [Bound services overview](https://developer.android.com/develop/background-work/services/bound-services)
- [Android Interface Definition Language (AIDL)](https://developer.android.com/guide/developing/tools/aidl.html)
- [Content provider basics](https://developer.android.com/guide/topics/providers/content-provider-basics)
- [Send simple data to other apps](https://developer.android.com/training/sharing/send)
