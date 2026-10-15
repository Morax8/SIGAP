package com.example.siaga.ui.riwayat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siaga.data.Incident
import com.example.siaga.data.IncidentHistory
import com.example.siaga.data.model.EmergencyState
import com.example.siaga.ui.components.SiagaBackdrop
import com.example.siaga.ui.components.SiagaGlassCard
import com.example.siaga.ui.components.SiagaPageHeader
import com.example.siaga.ui.theme.SiagaTextDim
import com.example.siaga.ui.theme.SiagaTextNavIdle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Tinggi frame desain, mengikuti layar Riwayat. */
private const val DESIGN_HEIGHT = 891.61f

/** Route layar Detail; {incidentId} diisi id laporan yang dikirim RiwayatScreen. */
const val ROUTE_INCIDENT_DETAIL = "incident_detail/{incidentId}"

/** Nama argument navigasi untuk id laporan. */
const val ARG_INCIDENT_ID = "incidentId"

/**
 * Detail satu laporan darurat, dibuka dari RiwayatScreen.
 *
 * Layar ini cuma menerima [incidentId] lewat navigation argument, lalu mengambil
 * datanya sendiri dari IncidentHistory berdasarkan id tersebut — jadi tidak ada
 * objek besar yang dikirim antar layar, cukup id-nya. Kalau id tidak ditemukan
 * (mis. riwayat sudah dibersihkan), tampil fallback sederhana.
 *
 * Gayanya sengaja mengikuti RiwayatScreen supaya konsisten: backdrop yang sama,
 * header SIAGA-03, dan kartu kaca.
 */
@Composable
fun IncidentDetailScreen(
    incidentId: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val history = remember { IncidentHistory(context) }
    val incidents by remember { history.incidents }.collectAsState(initial = emptyList())
    val incident = incidents.firstOrNull { it.id == incidentId }

    SiagaBackdrop(designHeight = DESIGN_HEIGHT) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(116.205.dp))

            Column(modifier = Modifier.padding(horizontal = 25.354.dp)) {
                // Layar ini di-push di atas Riwayat, jadi back hardware/gesture
                // maupun tombol ini sama-sama kembali ke daftar laporan.
                Text(
                    text = "← Kembali",
                    fontSize = 12.677.sp,
                    color = SiagaTextNavIdle,
                    modifier = Modifier.clickable(onClick = onBack),
                )
                Spacer(Modifier.height(16.902.dp))
                SiagaPageHeader(
                    title = "Detail Laporan",
                    subtitle = "Rincian laporan daruratmu.",
                )
            }

            Spacer(Modifier.height(21.128.dp))

            if (incident == null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 25.354.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Laporan tidak ditemukan.",
                        fontSize = 13.733.sp,
                        lineHeight = 19.91.sp,
                        color = SiagaTextDim,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 25.354.dp),
                ) {
                    IncidentDetailCard(incident)
                }
            }
        }
    }
}

/** Kartu isi laporan — tata letaknya mengikuti kartu di RiwayatScreen. */
@Composable
private fun IncidentDetailCard(incident: Incident) {
    val formatter = remember { SimpleDateFormat("d MMM yyyy • HH:mm", Locale("id", "ID")) }
    val context = LocalContext.current

    SiagaGlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatter.format(Date(incident.timestamp)),
                fontSize = 10.564.sp,
                color = SiagaTextNavIdle,
                modifier = Modifier.weight(1f),
            )
            if (incident.state == EmergencyState.CRITICAL) {
                Box(Modifier.size(6.338.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                Spacer(Modifier.size(6.338.dp))
                Text(
                    text = "KRITIS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.508.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(Modifier.height(8.451.dp))

        Text(
            text = incident.report,
            fontWeight = FontWeight.Bold,
            fontSize = 13.733.sp,
            lineHeight = 19.91.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )

        incident.instruction?.let { instruction ->
            Spacer(Modifier.height(6.338.dp))
            Text(
                text = instruction,
                fontSize = 12.677.sp,
                lineHeight = 18.dp.value.sp,
                color = SiagaTextNavIdle,
            )
        }

        incident.state?.let { state ->
            Spacer(Modifier.height(8.451.dp))
            Text(
                text = "Status: ${state.name}",
                fontSize = 11.621.sp,
                color = SiagaTextDim,
            )
        }

        incident.hospitalName?.let { name ->
            Spacer(Modifier.height(8.451.dp))
            Text(
                text = "Dirujuk ke $name",
                fontWeight = FontWeight.Bold,
                fontSize = 11.621.sp,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Tombol telepon cuma muncul kalau nomor rumah sakitnya ada; pakai
        // ACTION_DIAL supaya user tetap mengonfirmasi sebelum benar-benar menelepon.
        incident.hospitalPhone?.let { phone ->
            Spacer(Modifier.height(12.677.dp))
            Button(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
                    )
                },
                shape = MaterialTheme.shapes.extraSmall,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 12.677.dp),
                modifier = Modifier.height(33.805.dp),
            ) {
                Text(
                    text = "Telepon $phone",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.621.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
