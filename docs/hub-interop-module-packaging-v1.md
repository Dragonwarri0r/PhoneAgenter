# Hub Interop Module Packaging v1

## 1. 文档目的

这份文档用于冻结 Hub Interop Protocol 在工程结构和对外分发上的模块边界。

它回答的问题是：

- 这套协议是否需要独立模块
- Android 集成方是否应该通过 AAR 使用它
- 协议本体和 Android 绑定是否应该拆开
- 哪些代码属于公共 contract，哪些代码绝不能进公共分发件

## 2. 决策摘要

结论是：

- **协议本体必须进行独立模块隔离**
- **外部 app 必须能够直接引用该公共协议模块与实现者交互**
- **Mobile Claw 只是协议实现者之一，不是协议本体本身**
- **首批 Android 落地至少需要“核心协议模块 + Android 绑定模块 + Host 实现模块 + 独立 probe app”四层**

如果只看 Android 调用方的接入体验，那么最终它消费的确实应该是一个 `AAR`。

但如果从协议长期演进看：

- 协议语义本体不应被 Android SDK 类型锁死
- Android AAR 只应承载 Android binding contract
- Host app 的 runtime / governance / UI / model internals 不应被打进公共分发件
- 独立 probe app 应和其他外部 app 一样，通过公共协议模块接入，而不是走 host 内部依赖

## 3. 推荐模块拆分

### 3.1 `:hub-interop-contract-core`

定位：

- 协议语义核心
- 尽量保持纯 Kotlin/JVM，未来可演进为 KMP-ready
- **这是必选模块，不是可选优化**
- 当前仓库中已经按此名称落地

建议包含：

- protocol version 常量
- 公共 entity 定义
- status / mode / scope / lifetime enum
- 逻辑层 descriptor 模型
- task / artifact / grant / compatibility 模型
- 公共 JSON shape 或 schema helper

不应包含：

- Android `Context`
- `Uri`
- `Bundle`
- `ContentResolver`
- `Activity`
- `PendingIntent`
- Room / Hilt / Compose / runtime internals

### 3.2 `:hub-interop-android-contract`

定位：

- Android `v1` 绑定层
- 对外给 Android 集成方消费的主 public SDK
- 同时给独立 probe app 与 Mobile Claw host 实现共同引用
- 当前仓库中已经按此名称落地

分发形态：

- Android library
- 对外产物通常是一个 `AAR`

建议包含：

- provider authority 常量
- public URI builder / parser
- method name 常量
- request / response Bundle key 常量
- Android-side client helper
- Android-side contract DTO adapter
- 授权确认页 intent builder
- 兼容性检查 helper

不应包含：

- Mobile Claw 内部 capability registry 逻辑
- governance 决策实现
- 本地模型调用实现
- UI 页面实现本体
- host-only repository / database / service 代码

### 3.3 `:app`

定位：

- Mobile Claw host implementation

建议包含：

- `HubInteropProvider` 的真实实现
- `MobileClawAppFunctions` 的真实实现
- governance / approval / audit / memory / knowledge 整合
- 授权页面、连接管理页面、control center 页面
- task / artifact / connected app 的持久化与调度

这个模块是协议实现者之一，不应再承载协议本体定义。

### 3.4 `:interop-probe-app`

定位：

- 独立协议验证 app
- 首批 Android 外部消费者

建议包含：

- 基于公共协议模块的 discovery UI
- capability invoke 测试入口
- 授权请求与 grant 状态验证入口
- task / artifact 读取验证入口
- compatibility / version signal 展示

强约束：

- 只能依赖公共协议模块与 Android binding 模块
- 不得依赖 `:app` 内部 runtime、repository、UI、policy 实现
- 必须把 Mobile Claw 当成外部实现者来调用

## 4. 为什么不建议只做一个 AAR

如果把协议本体、Android 绑定、host 实现全部塞进一个 AAR，会有几个问题：

- 外部 app 会被迫依赖 host 内部实现细节
- 跨平台语义会被 Android 类型侵入
- 后续做桌面或其他平台时，协议对象无法复用
- 版本升级时，公共 contract 和 host 行为会被一起绑定，演进成本很高

