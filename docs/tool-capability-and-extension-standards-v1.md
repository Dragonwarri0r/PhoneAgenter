# Tool, Capability, and Extension Standards v1

## 1. 文档目的

这份文档用于把 `v1` 中与工具调用、系统能力、多模态输入、外部互操作、扩展钩子相关的设计冻结下来。

目标不是一次性定义所有实现细节，而是先统一下面几件事：

- 我们选用哪些现有标准作为基础
- Mobile Claw 内部 tool contract 应该长什么样
- 日历、闹钟、分享、图片、音频这类常见能力应该如何接入
- 为什么这些能力必须遵守“按需调用”
- 扩展钩子怎样从 `006` 的 portability hooks 发展成统一扩展系统

## 2. 标准选型结论

基于 Android 官方能力边界、当前项目路线和现有 `gallery` 参考工程，我建议采用一套混合标准栈。

这个结论是基于官方文档做出的产品/架构推断，不是某一篇文档直接给出的单一答案。

### 2.1 内部 Tool Descriptor

内部 tool contract 采用 `MCP-style tool descriptor + JSON Schema` 作为基础语义：

- `name`
- `description`
- `inputSchema`
- `structured output`
- tool discovery 与按需调用语义

原因：

- 这是当前最清晰、跨模型生态最容易对齐的一种工具描述方式
- `inputSchema` 很适合承载日历、闹钟、分享、媒体附件等结构化参数
- 未来如果需要接外部 agent 或远端 orchestrator，这一层最容易迁移

### 2.2 Android 原生能力绑定

Android 侧能力暴露与调用采用：

- `App Functions` 作为 Android-native callable capability 的首选标准
- `ContentProvider / Calendar Provider / Photo Picker / Intent / Sharesheet / FileProvider` 作为实际系统绑定层
- `Accessibility` 继续保留为最后级别 fallback，不作为默认主路径

原因：

- `App Functions` 适合“让 app 向 assistant app 暴露可调用功能”这一方向
- 但日历、闹钟、图片选择、文件读取等设备级动作，仍需要落到 Android 现有 provider / intent / picker 机制上

### 2.3 外部 App 调用与内容互通

其他 app 与 Mobile Claw 的互操作优先采用：

- `AppFunctions` 作为显式 callable capability 的首选 Android-native adapter
- 自定义 `HubInteropProvider` + `content://` URI grants 作为 discovery / auth / task / resource 的基线 transport
- `ACTION_SEND` / `ACTION_SEND_MULTIPLE` 作为 compatibility ingress
- `ACTION_OPEN_DOCUMENT`
- Photo Picker 返回的受控媒体访问句柄

更完整的 Android IPC 分层与边界，以上游文档 [Hub Interop Android IPC Design v1](./hub-interop-android-ipc-v1.md) 为准。

对外 contract 的模块边界与分发方式，以上游文档 [Hub Interop Module Packaging v1](./hub-interop-module-packaging-v1.md) 为准。

### 2.4 扩展注册

扩展系统建立在 `006-sync-extension-hooks` 的 `ExtensionRegistration` 之上，但要扩到更完整的 runtime surface。

也就是说，`006` 不是废弃，而是升级基础：

- 从 portability-oriented hooks
- 变成 runtime-wide extension surface

## 3. 系统级原则

### 3.1 按需调用，而不是常驻暴露

常见能力必须 `on-demand invocation`：

- 不应该把所有工具永远堆在 UI 上
- 不应该把所有工具永远暴露给 planner
- 只在当前模型能力、用户意图、caller scope、policy 和上下文允许时暴露

这样做的原因：

- 降低 UI 膨胀
- 降低 planner 误调用概率
- 降低高风险动作的噪音
- 让 capability growth 不会把工作区做成控制台

### 3.2 会改动系统状态的工具必须显式化

只读和写入型工具必须分开建模：

- `read`: 查询、检索、读取上下文
- `write`: 改动设备状态或 app 状态
- `dispatch`: 触发外部分享、消息发送、闹钟、跳转

所有 `write / dispatch` 工具都需要：

- risk annotation
- preview capability
- scope requirement
- audit event

显式 `read` tool 也需要自己的合同，但它的要求不同：

- 必须有 bounded query scope
- 必须能返回 `matched / no_results / unavailable`
- 可以自动执行，但前提是它是低风险读取且 selection confidence 足够高
- 不能伪装成被动上下文附着

### 3.3 多模态入口必须受模型能力 gating

图片和音频入口不能只因为 UI 能做就一直显示。

它们必须同时满足：

- 当前选中的模型支持
- 当前任务流允许
- 当前 policy 允许
- 当前设备权限与 picker 能力可用

### 3.4 扩展优先于特例

新增能力时优先问：

- 能不能作为 tool descriptor 加一条 binding
- 能不能作为新的 provider adapter 接进现有 contract
- 能不能作为新的 ingress / context source / export extension 注册

