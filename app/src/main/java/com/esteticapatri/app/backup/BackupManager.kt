package com.esteticapatri.app.backup

import android.content.Context
import android.net.Uri
import com.esteticapatri.app.data.local.AppointmentEntity
import com.esteticapatri.app.data.local.ClientEntity
import com.esteticapatri.app.data.local.EsteticaDatabase
import com.esteticapatri.app.data.local.ServiceEntity
import com.esteticapatri.app.data.local.WorkingHoursEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class BackupManager(
    private val context: Context,
    private val db: EsteticaDatabase
) : BackupExporter, BackupRestorer {

    companion object {
        const val CURRENT_FORMAT_VERSION = 1
        const val APP_VERSION = "0.1.0"
        private const val MAX_LOCAL_BACKUPS = 14
        private const val GCM_IV_LENGTH_BYTES = 12
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val PBKDF2_ITERATIONS = 10000
    }

    private val backupDir: File
        get() = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }

    override suspend fun createLocalBackup(): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            val file = performCreateBackup(password = null)
            file.absolutePath
        }
    }

    suspend fun createLocalBackup(password: String?): Result<File> = runCatching {
        withContext(Dispatchers.IO) {
            performCreateBackup(password)
        }
    }

    override suspend fun exportBackup(destinationUri: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val uri = Uri.parse(destinationUri)
            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: error("No se pudo abrir el destino para exportar backup.")
            outputStream.use { out ->
                val jsonString = buildBackupJson(password = null)
                out.write(jsonString.toByteArray(StandardCharsets.UTF_8))
            }
        }
    }

    suspend fun exportBackupWithPassword(destinationUri: String, password: String?): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val uri = Uri.parse(destinationUri)
            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: error("No se pudo abrir el destino para exportar backup.")
            outputStream.use { out ->
                val jsonString = buildBackupJson(password)
                out.write(jsonString.toByteArray(StandardCharsets.UTF_8))
            }
        }
    }

    override suspend fun restoreFrom(sourceUri: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val uri = Uri.parse(sourceUri)
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: error("No se pudo leer el archivo de backup.")
            val content = inputStream.use { it.readBytes().toString(StandardCharsets.UTF_8) }
            performRestore(content, password = null)
        }
    }

    suspend fun restoreWithPassword(sourceUri: String, password: String?): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val uri = Uri.parse(sourceUri)
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: error("No se pudo leer el archivo de backup.")
            val content = inputStream.use { it.readBytes().toString(StandardCharsets.UTF_8) }
            performRestore(content, password)
        }
    }

    fun listLocalBackups(): List<File> {
        val files = backupDir.listFiles { f -> f.isFile && f.name.startsWith("backup_") && f.name.endsWith(".json") }
        return files?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    private suspend fun performCreateBackup(password: String?): File {
        val timestamp = System.currentTimeMillis()
        val tempFile = File(backupDir, "temp_backup_$timestamp.tmp")
        val finalFile = File(backupDir, "backup_$timestamp.json")

        val jsonString = buildBackupJson(password)
        FileOutputStream(tempFile).use { fos ->
            fos.write(jsonString.toByteArray(StandardCharsets.UTF_8))
            fos.flush()
        }

        // Validate that temp file can be parsed correctly
        val testContent = FileInputStream(tempFile).use { it.readBytes().toString(StandardCharsets.UTF_8) }
        val testJson = JSONObject(testContent)
        val manifestObj = testJson.getJSONObject("manifest")
        require(manifestObj.getInt("formatVersion") == CURRENT_FORMAT_VERSION) { "Archivo temporal inválido" }

        // Atomic rename
        if (!tempFile.renameTo(finalFile)) {
            tempFile.copyTo(finalFile, overwrite = true)
            tempFile.delete()
        }

        applyRetentionPolicy()
        return finalFile
    }

    private fun applyRetentionPolicy() {
        val backups = listLocalBackups()
        if (backups.size > MAX_LOCAL_BACKUPS) {
            val toDelete = backups.drop(MAX_LOCAL_BACKUPS)
            for (file in toDelete) {
                file.delete()
            }
        }
    }

    private suspend fun buildBackupJson(password: String?): String {
        // Collect current data snapshot
        val clients = db.clientDao().observeActive().first()
        val services = db.serviceDao().observeActive().first()
        val workingHours = db.workingHoursDao().getAll()
        val appointments = db.appointmentDao().observeBetween(0L, Long.MAX_VALUE).first()

        val dataObj = JSONObject().apply {
            put("clients", JSONArray().apply {
                clients.forEach { c ->
                    put(JSONObject().apply {
                        put("id", c.id)
                        put("name", c.name)
                        put("phone", c.phone ?: JSONObject.NULL)
                        put("normalizedPhone", c.normalizedPhone ?: JSONObject.NULL)
                        put("notes", c.notes ?: JSONObject.NULL)
                        put("createdAtEpochMillis", c.createdAtEpochMillis)
                        put("active", c.active)
                    })
                }
            })
            put("services", JSONArray().apply {
                services.forEach { s ->
                    put(JSONObject().apply {
                        put("id", s.id)
                        put("name", s.name)
                        put("durationMinutes", s.durationMinutes)
                        put("priceMinorUnits", s.priceMinorUnits ?: JSONObject.NULL)
                        put("notes", s.notes ?: JSONObject.NULL)
                        put("active", s.active)
                    })
                }
            })
            put("workingHours", JSONArray().apply {
                workingHours.forEach { wh ->
                    put(JSONObject().apply {
                        put("dayOfWeek", wh.dayOfWeek)
                        put("enabled", wh.enabled)
                        put("startMinuteOfDay", wh.startMinuteOfDay)
                        put("endMinuteOfDay", wh.endMinuteOfDay)
                    })
                }
            })
            put("appointments", JSONArray().apply {
                appointments.forEach { a ->
                    put(JSONObject().apply {
                        put("id", a.id)
                        put("clientId", a.clientId)
                        put("serviceId", a.serviceId)
                        put("startEpochMillis", a.startEpochMillis)
                        put("endEpochMillis", a.endEpochMillis)
                        put("status", a.status)
                        put("notes", a.notes ?: JSONObject.NULL)
                        put("createdAtEpochMillis", a.createdAtEpochMillis)
                        put("updatedAtEpochMillis", a.updatedAtEpochMillis)
                    })
                }
            })
        }

        val dataString = dataObj.toString()
        val dataBytes = dataString.toByteArray(StandardCharsets.UTF_8)
        val checksumSha256 = sha256Hex(dataBytes)

        val isEncrypted = !password.isNullOrEmpty()
        val payloadToStore: String

        if (isEncrypted) {
            val (salt, iv, ciphertext) = encryptData(dataBytes, password!!)
            val encObj = JSONObject().apply {
                put("salt", android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
                put("iv", android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP))
                put("ciphertext", android.util.Base64.encodeToString(ciphertext, android.util.Base64.NO_WRAP))
            }
            payloadToStore = encObj.toString()
        } else {
            payloadToStore = dataString
        }

        val root = JSONObject().apply {
            put("manifest", JSONObject().apply {
                put("formatVersion", CURRENT_FORMAT_VERSION)
                put("appVersionName", APP_VERSION)
                put("createdAtEpochMillis", System.currentTimeMillis())
                put("checksumSha256", checksumSha256)
                put("encrypted", isEncrypted)
            })
            put("payload", payloadToStore)
        }

        return root.toString()
    }

    private suspend fun performRestore(jsonContent: String, password: String?) {
        val root = JSONObject(jsonContent)
        val manifestObj = root.getJSONObject("manifest")
        val formatVersion = manifestObj.getInt("formatVersion")
        require(formatVersion == CURRENT_FORMAT_VERSION) {
            "Versión de respaldo incompatible: $formatVersion (esperada: $CURRENT_FORMAT_VERSION)"
        }

        val checksumSha256 = manifestObj.getString("checksumSha256")
        val isEncrypted = manifestObj.getBoolean("encrypted")
        val rawPayload = root.getString("payload")

        val dataBytes: ByteArray
        if (isEncrypted) {
            require(!password.isNullOrEmpty()) { "Este respaldo está cifrado y requiere contraseña." }
            val encObj = JSONObject(rawPayload)
            val salt = android.util.Base64.decode(encObj.getString("salt"), android.util.Base64.NO_WRAP)
            val iv = android.util.Base64.decode(encObj.getString("iv"), android.util.Base64.NO_WRAP)
            val ciphertext = android.util.Base64.decode(encObj.getString("ciphertext"), android.util.Base64.NO_WRAP)
            dataBytes = decryptData(ciphertext, password, salt, iv)
        } else {
            dataBytes = rawPayload.toByteArray(StandardCharsets.UTF_8)
        }

        val actualChecksum = sha256Hex(dataBytes)
        require(actualChecksum.equals(checksumSha256, ignoreCase = true)) {
            "Error de integridad: el checksum SHA-256 no coincide."
        }

        val dataObj = JSONObject(dataBytes.toString(StandardCharsets.UTF_8))

        // Paso obligatorio: generar backup preventivo antes de tocar la base de datos
        performCreateBackup(password = null)

        // Parse entities
        val parsedClients = parseClients(dataObj.getJSONArray("clients"))
        val parsedServices = parseServices(dataObj.getJSONArray("services"))
        val parsedWorkingHours = parseWorkingHours(dataObj.getJSONArray("workingHours"))
        val parsedAppointments = parseAppointments(dataObj.getJSONArray("appointments"))

        // Restore inside transaction
        db.runInTransaction {
            kotlinx.coroutines.runBlocking {
                // Insert or update all entities
                parsedClients.forEach { c -> db.clientDao().insert(c) }
                parsedServices.forEach { s -> db.serviceDao().insert(s) }
                if (parsedWorkingHours.isNotEmpty()) {
                    db.workingHoursDao().insertAll(parsedWorkingHours)
                }
                parsedAppointments.forEach { a -> db.appointmentDao().insert(a) }
            }
        }
    }

    private fun parseClients(arr: JSONArray): List<ClientEntity> = (0 until arr.length()).map { i ->
        val o = arr.getJSONObject(i)
        ClientEntity(
            id = o.getLong("id"),
            name = o.getString("name"),
            phone = if (o.isNull("phone")) null else o.getString("phone"),
            normalizedPhone = if (o.isNull("normalizedPhone")) null else o.getString("normalizedPhone"),
            notes = if (o.isNull("notes")) null else o.getString("notes"),
            createdAtEpochMillis = o.optLong("createdAtEpochMillis", System.currentTimeMillis()),
            active = o.optBoolean("active", true)
        )
    }

    private fun parseServices(arr: JSONArray): List<ServiceEntity> = (0 until arr.length()).map { i ->
        val o = arr.getJSONObject(i)
        ServiceEntity(
            id = o.getLong("id"),
            name = o.getString("name"),
            durationMinutes = o.getInt("durationMinutes"),
            priceMinorUnits = if (o.isNull("priceMinorUnits")) null else o.getLong("priceMinorUnits"),
            notes = if (o.isNull("notes")) null else o.getString("notes"),
            active = o.optBoolean("active", true)
        )
    }

    private fun parseWorkingHours(arr: JSONArray): List<WorkingHoursEntity> = (0 until arr.length()).map { i ->
        val o = arr.getJSONObject(i)
        WorkingHoursEntity(
            dayOfWeek = o.getInt("dayOfWeek"),
            enabled = o.getBoolean("enabled"),
            startMinuteOfDay = o.getInt("startMinuteOfDay"),
            endMinuteOfDay = o.getInt("endMinuteOfDay")
        )
    }

    private fun parseAppointments(arr: JSONArray): List<AppointmentEntity> = (0 until arr.length()).map { i ->
        val o = arr.getJSONObject(i)
        AppointmentEntity(
            id = o.getLong("id"),
            clientId = o.getLong("clientId"),
            serviceId = o.getLong("serviceId"),
            startEpochMillis = o.getLong("startEpochMillis"),
            endEpochMillis = o.getLong("endEpochMillis"),
            status = o.getString("status"),
            notes = if (o.isNull("notes")) null else o.getString("notes"),
            createdAtEpochMillis = o.optLong("createdAtEpochMillis", System.currentTimeMillis()),
            updatedAtEpochMillis = o.optLong("updatedAtEpochMillis", System.currentTimeMillis())
        )
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun encryptData(data: ByteArray, password: String): Triple<ByteArray, ByteArray, ByteArray> {
        val random = SecureRandom()
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val iv = ByteArray(GCM_IV_LENGTH_BYTES).also { random.nextBytes(it) }

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val ciphertext = cipher.doFinal(data)
        return Triple(salt, iv, ciphertext)
    }

    private fun decryptData(ciphertext: ByteArray, password: String, salt: ByteArray, iv: ByteArray): ByteArray {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        return cipher.doFinal(ciphertext)
    }
}
