package com.example.siaga.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Skema warna SIAGA (Material 3).
 *
 * Setiap slot dipetakan ke palet SIAGA di Color.kt dengan nilai hex yang sama
 * persis seperti desain lama. Jadi tampilan tidak berubah — yang berubah hanya
 * caranya: warna kini dibaca lewat MaterialTheme.colorScheme di semua layar,
 * bukan val top-level yang tersebar.
 */
private val SiagaColorScheme = darkColorScheme(
    primary = SiagaRed,               // merah darurat = identitas aplikasi
    onPrimary = Color.White,
    secondary = SiagaRed,
    onSecondary = Color.White,
    background = SiagaBackgroundStart, // ujung atas gradient latar
    onBackground = SiagaTextPrimary,
    surface = SiagaBackgroundEnd,      // ujung bawah gradient / permukaan gelap
    onSurface = SiagaTextPrimary,
    surfaceVariant = SiagaBackgroundEnd,
    onSurfaceVariant = SiagaTextMuted,
    error = SiagaRed,                  // app darurat: merah tetap jadi identitas
    onError = Color.White,
    outline = GlassBorder,             // garis tepi kaca (putih 18%)
    outlineVariant = GlassBorder,
)

/**
 * Tema Material 3 aplikasi SIAGA.
 *
 * DARK-ONLY BY DESIGN: latar gelap dengan aksen merah darurat. Karena itu
 * [darkTheme] selalu true dan mode terang/gelap sistem tidak dibaca lagi, dan
 * [dynamicColor] dimatikan supaya warna wallpaper (Material You) tidak pernah
 * bocor ke identitas aplikasi. Kedua parameter dipertahankan hanya agar
 * pemanggil lama tetap kompabel; nilainya sengaja diabaikan.
 */
@Composable
fun SIAGATheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SiagaColorScheme,
        typography = Typography,
        shapes = SiagaShapes,
        content = content,
    )
}
