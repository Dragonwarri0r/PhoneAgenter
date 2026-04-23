# Research: Runtime Hooks And Context Sources

## Decision 1: Introduce a shared runtime contribution surface instead of expanding ad hoc callbacks

- **Decision**: Add a dedicated runtime contribution language that lifecycle hooks, context sources, and future knowledge-source contributors can all map into.
- **Why**: Existing extension, system-source, memory, and session layers already contribute behavior or context, but they do so through scattered mechanisms. `019` needs one shared runtime language so later growth stays coherent.
- **Alternatives considered**:
  - Keep hook semantics inside `RuntimeSessionOrchestrator`: rejected because it would keep lifecycle behavior centralized but non-extensible.
  - Add separate contracts for hooks and context sources: rejected because the roadmap explicitly wants one contribution family, not two parallel entry models.

## Decision 2: Treat lifecycle-oriented and context-oriented contributors as two modes of the same registration

- **Decision**: Use one registration shape with contribution type and lifecycle-point metadata rather than two unrelated contributor systems.
- **Why**: Some contributors primarily observe or gate lifecycle stages, while others primarily attach request-time context. The system still needs one discovery, eligibility, provenance, and management language across both.
- **Alternatives considered**:
  - Model only context contributors first: rejected because behavior-oriented hooks would keep leaking back in as one-off runtime callbacks.
  - Model only lifecycle hooks first: rejected because request-time context attachment is already a meaningful product concern.

## Decision 3: Keep active-task visibility concise and push deep contributor truth into existing management surfaces

- **Decision**: Surface short contribution summaries in the current task flow, while richer provenance, availability, and policy details stay in control-center and detail surfaces.
- **Why**: The updated roadmap explicitly fixes chat as the front stage and control/detail layers as the truth surfaces. Contributors should extend that structure, not break it.
- **Alternatives considered**:
  - Show full contributor metadata inline with every turn: rejected because it would overwhelm the task flow.
  - Keep contributions fully hidden until users open deep management views: rejected because it would make runtime behavior feel opaque.

## Decision 4: Limit reversible management to basic availability state in this slice

- **Decision**: `019` supports reversible contributor management such as enable, disable, inspect, and comparable availability controls, but not full provider schema editing or re-mount logic.
- **Why**: The roadmap already identifies richer extension/runtime configuration as later work. This slice should prove governability without swallowing broader system management.
- **Alternatives considered**:
  - Add full contributor configuration editing now: rejected because it would inflate scope and blur the boundary with future system management work.
  - Leave contributors completely unmanaged: rejected because then `019` would not improve real operability enough.

## Decision 5: Keep `019` strictly before durable corpus and workflow execution

- **Decision**: `019` stops at request-time contribution contracts, explainability, and reversible contributor management.
- **Why**: Durable knowledge ingestion belongs to `020`, and workflow/DAG execution belongs to `021`. Those milestones depend on `019`, but `019` should not absorb them.
- **Alternatives considered**:
  - Add corpus ingestion to prove knowledge-source value immediately: rejected because it would collapse request-time contribution and durable corpus management into one milestone.
  - Add hook-driven workflow logic to prove extensibility breadth: rejected because it would front-load automation complexity before workflow contracts exist.
