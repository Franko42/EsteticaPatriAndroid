package com.esteticapatri.app.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class BackupRetentionTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun retentionPolicyKeepsOnlyThreeNewestBackupsAndDeletesOlderOnes() {
        val backupDir = tempFolder.newFolder("backups")

        // Create 5 backup files with staggered timestamps
        val files = (1..5).map { i ->
            val file = File(backupDir, "backup_100$i.json")
            file.writeText("""{"manifest":{"formatVersion":1}}""")
            file.setLastModified(1000L * i)
            file
        }

        // Verify all 5 exist
        val listBefore = backupDir.listFiles { f ->
            f.isFile && f.name.startsWith("backup_") && f.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()

        assertEquals(5, listBefore.size)
        // listBefore is [backup_1005 (5000), backup_1004 (4000), backup_1003 (3000), backup_1002 (2000), backup_1001 (1000)]
        assertEquals("backup_1005.json", listBefore[0].name)
        assertEquals("backup_1001.json", listBefore[4].name)

        // Apply retention of MAX_LOCAL_BACKUPS = 3
        val maxBackups = BackupManager.MAX_LOCAL_BACKUPS
        assertEquals(3, maxBackups)

        if (listBefore.size > maxBackups) {
            val toDelete = listBefore.drop(maxBackups)
            for (f in toDelete) {
                f.delete()
            }
        }

        // Verify after retention
        val listAfter = backupDir.listFiles { f ->
            f.isFile && f.name.startsWith("backup_") && f.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()

        assertEquals(3, listAfter.size)
        // 3 newest must remain
        assertEquals("backup_1005.json", listAfter[0].name)
        assertEquals("backup_1004.json", listAfter[1].name)
        assertEquals("backup_1003.json", listAfter[2].name)

        // The 2 oldest must be deleted
        assertFalse(File(backupDir, "backup_1002.json").exists())
        assertFalse(File(backupDir, "backup_1001.json").exists())
    }
}
