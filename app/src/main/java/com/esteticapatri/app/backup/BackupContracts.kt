package com.esteticapatri.app.backup

data class BackupManifest(
    val formatVersion: Int,
    val appVersionName: String,
    val createdAtEpochMillis: Long,
    val checksumSha256: String,
    val encrypted: Boolean
)

interface BackupExporter {
    suspend fun createLocalBackup(): Result<String>
    suspend fun exportBackup(destinationUri: String): Result<Unit>
}

interface BackupRestorer {
    /** Validar contenido y generar copia preventiva antes de reemplazar datos. */
    suspend fun restoreFrom(sourceUri: String): Result<Unit>
}
