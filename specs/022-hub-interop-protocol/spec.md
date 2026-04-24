# Feature Specification: Hub Interop Protocol And Federated Capability Exchange

**Feature Branch**: `022-hub-interop-protocol`
**Created**: 2026-04-23
**Status**: Superseded (Docs Baseline Only)
**Input**: User description: "Analyze the roadmap and current codebase, then improve Mobile Claw's cross-app communication protocol by referencing MCP, A2A, and similar protocols. Mobile Claw should act as a managed local hub that can expose model-backed and internal capabilities to other apps, accept external capabilities into its own runtime, and govern those relationships through the existing management, policy, and memory framework."

> Note: This exploratory spec is no longer the implementation milestone.
> The agreed protocol work is now organized in docs under [Hub Interop Docs Index v1](/Users/youxuezhe/StudioProjects/mobile_claw/docs/hub-interop-docs-index-v1.md), and implementation starts from `024-shared-interop-contract`, `025-mobileclaw-interop-host`, and `026-interop-probe-app`.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Discover And Invoke Mobile Claw As A Governed Capability Hub (Priority: P1)

As a user or developer, I want other apps to discover and invoke Mobile Claw through a stable explicit contract instead of only through generic content sharing, so Mobile Claw can expose model-backed and internal capabilities as a true managed hub.

**Why this priority**: This is the core protocol gap in the current roadmap line. `ACTION_SEND` proves ingress, but it does not provide explicit capability discovery, invocation semantics, versioning, or governable callable surfaces.

**Independent Test**: Describe at least one externally callable Mobile Claw capability through the new protocol family, then verify a separate app can discover it, understand its invocation contract, and submit a governed request without relying on share-only heuristics.

**Acceptance Scenarios**:

1. **Given** an external app wants to use Mobile Claw as a callable hub, **When** it discovers Mobile Claw through the protocol, **Then** it can read a stable self-description of available capabilities, supported interaction styles, trust requirements, and compatibility information.
2. **Given** an external app invokes a covered Mobile Claw capability, **When** the request enters the runtime, **Then** it retains explicit caller identity, requested operation, scope intent, and governed execution semantics rather than being treated as an unstructured share payload.

---

### User Story 2 - Connect External Apps As Capability Providers Or Agent Peers (Priority: P2)

As a user or platform operator, I want Mobile Claw to accept external app capabilities and agent-like peers through the same interoperable family, so Mobile Claw can extend itself with outside tools, context, and delegated work instead of only receiving handoffs.

**Why this priority**: Mobile Claw's long-term value as a control hub depends not only on exposing its own abilities, but also on federating abilities from other apps into one governed runtime surface.

**Independent Test**: Describe at least one external provider or peer app through the new protocol family, then verify Mobile Claw can represent it as a managed connected surface with compatibility, trust, and enablement semantics.

**Acceptance Scenarios**:

1. **Given** an external app offers callable capabilities or request-time context, **When** Mobile Claw connects to it, **Then** the app can be represented as a discoverable managed integration with stable identity, capability summary, trust requirements, and availability state.
2. **Given** an external app is incompatible, untrusted, disabled, or only partially supported, **When** Mobile Claw evaluates or displays it, **Then** the system explains the limitation clearly instead of treating the app as fully available.

---

### User Story 3 - Integrate Through A Stable Shared Public Protocol Contract (Priority: P1)

As an Android app developer, I want a stable reusable public protocol contract that is isolated from any single host implementation, so I can integrate against supported discovery, invocation, authorization, task, and artifact semantics without reverse-engineering internal strings or linking against host-only implementation code.

**Why this priority**: The first platform slice is Android, and the protocol is intended to be consumed by external apps directly. If the shared contract is not isolated from Mobile Claw, the protocol will collapse into host-specific internals before it becomes reusable.

**Independent Test**: Build or describe a separate Android caller that uses the shared public protocol contract to discover Mobile Claw, invoke one governed capability, handle an authorization-required response, and continue without copying opaque implementation details from the host app.

**Acceptance Scenarios**:

1. **Given** an Android caller wants to integrate with Mobile Claw, **When** it uses the supported shared public protocol contract, **Then** it can construct requests, interpret responses, and route task or authorization handles without depending on host-internal runtime classes.
2. **Given** the caller and Mobile Claw do not fully align on supported contract or platform capabilities, **When** the caller attempts discovery or invocation, **Then** compatibility limits are surfaced through explicit public signals instead of silent failure or undefined behavior.

---

### User Story 4 - Validate Interop Through A Separate Protocol Consumer App (Priority: P2)

As a platform developer, I want a separate app to validate the shared protocol contract against Mobile Claw, so the protocol is proven through an external consumer path instead of only through host-internal tests or assumptions.

**Why this priority**: The protocol only becomes real when a separate app can consume it without privileged access to Mobile Claw internals. This validation app is also the first guardrail against host-specific leakage in the public contract.

