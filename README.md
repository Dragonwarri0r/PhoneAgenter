# mobile_claw

`mobile_claw` 是一个 `Android + 单用户 + 本地优先` 的个人 Agent Runtime 项目。它的目标不是做一个单纯的聊天壳，而是把本地模型、设备能力、治理审批、知识检索和自动化工作流收进同一个可解释、可恢复的运行时里。

## 当前重点

- 会话与运行时主链路：workspace、runtime session、risk / policy / approval、audit 已经打通。
- 设备能力：支持统一 tool contract、Android capability bridge、显式 read / write capability、审批前预览与路由解释。
- 知识与自动化：已补到 knowledge ingestion / retrieval 和 workflow / automation surface。
- 能力推断与 read tools：workspace 自由输入不再一律回到 `generate.reply`，而是先经过保守能力选择；`calendar.read` 已经成为可执行、可解释、可通过 interop 暴露的低风险 read capability。
- Hub Interop：公共协议模块、Android binding、Mobile Claw Host、独立 Probe App、可信 host 边界和 conformance suite 已形成第一条可验证闭环。

## 先读哪里

- [Agent Runtime Project Roadmap v1](./docs/project-roadmap-v1.md)
- [Spec Breakdown v1](./docs/spec-breakdown-v1.md)
- [Tool, Capability, and Extension Standards v1](./docs/tool-capability-and-extension-standards-v1.md)
- [Hub Interop Protocol Design v1](./docs/hub-interop-protocol-design-v1.md)
- [Hub Interop 027-029 Spec Split v1](./docs/hub-interop-027-029-spec-split-v1.md)
- [Room Migration Constraints](./docs/room-migration-constraints.md)

如果你想直接看最近在做什么，建议从这些 specs 开始：

- `specs/023-capability-inference-read-tools`
- `specs/024-shared-interop-contract`
- `specs/025-mobileclaw-interop-host`
- `specs/026-interop-probe-app`
- `specs/027-public-interop-contract-stabilization`
- `specs/028-mobileclaw-trusted-interop-host`
- `specs/029-interop-probe-conformance-suite`

完整 specs 列表当前覆盖 `001` 到 `029`，都在 [`specs/`](./specs) 目录下。`022-hub-interop-protocol` 是已被后续协议文档和 `024+` 实现路线取代的 docs baseline。

## 最近实现状态

- `023-capability-inference-read-tools`
  现在的 workspace 输入先走 capability selection，再决定 reply / read / action。
  读能力和被动 system-source ingestion 已经分层。
  `calendar.read` 已支持真实读取、无结果与不可用说明。
  `calendar.write` / `calendar.delete` 已支持结构化预览、审批和真实日历写入/删除。

- `024-shared-interop-contract`
  已拆出 `:hub-interop-contract-core` 和 `:hub-interop-android-contract`。
  Mobile Claw、Host 实现和 Probe App 现在共享同一套 public protocol / Android binding，而不是复制 host-owned 常量。

- `025-mobileclaw-interop-host`
  `:app` 已实现 `HubInteropProvider`，支持 discovery、authorization、capability invocation、task 和 artifact continuation。
  `ACTION_SEND` 保留为 compatibility ingress，公共 interop 入口转向 shared contract。

- `026-interop-probe-app`
  已加入独立 `:interop-probe-app` 模块。
  Probe 只依赖共享协议模块，可以作为外部 caller 验证 discovery、authorization、invocation、task、artifact 和 compatibility 行为。

- `027-public-interop-contract-stabilization`
  public method family、status taxonomy、descriptor v1、compatibility policy 和 Android Bundle codec 已稳定到可测试的公共边界。
  contract modules 继续保持与 `:app`、Room、Hilt、Compose、host runtime internals 解耦。

- `028-mobileclaw-trusted-interop-host`
  Host 边界已改为 host-attested caller identity。
  request Bundle 里的 caller metadata 只用于展示和诊断，grant lookup、task ownership、artifact access 和 audit identity 使用 host-derived caller fingerprint。
  interop 暴露能力保持收敛：`generate.reply` 加 bounded `calendar.read`。

- `029-interop-probe-conformance-suite`
  Probe 已从手动验证 app 升级为 conformance suite。
  支持 manual diagnostics、自动 conformance matrix、spoof diagnostics、version/compatibility cases、task/artifact checks 和可分享报告。

- workspace UI
  首页和 control center 已做过一轮减法与重排。
  顶部面板自动收起后不会自动重新展开，需要用户手动从菜单显示。

## Hub Interop 协议设计

Hub Interop 现在被拆成三层产品边界：

