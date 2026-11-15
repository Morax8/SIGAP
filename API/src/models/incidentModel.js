// ==============================================
// FILE: src/models/incidentModel.js (CRUD laporan darurat)
// ==============================================
// CREATE: laporan darurat dikirim app setiap AI selesai menganalisis.
// READ  : daftar laporan per device, dipakai layar Riwayat.

const { pool } = require('../config/database');

// Simpan laporan baru. client_id dipakai sebagai kunci supaya retry
// (misal koneksi putus lalu dikirim ulang) tidak bikin data dobel.
async function createIncident({
    clientId,
    deviceId,
    report,
    instruction = null,
    state = null,
    hospitalName = null,
    hospitalPhone = null,
    latitude = null,
    longitude = null,
}) {
    const { rows } = await pool.query(
        `INSERT INTO incidents
            (client_id, device_id, report, instruction, state, hospital_name, hospital_phone, latitude, longitude)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
         ON CONFLICT (client_id) DO UPDATE SET
            report = EXCLUDED.report,
            instruction = COALESCE(EXCLUDED.instruction, incidents.instruction),
            state = COALESCE(EXCLUDED.state, incidents.state),
            hospital_name = COALESCE(EXCLUDED.hospital_name, incidents.hospital_name),
            hospital_phone = COALESCE(EXCLUDED.hospital_phone, incidents.hospital_phone),
            latitude = COALESCE(EXCLUDED.latitude, incidents.latitude),
            longitude = COALESCE(EXCLUDED.longitude, incidents.longitude)
         RETURNING client_id, created_at`,
        [clientId, deviceId, report, instruction, state, hospitalName, hospitalPhone, latitude, longitude]
    );
    return rows[0];
}

// Ambil laporan milik satu device, terbaru dulu.
async function listIncidents({ deviceId, limit = 50 }) {
    const { rows } = await pool.query(
        `SELECT client_id, created_at, report, instruction, state, hospital_name, hospital_phone, latitude, longitude
         FROM incidents
         WHERE device_id = $1
         ORDER BY created_at DESC
         LIMIT $2`,
        [deviceId, limit]
    );
    return rows;
}

module.exports = { createIncident, listIncidents };
