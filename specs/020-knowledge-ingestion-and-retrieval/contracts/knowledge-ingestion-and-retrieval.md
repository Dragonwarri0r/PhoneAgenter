# Contract: Knowledge Ingestion And Retrieval

## Purpose

Define one managed local knowledge layer that supports ingestion, corpus visibility, retrieval explainability, and reversible knowledge management while staying separate from memory and workflow controls.

## Managed Corpus Contract

Every managed knowledge asset must provide:

- stable asset identity
- source identity and provenance
- ingestion state
- freshness or last-known update state
- availability state
- user-facing summary

## Ingestion Contract

The product must be able to ingest supported local knowledge sources into managed knowledge assets and preserve:

- source type
- ingestion status
- failure or partial-ingestion state when relevant
- revisitability through the Knowledge area

## Retrieval Contract

Request-time retrieval support must preserve:

- source-linked citations
- concise relevance summary
- redaction-aware presentation
- distinction from memory and ephemeral context

## Visibility Contract

The product must surface:

- concise retrieval support in current task flow
- deeper asset state and provenance in the Knowledge area
- clear limitation messaging for stale, partial, excluded, or missing assets

## Management Contract

Supported first-slice knowledge actions are reversible and non-destructive, such as:

- refresh
- include in retrieval
- exclude from retrieval
- inspect

Destructive purge and advanced diagnostics remain outside this milestone.

## Separation Contract

`020` must keep:

- durable knowledge corpus management separate from conversational memory
- request-time retrieval support separate from durable asset state
- Knowledge-area management separate from workflow/automation controls
