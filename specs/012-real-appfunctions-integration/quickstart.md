# Quickstart: Real AppFunctions Integration

## Goal

Verify that Mobile Claw no longer uses a seeded-only AppFunctions boundary and now includes a real AndroidX AppFunctions service plus framework-backed bridge behavior.

## Preconditions

- Build the app successfully with the new AppFunctions dependencies
- Confirm generated AppFunctions outputs exist in the build tree
- Run on a device or emulator that can at least build against API 36

## Walkthrough

1. Build the app.
2. Confirm the manifest contains a real AppFunction service registration.
3. Confirm generated AppFunctions metadata exists in the build output.
4. Open the workspace and inspect route/provider status for an AppFunctions-mapped capability.
5. Verify supported conditions show real AppFunctions wording.
6. Verify unsupported conditions fall back cleanly without a crash.

## Validation Notes

- The bridge should no longer report seeded-only AppFunctions availability.
- Workspace wording should distinguish real framework availability from fallback routing.
- Existing Intent/Share fallback must still work if AppFunctions is not supported.

## Implementation Validation

- `./gradlew :app:compileDebugKotlin` passed
- `./gradlew :app:assembleDebug` passed
- `./gradlew :app:lintDebug` passed
- Lint report: `/Users/youxuezhe/StudioProjects/mobile_claw/app/build/reports/lint-results-debug.html`
- Generated AppFunctions outputs observed:
  - `/Users/youxuezhe/StudioProjects/mobile_claw/app/build/generated/ksp/debug/kotlin/com/mobileclaw/app/runtime/appfunctions/$MobileClawAppFunctions_AppFunctionInvoker.kt`
  - `/Users/youxuezhe/StudioProjects/mobile_claw/app/build/generated/ksp/debug/kotlin/com/mobileclaw/app/runtime/appfunctions/$MobileClawAppFunctions_AppFunctionInventory.kt`
  - `/Users/youxuezhe/StudioProjects/mobile_claw/app/build/generated/ksp/debug/kotlin/com/mobileclaw/app/runtime/appfunctions/MobileClawAppFunctionsIds.kt`
- Merged manifest contains AndroidX AppFunctions services and `app_functions*.xml` properties:
  - `/Users/youxuezhe/StudioProjects/mobile_claw/app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml`
