# Research: Trusted External Handoff Entry

## Decision 1: Use `ACTION_SEND` with `text/plain` as the First Real External Entry

**Decision**: The first productized external handoff path will be an Android activity entry that receives `Intent.ACTION_SEND` with MIME type `text/plain`.

**Rationale**:

- This is the most direct way to let users hand off content from another Android app through the Android Sharesheet.
- It matches the current spec boundary of a text-first external handoff rather than a full SDK or plugin protocol.
- Android’s official sharing guidance explicitly supports registering an activity intent-filter for `ACTION_SEND` with `text/plain` and handling the incoming text inside the receiving activity.

**Alternatives considered**:

- **Custom app-to-app SDK or bound service first**: Rejected because it is too heavy for the first real user-facing handoff path.
- **AppFunctions-only entry**: Rejected because the project is still on SDK 35 and the first product flow should not wait for full framework uplift.
- **Support `ACTION_SEND_MULTIPLE` or rich content immediately**: Rejected because it would enlarge the milestone before the normalized ingress contract is stable.

## Decision 2: Normalize Android Intents in a Dedicated Ingress Layer Before Runtime Submission

**Decision**: Add a dedicated `runtime/ingress` layer that parses Android intents into a normalized inbound handoff contract before producing a canonical `RuntimeRequest`.

**Rationale**:

- `spec007` explicitly requires downstream runtime layers to avoid Android entry-point details.
- The runtime pipeline from `002-006` is already stable and should remain ignorant of manifest actions, MIME checks, and intent extras.
- A dedicated ingress layer keeps share parsing, caller/source extraction, and malformed-input rejection in one place.

**Alternatives considered**:

- **Parse the incoming intent directly inside `AgentWorkspaceViewModel`**: Rejected because it would leak Android-specific handling into UI state management.
- **Extend `RuntimeSessionOrchestrator` to read raw `Intent` data**: Rejected because it would violate the adapter boundary and make orchestration platform-specific.

## Decision 3: Use a One-Shot Handoff Coordinator Between `MainActivity` and the Workspace

**Decision**: Bridge external handoffs into the Compose workspace through a lightweight one-shot coordinator that publishes normalized inbound requests to the workspace layer.

**Rationale**:

- `MainActivity` is already the single manifest entry point and the workspace is already the primary surface.
- The user must see an external handoff as a new or resumed agent session, not as hidden background work.
- A coordinator lets us handle `onCreate()` and `onNewIntent()` consistently without teaching the runtime or UI to parse activity intents.

**Alternatives considered**:

- **Submit external requests directly from `MainActivity` into the runtime**: Rejected because it would make it harder for the workspace to show the incoming content and source state before or during execution.
- **Persist a full pending handoff queue in Room**: Rejected because the first milestone only needs lightweight, local, one-shot landing behavior.

## Decision 4: Treat Caller Identity as Best-Effort Metadata and Degrade Safely When It Is Missing

**Decision**: Collect the best available caller/source metadata at ingress time, using package/referrer/caller hints when available, and normalize missing or ambiguous identity as an unverified external caller.

**Rationale**:

- On current Android flows, a share target can reliably receive the payload through `ACTION_SEND`, but caller package identity may not always be available through every launch path.
- Android API 35 introduces `ComponentCaller`, but access to the caller package still depends on platform conditions such as share identity support or explicit caller behavior.
- Safe degradation fits the constitution: low-risk text handoffs can still land in the runtime, while restricted actions continue to depend on trust evaluation rather than hidden assumptions.

**Alternatives considered**:

- **Require a verified caller package for every external handoff**: Rejected because it would make the first share-based handoff path brittle or unusable in common cases.
- **Assume every external share is trusted if it reaches the activity**: Rejected because it would violate the safety boundary established in `004` and `005`.

## Decision 5: Reuse the Existing Runtime, Policy, Capability, and Audit Stack End-to-End

**Decision**: Once normalized, an external handoff becomes the same canonical `RuntimeRequest` shape used by the workspace and must flow through the existing context, planning, risk, policy, approval, capability, and audit stack.

**Rationale**:

- This is the key architectural promise of `007`: a new ingress path without a forked runtime.
- The user should see one consistent session model, whether the request originated inside the workspace or outside the app.
- Existing audit and trust surfaces from `004` and `005` already provide the explanation scaffolding needed by `007`.

**Alternatives considered**:

- **Introduce a separate “external request” execution lane**: Rejected because it would fragment behavior and make future governance harder.
- **Hide source/trust details from the workspace once normalized**: Rejected because `007` explicitly requires source and trust visibility.
