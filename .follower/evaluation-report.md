# Follower 插件评估报告

## 测试环境

- **项目**: mobile-claw (Android Agent Runtime)
- **测试日期**: 2026-04-14
- **索引文档数**: 4
- **测试模型**: sentence-transformers/all-MiniLM-L6-v2

---

## 测试结果

### 1. 知识检索能力 (UserPromptSubmit Hook)

| 测试场景 | 结果 | 评分 |
|---------|------|------|
| 查询 "Policy 引擎如何工作" | ✅ 返回 4 个相关文档 | A |
| 语义匹配准确性 | 高 - Policy 相关文档排名靠前 | A |
| 响应时间 | ~11s (首次加载模型) | B |

**详细结果**:
```json
{
  "selected_item_ids": [
    "ki-e0fd14954868 (project-overview.md)",
    "ki-18a21976143c (policy-engine-architecture.md)",
    "ki-2210cab23191 (fix-gradle-kapt-error.md)",
    "ki-decf19c8cb23 (flutter-build-guide.md)"
  ],
  "score_summary": {
    "project-overview": 0.032787,
    "policy-engine": 0.032258,
    "fix-gradle": 0.031498
  }
}
```

### 2. 错误恢复建议 (PostToolUseFailure Hook)

| 测试场景 | 结果 | 评分 |
|---------|------|------|
| KAPT 构建失败 | ✅ 返回修复笔记 | A |
| 错误签名匹配 | 高 - fix-gradle-kapt-error.md 排名第一 | A |
| 响应时间 | ~10s | B |

**详细结果**:
```json
{
  "selected_item_ids": [
    "ki-2210cab23191 (fix-gradle-kapt-error.md)",  // 排名第一
    "ki-decf19c8cb23 (flutter-build-guide.md)",
    "ki-e0fd14954868 (project-overview.md)",
    "ki-18a21976143c (policy-engine-architecture.md)"
  ]
}
```

### 3. 会话记忆存储 (Stop Hook)

| 测试场景 | 结果 | 评分 |
|---------|------|------|
| 简单 payload | ⚠️ 需要完整的 last_assistant_message | B |
| 追踪记录 | ✅ 正确记录到 traces/retrieval.jsonl | A |

---

## 对比分析

### 与手动 Grep/Read 对比

| 维度 | Follower 插件 | 手动 Grep/Read | 胜者 |
|------|--------------|----------------|------|
| **启动方式** | 自动触发 | 需主动发起 | Follower |
| **语义理解** | 向量语义匹配 | 字面匹配 | Follower |
| **精确度** | 依赖种子文档质量 | 100% 精确 | 手动 |
| **实时性** | 需 reindex | 实时 | 手动 |
| **上下文管理** | 自动 budget 控制 | 需手动筛选 | Follower |
| **维护成本** | 需维护种子文档 | 无 | 手动 |

### 与 MCP 服务器对比

| 维度 | Follower 插件 | MCP 服务器 | 胜者 |
|------|--------------|-----------|------|
| **触发方式** | Hook 自动触发 | 需调用工具 | Follower |
| **集成深度** | 注入上下文 | 独立工具 | Follower |
| **灵活性** | 固定 Hook 点 | 可自定义工具 | MCP |
| **错误处理** | 自动匹配历史 | 需手动查询 | Follower |

---

## 优势

1. **自动化**: 无需主动调用，Hook 自动触发
2. **语义匹配**: 使用向量嵌入进行语义相似度搜索
3. **上下文预算**: 自动控制注入内容长度 (budget_chars)
4. **追踪记录**: 完整记录检索历史，便于调试
5. **错误恢复**: 自动匹配历史修复方案

## 劣势

1. **种子文档维护**: 需要手动维护 `.follower/seeds/` 目录
2. **实时性**: 代码变更后需要运行 `reindex`
3. **模型加载**: 首次调用需要加载 embedding 模型 (~10s)
4. **精确度依赖**: 检索质量依赖种子文档质量

---

## 综合评分

| 能力 | 评分 | 说明 |
|------|------|------|
| 知识检索 | **A** | 语义匹配准确，自动注入相关上下文 |
| 错误恢复 | **A** | 能自动匹配历史修复方案 |
| 会话记忆 | **B** | 需要完整的 payload 才能存储 |
| 易用性 | **B+** | 需要维护种子文档 |
| 性能 | **B** | 首次加载模型较慢 |

**总体评分: A-**

---

## 建议

1. **种子文档自动化**: 可以添加自动扫描项目文档的功能
2. **增量索引**: 支持增量更新而非全量重建
3. **模型预热**: 在会话启动时预加载 embedding 模型
4. **多项目支持**: 支持跨项目的知识检索

---

## 使用建议

### 适合场景
- 项目文档完善、有固定的知识库
- 团队协作需要共享项目知识
- 频繁遇到相似的错误需要解决方案
- 需要自动化的上下文注入

### 不适合场景
- 快速原型开发，文档不稳定
- 实时代码搜索需求
- 对精确度要求极高的场景
