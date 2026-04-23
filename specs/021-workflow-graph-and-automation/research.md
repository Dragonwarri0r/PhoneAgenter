# Research: Workflow Graph And Automation

## Decision 1: Start with durable workflow contracts, not a heavy visual graph builder

- **Decision**: The first workflow slice focuses on stable workflow definitions, steps, triggers, and availability state rather than a full freeform graph editor.
- **Why**: The roadmap explicitly warns against beginning with a graph editor. The first value comes from durable, reusable workflow structure, not from advanced visual authoring.
- **Alternatives considered**:
  - Build a full visual graph builder first: rejected because it would front-load editing complexity before execution semantics are stable.
  - Keep workflows implicit inside prompts only: rejected because `021` exists to make repeatable multi-step work durable and governable.

## Decision 2: Reuse existing proposal, policy, approval, and result language for workflow steps

- **Decision**: Side-effecting workflow steps should use the same proposal, policy, approval, and result semantics already established for request execution.
- **Why**: Automation must feel like an extension of the current runtime, not a second execution plane with different safety rules.
- **Alternatives considered**:
  - Create automation-specific approval semantics: rejected because it would fragment trust and governance.
  - Allow workflow steps to bypass current proposal/approval language: rejected because it would violate the product’s safety model.

## Decision 3: Make resumable checkpoints a first-class workflow concern

- **Decision**: Workflow runs should preserve explicit checkpoints and run-state transitions so pauses, approvals, interruptions, and resumptions remain explainable.
- **Why**: Multi-step execution only becomes trustworthy if users can tell where a run paused, what is blocking it, and whether it can continue.
- **Alternatives considered**:
  - Treat workflow runs as fire-and-forget background jobs: rejected because it would make failures and interruptions too opaque.
  - Only keep a simple completed/failed state: rejected because it would not support pauses or resumability.

## Decision 4: Keep automation management in existing control/detail surfaces

- **Decision**: Global automation management belongs in the control center’s Automation area and in workflow/run detail views, while the conversation/session layers only show task-relevant run summaries.
- **Why**: The updated roadmap fixes conversation as the task surface and control/detail as the system surface. Workflow support should reinforce that model.
- **Alternatives considered**:
  - Add a separate automation console: rejected because it would split the product again.
  - Keep workflow management only in chat: rejected because global automation state is not a chat-first concern.

## Decision 5: Keep `021` local-first and away from remote orchestration scope

- **Decision**: The first automation slice stops at local workflow contracts, resumable execution, and control-plane visibility.
- **Why**: The roadmap explicitly excludes remote orchestration, distributed execution, and marketplace flow sharing from this milestone.
- **Alternatives considered**:
  - Add cloud-triggered or multi-device automation immediately: rejected because it breaks local-first focus and inflates the slice.
  - Delay run history and checkpoint visibility until later: rejected because resumability and explainability are core value in this milestone.
