package com.example.siaga.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Sistem bentuk SIAGA.
 *
 * Radius diambil dari yang SUDAH dipakai desain Figma sekarang, jadi memetakan
 * komponen ke MaterialTheme.shapes tidak mengubah tampilan sedikit pun:
 * - extraSmall : sudut input & tombol aksi kecil   (10.564dp)
 * - small      : sudut chip (lokasi / Data Diri)    (13.733dp)
 * - medium     : sudut kartu kaca & tombol onboarding (16.903dp)
 * - large      : sudut tombol pill (Batalkan)       (23.241dp)
 * - extraLarge : sudut nav bar bawah                (25.354dp)
 */
val SiagaShapes = Shapes(
    extraSmall = RoundedCornerShape(10.564.dp),
    small = RoundedCornerShape(13.733.dp),
    medium = RoundedCornerShape(16.903.dp),
    large = RoundedCornerShape(23.241.dp),
    extraLarge = RoundedCornerShape(25.354.dp),
)

/**
 * Radius sudut atas nav bar bawah.
 *
 * Nav bar hanya membulatkan dua sudut atas (topStart/topEnd), jadi tidak bisa
 * langsung memakai [SiagaShapes.extraLarge] yang membulatkan keempat sudut —
 * itu akan mengubah tampilan. Nilainya sama dengan extraLarge (25.354dp) dan
 * dipusatkan di sini supaya tidak jadi magic number di komponen.
 */
val SiagaNavTopCorner = 25.354.dp
