package com.example.siaga.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.siaga.R
import com.example.siaga.ui.theme.GlassBorder
import com.example.siaga.ui.theme.GlassFill
import com.example.siaga.ui.theme.SiagaNavTopCorner
import com.example.siaga.ui.theme.SiagaTextDim
import com.example.siaga.ui.theme.SiagaTextNavIdle

// Warna kaca & teks berjenjang dipusatkan di ui/theme/Color.kt (warna ekstensi
// tema); warna semantik diambil lewat MaterialTheme.colorScheme.

/** Halaman yang bisa dipilih lewat nav bar bawah. */
enum class SiagaTab(val label: String, val icon: Int) {
    DARURAT("Darurat", R.drawable.ic_nav_darurat),
    FASKES("Faskes", R.drawable.ic_nav_faskes),
    RIWAYAT("Riwayat", R.drawable.ic_nav_riwayat),
}

/**
 * Nav bar bawah dari desain SIAGA-05.
 *
 * Item "Respon" yang ada di Figma sengaja dibuang — itu pintu ke dashboard
 * responder (frame 07–11), persona terpisah yang di luar lingkup.
 */
@Composable
fun SiagaNavBar(
    selected: SiagaTab,
    onSelect: (SiagaTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Hanya dua sudut atas yang dibulatkan; lihat SiagaNavTopCorner di Shape.kt
    // (memakai shapes.extraLarge langsung akan membulatkan keempat sudut).
    val navShape = RoundedCornerShape(topStart = SiagaNavTopCorner, topEnd = SiagaNavTopCorner)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(95.077.dp)
            .background(GlassFill, navShape)
            .border(1.056.dp, GlassBorder, navShape)
            .padding(top = 12.677.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        SiagaTab.entries.forEach { tab ->
            NavItem(tab, selected == tab) { onSelect(tab) }
        }
    }
}

@Composable
private fun NavItem(tab: SiagaTab, selected: Boolean, onClick: () -> Unit) {
    val tint = if (selected) MaterialTheme.colorScheme.onSurface else SiagaTextNavIdle
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            Image(
                painter = painterResource(tab.icon),
                contentDescription = tab.label,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint),
                modifier = Modifier.size(23.241.dp),
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .offset(y = (-9.5).dp)
                        .size(4.226.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
        Spacer(Modifier.height(9.508.dp))
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = tint,
        )
    }
}

/** Judul halaman, mengikuti gaya header SIAGA-03. */
@Composable
fun SiagaPageHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.451.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = SiagaTextDim,
        )
    }
}

/** Kartu kaca, gaya sama dengan kartu izin SIAGA-03. */
@Composable
fun SiagaGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GlassFill, MaterialTheme.shapes.medium)
            .border(1.056.dp, GlassBorder, MaterialTheme.shapes.medium)
            .padding(16.903.dp),
        content = content,
    )
}
