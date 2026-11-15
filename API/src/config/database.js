// ==============================================
// FILE: src/config/database.js
// ==============================================
// PostgreSQL (Supabase) connection pool.
// DATABASE_URL diambil dari Supabase > Project Settings > Database >
// Connection string > Connection pooling (host *.pooler.supabase.com).

const { Pool } = require('pg');

require('dotenv').config();

// ==============================================
// BUAT POOL KONEKSI
// ==============================================

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    // Supabase pakai sertifikat yang belum tentu ada di CA bundle lokal
    ssl: { rejectUnauthorized: false },
    max: 10,
    idleTimeoutMillis: 30000,
    connectionTimeoutMillis: 10000,
});

// ==============================================
// FUNGSI TEST KONEKSI
// ==============================================

async function connectDB() {
    try {
        await pool.query('select 1');
        console.log('✅ Database connected! (Supabase Postgres)');
        return true;
    } catch (error) {
        console.error('❌ Database connection failed:', error.message);
        return false;
    }
}


module.exports = { pool, connectDB };