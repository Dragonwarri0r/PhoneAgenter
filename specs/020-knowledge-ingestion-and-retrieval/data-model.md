# Data Model: Knowledge Ingestion And Retrieval

## KnowledgeAsset

Managed durable knowledge source visible in the Knowledge area.

**Fields**
- `knowledgeAssetId`: stable asset identity
- `title`: user-facing label
- `sourceType`: file, folder, document collection, or similar local source category
- `provenanceLabel`: where the asset came from
- `availabilityState`: healthy, stale, partial, excluded, missing, or similar state
- `ingestionSummary`: concise ingestion status
- `lastKnownFreshness`: user-visible freshness marker

## KnowledgeIngestionRecord

User-visible ingestion state for a managed knowledge asset.

**Fields**
- `knowledgeAssetId`
- `ingestionState`: pending, ingesting, ready, partial, failed
- `lastIngestedAt`
- `lastErrorSummary`
- `currentScopeSummary`

## RetrievalCitation

Explainable link between request-time support and a source asset.

**Fields**
- `citationId`
- `knowledgeAssetId`
- `sourceLabel`
- `relevanceSummary`
- `redactionState`
- `requestId`

## RetrievalSupportSummary

Concise request-time explanation of why a knowledge asset contributed.

**Fields**
- `requestId`
- `knowledgeAssetId`
- `summary`
- `confidenceLabel`
- `provenanceLabel`
- `isVisibleInline`

## KnowledgeAvailabilityState

Managed visibility and contribution state for a knowledge asset.

**Fields**
- `knowledgeAssetId`
- `state`
- `supportsRefresh`
- `supportsRetrievalInclusionChange`
- `reasonIfUnavailable`

## ManagedKnowledgeEntry

Control/detail-surface summary for one knowledge asset.

**Fields**
- `knowledgeAssetId`
- `title`
- `statusLine`
- `freshnessLine`
- `usageSummary`
- `detailRoute`
