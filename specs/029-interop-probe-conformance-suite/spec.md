# Feature Specification: Interop Probe Conformance Suite

**Feature Branch**: `029-interop-probe-conformance-suite`
**Created**: 2026-04-24
**Status**: Draft
**Input**: User description: "Upgrade the Interop Probe App from a manual demo client into a repeatable conformance suite that validates the public Hub Interop protocol and Mobile Claw trusted host behavior without depending on :app internals."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Run Manual Protocol Diagnostics (Priority: P1)

As a platform developer, I want the probe app to keep a clear manual mode, so I can inspect discovery, authorization, invocation, task, artifact, revoke, and report steps while debugging a host.

**Why this priority**: Manual diagnostics are still the fastest way to understand a failing host behavior before turning the same path into automated conformance.

**Independent Test**: Install the probe app with a compatible host and manually run discover, request authorization, refresh grant, invoke, poll task, load artifact, revoke, and export report using only public contract modules.

**Acceptance Scenarios**:

1. **Given** Mobile Claw is installed, **When** a developer runs manual discovery, **Then** the probe displays host package, authority, protocol version, supported methods, capabilities, compatibility, and availability.
2. **Given** a task produces an artifact, **When** a developer polls the task and loads the artifact, **Then** the probe timeline shows raw status codes, summaries, and artifact metadata without host-internal classes.

---

### User Story 2 - Run A Repeatable Conformance Matrix (Priority: P1)

As a protocol maintainer, I want the probe app to run an automated conformance matrix, so public contract and host regressions are visible without manually clicking every step.

**Why this priority**: The protocol is intended for third-party apps. A repeatable conformance matrix prevents docs, contract modules, host behavior, and probe expectations from drifting apart.

**Independent Test**: Tap one conformance action and verify the probe runs compatibility, spoof, authorization lifecycle, invocation, task, artifact, revoke, malformed request, downgraded version, and incompatible version cases into a pass/fail report.

**Acceptance Scenarios**:

1. **Given** a compatible Mobile Claw host is installed, **When** conformance mode runs, **Then** each supported case records pass/fail, raw status, expected status, message, and timeline entry.
2. **Given** the host rejects a malformed or incompatible request, **When** conformance mode runs, **Then** the failure is reported as an expected diagnostic outcome rather than a generic app crash.

---

### User Story 3 - Export Shareable Host Behavior Reports (Priority: P2)

As a team member reviewing Hub Interop behavior, I want a shareable conformance report, so I can compare host behavior across runs and discuss failures with enough evidence.

**Why this priority**: Public protocol work needs portable evidence. A good report lets us ask another model or teammate for review without rerunning the same device flow.

**Independent Test**: Run manual or conformance diagnostics, export the report, and verify it includes host identity, authority, protocol version, supported methods, supported capabilities, pass/fail matrix, raw statuses, failure reasons, and timeline.

**Acceptance Scenarios**:

1. **Given** a conformance run includes both passing and failing cases, **When** the report is exported, **Then** the report groups each case with expected status, actual status, failure reason, and timestamp.
2. **Given** the probe app is reviewed for dependencies, **When** build files and source imports are inspected, **Then** the probe still depends only on the shared contract modules and Android platform APIs, not `:app`.

---

### Edge Cases

- Host app is not installed or provider authority is missing.
- Host supports discovery but not all methods in the conformance matrix.
- Authorization remains pending and cannot be approved during a run.
- Host behavior is correct but calendar permission is unavailable.
- Claimed identity mismatch is rejected before invocation.
- Task completes without artifact, fails, expires, or belongs to another caller.
- Minor version downgrade is expected but major version incompatibility is required.
- Malformed request returns a public error instead of throwing into the probe UI.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The probe app MUST retain a manual mode for discover, request authorization, refresh grant, invoke, poll task, load artifact, revoke, and export report.
- **FR-002**: The probe app MUST add a conformance mode that runs a repeatable matrix of compatibility, authorization, spoof, invocation, task, artifact, revoke, malformed request, downgraded, and incompatible cases.
- **FR-003**: The probe app MUST validate `generate.reply` and bounded `calendar.read` flows when the host exposes them.
- **FR-004**: The probe app MUST intentionally send claimed identity mismatch diagnostics and report whether the host treats claimed metadata as untrusted.
- **FR-005**: Each conformance case MUST capture expected status, actual status, raw status value, compatibility state, message, and pass/fail result.
- **FR-006**: Reports MUST include host package, authority, protocol version, supported methods, supported capabilities, test matrix, pass/fail result, failure reason, raw status codes, and timeline.
- **FR-007**: Probe UI MUST surface unavailable, unsupported, pending, denied, downgraded, incompatible, malformed, task, and artifact outcomes without assuming happy-path success.
- **FR-008**: The probe app MUST continue to depend only on `:hub-interop-contract-core`, `:hub-interop-android-contract`, and normal Android/Compose dependencies; it MUST NOT depend on `:app`.
- **FR-009**: User-visible conformance output MUST support English and Simplified Chinese automatically via device locale where existing probe copy does.

### Key Entities *(include if feature involves data)*

- **ProbeManualFlow**: Ordered manual diagnostic actions and their latest outcomes.
- **ConformanceCase**: One expected protocol behavior with input, expected status, actual status, result, and explanation.
- **ConformanceRun**: A group of conformance cases executed against a host at a specific time.
- **ProbeReport**: Shareable report summarizing host identity, protocol support, matrix results, raw statuses, failures, and timeline.
- **SpoofDiagnosticScenario**: A conformance case that sends mismatched claimed identity metadata to validate host behavior.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A developer can run the full manual lifecycle from discovery through revoke and report export without using any `:app` implementation dependency.
- **SC-002**: A single conformance action produces a pass/fail matrix covering compatibility, authorization lifecycle, spoof diagnostics, invocation, task, artifact, revoke, malformed request, downgraded version, and incompatible version.
- **SC-003**: The exported report includes enough raw status and timeline data to reproduce or discuss host failures without rerunning the probe immediately.
- **SC-004**: Unit tests cover conformance result reduction, report formatting, and dependency-isolation assumptions for the probe client layer.

## Assumptions

- `027` has stabilized the public contract and status taxonomy.
- `028` has exposed trusted-host behavior for `generate.reply` and bounded `calendar.read`.
- The first conformance suite remains local/manual-device oriented; CI device automation can be added later.
- The probe is a validation tool, not a polished third-party client product.