**Independent Test**: Build or describe a separate protocol consumer app that depends only on the public protocol contract and Android binding layer, then verify it can complete discovery, authorization, governed invocation, and at least one task or artifact flow against Mobile Claw.

**Acceptance Scenarios**:

1. **Given** a separate protocol consumer app is installed alongside Mobile Claw, **When** it attempts to interact with Mobile Claw through the public contract, **Then** it can complete the supported interop flows without linking against Mobile Claw host-internal implementation classes.
2. **Given** the public contract drifts away from the actual Mobile Claw implementation, **When** the validation app exercises supported flows, **Then** incompatibility is surfaced as explicit contract or compatibility failure rather than hidden runtime breakage.

---

### User Story 5 - Coordinate Stateful Multi-Turn Or Long-Running Work Across App Boundaries (Priority: P3)

As a user, I want Mobile Claw to support stateful cross-app collaboration when work is too large for a single tool call, so other apps and future agent-like integrations can exchange task state, required follow-up input, progress, and artifacts without collapsing everything into one synchronous call.

**Why this priority**: Once Mobile Claw becomes a real hub, some cross-app interactions will be short-lived capability calls while others will be task-oriented and multi-step. The protocol needs to distinguish those shapes instead of overloading one ingress path.

**Independent Test**: Represent one stateful cross-app task that can move through submission, progress, input-required, and completion states, then verify the task can be tracked and governed without inventing a second execution model.

**Acceptance Scenarios**:

1. **Given** a cross-app request requires multi-turn coordination or long-running execution, **When** it is submitted through the protocol, **Then** the system can represent it using stable task identity, status, progress, and result semantics.
2. **Given** a cross-app task needs more user input, produces artifacts, or completes asynchronously, **When** Mobile Claw displays or governs it, **Then** the task remains explainable and manageable through the existing runtime, approval, audit, and control-center language.

---

### Edge Cases

- What happens when a connected app supports capability discovery but not stateful task collaboration, or vice versa?
- What happens when a caller and provider disagree on protocol version, capability shape, or supported interaction mode?
- What happens when a connected app exposes a capability that is discoverable but currently unavailable because trust, scope, policy, or prerequisites are missing?
- What happens when a long-running cross-app task is interrupted, requires follow-up input, or loses the external provider before completion?
- What happens when Mobile Claw exposes or consumes capabilities that carry sensitive memory, knowledge, attachment, or URI-grant context and the receiving side has narrower trust guarantees?
- What happens when two connected apps expose overlapping capabilities and Mobile Claw must choose, prioritize, or disable one without confusing the user?
- What happens when an Android caller supports the shared public protocol contract but cannot use one preferred platform adapter path and must continue through the baseline transport without changing protocol semantics?
- What happens when a caller upgrades its shared public protocol contract but the installed Mobile Claw host only supports an older protocol family version or narrower interaction set?
- What happens when the separate probe app accidentally starts depending on Mobile Claw host-internal implementation classes and no longer represents a real external consumer?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST define a stable cross-app protocol family for Mobile Claw that goes beyond generic share ingress and explicitly supports discovery, invocation, compatibility, trust, and governance semantics.
- **FR-002**: The protocol family MUST distinguish at minimum three interaction shapes: content handoff ingress, explicit callable capability invocation, and stateful task-oriented collaboration.
- **FR-003**: The protocol family MUST provide a stable self-description object for any connected app surface, including identity, supported interaction styles, available capabilities or skills, trust requirements, and compatibility metadata.
- **FR-004**: Mobile Claw MUST be representable through that self-description as an externally callable hub rather than only as a passive share target.
- **FR-005**: External apps that connect into Mobile Claw MUST also be representable through the same family as capability providers, context contributors, or agent-like peers when those roles apply.
- **FR-006**: Explicit callable capabilities MUST expose stable invocation contracts, including human-readable purpose, machine-readable input shape, expected output shape or artifact semantics, and governing scope or approval expectations.
- **FR-007**: The protocol family MUST support request-time context and resource exchange semantics so connected apps can share bounded external context without forcing Mobile Claw to treat every interaction as plain text.
- **FR-008**: The protocol family MUST support stateful task semantics for covered flows, including stable task identity, lifecycle state, progress or status updates, follow-up input requirements, and result or artifact reporting.
- **FR-009**: The system MUST preserve caller identity, source trust, scope intent, and compatibility information from protocol ingress through runtime planning, governance, approval, audit, and explainability surfaces.
- **FR-010**: Mobile Claw MUST let users inspect and manage connected app surfaces through the existing governed control model, including trust state, enablement state, availability, and recent interaction outcomes.
- **FR-011**: The protocol family MUST support explicit version negotiation or compatibility signaling so unsupported or downgraded interactions are detectable and explainable.
- **FR-012**: The protocol family MUST support extensibility so new capability kinds, context payloads, or task metadata can be added without redefining the core protocol each time.
- **FR-013**: The system MUST remain local-first and MUST NOT require mandatory cloud registration or remote orchestration to prove the first protocol slice.
- **FR-014**: User-facing labels and explanations for connected apps, callable capabilities, task state, compatibility, and trust outcomes MUST support English and Simplified Chinese automatically via device locale.
- **FR-015**: This milestone MUST build on the existing runtime, governance, tool, extension, memory, knowledge, and control-center foundations rather than introducing a parallel execution stack.
- **FR-016**: This milestone MUST treat the current share-based ingress as a compatibility path, not as the only long-term contract for governed cross-app capability exchange.
- **FR-017**: The first Android-facing slice of the protocol family MUST define a stable public Android binding for discovery, governed invocation, authorization, task status, and resource or artifact handles while preserving transport-agnostic protocol semantics at the family level.
- **FR-018**: The first Android-facing slice MUST isolate the shared protocol contract from any single host implementation so that Mobile Claw and external Android apps can both consume the same versioned public contract surface.
- **FR-019**: Android integrators MUST be able to consume a versioned reusable shared public protocol contract that exposes supported identifiers, request or response semantics, handle semantics, and compatibility metadata without depending on Mobile Claw host-internal runtime implementation classes.
- **FR-020**: Governed discovery, authorization, task, and artifact flows MUST remain available through an explicit baseline Android transport even when a preferred Android capability-invocation adapter path is unavailable, unsupported, or only partially available to a caller.
- **FR-021**: The shared public protocol contract MUST remain narrower than the host implementation, separating stable protocol and binding contracts from host-only execution, governance, model-serving, persistence, and UI internals.
- **FR-022**: The first Android slice MUST include a separate protocol consumer validation app that depends on the shared public protocol contract and exercises discovery, authorization, governed invocation, and at least one task or artifact flow against Mobile Claw.
- **FR-023**: The separate protocol consumer validation app MUST NOT depend on Mobile Claw host-internal runtime implementation classes, repositories, UI internals, or governance implementation details.

