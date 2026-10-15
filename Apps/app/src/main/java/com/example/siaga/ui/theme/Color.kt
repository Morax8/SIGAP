package com.example.siaga.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Palet inti SIAGA (dari Figma "GH-7.0").
//
// Nilai hex di bawah IDENTIK dengan desain lama; yang berubah hanyalah cara
// pakainya — warna semantik kini dipetakan ke slot Material 3 di Theme.kt dan
// diambil layar lewat MaterialTheme.colorScheme, bukan val top-level yang
// tersebar. Karena hex-nya sama, hasil render tetap pixel-identik.
// ============================================================================

/** Gradient latar gelap: dari ungu kehitaman ke merah kehitaman. */
val SiagaBackgroundStart = Color(0xFF0D0710)
val SiagaBackgroundEnd = Color(0xFF170A0A)

/** Merah darurat — identitas aplikasi, dipakai di tombol mic & aksen kritis. */
val SiagaRed = Color(0xFFC0392B)

/** Teks utama (nyaris putih) di atas latar gelap. */
val SiagaTextPrimary = Color(0xFFF0F0F0)

/** Teks redup untuk subtitle / teks sekunder. */
val SiagaTextMuted = Color(0xFF888888)

// ============================================================================
// Warna ekstensi tema.
//
// Efek kaca (glassmorphism) dan teks berjenjang TIDAK bisa masuk slot
// colorScheme Material 3: slot itu semantik & opak, sedangkan warna-warna ini
// transparan (alpha) dan ditumpuk di atas gradient. Jadi semuanya didefinisikan
// sebagai named val di sini dan dipakai bersama oleh semua layar — tidak ada
// lagi magic number Color(0x...) yang tersebar di screens.
// ============================================================================

/** Isian kartu/panel kaca: putih 8% di atas gradient. */
val GlassFill = Color(0x14FFFFFF)

/** Garis tepi kaca: putih 18%. */
val GlassBorder = Color(0x2EFFFFFF)

/** Isian kaca yang lebih padat: putih 6% (tombol overlay & Batalkan). */
val GlassFillStrong = Color(0x0FFFFFFF)

/** Teks berjenjang, dari yang paling terang ke paling redup. */
val SiagaTextDim = Color(0xFF555555)
val SiagaTextNavIdle = Color(0xFF666666)
val SiagaTextDimmer = Color(0xFF444444)

/** Cincin progres mic di layar darurat (merah pada beberapa tingkat alpha). */
val SiagaRingActiveOuter = Color(0x40C0392B) // merah 25%
val SiagaRingActiveInner = Color(0x66C0392B) // merah 40%
val SiagaRingIdle = Color(0x1FC0392B)        // redup saat tidak merekam

/** Lingkaran latar ikon izin di onboarding: merah 15%. */
val SiagaIconCircle = Color(0x26C0392B)

/** Cincin dekoratif lencana mic di layar Selamat Datang. */
val SiagaWelcomeRingOuter = Color(0xFF1E1E1E)
val SiagaWelcomeRingInner = Color(0xFF2A1010)

/** Warna status koneksi di top bar layar darurat. */
val SiagaStatusConnected = Color(0xFF22C55E)  // hijau
val SiagaStatusConnecting = Color(0xFFF9A825) // kuning

/** Scrim (latar semi-transparan) di belakang overlay & papan ketik. */
val SiagaInputScrim = Color(0xF2101010)    // di belakang input ketik manual
val SiagaOverlayScrim = Color(0xF20D0710)  // di belakang overlay benturan
