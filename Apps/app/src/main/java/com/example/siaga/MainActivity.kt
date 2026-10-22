package com.example.siaga

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.siaga.data.OnboardingPreferences
import com.example.siaga.ui.chat.ChatScreen
import com.example.siaga.ui.chat.ChatViewModel
import com.example.siaga.ui.chat.ConnectionStatus
import com.example.siaga.ui.components.SiagaNavBar
import com.example.siaga.ui.components.SiagaTab
import com.example.siaga.ui.faskes.FaskesScreen
import com.example.siaga.ui.kontak.KontakDaruratScreen
import com.example.siaga.ui.kontak.ROUTE_KONTAK_DARURAT
import com.example.siaga.ui.onboarding.OnboardingPermissionsScreen
import com.example.siaga.ui.onboarding.OnboardingWelcomeScreen
import com.example.siaga.ui.riwayat.ARG_INCIDENT_ID
import com.example.siaga.ui.riwayat.IncidentDetailScreen
import com.example.siaga.ui.riwayat.ROUTE_INCIDENT_DETAIL
import com.example.siaga.ui.riwayat.RiwayatScreen
import com.example.siaga.ui.splash.SplashScreen
import com.example.siaga.ui.theme.SIAGATheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Wajib dipanggil sebelum setContent: menukar tema splash bawaan
        // (Theme.SIAGA.Splash) ke tema aplikasi begitu jendela siap digambar.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SIAGATheme {
                SIAGAApp(startListening = intent?.getBooleanExtra(EXTRA_START_LISTENING, false) == true)
            }
        }
    }

    companion object {
        /** Dikirim widget SOS: begitu layar darurat siap, langsung buka mic. */
        const val EXTRA_START_LISTENING = "start_listening"
    }
}

// ===== Route navigasi (string-based, tanpa type-safe navigation) =====

// Alur awal: splash -> onboarding (kalau belum pernah) -> layar utama.
private const val ROUTE_SPLASH = "splash"
private const val ROUTE_ONBOARDING_WELCOME = "onboarding_welcome"
private const val ROUTE_ONBOARDING_PERMISSIONS = "onboarding_permissions"
private const val ROUTE_EMERGENCY = "emergency"

// Tiga tab utama di dalam layar darurat.
private const val ROUTE_FASKES = "faskes"
private const val ROUTE_RIWAYAT = "riwayat"

