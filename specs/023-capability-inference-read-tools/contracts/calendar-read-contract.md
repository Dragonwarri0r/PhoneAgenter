# Contract: Calendar Read

## Purpose

Define the first concrete explicit read-tool contract for looking up bounded calendar information from the workspace.

## Request Shape

| Field | Required | Description |
|---|---|---|
| `capability_id` | Yes | Must be `calendar.read` |
| `query_text` | Yes | User lookup request |
| `time_window` | Yes | Bounded lookup window such as today or next 7 days |
| `result_limit` | Yes | Maximum number of calendar items to return |
| `permission_requirement` | Yes | Calendar access requirement summary |
| `route_explanation` | Yes | Why calendar lookup was chosen |

## Result Shape

| Field | Description |
|---|---|
| `outcome_kind` | `matched`, `no_results`, `unavailable`, or `failed` |
| `event_summaries` | Conversational summaries of matching events |
| `result_count` | Number of returned events |
| `user_message` | Final workspace-visible outcome |
| `audit_summary` | Stable summary for runtime audit trails |

## Behavioral Rules

1. Calendar lookup must stay distinct from calendar creation or modification.
2. Calendar lookup must use bounded time windows and bounded result counts.
3. When no events match, the result must explicitly say so.
4. When access is unavailable, the result must explain the limitation and possible recovery.
5. The workspace must expose why calendar lookup was selected when it is used.