而不是先写一段新的 if/else 流程。

## 4. 推荐的统一合同

我们在 `v1` 应至少固定三层合同。

### 4.1 Tool Descriptor

这是模型和 planner 看到的能力描述。

```kotlin
data class ToolDescriptor(
    val toolId: String,
    val displayName: String,
    val description: String,
    val inputSchemaJson: String,
    val outputSchemaJson: String?,
    val capabilityKind: CapabilityKind,
    val riskLevel: String,
    val requiredScopes: List<String>,
    val supportsPreview: Boolean,
    val requiresConfirmation: Boolean,
    val invocationMode: InvocationMode,
    val visibilityPolicy: VisibilityPolicy,
    val androidBindings: List<String>,
)
```

最关键的字段是：

- `toolId`: 稳定能力标识，例如 `calendar.write`
- `inputSchemaJson`: 参数合同
- `riskLevel`: 风险等级
- `requiredScopes`: scope / permission 约束
- `invocationMode`: 是否按需暴露
- `androidBindings`: 映射到哪些 Android 执行绑定

### 4.2 Capability Binding

这是 Android 侧实际执行绑定。

```kotlin
data class CapabilityBinding(
    val bindingId: String,
    val toolId: String,
    val bindingType: BindingType,
    val androidContract: String,
    val permissions: List<String>,
    val callerTrustRequired: Boolean,
    val supportedMimeTypes: List<String>,
    val primary: Boolean,
)
```

`bindingType` 建议至少覆盖：

- `APP_FUNCTION`
- `CONTENT_PROVIDER`
- `INTENT`
- `SHARE_TARGET`
- `PICKER`
- `FILE_PROVIDER`
- `SYNC_TRANSPORT`

### 4.3 Runtime Extension Registration

这是统一扩展面。

它建立在 `006` 的 `ExtensionRegistration` 上，但扩展到整个 runtime：

```kotlin
data class RuntimeExtensionRegistration(
    val extensionId: String,
    val extensionType: RuntimeExtensionType,
    val displayName: String,
    val contributedTools: List<String>,
    val requiredRecordFields: List<String>,
    val privacyGuarantee: String,
    val enabledByDefault: Boolean,
)
```

`RuntimeExtensionType` 建议至少包括：

- `INGRESS`
- `TOOL_PROVIDER`
- `CONTEXT_SOURCE`
- `EXPORT`
- `IMPORT`
- `SYNC_TRANSPORT`

## 5. 推荐调用生命周期

统一调用链建议固定为：

1. discover tool
2. preflight policy and scope check
3. fill structured arguments
4. validate against schema
5. preview side effects
6. request confirmation if needed
7. execute through Android binding
8. audit outcome and write back context

这条链路的价值在于：

- 多模态输入、日历、闹钟、分享都能走同一个框架
- 外部 caller 进来时，也能复用相同 policy / audit / preview 体系
- 新扩展只要接在合同边界上，就不会重写执行主链路

## 6. 常见能力的推荐标准绑定

| Capability Family | Primary Binding | Recommended Fallback | Notes |
|---|---|---|---|
| `calendar.read` | `Calendar Provider` | none in first cut | Requires scoped queries and `READ_CALENDAR` when used directly |
| `contacts.read` | `Contacts Provider` | unavailable until a local read provider is wired | Reuses the same explicit read-provider registration surface |
| `calendar.write` | structured tool + provider/app binding | `Intent.ACTION_INSERT` to calendar app | Default to preview/confirm before commit |
| `alarm.set` | structured tool + Android alarm binding | `AlarmClock.ACTION_SET_ALARM` | Keep user-visible and auditable |
| `alarm.dismiss` | structured tool + Android alarm binding | `AlarmClock.ACTION_DISMISS_ALARM` | Only when device/app surface supports it |
| `alarm.show` | structured tool + Android alarm binding | `AlarmClock.ACTION_SHOW_ALARMS` | Good low-risk navigation action |
| `message.send` | structured tool + provider/app binding | `ACTION_SENDTO` / SMS app dispatch | High-risk and always previewable |
| `share.outbound` | Sharesheet binding | `ACTION_SEND` / `ACTION_SEND_MULTIPLE` | Use chooser and auditable export text |
| `ingress.share_text` | share target contract | explicit app handoff later | This is the current `007` path |
| `ingress.share_media` | share target + `content://` URI contract | `ACTION_OPEN_DOCUMENT` import flow | Needed for multimodal handoff |
| `media.pick.image` | Photo Picker | document picker only if necessary | Prefer picker because it is bounded and user-mediated |
| `media.pick.audio` | `ACTION_OPEN_DOCUMENT` with `audio/*` | app-local recorder flow | Persist URI permission only if background reuse is required |
| `media.record.audio` | explicit recorder flow | none | Must remain user-gesture initiated with `RECORD_AUDIO` |

