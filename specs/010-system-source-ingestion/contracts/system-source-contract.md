# Contract: System Source Ingestion

## Purpose

Define the internal contract for ingesting Android system sources into runtime-readable memory and exposing source status to the workspace.

## Source Descriptor Surface

### `SystemSourceRepository.observeDescriptors()`

Returns a small list of supported descriptors with permission and availability state.

Supported sources in this milestone:

- `contacts`
- `calendar`

## Ingestion Surface

### `SystemSourceIngestionService.ingestForRequest(request)`

Runs bounded, permission-aware ingestion for relevant sources and returns:

- updated source descriptors
- ingestion results
- current-request source contributions

## Runtime Context Surface

### `RuntimeContextPayload.systemSourceContributions`

The context payload should expose which sources contributed to the latest runtime context so the workspace can explain it.

## UI Surface

The workspace must be able to:

- request contacts/calendar permissions
- show current source availability
- show whether contacts/calendar contributed to the latest request
