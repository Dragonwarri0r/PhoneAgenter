# Research: Safe Execution Policy and Approval Flow

## Decision 1: Separate Risk Classification from Final Policy Resolution

**Decision**: Model risk classification and final policy authorization as separate steps and separate persisted entities.

**Rationale**:

- The roadmap and constitution already require classifier output to be advisory rather than authoritative.
- This separation keeps hard-confirm and deny rules deterministic even when classifier confidence is wrong.
- It also gives audit logs enough structure to explain why policy overrode the classifier.

**Alternatives considered**:

- **Use classifier output directly as the execution decision**: Rejected because it would blur safety boundaries and make hard rules harder to reason about.
- **Skip classifier output and rely only on static rules**: Rejected because the product still needs flexible low-risk automation and richer explanations.

## Decision 2: Persist Audit, Approval, and Policy State in Room

**Decision**: Store risk assessments, policy decisions, approval requests, approval outcomes, and audit events in Room.

**Rationale**:

- `004` introduces structured local records with query and replay value.
- Room fits the existing app architecture and keeps data local-first.
- Approval history and audit traces benefit from indexed local querying rather than transient in-memory state.

**Alternatives considered**:

- **Keep policy and audit state in memory only**: Rejected because it would weaken explainability and make validation brittle.
- **Store audit records in DataStore or flat files**: Rejected because querying and linking records to sessions would become awkward.

## Decision 3: Gate the Existing Runtime Session Pipeline Instead of Creating a Side Approval Flow

**Decision**: Add policy resolution and approval handling into the existing runtime session pipeline from `002`.

**Rationale**:

- `002` already established the session lifecycle and provider routing seams.
- Approval should be visible as one more runtime stage, not a separate subsystem the UI has to orchestrate manually.
- This keeps session state, audit state, and execution outcomes aligned.

**Alternatives considered**:

- **Create a UI-owned approval flow outside the runtime**: Rejected because it would split execution truth across layers.
- **Make providers handle their own approvals**: Rejected because safety policy must be common across providers.

## Decision 4: Start with a Lightweight Approval Surface in the Workspace

**Decision**: Use a lightweight bottom sheet or dialog in the workspace as the first approval surface.

**Rationale**:

- It matches the current app architecture and keeps milestone scope bounded.
- The user only needs one clear preview, explanation, and approve/reject action hierarchy in `v0`.
- It leaves room for richer audit browsing later without blocking safe execution now.

**Alternatives considered**:

- **Add a dedicated approval screen**: Rejected because it would add navigation complexity before the policy model is stable.
- **Use only snackbars or transient banners**: Rejected because approval requires richer previews and explicit decisions.

## Decision 5: Add a Shared Localization Layer for Runtime and UI Text

**Decision**: Introduce Android string resources for English and Simplified Chinese plus a shared non-Compose string resolver for runtime and ViewModel messaging.

**Rationale**:

- Current user-facing strings are mostly hardcoded in Kotlin, including runtime state and failure messages.
- `004` will add even more approval and audit text, so delaying localization would multiply later cleanup.
- Android resource qualifiers already support automatic locale selection based on device language.

**Alternatives considered**:

- **Keep current English-only strings until later**: Rejected because approval and audit UX should be understandable in the user’s language from the start.
- **Localize only Compose UI and leave runtime messages hardcoded**: Rejected because runtime status, approval rationales, and failure paths are user-visible too.
