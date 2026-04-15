# Contract: Runtime Control Center

## Purpose

Provide one coherent in-app control surface that keeps conversation primary while letting users inspect runtime trace and enter supported artifact-management flows.

## Core Control Contract

The control center must provide:

- one primary entry from the active workspace
- one coherent runtime trace snapshot
- one managed-artifact entry list covering supported families
- clear explanations for unavailable or non-editable states

## Trace Contract

Every active or recent request shown in the control center must be representable through a shared trace model covering:

- source and caller trust
- selected tool or action path
- approval or denial status
- context contribution
- extension contribution
- current limiting conditions

## Managed Artifact Contract

Supported artifact families must be representable through one common entry shape:

- memory-management artifact
- caller governance artifact
- approval artifact
- extension artifact

Artifact entries may route to existing editors or sheets, but they must be presented through one control-center language first.

## Conversation-First Contract

The control center must not replace the built-in multimodal chat as the primary surface.
It must be reachable from the active session and dismissible without disrupting the conversation state.

## Availability Contract

If a section, contributor, or artifact is unavailable, degraded, incompatible, or not editable, the control center must show the limitation and avoid presenting dead controls.