说明：

- `calendar.read` 和 `calendar.write` 不应混成一个 tool
- 显式 `read tool` 与被动 `context source` 不应混成同一条执行路径
- `alarm.*` 需要独立 family，而不是挤进 `calendar.write`
- `share.outbound` 与 `ingress.share_*` 是两个方向，不应混淆
- workspace freeform inference 可以自动选择低风险 `read`，但不应静默自动执行 `write / dispatch`

## 7. 多模态支持规范

### 7.1 模型能力声明

模型导入和模型目录应该明确保留：

- `supportImage`
- `supportAudio`

这与 `gallery` 参考工程的 `llmSupportImage / llmSupportAudio` 路线一致。

### 7.2 工作区入口

工作区应采用：

- 文本输入为主
- 附件轨道为辅
- 图片/音频附件预览为轻量 rail 或 chip
- 不把附件控制区长期展开成新的主布局层

换句话说：

- 能力必须直观可见
- 但默认不应占据过多纵向空间

### 7.3 请求归一化

多模态 request 不应只是把图片或音频塞成一个附属字段。

建议统一成：

- `text parts`
- `image parts`
- `audio parts`
- `source metadata`
- `uri grant metadata`

这样外部 share media 与工作区内附件就能收敛到同一 canonical request shape。

## 8. 外部互操作规范

### 8.1 Inbound

Mobile Claw 在 `v1` 应优先支持：

- `ACTION_SEND` + `text/plain`
- `ACTION_SEND_MULTIPLE` + 媒体 URI

所有 inbound contract 都应携带：

- `entryType`
- `originApp`
- `packageName`
- `trustState`
- `trustReason`
- `uriGrantSummary`

### 8.2 Outbound / Callable Surface

当我们向其他 app 或 assistant 暴露能力时，应该优先对齐：

- `App Functions`
- 结构化 tool schema
- caller trust 和 scope gating

也就是说，对外暴露的能力不应该只是“某个隐式 intent 碰巧能拉起”。

### 8.3 URI 与文件策略

跨 app 内容传递优先采用：

- `content://`
- 临时 URI grant
- `FileProvider` 暴露导出文件

不建议在合同里传播 app 内部文件路径。

## 9. 统一扩展面规范

`006` 已经证明了 portability 和 provider hooks 可以通过合同表达。

`v1` 需要再往前走一步，把下列扩展点统一化：

- `IngressAdapter`
- `ToolProviderAdapter`
- `ContextSourceConnector`
- `ExportAdapter`
- `ImportAdapter`
- `SyncTransportAdapter`

每个扩展至少都要声明：

- 它贡献哪些 tool 或能力
- 它依赖哪些 record fields / metadata
- 它的隐私保证是什么
- 它默认启用还是按需启用
- 它是否要求 caller trust

这样做的好处是：

- 新能力不再总是落到 runtime core
- 不同扩展的审计、治理、兼容性检查可以统一
- 工具规范和扩展钩子不会分裂成两套体系

## 10. 与路线图的映射

这份规范文档直接支撑：

- `013-workspace-information-architecture`
- `014-multimodal-ingress-and-composer`
- `015-tool-contract-standardization`
- `016-external-caller-interop-contracts`
- `017-unified-extension-surface`

同时也会反向约束：

- `008-structured-action-payloads`
- `010-system-source-ingestion`
- `012-real-appfunctions-integration`

## 11. 参考来源

官方标准与文档：

- [Android App Functions overview](https://developer.android.com/ai/appfunctions)
- [AppFunctionManager reference](https://developer.android.com/reference/android/app/appfunctions/AppFunctionManager)
- [Android common intents, including calendar and alarm actions](https://developer.android.com/guide/components/intents-common)
- [Calendar Provider overview](https://developer.android.com/identity/providers/calendar-provider)
- [Send simple data to other apps](https://developer.android.com/training/sharing/send)
- [Photo Picker](https://developer.android.com/training/data-storage/shared/photopicker)
- [Open documents and files](https://developer.android.com/training/data-storage/shared/documents-files)
- [FileProvider reference](https://developer.android.com/reference/androidx/core/content/FileProvider)
- [Model Context Protocol: Tools](https://modelcontextprotocol.io/specification/draft/server/tools)

本地参考实现：

- `/Users/youxuezhe/StudioProjects/gallery/Android/src/app/src/main/java/com/google/ai/edge/gallery/data/Model.kt`
- `/Users/youxuezhe/StudioProjects/gallery/Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/common/chat/ChatPanel.kt`
- `/Users/youxuezhe/StudioProjects/gallery/Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/common/chat/MessageInputText.kt`
- `/Users/youxuezhe/StudioProjects/gallery/Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/llmchat/LlmChatScreen.kt`
- `/Users/youxuezhe/StudioProjects/mobile_claw/specs/006-sync-extension-hooks/contracts/sync-extension-contract.md`
