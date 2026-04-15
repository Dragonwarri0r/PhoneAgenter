# Research: Real AppFunctions Integration

## Decision 1: Use AndroidX AppFunctions rather than platform-only APIs directly

**Decision**: Integrate `androidx.appfunctions`, `androidx.appfunctions:appfunctions-service`, and `androidx.appfunctions:appfunctions-compiler`.

**Rationale**: AndroidX provides the compatibility-oriented Jetpack surface Google currently recommends, while still bridging to the platform service on supported versions. This fits the project's adapter-first architecture better than hardwiring platform classes directly everywhere.

**Alternatives considered**:

- Stay on the seeded bridge only: rejected because it does not satisfy the milestone.
- Use only `android.app.appfunctions` APIs directly: rejected because it raises the coupling and makes the fallback story rougher.

## Decision 2: Upgrade build tooling for KSP and API 36

**Decision**: Move the app to `compileSdk/targetSdk 36` and add KSP because the AppFunctions compiler is KSP-based and the platform classes exist at API 36.

**Rationale**: This is the smallest honest change set that allows real integration without reflection hacks or fake stubs.

**Alternatives considered**:

- Keep `compileSdk 35`: rejected because real AppFunctions platform classes are unavailable there.
- Try to avoid KSP: rejected because the AndroidX compiler is the intended metadata generation path.

## Decision 3: Expose a very small self-package AppFunctions surface first

**Decision**: Expose a small Mobile Claw-owned function set and have the bridge probe self-package availability first.

**Rationale**: This proves end-to-end real integration while keeping the milestone independently demoable and avoiding cross-app discovery scope creep.

**Alternatives considered**:

- Full third-party AppFunctions interop immediately: rejected as too large.
- No exposed functions, discovery only: rejected because it would not prove real service integration.

## Decision 4: Preserve existing Intent/Share fallback unchanged

**Decision**: Keep current fallback ordering and only upgrade the AppFunctions branch from seeded to real.

**Rationale**: `012` should deepen the top-priority provider path, not destabilize the rest of the runtime.

**Alternatives considered**:

- Rework all capability routing at the same time: rejected because it would obscure what `012` actually proved.
