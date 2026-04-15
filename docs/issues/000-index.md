# Issues Index

所有 spec review 发现的问题汇总。按优先级排列，每条标注所属 spec 和来源文档。

**文档列表**
- [001-layout-and-keyboard.md](001-layout-and-keyboard.md) — 布局与键盘问题（spec1）
- [002-spec-gaps.md](002-spec-gaps.md) — Spec1/2 首轮遗漏
- [003-spec345-review.md](003-spec345-review.md) — Spec3/4/5 + 布局跟进
- [004-spec6-12-review.md](004-spec6-12-review.md) — Spec6–12 + 全部问题跟进

---

## 未解决问题

### 高优先级

| ID | 问题 | 来源 |
|---|---|---|
| Spec6-01 | `mustRedactEvidence` 始终为 true，SHAREABLE_FULL 记录在 export 中也被截断，与 FR-004/005 矛盾 | [003](003-spec345-review.md) / [004](004-spec6-12-review.md) |
| Spec12-01 | `MobileClawAppFunctions.draftReply` 无超时，本地模型挂起时 AppFunction 调用永久阻塞 | [004](004-spec6-12-review.md) |
| Spec4-02 | 审批 PENDING 状态下 session 被取消时，DB 里留下无 outcome 行的孤立 ApprovalRequest | [003](003-spec345-review.md) |

### 中优先级

| ID | 问题 | 来源 |
|---|---|---|
| Spec7-01 | 外部 handoff 来源没有明确展示给用户（FR-010：用户应看到 handoff 触发了新 session） | [004](004-spec6-12-review.md) |
| Spec7-02 | `ExternalHandoffCoordinator` 只保留一个 pending 事件，快速连续分享会丢失前一条 | [004](004-spec6-12-review.md) |
| Spec8-02 | `RiskClassifier` 依赖结构化 payload 的 completeness，但这取决于 orchestrator 中 normalization 先于 classification 执行 | [004](004-spec6-12-review.md) |
| Spec9-01 | `GovernanceActivityItem.scopeLabel` 始终为空，`GovernanceCenterSheet` 对应列永远空白 | [004](004-spec6-12-review.md) |
| Spec10-02 | 每次 `calendar.write` 请求都静默查询用户日历，无用户知情或控制 | [004](004-spec6-12-review.md) |
| Spec10-03 | `SystemSourceIngestionService` 每次请求都 upsert 相同联系人/日历记录，无去重 | [004](004-spec6-12-review.md) |
| Spec9-02 | 删除 caller 记录时 `ScopeGrantRecord` 孤立行不清理（无 cascade） | [004](004-spec6-12-review.md) |
| OBS-02 | `RuntimeStatusUiModel.isTerminal` 在每次 turn 完成后都设为 true，语义不准确 | [002](002-spec-gaps.md) |

### 低优先级

| ID | 问题 | 来源 |
|---|---|---|
| Cross-01 | 调试用方括号标记（`[blocked]`、`[message]` 等）仍在生产代码中以 0.99 置信度生效 | [004](004-spec6-12-review.md) |
| Cross-02 | 只有 2 个测试文件，policy engine / risk classifier / memory retrieval / normalizer 均无覆盖 | [004](004-spec6-12-review.md) |
| Spec10-01 | 停用词过滤仅覆盖英文，中文输入的 token 过滤无效 | [004](004-spec6-12-review.md) |
| Spec11-01 | `PortabilityBundleShareService` 重复添加 `FLAG_ACTIVITY_NEW_TASK` 两次 | [004](004-spec6-12-review.md) |
| Spec11-02 | share 时仅依赖 preview 的 `canShare` 标志，未在 service 层二次校验 exposure policy | [004](004-spec6-12-review.md) |
| Spec12-02 | `AppFunctionExposureCatalog` 只映射 2 个 capability，其余 4 个无 AppFunction 路径（需文档说明） | [004](004-spec6-12-review.md) |
| Spec3-02 | Extension compatibility 检查结果从未在运行时使用 | [003](003-spec345-review.md) |
| Spec3-03 | `MemoryRetrievalService` 评分相同时排序不确定 | [003](003-spec345-review.md) |
| Spec9-03 | `GovernanceCenterSheet` 对 trust mode 切换无防抖，快速点击会触发多次 DB 写 | [004](004-spec6-12-review.md) |
| Layout-01B | 首帧 padding 依赖 `onSizeChanged`，fallback 常量是魔法值（64dp/84dp） | [003](003-spec345-review.md) |

---

## 已解决问题

| ID | 问题 | 解决轮次 |
|---|---|---|
| Layout-01A | 键盘收起后面板不自动复原 | 004 轮 |
| Layout-01C | Manifest 缺 `windowSoftInputMode="adjustResize"` | 004 轮 |
| Layout-01D | Conversation 卡片标题永远显示占用空间 | 004 轮 |
| Layout-root | 整体 Column 布局导致键盘挤压输入框 | 003 轮 |
| Spec4-01 | RiskClassifier 使用方括号标记分类 | 004 轮 |
| Spec5-01 | AppFunctionBridge 是纯 seeded stub | 004 轮 |
| Approval-timeout | PendingApprovalCoordinator 无超时 | 004 轮 |
| GAP-S1-03 | `CapabilityFailed` 事件被静默丢弃 | 003 轮 |
| GAP-S2-01 | `NoOpRuntimeContextLoader` 不做任何事 | 003 轮 |
| GAP-S2-04 | `getProvider(plan)` 无 provider 时无保护 | 003 轮 |
| OBS-01 | `stageLabel`/`contextSummary` 与 `runtimeStatus` 双轨同步 | 003 轮 |
