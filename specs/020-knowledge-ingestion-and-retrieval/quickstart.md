# Quickstart: Knowledge Ingestion And Retrieval

## Goal

Validate that Mobile Claw now treats durable local knowledge as a managed runtime layer with ingestion, retrieval visibility, and Knowledge-area management that remain distinct from memory and ephemeral request-time context.

## Preconditions

- Build the app successfully
- Have runtime contribution contracts from `019`
- Have control surfaces capable of showing Knowledge as its own management area
- Have at least one supported local source type wired into the first knowledge slice

## Manual Validation Scenarios

1. **Local ingestion**
   - Ingest a supported local knowledge source
   - Confirm the source appears as a managed knowledge asset with source identity and ingestion state

2. **Request-time retrieval visibility**
   - Trigger a request that uses ingested knowledge
   - Confirm the active task flow shows concise source-linked retrieval support rather than opaque background knowledge usage

3. **Knowledge-area management**
   - Open a managed knowledge asset from the Knowledge area
   - Confirm freshness, availability state, and supported reversible actions are visible and understandable

4. **Separation from memory**
   - Inspect one request that uses both memory and knowledge
   - Confirm the product distinguishes memory-derived context from durable knowledge support

5. **Locale**
   - Run once in English and once in Simplified Chinese
   - Confirm ingestion, freshness, retrieval, and limitation labels localize correctly

## Follow-up Notes

- 2026-04-22 validation snapshot:
  - `./gradlew :app:compileDebugKotlin`
  - `./gradlew :app:assembleDebug :app:lintDebug`
- This implementation wires the first managed-slice ingestion path for local text files and multi-document collections through the Knowledge area. Real-device walkthroughs were intentionally skipped for this pass.
- This milestone is considered complete when durable local knowledge no longer behaves like a hidden memory side path or a generic file import list.
- Advanced ranking quality work, destructive corpus operations, and broad connector expansion remain outside this first knowledge slice.
- Workflow and automation behavior remain out of scope here and should not be required to validate knowledge corpus value.
