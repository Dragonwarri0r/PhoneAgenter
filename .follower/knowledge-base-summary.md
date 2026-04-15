# Mobile Claw 知识库索引完成

## 禂述

已成功将 Mobile Claw 项目的知识写入 Follower 知识库。

**索引统计**: 17 个知识条目
**最后索引时间**: 2026-04-14 10:15:54

---

## 猉标签分类的知识文档

### Architecture (架构)
| 文件 | 描述 |
|------|------|
| `010-project-overview.jsonl` | 项目概述和产品定位 |
| `020-runtime-architecture.jsonl` | Runtime 分层架构 |
| `040-specs-overview.jsonl` | Specs 规格概览 |

### Component(组件)
| 文件 | 描述 |
|------|------|
| `030-policy-engine.jsonl` | Policy 引擎组件 |
| `031-memory-system.jsonl` | Memory 系统组件 |
| `032-capability-provider.jsonl` | Capability Provider 组件 |
| `033-governance-system.jsonl` | Governance 治理系统 |
| `034-structured-action.jsonl` | Structured Action 组件 |
| `035-system-source.jsonl` | System Source 系统源 |
| `036-multimodal-input.jsonl` | Multimodal Input 多模态输入 |

### Fix Note(修复笔记)
| 文件 | 描述 |
|------|------|
| `050-fix-gradle-kapt.jsonl` | Gradle KAPT 构建失败 |
| `051-fix-room-migration.jsonl` | Room 数据库迁移错误 |
| `052-fix-compose-compilation.jsonl` | Jetpack Compose 编译错误 |

### Guide(开发指南)
| 文件 | 描述 |
|------|------|
| `060-guide-adding-capability.jsonl` | 添加新的 Capability |
| `061-guide-design-system.jsonl` | 设计系统规范 |
| `062-guide-local-chat.jsonl` | Local Chat Gateway 使用 |

---

## 标签统计
| 标签 | 数量 |
|------|------|
| architecture | 2 |
| component | 7 |
| spec | 1 |
| fix | 3 |
| guide | 3 |
| runtime | 1 |
| policy | 1 |
| memory | 1 |
| capability | 1 |
| governance | 1 |
| action | 1 |
| system-source | 1 |
| multimodal | 1 |
| gradle | 1 |
| kapt | 1 |
| build | 1 |
| room | 1 |
| database | 1 |
| migration | 1 |
| compose | 1 |
| ui | 1 |
| development | 1 |
| extension | 1 |
| design | 1 |
| chat | 1 |
| gateway | 1 |

---

## 使用方式

### 1. Hook 自动触发
- **UserPromptSubmit**: 用户提交提示时自动注入相关上下文
- **PostToolUseFailure**: 卽令失败时自动提供修复建议
- **Stop**: 会话结束时自动保存会话记忆

### 2. 手动检索
```bash
follower hook-prompt --project-dir .
# 输入: {"prompt": "Policy 引擎如何工作?", "cwd": "."}
```

### 3. 更新知识库
```bash
# 添加新知识文档到 .follower/seeds/ 目录
follower reindex --project-dir .
```