@Composable
fun SIAGAApp(startListening: Boolean = false) {
    // ViewModel diambil di sini, bukan di dalam ChatScreen, supaya socket mulai
    // menyambung sejak splash tampil — waktunya kepakai, bukan sekadar menunggu.
    // Instansinya milik Activity, jadi ChatScreen nanti dapat yang sama.
    val chatViewModel: ChatViewModel = viewModel()
    val context = LocalContext.current
    val onboardingPrefs = remember { OnboardingPreferences(context) }
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    // Selesai onboarding: tandai sudah, lalu lompat ke layar utama. Seluruh
    // halaman onboarding di-pop (inclusive) supaya tombol back keluar aplikasi,
    // bukan balik lagi ke halaman izin.
    fun finishOnboarding() {
        scope.launch { onboardingPrefs.setCompleted() }
        navController.navigate(ROUTE_EMERGENCY) {
            popUpTo(ROUTE_ONBOARDING_WELCOME) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = ROUTE_SPLASH,
        enterTransition = { fadeIn(tween(250)) },
        exitTransition = { fadeOut(tween(250)) },
        popEnterTransition = { fadeIn(tween(250)) },
        popExitTransition = { fadeOut(tween(250)) },
    ) {
        composable(ROUTE_SPLASH) {
            SplashScreen()
            SplashGate(chatViewModel, onboardingPrefs) { alreadyOnboarded ->
                // Kalau sudah pernah onboarding langsung ke layar utama, kalau
                // belum mulai dari halaman selamat datang. Splash di-pop
                // inclusive supaya tidak bisa kembali lagi ke sini.
                val target =
                    if (alreadyOnboarded) ROUTE_EMERGENCY else ROUTE_ONBOARDING_WELCOME
                navController.navigate(target) {
                    popUpTo(ROUTE_SPLASH) { inclusive = true }
                }
            }
        }

        composable(ROUTE_ONBOARDING_WELCOME) {
            OnboardingWelcomeScreen(
                onStart = { navController.navigate(ROUTE_ONBOARDING_PERMISSIONS) },
                onSkip = ::finishOnboarding,
            )
        }

        composable(ROUTE_ONBOARDING_PERMISSIONS) {
            OnboardingPermissionsScreen(onDone = ::finishOnboarding)
        }

        // Nav bar-nya bagian dari desain tiap layar (SIAGA-05), jadi diberikan
        // sebagai slot, bukan dibungkus scaffold dari luar.
        composable(ROUTE_EMERGENCY) {
            MainTabs(chatViewModel, startListening)
        }
    }
}

/**
 * Menahan splash sampai socket tersambung, lalu memberi tahu ke mana harus
 * pindah. Dua pagar:
 * - [SPLASH_MIN_MS]: kalau server keburu jawab, splash tidak berkedip sekejap.
 * - [SPLASH_MAX_MS]: kalau server tidak terjangkau, aplikasi tetap jalan
 *   (layar utama sudah punya indikator "Terputus" + tombol coba lagi).
 */
@Composable
private fun SplashGate(
    viewModel: ChatViewModel,
    onboardingPrefs: OnboardingPreferences,
    onFinished: (alreadyOnboarded: Boolean) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val startedAt = remember { System.currentTimeMillis() }

    LaunchedEffect(Unit) {
        val alreadyOnboarded = onboardingPrefs.isCompleted.first()
        withTimeoutOrNull(SPLASH_MAX_MS.milliseconds) {
            snapshotFlow { uiState.connectionStatus }
                .first { it == ConnectionStatus.CONNECTED }
        }
        val shown = System.currentTimeMillis() - startedAt
        if (shown < SPLASH_MIN_MS) delay(SPLASH_MIN_MS - shown)
        onFinished(alreadyOnboarded)
    }
}

/** Tiga halaman utama, berpindah lewat nav bar bawah. */
@Composable
private fun MainTabs(chatViewModel: ChatViewModel, startListening: Boolean = false) {
    val navController = rememberNavController()
    val context = LocalContext.current

    fun openDialer(phoneNumber: String) {
        context.startActivity(
            Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
        )
    }

    // Pindah tab ala bottom-navigation: pop sampai destination awal graph
    // (sambil menyimpan state-nya), single-top, lalu pulihkan state — supaya
    // back-stack tidak menumpuk setiap kali pindah tab.
    fun navigateToTab(tab: SiagaTab) {
        val route = when (tab) {
            SiagaTab.DARURAT -> ROUTE_EMERGENCY
            SiagaTab.FASKES -> ROUTE_FASKES
            SiagaTab.RIWAYAT -> ROUTE_RIWAYAT
        }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Nav bar diberikan sebagai slot ke tiap layar. Tab aktif dibaca dari
    // back-stack (bukan state lokal), jadi highlight selalu mengikuti layar
    // yang sedang tampil — termasuk setelah kembali dari layar Detail Riwayat.
    val bottomBar: @Composable () -> Unit = {
        val currentEntry by navController.currentBackStackEntryAsState()
        val selected = when (currentEntry?.destination?.route) {
            ROUTE_FASKES -> SiagaTab.FASKES
            ROUTE_RIWAYAT, ROUTE_INCIDENT_DETAIL, ROUTE_KONTAK_DARURAT -> SiagaTab.RIWAYAT
            else -> SiagaTab.DARURAT
        }
        SiagaNavBar(selected = selected, onSelect = ::navigateToTab)
    }

    NavHost(
        navController = navController,
        startDestination = ROUTE_EMERGENCY,
        enterTransition = { fadeIn(tween(250)) },
        exitTransition = { fadeOut(tween(250)) },
        popEnterTransition = { fadeIn(tween(250)) },
        popExitTransition = { fadeOut(tween(250)) },
    ) {
        composable(ROUTE_EMERGENCY) {
            ChatScreen(chatViewModel, bottomBar, startListening)
        }

        // Di halaman Faskes user yang memilih sendiri, jadi cukup buka dialer —
        // panggilan otomatis hanya untuk kondisi kritis di layar darurat.
        composable(ROUTE_FASKES) {
            FaskesScreen(onCall = ::openDialer, bottomBar = bottomBar)
        }

        composable(ROUTE_RIWAYAT) {
            RiwayatScreen(
                bottomBar = bottomBar,
                // Kirim id laporan saja; layar Detail mengambil datanya sendiri
                // dari IncidentHistory berdasarkan id ini.
                onIncidentClick = { id -> navController.navigate("incident_detail/$id") },
                // Tombol "Data Diri" di header membuka form kontak darurat.
                onEditKontak = { navController.navigate(ROUTE_KONTAK_DARURAT) },
            )
        }

        // Layar Detail menerima incidentId sebagai argument navigasi.
        composable(
            route = ROUTE_INCIDENT_DETAIL,
            arguments = listOf(navArgument(ARG_INCIDENT_ID) { type = NavType.StringType }),
        ) { entry ->
            val incidentId = entry.arguments?.getString(ARG_INCIDENT_ID).orEmpty()
            IncidentDetailScreen(
                incidentId = incidentId,
                onBack = { navController.popBackStack() },
            )
        }

        // Form Data Diri (kontak darurat), di-push di atas Riwayat seperti Detail.
        composable(ROUTE_KONTAK_DARURAT) {
            KontakDaruratScreen(onBack = { navController.popBackStack() })
        }
    }
}

private const val SPLASH_MIN_MS = 900L
private const val SPLASH_MAX_MS = 2_500L