### Key Entities *(include if feature involves data)*

- **HubSurfaceDescriptor**: Stable self-description of a Mobile Claw or external app surface, including identity, supported interaction styles, capabilities or skills, trust requirements, and compatibility information.
- **CallableCapabilityDescriptor**: Explicit capability contract for tool-like invocation, including input shape, output or artifact expectations, scope semantics, and approval posture.
- **ConnectedAppRecord**: Managed representation of an external app or peer surface that Mobile Claw can discover, trust, enable, disable, or evaluate for compatibility.
- **InteropTaskRecord**: Stateful cross-app task record describing submission, progress, input-required, completion, failure, or interruption across app boundaries.
- **InteropArtifact**: Result payload produced by a cross-app task or invocation, such as text, file-like outputs, structured summaries, or other governed artifacts.
- **CompatibilitySignal**: Versioning and feature-compatibility metadata used to explain whether two app surfaces can safely interoperate and what was downgraded when they cannot fully align.
- **SharedPublicProtocolContract**: Versioned developer-consumable contract surface that is isolated from any single host implementation and exposes supported public identifiers, handle semantics, request or response shapes, and compatibility metadata without exposing host-only implementation internals.
- **InteropProbeApp**: Separate protocol consumer app used to validate that the shared public protocol contract interoperates with Mobile Claw through real external app flows rather than host-internal shortcuts.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: At least one Mobile Claw capability can be explicitly discovered and invoked by another app without depending on share-only heuristics.
- **SC-002**: At least one external app surface can be represented inside Mobile Claw as a managed connected capability provider or peer with visible trust and compatibility state.
- **SC-003**: The protocol family can represent both short-lived callable interactions and stateful task-oriented interactions without collapsing them into the same request shape.
- **SC-004**: Users can inspect connected app trust, compatibility, and recent interaction state through the existing governed control model rather than through hidden debug-only flows.
- **SC-005**: Mobile Claw and at least one separate Android caller can both consume the same shared public protocol contract without manually copying opaque host implementation constants.
- **SC-006**: A separate protocol consumer app can complete at least one governed integration flow against Mobile Claw without linking against host-internal execution classes.

## Assumptions

- The current `ACTION_SEND`-based ingress remains useful as a backward-compatible handoff path, but it is insufficient as the primary contract for future hub-grade interoperability.
- Existing tool descriptors, governance records, runtime contribution contracts, knowledge management, and runtime control surfaces remain the foundation for protocol governance and explainability.
- The first protocol-hardening slice should establish the contract family and managed connected-app model before a dedicated validation app or broader ecosystem rollout.
- Short-lived callable capability exchange and longer-running task collaboration should share one protocol family but remain distinct interaction shapes.
- The first Android caller-facing shared protocol contract must be separately versioned and isolated from host implementation, even if the exact packaging and publication mechanics evolve during planning.