- `Hub Interop Protocol`
  独立公共协议 / SDK / contract，定义 discovery、authorization、capability invocation、task polling、artifact loading、compatibility diagnostics 等外部调用语义。

- `Mobile Claw Host`
  Mobile Claw 对协议的本地实现。它通过 `HubInteropProvider` 接住外部请求，并把请求送回 authorization、governance、policy、approval、runtime session、capability router、provider execution、audit 和 control center。

- `Interop Probe App`
  独立外部 caller 和 conformance client。它只依赖公共协议模块，不依赖 `:app`，用于证明协议和 host 行为可以被第三方 app 使用和验证。

协议链路固定为：

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

协议层本身不执行能力，只定义公共方法、请求/响应、状态码、descriptor、handle 和 compatibility 语义。Mobile Claw Host 才负责执行能力和治理。`ACTION_SEND` / `ACTION_SEND_MULTIPLE` 继续作为 compatibility ingress，但不再是长期主协议入口。

当前公共方法族包括：

- `discover_surface`
- `request_authorization`
- `get_grant_status`
- `revoke_grant`
- `invoke_capability`
- `get_task`
- `get_artifact`

当前公共状态语义覆盖 `ok`、`bad_request`、`unauthorized`、`authorization_required`、`authorization_pending`、`forbidden`、`not_found`、`expired`、`incompatible_version`、`unsupported_capability`、`provider_unavailable`、`permission_unavailable`、`policy_denied`、`approval_required`、`approval_rejected`、`execution_failed` 和 `internal_error`。

当前 host 只通过 interop 暴露两类收敛能力：

- `generate.reply`
  证明基础 invocation、authorization、task 和 artifact 链路。

- `calendar.read`
  证明真实 Android local read capability、权限不可用、provider 不可用、bounded query、no-results、calendar summary artifact 和 audit/control-center 可见性。

## 协议验证链路

协议相关验证分成三层，分别保护 public contract、host trust boundary 和外部 caller 行为。

- Contract module tests
  覆盖 public identifiers、method/status taxonomy、descriptor v1 validation、compatibility evaluator、Android Bundle codec roundtrip 和 request/response adapter。

- Host interop tests
  覆盖 `HubInteropProvider` discovery、authorization lifecycle、host-attested caller identity、claimed metadata spoof 防护、grant lookup、revoked caller、task/artifact ownership、expired/not-found/forbidden 语义，以及 bounded `calendar.read` invocation 和 artifact mapping。

- Probe conformance tests
  覆盖 manual diagnostics、conformance matrix、spoof diagnostic、minor downgrade、major incompatibility、malformed request、authorization pending/granted/revoked、`generate.reply`、bounded `calendar.read`、task polling、artifact loading、report formatting 和 `:app` dependency isolation。

推荐的协议验证命令：

```bash
./gradlew :hub-interop-contract-core:test \
  :hub-interop-android-contract:testDebugUnitTest \
  :app:testDebugUnitTest \
  :interop-probe-app:testDebugUnitTest \
  --no-daemon
```

## 开发命令

- 编译：`./gradlew :app:compileDebugKotlin --no-daemon`
- 单测：`./gradlew :app:testDebugUnitTest --tests "com.mobileclaw.app.runtime.intent.RuntimeIntentHeuristicsTest" --no-daemon`
- Hub Interop contract 测试：`./gradlew :hub-interop-contract-core:test :hub-interop-android-contract:testDebugUnitTest --no-daemon`
- Probe conformance 测试：`./gradlew :interop-probe-app:testDebugUnitTest --no-daemon`
- 安装到已连接设备：`./gradlew :app:installDebug --no-daemon`

## 代码组织

- `app/src/main/java/com/mobileclaw/app/runtime/`
  runtime、policy、capability、provider、knowledge、workflow 等核心运行时逻辑。
- `app/src/main/java/com/mobileclaw/app/runtime/interop/`
  Mobile Claw Hub Interop host implementation、authorization、task、artifact、trusted caller identity 等外部协议入口。
- `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
  workspace 主界面、control center、automation、composer 等 UI。
- `hub-interop-contract-core/`
  host-agnostic Hub Interop protocol entities、descriptors、version 和 compatibility primitives。
- `hub-interop-android-contract/`
  Android binding、method/status constants、URI/request helpers 和 Bundle codecs。
- `interop-probe-app/`
  独立外部 caller / conformance client，用来验证公共协议和 Mobile Claw Host 行为。
- `specs/`
  所有里程碑 spec、plan、tasks 和相关契约文档。

建议阅读顺序是：`roadmap -> spec breakdown -> 目标 spec -> 代码实现`。
