package com.esteticapatri.app.updater

import org.junit.Assert.*
import org.junit.Test

class AppUpdateManagerTest {

    @Test
    fun `isNewerVersion detects higher patch version`() {
        assertTrue(AppUpdateManager.isNewerVersion("0.1.0", "v0.1.1"))
        assertTrue(AppUpdateManager.isNewerVersion("0.1.0", "0.1.1"))
    }

    @Test
    fun `isNewerVersion detects higher minor version`() {
        assertTrue(AppUpdateManager.isNewerVersion("0.1.0", "v0.2.0"))
        assertTrue(AppUpdateManager.isNewerVersion("0.1.9", "0.2.0"))
    }

    @Test
    fun `isNewerVersion detects higher major version`() {
        assertTrue(AppUpdateManager.isNewerVersion("0.9.9", "v1.0.0"))
        assertTrue(AppUpdateManager.isNewerVersion("1.0.0", "2.0.0"))
    }

    @Test
    fun `isNewerVersion returns false for same version`() {
        assertFalse(AppUpdateManager.isNewerVersion("0.1.0", "0.1.0"))
        assertFalse(AppUpdateManager.isNewerVersion("v0.1.0", "0.1.0"))
        assertFalse(AppUpdateManager.isNewerVersion("0.1.0", "v0.1.0"))
    }

    @Test
    fun `isNewerVersion returns false for older version`() {
        assertFalse(AppUpdateManager.isNewerVersion("0.2.0", "v0.1.9"))
        assertFalse(AppUpdateManager.isNewerVersion("1.0.0", "0.9.5"))
    }

    @Test
    fun `isNewerVersion handles missing patch numbers`() {
        assertTrue(AppUpdateManager.isNewerVersion("0.1", "0.1.1"))
        assertFalse(AppUpdateManager.isNewerVersion("0.1.0", "0.1"))
    }
}
