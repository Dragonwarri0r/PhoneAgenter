# mobile_claw

本项目用于设计和实现一个 `Android + 单用户 + 本地优先` 的个人 Agent Runtime。

当前项目级路线和范围定义见：

- [Agent Runtime Project Roadmap v0](./docs/project-roadmap-v0.md)
- [Spec Breakdown v0](./docs/spec-breakdown-v0.md)
- [Agent Runtime Project Roadmap v1](./docs/project-roadmap-v1.md)
- [Spec Breakdown v1](./docs/spec-breakdown-v1.md)
- [Tool, Capability, and Extension Standards v1](./docs/tool-capability-and-extension-standards-v1.md)
- [Room Migration Constraints](./docs/room-migration-constraints.md)

当前已按 `spec-kit` 初始化，并拆出一组按里程碑组织的 feature specs：

- `specs/001-android-agent-shell`
- `specs/002-runtime-session-pipeline`
- `specs/003-persona-memory-fabric`
- `specs/004-safe-execution-policy`
- `specs/005-android-capability-bridge`
- `specs/006-sync-extension-hooks`
- `specs/007-external-runtime-entry`

建议先从 roadmap 和 spec breakdown 开始阅读，再逐个细化对应 spec。
