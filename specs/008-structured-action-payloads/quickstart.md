# Quickstart: Structured Action Payloads

## Purpose

Use this guide to validate milestone `008` as the first structured execution layer above the current runtime, policy, and capability stack.

## Preconditions

- `004`, `005`, and `007` are already implemented and working
- The workspace can execute or preview message, calendar, and share actions through the current runtime path
- Source/trust and route explanation are already visible in runtime status or audit surfaces

## Validation Flow 1: Structured Message Draft

1. Submit a request such as `Send Alice a message saying I will be 10 minutes late.`
2. Verify:
   - the runtime produces a structured message payload
   - the workspace or approval surface shows extracted message fields
   - execution no longer depends only on the raw request text

## Validation Flow 2: Structured Calendar Write

1. Submit a request such as `Create a meeting tomorrow at 3pm called Project Check-in.`
2. Verify:
   - the runtime produces a structured calendar payload
   - title/time hints appear as structured fields
   - missing or partial scheduling data produces a safer path than blind execution

## Validation Flow 3: Structured External Share

1. Submit a request such as `Share this summary with the team channel.`
2. Verify:
   - the runtime produces a structured share payload
   - the outbound share content is visible in preview or status
   - downstream share execution uses the structured share content

## Validation Flow 4: Partial Payload Safety

1. Submit a request with an ambiguous target, such as `Send this to them later.`
2. Verify:
   - the runtime marks the structured payload as partial or insufficient
   - the system does not silently execute a high-impact action
   - the reason is visible in runtime or audit surfaces

## Notes

- `008` is intentionally heuristic-first; completeness state matters more than perfect extraction.
- This milestone is complete when supported actions stop behaving like raw prompt passthrough and start behaving like explicit execution payloads.
- Validation completed on 2026-04-09 with:
  - `./gradlew :app:compileDebugKotlin`
  - `./gradlew :app:assembleDebug`
  - `./gradlew :app:lintDebug`
- Implementation notes:
  - `message.send`, `calendar.write`, and `external.share` now normalize into structured payloads before provider execution.
  - The workspace context card and approval flow now surface structured field lines, completeness, and warnings.
  - Android intent/share execution now prefers structured payload fields instead of reconstructing only from the raw request string.
