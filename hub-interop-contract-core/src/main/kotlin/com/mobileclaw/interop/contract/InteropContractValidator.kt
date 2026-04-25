package com.mobileclaw.interop.contract

data class InteropValidationResult(
    val isValid: Boolean,
    val issues: List<String>,
)

object InteropContractValidator {
    fun validate(surface: HubSurfaceDescriptor): InteropValidationResult {
        val issues = buildList {
            if (surface.surfaceId.isBlank()) add("surface_id_missing")
            if (surface.displayName.isBlank()) add("surface_display_name_missing")
            if (InteropVersion.parse(surface.contractVersion) == null) add("surface_contract_version_invalid")
            if (surface.capabilities.any { it.capabilityId.isBlank() }) add("capability_id_missing")
            surface.capabilities.forEach { capability ->
                addAll(validate(capability).issues.map { "capability:${capability.capabilityId}:$it" })
            }
        }
        return InteropValidationResult(
            isValid = issues.isEmpty(),
            issues = issues,
        )
    }

    fun validate(capability: InteropCapabilityDescriptor): InteropValidationResult {
        val issues = buildList {
            if (capability.capabilityId.isBlank()) add("capability_id_missing")
            if (capability.displayName.isBlank()) add("capability_display_name_missing")
            if (capability.summary.isBlank()) add("capability_summary_missing")
            if (capability.inputSchemaVersion.isBlank()) add("capability_input_schema_version_missing")
            if (capability.outputArtifactTypes.any { it.isBlank() }) add("capability_output_artifact_type_blank")
            if (capability.requiredScopes.any { it.isBlank() }) add("capability_required_scope_blank")
        }
        return InteropValidationResult(
            isValid = issues.isEmpty(),
            issues = issues,
        )
    }

    fun validate(grant: InteropGrantDescriptor): InteropValidationResult {
        val issues = buildList {
            if (grant.scopes.any { it.isBlank() }) add("grant_scope_blank")
            if (grant.expiresAtEpochMillis != null && grant.expiresAtEpochMillis <= 0L) {
                add("grant_expires_at_invalid")
            }
        }
        return InteropValidationResult(
            isValid = issues.isEmpty(),
            issues = issues,
        )
    }

    fun validate(task: InteropTaskDescriptor): InteropValidationResult {
        val issues = buildList {
            if (task.displayName.isBlank()) add("task_display_name_missing")
            if (task.expiresAtEpochMillis != null && task.expiresAtEpochMillis <= 0L) {
                add("task_expires_at_invalid")
            }
            if (task.deletedAtEpochMillis != null && task.deletedAtEpochMillis <= 0L) {
                add("task_deleted_at_invalid")
            }
        }
        return InteropValidationResult(
            isValid = issues.isEmpty(),
            issues = issues,
        )
    }

    fun validate(artifact: InteropArtifactDescriptor): InteropValidationResult {
        val issues = buildList {
            if (artifact.displayName.isBlank()) add("artifact_display_name_missing")
            if (artifact.mimeType.isBlank()) add("artifact_mime_type_missing")
            if (artifact.artifactType.isBlank()) add("artifact_type_missing")
            if (artifact.expiresAtEpochMillis != null && artifact.expiresAtEpochMillis <= 0L) {
                add("artifact_expires_at_invalid")
            }
            if (artifact.deletedAtEpochMillis != null && artifact.deletedAtEpochMillis <= 0L) {
                add("artifact_deleted_at_invalid")
            }
        }
        return InteropValidationResult(
            isValid = issues.isEmpty(),
            issues = issues,
        )
    }
}
