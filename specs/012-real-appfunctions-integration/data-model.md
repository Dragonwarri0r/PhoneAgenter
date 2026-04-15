# Data Model: Real AppFunctions Integration

## AppFunctionExposureDefinition

- `functionId`: framework-visible function identifier
- `capabilityId`: mapped runtime capability id
- `displayName`: user-visible label
- `exposureKind`: `reply`, `portability`, or other Mobile Claw function class
- `enabledByDefault`: whether the function should be enabled at install time

## AppFunctionCapabilityMapping

- `capabilityId`: runtime capability id
- `targetPackageName`: package that exposes the function
- `functionId`: AppFunctions identifier
- `providerLabel`: user-visible provider label

## AppFunctionFrameworkStatus

- `isPlatformSupported`: whether the current device/build supports framework-backed AppFunctions
- `isServiceRegistered`: whether Mobile Claw has a real AppFunctions service registered
- `isFunctionEnabled`: whether the mapped function is enabled
- `statusHeadline`: localized user-facing status
- `statusDetail`: localized explanation

## AppFunctionExecutionBridgeResult

- `capabilityId`: runtime capability id
- `providerId`: resolved provider id
- `availabilityState`: `available`, `unavailable`, or `restricted`
- `reason`: localized explanation
- `routeMetadata`: additional bridge metadata for workspace/debug visibility
