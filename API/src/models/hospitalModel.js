// ==============================================
// FILE: src/models/hospitalModel.js (Query Database RS)
// ==============================================

const { pool } = require('../config/database');

// ==============================================
// DATA FALLBACK (cermin dari seed.sql)
// Dipakai kalau database tidak bisa dihubungi atau tabelnya kosong,
// supaya API tetap melayani data RS: list /api/hospitals buat UI
// Faskes, dan pencarian RS terdekat buat alur dispatch darurat.
// ==============================================
const FALLBACK_HOSPITALS = [
    { id: 1, name: 'RS Bethsaida Gading Serpong', phone: '021-29309999', address: 'Jl. Boulevard Raya Gading Serpong, Tangerang', latitude: -6.2394, longitude: 106.6281, district: 'Gading Serpong' },
    { id: 2, name: 'RS St. Carolus Summarecon Serpong', phone: '021-54220811', address: 'Jl. Gading Golf Boulevard, Gading Serpong, Tangerang', latitude: -6.2367, longitude: 106.6350, district: 'Gading Serpong' },
    { id: 3, name: 'Siloam Hospitals Kelapa Dua', phone: '021-80639900', address: 'Jl. Kelapa Dua Raya No.1001, Kelapa Dua, Tangerang', latitude: -6.2238, longitude: 106.6142, district: 'Kelapa Dua' },
    { id: 4, name: 'Eka Hospital BSD', phone: '021-25655555', address: 'Central Business District Lot IX, BSD City, Tangerang Selatan', latitude: -6.3009, longitude: 106.6683, district: 'BSD' },
    { id: 5, name: 'Omni Hospital Alam Sutera', phone: '021-29779999', address: 'Jl. Alam Sutera Boulevard No.Kav. 25, Tangerang Selatan', latitude: -6.2246, longitude: 106.6539, district: 'Alam Sutera' },
    { id: 6, name: 'RSUD Kabupaten Tangerang', phone: '021-5523333', address: 'Jl. Jend. Ahmad Yani No.9, Tangerang', latitude: -6.1767, longitude: 106.6303, district: 'Tangerang' },
];

// Haversine manual (rumus sama persis dengan query SQL di bawah),
// buat ngitung distance_km dari data fallback.
function haversineKm(lat1, lng1, lat2, lng2) {
    const toRad = (deg) => (deg * Math.PI) / 180;
    return 6371 * Math.acos(
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
        Math.cos(toRad(lng2) - toRad(lng1)) +
        Math.sin(toRad(lat1)) * Math.sin(toRad(lat2))
    );
}

// Cari RS terdekat dari data fallback. Logika prioritas district
// disamain dengan query SQL: kalo ada district, prioritasin RS
// di district yang sama; kalo nggak ada, cari yang terdekat.
function nearestFromFallback(lat, lng, district = null) {
    let candidates = FALLBACK_HOSPITALS;
    if (district) {
        const sameDistrict = candidates.filter((h) => h.district === district);
        if (sameDistrict.length > 0) candidates = sameDistrict;
    }
    let best = null;
    for (const h of candidates) {
        const distance_km = haversineKm(lat, lng, h.latitude, h.longitude);
        if (!best || distance_km < best.distance_km) {
            best = { ...h, distance_km };
        }
    }
    return best;
}

// FUNGSI CARI RS TERDEKAT
async function findNearestHospital(lat, lng, district = null) {
    let query = `
        SELECT 
            id, 
            name, 
            phone, 
            address,
            latitude, 
            longitude,
            district,
            (6371 * ACOS(
                COS(RADIANS(?)) * COS(RADIANS(latitude)) * 
                COS(RADIANS(longitude) - RADIANS(?)) + 
                SIN(RADIANS(?)) * SIN(RADIANS(latitude))
            )) AS distance_km
        FROM hospitals
        WHERE is_active = 1
    `;
    
    // Parameter buat query (lat, lng, lat)
    const params = [lat, lng, lat];
    
    // Kalo ada district, prioritasin RS di district yang sama
    if (district) {
        query += ` AND district = ?`;
        params.push(district);
    }
    
    // Urutkan dari jarak terdekat, ambil 1
    query += ` ORDER BY distance_km LIMIT 1`;
    
    try {
        // Eksekusi query
        const [rows] = await pool.execute(query, params);
        // Kalo gak ada RS di district yang sama, fallback: cari terdekat tanpa filter district
        if (!rows[0] && district) {
            return findNearestHospital(lat, lng, null);
        }
        // DB bisa dihubungi tapi hasilnya kosong (tabel kosong) -> data fallback
        return rows[0] || nearestFromFallback(lat, lng, district);
    } catch (error) {
        console.error('❌ Query error:', error.message);
        // DB tidak bisa dihubungi -> data fallback
        console.warn('⚠️ Pakai data RS fallback (database tidak tersedia)');
        return nearestFromFallback(lat, lng, district);
    }
}

// FUNGSI AMBIL SEMUA RS

async function getAllHospitals() {
    try {
        const [rows] = await pool.execute(
            'SELECT id, name, phone, address, latitude, longitude, district FROM hospitals WHERE is_active = 1'
        );
        // DB bisa dihubungi tapi tabel kosong -> data fallback
        return rows.length > 0 ? rows : FALLBACK_HOSPITALS;
    } catch (error) {
        console.error('❌ Query error:', error.message);
        // DB tidak bisa dihubungi -> data fallback
        console.warn('⚠️ Pakai data RS fallback (database tidak tersedia)');
        return FALLBACK_HOSPITALS;
    }
}

module.exports = { findNearestHospital, getAllHospitals };