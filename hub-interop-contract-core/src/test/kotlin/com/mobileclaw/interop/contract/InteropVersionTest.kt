package com.mobileclaw.interop.contract

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InteropVersionTest {
    @Test
    fun parseAcceptsMajorMinorVersions() {
        val version = InteropVersion.parse("1.2")

        assertEquals(1, version?.major)
        assertEquals(2, version?.minor)
        assertEquals("1.2", version?.value)
    }

    @Test
    fun parseTreatsMajorOnlyAsMinorZero() {
        val version = InteropVersion.parse("3")

        assertEquals(3, version?.major)
        assertEquals(0, version?.minor)
    }

    @Test
    fun parseRejectsInvalidValues() {
        assertNull(InteropVersion.parse(""))
        assertNull(InteropVersion.parse("a.b"))
        assertNull(InteropVersion.parse("1.2.3"))
    }

    @Test
    fun versionsCompareByMajorThenMinor() {
        assertTrue(InteropVersion(1, 1) > InteropVersion(1, 0))
        assertTrue(InteropVersion(2, 0) > InteropVersion(1, 9))
    }
}
