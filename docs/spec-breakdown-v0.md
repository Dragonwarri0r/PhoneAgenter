# Spec Breakdown v0

## Purpose

This document maps the project roadmap into a set of `spec-kit` feature specs.

The split follows two rules:

- each spec should be small enough to plan and implement as one milestone
- each spec should still deliver a meaningful, demoable increment

## Milestone to Spec Map

| Spec | Milestone | Primary Goal | Why This Is a Good Slice | Depends On |
|---|---|---|---|---|
| `001-android-agent-shell` | M1 | Create the Android app shell, local model workspace, and usable chat session UI | Gives us the first end-to-end local interaction loop without waiting for cross-app execution | None |
| `002-runtime-session-pipeline` | M2 | Establish the unified runtime execution session and orchestration pipeline | Defines the request lifecycle before memory, policy, and Android adapters deepen it | 001 |
| `003-persona-memory-fabric` | M3 | Add persona boundaries and scoped memory retrieval/writeback | Makes the runtime context-aware without entangling memory with UI or policy | 002 |
| `004-safe-execution-policy` | M4 | Add risk classification, policy decisions, approval gating, and audit events | Creates the safety boundary for automation and turns runtime actions into controlled operations | 002, 003 |
| `005-android-capability-bridge` | M5 | Connect runtime capabilities to Android through AppFunctions-first integration | Makes the agent actually useful across apps while preserving internal contracts | 002, 004 |
| `006-sync-extension-hooks` | M6 | Reserve sync, merge, share, and extension hooks without implementing full sync | Prevents v0 schemas from blocking multi-device or provider growth later | 003, 004, 005 |

## Recommended Build Order

1. `001-android-agent-shell`
2. `002-runtime-session-pipeline`
3. `003-persona-memory-fabric`
4. `004-safe-execution-policy`
5. `005-android-capability-bridge`
6. `006-sync-extension-hooks`

## Why This Cut

This decomposition avoids two common problems:

- a giant umbrella spec that mixes UI, runtime, memory, Android integration, and policy into one unimplementable milestone
- overly tiny specs that only produce internal scaffolding and never result in a demoable increment

The first five specs align with the practical `v0` path.
The sixth spec is intentionally narrower and exists to keep the architecture open for later sync and extension work.

## Gallery Reference Mapping

The `gallery` project should be used as an implementation reference for local model and chat UX, especially for `001`.
Those references should inform later planning and implementation, not be copied directly into requirement text.

Primary reference files:

- `/Users/youxuezhe/StudioProjects/gallery/Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/common/chat/ChatView.kt`
- `/Users/youxuezhe/StudioProjects/gallery/Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/common/chat/ChatViewModel.kt`
- `/Users/youxuezhe/StudioProjects/gallery/Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/llmchat/LlmChatScreen.kt`
- `/Users/youxuezhe/StudioProjects/gallery/Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/llmchat/LlmChatViewModel.kt`
- `/Users/youxuezhe/StudioProjects/gallery/Android/src/app/src/main/java/com/google/ai/edge/gallery/ui/modelmanager/ModelManagerViewModel.kt`
- `/Users/youxuezhe/StudioProjects/gallery/Android/src/app/src/main/java/com/google/ai/edge/gallery/customtasks/agentchat/AgentChatScreen.kt`

Project-local UI references:

- `/Users/youxuezhe/StudioProjects/mobile_claw/DESIGN.md`
- `/Users/youxuezhe/StudioProjects/mobile_claw/screen.png`

Reference guidance:

- `001` should borrow structure from the gallery chat shell and local model lifecycle
- `002` can borrow session and streaming patterns, but should not inherit gallery-specific task abstractions blindly
- `004` may borrow message and progress visualization ideas for approval and audit feedback
- `005` should remain runtime-contract first, even if UI patterns in gallery help expose capability status
- `001`, `002`, and `004` should also absorb the "Digital Atrium" UI direction from `DESIGN.md`, especially the tonal layering, no-line rule, glass-like agent surfaces, and lightweight success or failure feedback

## Active Scope vs Deferred Scope

Active `v0` specs:

- `001-android-agent-shell`
- `002-runtime-session-pipeline`
- `003-persona-memory-fabric`
- `004-safe-execution-policy`
- `005-android-capability-bridge`

Deferred-but-reserved spec:

- `006-sync-extension-hooks`

This means `006` should define contracts and metadata expectations, but should not force actual background sync into `v0`.
