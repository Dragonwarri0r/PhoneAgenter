# Real AppFunctions Contract

## Intent

`012` upgrades Mobile Claw from a seeded AppFunctions abstraction to a real AndroidX AppFunctions integration.

The contract is:

1. Mobile Claw exposes a real AppFunction service
2. A small set of Mobile Claw functions is published through generated metadata
3. The capability bridge probes real AppFunctions-backed availability where supported
4. Unsupported devices fall back cleanly to existing routing

## Exposure Contract

Mobile Claw should expose at least one real function per chosen exposure slice.

Recommended first slice:

- reply draft generation
- portability summary export

## Bridge Contract

The bridge should map runtime capability ids to AppFunctions identifiers and package names.

For supported devices:

- probe real availability using AndroidX AppFunctions
- surface truthful provider status

For unsupported devices:

- mark AppFunctions as unavailable
- continue through Intent/Share fallback

## Workspace Contract

Runtime and workspace status should distinguish:

- `real_appfunctions_available`
- `real_appfunctions_unavailable`
- `fallback_in_use`

## Safety Rules

- Real AppFunctions support must not bypass policy or approval decisions.
- Unsupported devices must not pretend AppFunctions are available.
- Service exposure should stay intentionally small in this milestone.
