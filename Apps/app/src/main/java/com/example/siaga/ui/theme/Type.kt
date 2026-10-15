package com.example.siaga.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.siaga.R

/**
 * Inter — font yang dipakai desain Figma SIAGA.
 * Filenya font variable (satu file untuk semua ketebalan), jadi tiap weight
 * dibuat dari file yang sama lewat variation settings.
 */
@OptIn(ExperimentalTextApi::class)
val Inter = FontFamily(
    Font(
        R.font.inter,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.inter,
        FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.inter,
        FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.inter,
        FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

/**
 * Tipografi SIAGA — semua gaya memakai Inter tanpa letter spacing, mengikuti
 * desain Figma. Ukuran sp diambil dari yang sudah dipakai layar sekarang, jadi
 * menerapkannya lewat MaterialTheme.typography tidak mengubah tampilan.
 *
 * Catatan: [Typography.bodyLarge] sengaja dibiarkan seperti bawaan template.
 * Layar SIAGA tidak mewarisinya (SiagaBackdrop menimpa LocalTextStyle dengan
 * Inter tanpa letter spacing), dan satu-satunya pemakainya adalah komponen
 * transient Material 3 (mis. Snackbar) — mengubahnya justru akan menggeser
 * tampilan komponen itu. Gaya yang benar-benar dipakai aplikasi didefinisikan
 * eksplisit di bawah ini.
 */
val Typography = Typography(
    // Judul halaman (header SIAGA-03): besar & tebal.
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 27.467.sp,
        letterSpacing = 0.sp,
    ),
    // Subtitle header & teks sekunder.
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 13.733.sp,
        letterSpacing = 0.sp,
    ),
    // Teks isi & input.
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.677.sp,
        letterSpacing = 0.sp,
    ),
    // Label kecil: item nav bar & chip.
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 9.508.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
)