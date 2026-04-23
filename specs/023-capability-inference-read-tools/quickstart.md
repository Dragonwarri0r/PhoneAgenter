# Quickstart: Capability Inference and Read Tools

## Preconditions

- The app is installed on a device where calendar permission can be granted or denied.
- The workspace has at least one runtime-ready local model.
- The test device contains at least one upcoming calendar event for positive-path validation.

## Scenario 1: Explicit Calendar Lookup From Workspace

1. Grant calendar access.
2. Open the main workspace.
3. Ask a clear lookup question such as:
   - `What's on my calendar today?`
   - `Show my schedule this afternoon.`
4. Verify that:
   - the workspace does not stay on plain generic reply behavior
   - the runtime surfaces a calendar lookup path and why it was chosen
   - the result is bounded and conversational
   - no secondary control surface is required to complete the lookup

## Scenario 2: Truthful No-Results Outcome

1. Keep calendar access granted.
2. Ask for a bounded time period with no matching events.
3. Verify that:
   - the runtime still selects the explicit read path
   - the result clearly states that no matching items were found
   - no fabricated or stale event details appear

## Scenario 3: Permission-Unavailable Outcome

1. Revoke calendar access.
2. Ask the same lookup question again.
3. Verify that:
   - the workspace reports that the lookup path is unavailable
   - the reason is truthful and user-understandable
   - the workspace offers a clear recovery direction instead of pretending it succeeded

## Scenario 4: Conservative Freeform Inference

1. Use a normal conversational prompt such as `Help me phrase a reply to Alice`.
2. Verify that the runtime stays on the reply path.
3. Use an ambiguous prompt such as `Can you help me around my meetings tomorrow?`
4. Verify that the runtime either stays in reply mode or asks for clearer intent rather than silently forcing an unrelated capability.
5. Use a clearly action-oriented higher-risk prompt such as `Add lunch with Bob tomorrow at 1 PM`.
6. Verify that the runtime does not silently auto-execute and instead remains governed by the normal preview or confirmation behavior.

## Scenario 5: Extension-Friendly Read Capability Registration

1. Seed or enable a second read-oriented capability/provider registration in the local development build or fixture set.
2. Trigger a request that makes the second capability relevant.
3. Verify that:
   - the capability is represented through the same discovery and availability model
   - the workspace explanation uses the same tool identity and route language
   - no new feature-specific control path is required
