# Feature Specification: Real AppFunctions Integration

**Feature Branch**: `012-real-appfunctions-integration`  
**Created**: 2026-04-09  
**Status**: Draft  
**Input**: User description: "Upgrade the seeded AppFunctions boundary into a real AndroidX AppFunctions integration when platform conditions allow."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Expose Real App Functions From Mobile Claw (Priority: P1)

As an Android user, I want Mobile Claw to expose a real AppFunctions surface, so assistants and compatible callers can discover real functions from the app instead of only relying on private fallback entry points.

**Why this priority**: This is the core product promise of `012`. Without real exposure, the AppFunctions layer remains a seeded stub.

**Independent Test**: Build the app with the AppFunctions service registered, then verify the app contains a real AppFunctions service and generated metadata.

**Acceptance Scenarios**:

1. **Given** the app is installed on a platform that supports AppFunctions, **When** the package is inspected, **Then** Mobile Claw exposes a real AppFunction service rather than only internal bridge metadata.
2. **Given** the build runs with AppFunctions-enabled dependencies, **When** the app compiles, **Then** generated AppFunctions metadata and service wiring are present.

---

### User Story 2 - Use Real Framework-Backed Discovery In The Bridge (Priority: P2)

As a developer and user, I want the capability bridge to consult real AppFunctions availability on supported devices, so provider selection reflects actual framework state instead of seeded assumptions.

**Why this priority**: Exposure alone is not enough; the runtime should stop pretending AppFunctions are available and instead inspect real framework availability where possible.

**Independent Test**: On a supported build, trigger capability resolution and verify the bridge marks real AppFunctions-backed providers available or unavailable based on actual framework conditions.

**Acceptance Scenarios**:

1. **Given** the device supports AppFunctions and Mobile Claw has exposed compatible functions, **When** the bridge resolves a mapped capability, **Then** it returns a provider descriptor backed by real AppFunctions availability.
2. **Given** the device does not support AppFunctions, **When** capability resolution runs, **Then** the bridge degrades cleanly to existing fallbacks without crashing.

---

### User Story 3 - Keep AppFunctions Product State Visible And Honest (Priority: P3)

As a user, I want Mobile Claw to clearly reflect whether AppFunctions integration is real, supported, or unavailable on my device, so the product does not overclaim platform capabilities.

**Why this priority**: Once the project upgrades from seeded to real integration, explainability matters more because platform support will vary by device and SDK level.

**Independent Test**: Open the workspace on supported and unsupported conditions and verify AppFunctions status remains visible and truthful.

**Acceptance Scenarios**:

1. **Given** the device or build does not support AppFunctions, **When** the workspace surfaces route status, **Then** the wording explains that AppFunctions are unavailable and fallback routing is being used.
2. **Given** real AppFunctions metadata is present, **When** the runtime surfaces provider status, **Then** the wording distinguishes real framework-backed availability from fallback routing.

---

### Edge Cases

- What happens when the app is built with AppFunctions support but runs on a device below the required platform level?
- What happens when generated AppFunctions metadata exists but no mapped capability is currently enabled?
- What happens when the service is registered correctly but execution permissions are not granted to the caller?
- What happens when AppFunctions discovery fails and the runtime must still continue through Intent/Share fallback?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST replace the seeded-only AppFunctions boundary with a real AndroidX AppFunctions integration path.
- **FR-002**: The app MUST register at least one real AppFunction service backed by AndroidX AppFunctions service APIs.
- **FR-003**: The build MUST include the required AppFunctions runtime/service/compiler integration needed to generate metadata for exposed functions.
- **FR-004**: The capability bridge MUST use framework-backed or AndroidX-backed AppFunctions availability checks on supported devices.
- **FR-005**: The bridge MUST keep the existing ordered fallback behavior when real AppFunctions are not available.
- **FR-006**: The app MUST expose at least one meaningful Mobile Claw function through the AppFunctions service.
- **FR-007**: User-facing provider state MUST distinguish real AppFunctions availability from fallback-only behavior.
- **FR-008**: User-facing AppFunctions status messaging MUST support English and Simplified Chinese automatically via device locale.
- **FR-009**: This milestone MUST not require third-party cross-app discovery to be fully implemented for arbitrary external apps.
- **FR-010**: This milestone MUST remain compatible with the existing runtime, policy, and capability contracts.

### Key Entities *(include if feature involves data)*

- **AppFunctionExposureDefinition**: Declared Mobile Claw function exposed through AndroidX AppFunctions.
- **AppFunctionFrameworkStatus**: Runtime-visible state describing whether AppFunctions are supported, registered, enabled, or unavailable.
- **AppFunctionCapabilityMapping**: Mapping between runtime capability ids and exposed/discovered AppFunction identifiers.
- **AppFunctionExecutionBridgeResult**: Result of framework-backed AppFunctions availability or execution probing used by the runtime bridge.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The app builds successfully with real AndroidX AppFunctions dependencies and generated metadata.
- **SC-002**: At least one Mobile Claw function is exposed through a registered AppFunctions service.
- **SC-003**: Capability resolution uses real AppFunctions-backed availability checks on supported devices and degrades cleanly on unsupported devices.
- **SC-004**: Workspace-visible AppFunctions wording remains accurate in both English and Simplified Chinese.

## Assumptions

- The milestone can target AndroidX AppFunctions with `compileSdk/targetSdk` uplift where needed.
- The first real AppFunctions integration only needs to expose a small set of Mobile Claw functions rather than a full runtime surface.
- Framework-backed discovery may initially focus on self-package functions before generalized third-party AppFunctions interop.
- Existing Intent/Share fallback remains the safety net for unsupported devices and capabilities.
