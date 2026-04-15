# Research: Permission Governance Center

## Decision 1: Store governance in Room beside existing policy/audit tables

**Decision**: Add governance entities to the existing Room database instead of creating a separate store.

**Rationale**:

- Caller trust, scope grants, approvals, and audits already belong to the same policy domain.
- The app already has Room, DAOs, and repositories for policy-adjacent state.
- Keeping governance nearby reduces join friction for recent-activity views.

**Alternatives considered**:

- DataStore: simpler for small preferences but awkward for per-caller and per-scope records.
- In-memory only: insufficient because user governance must persist across app restarts.

## Decision 2: Put the first governance center inside the workspace as a modal sheet

**Decision**: Add a governance center sheet launched from the existing workspace header/panel area.

**Rationale**:

- This preserves a single product surface and avoids turning `009` into a navigation or settings refactor.
- Users can inspect governance in the same place where runtime status, approvals, and external handoffs are already visible.

**Alternatives considered**:

- Dedicated settings screen: too large for this milestone.
- Hidden developer-only panel: fails the product goal of visible governance.

## Decision 3: Resolve runtime governance in the capability layer before restricted routing continues

**Decision**: Apply governance overrides in `CallerVerifier`/`CapabilityRouter` using a repository-backed governance snapshot.

**Rationale**:

- The capability layer already decides whether restricted execution can continue.
- This is the narrowest place to apply caller-level trust and scope-level grants without duplicating routing logic downstream.

**Alternatives considered**:

- Apply only in `PolicyEngine`: too late; routing and caller verification would already have advanced.
- Apply only in UI: not enforceable.

## Decision 4: Start with a bounded, known set of scopes

**Decision**: Support governance editing for the scopes already represented in `ActionScope`.

**Rationale**:

- Those scopes are already used by risk/policy/runtime.
- This keeps the UI understandable and avoids inventing a second scope taxonomy.

**Alternatives considered**:

- Free-form scope strings: too error-prone for v1.
- One giant allow-all toggle: not enough governance value.
