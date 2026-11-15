// ==============================================
// FILE: scripts/seed.js (Jalankan seed.sql ke Supabase)
// ==============================================
// Pakai: npm run seed
// Butuh DATABASE_URL di .env (connection string Supabase pooler).

require('dotenv').config();

const fs = require('fs');
const path = require('path');
const { Client } = require('pg');

(async () => {
    const sql = fs.readFileSync(path.join(__dirname, '..', 'seed.sql'), 'utf8');

    const client = new Client({
        connectionString: process.env.DATABASE_URL,
        ssl: { rejectUnauthorized: false },
    });

    await client.connect();
    await client.query(sql);

    const { rows } = await client.query('select count(*)::int as total from hospitals');
    console.log(`✅ Seed selesai. Total RS: ${rows[0].total}`);

    await client.end();
})().catch((err) => {
    console.error('❌ Seed gagal:', err.message);
    process.exit(1);
});
