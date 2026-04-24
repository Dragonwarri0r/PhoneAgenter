package com.mobileclaw.interop.contract

data class InteropVersion(
    val major: Int,
    val minor: Int,
) : Comparable<InteropVersion> {
    val value: String = "$major.$minor"

    override fun compareTo(other: InteropVersion): Int {
        return compareValuesBy(this, other, InteropVersion::major, InteropVersion::minor)
    }

    override fun toString(): String = value

    companion object {
        val CURRENT: InteropVersion = InteropVersion(1, 0)

        fun parse(raw: String?): InteropVersion? {
            val normalized = raw?.trim().orEmpty()
            if (normalized.isBlank()) return null
            val parts = normalized.split('.')
            val major = parts.getOrNull(0)?.toIntOrNull() ?: return null
            val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
            if (parts.size > 2) return null
            return InteropVersion(major = major, minor = minor)
        }
    }
}
