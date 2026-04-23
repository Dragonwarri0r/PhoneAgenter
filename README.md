# mobile_claw

`mobile_claw` 是一个 `Android + 单用户 + 本地优先` 的个人 Agent Runtime 项目。它的目标不是做一个单纯的聊天壳，而是把本地模型、设备能力、治理审批、知识检索和自动化工作流收进同一个可解释、可恢复的运行时里。

## 当前重点

- 会话与运行时主链路：workspace、runtime session、risk / policy / approval、audit 已经打通。
- 设备能力：支持统一 tool contract、Android capability bridge、显式 read / write capability、审批前预览与路由解释。
- 知识与自动化：已补到 knowledge ingestion / retrieval 和 workflow / automation surface。
- 最新一轮能力层补强：workspace 自由输入不再一律回到 `generate.reply`，而是先经过保守能力选择；同时 `calendar.read`、`calendar.write`、`calendar.delete` 已经具备真实 provider、预览、审批和结果解释。

## 先读哪里

- [Agent Runtime Project Roadmap v1](./docs/project-roadmap-v1.md)
- [Spec Breakdown v1](./docs/spec-breakdown-v1.md)
- [Tool, Capability, and Extension Standards v1](./docs/tool-capability-and-extension-standards-v1.md)
- [Room Migration Constraints](./docs/room-migration-constraints.md)

如果你想直接看最近在做什么，建议从这些 specs 开始：

- `specs/018-runtime-control-center`
- `specs/020-knowledge-ingestion-and-retrieval`
- `specs/021-workflow-graph-and-automation`
- `specs/023-capability-inference-read-tools`

完整 specs 列表当前覆盖 `001` 到 `026`，都在 [`specs/`](./specs) 目录下。

## 最近实现状态

- `023-capability-inference-read-tools`
  现在的 workspace 输入先走 capability selection，再决定 reply / read / action。
  读能力和被动 system-source ingestion 已经分层。
  `calendar.read` 已支持真实读取、无结果与不可用说明。
  `calendar.write` / `calendar.delete` 已支持结构化预览、审批和真实日历写入/删除。

- workspace UI
  首页和 control center 已做过一轮减法与重排。
  顶部面板自动收起后不会自动重新展开，需要用户手动从菜单显示。

## 开发命令

- 编译：`./gradlew :app:compileDebugKotlin --no-daemon`
- 单测：`./gradlew :app:testDebugUnitTest --tests "com.mobileclaw.app.runtime.intent.RuntimeIntentHeuristicsTest" --no-daemon`
- 安装到已连接设备：`./gradlew :app:installDebug --no-daemon`

## 代码组织

- `app/src/main/java/com/mobileclaw/app/runtime/`
  runtime、policy、capability、provider、knowledge、workflow 等核心运行时逻辑。
- `app/src/main/java/com/mobileclaw/app/ui/agentworkspace/`
  workspace 主界面、control center、automation、composer 等 UI。
- `specs/`
  所有里程碑 spec、plan、tasks 和相关契约文档。

建议阅读顺序是：`roadmap -> spec breakdown -> 目标 spec -> 代码实现`。
