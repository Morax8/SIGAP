package com.example.siaga.data

import android.content.Context
import android.provider.Settings
import com.example.siaga.data.model.EmergencyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Sinkronisasi riwayat laporan darurat ke backend (Supabase).
 *
 * CREATE: setiap laporan yang selesai dianalisis AI dikirim ke `POST /api/incidents`.
 * READ  : layar Riwayat mengambil daftar dari `GET /api/incidents?device_id=...`.
 *
 * Data lokal (DataStore) tetap dipakai sebagai cadangan kalau server tidak bisa
 * dihubungi, jadi app tetap jalan offline.
 */
class IncidentRepository(context: Context) {

    /** Android ID dipakai supaya riwayat tiap HP terpisah. */
    private val deviceId: String = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID,
    ) ?: "unknown"

    suspend fun save(
        clientId: String,
        report: String,
        instruction: String?,
        state: String?,
        hospitalName: String?,
        hospitalPhone: String?,
        latitude: Double?,
        longitude: Double?,
    ): Boolean = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("clientId", clientId)
            put("deviceId", deviceId)
            put("report", report)
            put("instruction", instruction)
            put("state", state)
            put("hospitalName", hospitalName)
            put("hospitalPhone", hospitalPhone)
            put("latitude", latitude)
            put("longitude", longitude)
        }

        val connection = (URL("${ServerConfig.BASE_URL}/api/incidents").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 5_000
            readTimeout = 8_000
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }

        try {
            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            connection.responseCode in 200..299
        } catch (_: Exception) {
            false
        } finally {
            connection.disconnect()
        }
    }

    suspend fun fetch(): List<Incident> = withContext(Dispatchers.IO) {
        val connection = (URL("${ServerConfig.BASE_URL}/api/incidents?device_id=$deviceId").openConnection() as HttpURLConnection).apply {
            connectTimeout = 5_000
            readTimeout = 8_000
        }

        val body = try {
            if (connection.responseCode != 200) error("HTTP ${connection.responseCode}")
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }

        val data = JSONObject(body).getJSONArray("data")
        (0 until data.length()).map { i ->
            val o = data.getJSONObject(i)
            Incident(
                id = o.getString("client_id"),
                timestamp = parseTimestamp(o.optString("created_at")),
                report = o.getString("report"),
                instruction = o.optString("instruction").takeIf { it.isNotBlank() && it != "null" },
                state = o.optString("state").takeIf { it.isNotBlank() && it != "null" }
                    ?.let { runCatching { EmergencyState.valueOf(it) }.getOrNull() },
                hospitalName = o.optString("hospital_name").takeIf { it.isNotBlank() && it != "null" },
                hospitalPhone = o.optString("hospital_phone").takeIf { it.isNotBlank() && it != "null" },
            )
        }
    }

    private fun parseTimestamp(iso: String): Long = runCatching {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(iso)?.time ?: System.currentTimeMillis()
    }.getOrDefault(System.currentTimeMillis())
}