因此，更稳的方式不是“不要 AAR”，而是：

**让 Android 绑定以 AAR 分发，但让协议本体与 host 实现保持分层。**

## 5. 推荐对外发布方式

对 Android 调用方，推荐只暴露下面这一层：

- `hub-interop-android-contract`

它可以传递依赖：

- `hub-interop-contract-core`

调用方的体验应当是：

1. 引入 Android contract AAR
2. 使用官方 helper 构造 URI、method、Bundle keys
3. 通过 `ContentResolver` / `AppFunctions` adapter 与 Mobile Claw 交互
4. 不需要复制魔法字符串
5. 不需要依赖 Mobile Claw app 内部 runtime 类

独立 probe app 也应遵循同一条接入路径。

## 6. 公共模块应暴露什么

Android public contract 至少应暴露：

- `InteropProtocolVersion`
- `InteropMethodNames`
- `InteropBundleKeys`
- `InteropStatus`
- `InteropUriContract`
- `HubSurfaceDescriptor`
- `CallableCapabilityDescriptor`
- `InteropGrant`
- `InteropTaskRecord`
- `InteropArtifactDescriptor`
- `CompatibilitySignal`
- `HubInteropClient` 或等价 helper

并且这些公共定义必须同时被：

- Mobile Claw host implementation 引用
- 独立 probe app 引用
- 后续任何外部 Android app 引用

## 7. 公共模块不应暴露什么

下列内容不应进入公共分发件：

- Hilt entry point
- runtime session orchestrator
- local model gateway
- Room entity / DAO
- Compose screen implementation
- host-only policy evaluator
- host-only approval workflow internals
- internal repository contract

## 8. 版本策略

模块分层之后，建议采用下面的版本策略：

- `contract-core`
  - 跟协议语义版本对齐
- `android-contract`
  - 跟 Android binding 版本对齐
- `app`
  - 跟 Mobile Claw host 发布节奏对齐

兼容性判断建议同时考虑：

- protocol family version
- android binding version
- host capability availability

## 9. 对 spec 的影响

这项决策会把后续 spec 的边界收得更清楚：

- spec 需要要求“存在一个可复用、可版本化、与 host 实现隔离的公共协议模块 / contract 分发层”
- 但 spec 不应把实现写死成“必须是某一种 Gradle 打包形式”

也就是说：

- docs 可以明确 Android 集成方最终消费 `AAR`
- spec 应表达为“可复用的公共 contract distribution”

## 10. 建议的实现落点

如果后续进入实现阶段，我建议模块名直接按下面收：

```text
:hub-interop-contract-core
:hub-interop-android-contract
:app
:interop-probe-app
```

首批 Android slice 不建议再退回“两层结构”，因为那会重新把协议本体和实现者绑死。

最小可接受落点应当至少是：

```text
:hub-interop-contract-core
:hub-interop-android-contract
:app
```

同时在同仓或相邻工程里补一个独立的 `:interop-probe-app` 作为首批消费者。

## 11. `027` Contract Stabilization Notes

`027-public-interop-contract-stabilization` 不改变模块边界，但把 public SDK 的可依赖面进一步固定：

- `:hub-interop-contract-core` 继续保持 host-agnostic，不依赖 `:app`、Room、Hilt、Compose、policy engine 或具体 provider。
- descriptor v1 补齐 caller-visible 字段：capability schema version、output artifact types、side-effect level、data sensitivity、boundedness、approval requirement、availability，以及 grant/task/artifact lifecycle。
- compatibility signal 明确区分 `supported`、`minor_version_downgraded`、`major_version_unsupported`、`malformed_version`、`required_unknown_fields`、`optional_unknown_fields`、`extension_namespace_fields`。
- `:hub-interop-android-contract` 固定 public status taxonomy：`ok`、`bad_request`、`unauthorized`、`authorization_required`、`authorization_pending`、`forbidden`、`not_found`、`expired`、`incompatible_version`、`unsupported_capability`、`provider_unavailable`、`permission_unavailable`、`policy_denied`、`approval_required`、`approval_rejected`、`execution_failed`、`internal_error`。
