# Quickstart: Tool Contract Standardization

## Goal

Validate that Mobile Claw resolves common productivity actions through standardized tool descriptors, on-demand visibility, and consistent preview/governance language.

## Preconditions

- Build the app successfully
- Have at least one ready local model available
- Have governance and approval flows working from `004`, structured actions from `008`, and workspace IA from `013`

## Manual Validation Scenarios

1. **On-demand tool visibility**
   - Send a plain reply-oriented request
   - Confirm only the relevant reply tool path is surfaced
   - Trigger a request implying calendar, alarm, message, or share
   - Confirm the matching tool family becomes visible or explainably degraded

2. **Standardized preview**
   - Trigger at least three covered tool families such as calendar write, alarm set, and share outbound
   - Confirm preview uses stable tool identity, side-effect classification, and ordered field lines
   - Confirm write/dispatch actions do not skip preview semantics

3. **Governance and audit alignment**
   - Allow one covered tool and deny another
   - Confirm approval, denial, route explanation, and audit all reference the same tool identity and scope language

4. **Bilingual wording**
   - Run once in English and once in Simplified Chinese
   - Confirm tool family labels, side-effect wording, and preview explanations localize correctly

## Follow-up Notes

- This milestone is considered complete when the first productivity tool catalog no longer depends on scattered one-off capability mappings for user-visible preview and governance language.
- Device-side manual validation is still recommended for alarm and message dispatch flows because actual execution may depend on available Android handlers.
- Validation completed for `./gradlew :app:compileDebugKotlin`, `./gradlew :app:assembleDebug`, and `./gradlew :app:lintDebug`.
- Current implementation surfaces standardized tool identity, side-effect wording, scope lines, and visibility explanations in runtime status, approval, governance summaries, and audit records for the covered tool families.
