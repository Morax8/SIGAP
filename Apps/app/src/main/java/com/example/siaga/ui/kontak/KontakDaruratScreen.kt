package com.example.siaga.ui.kontak

import android.app.Application
import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.siaga.data.ContactData
import com.example.siaga.data.ContactPreferences
import com.example.siaga.ui.components.SiagaBackdrop
import com.example.siaga.ui.components.SiagaGlassCard
import com.example.siaga.ui.components.SiagaPageHeader
import com.example.siaga.ui.theme.GlassBorder
import com.example.siaga.ui.theme.SiagaTextNavIdle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tinggi frame desain, mengikuti layar SIAGA lainnya. */
private const val DESIGN_HEIGHT = 891.61f

/** Route layar form Data Diri (kontak darurat). */
const val ROUTE_KONTAK_DARURAT = "kontak_darurat"

/** Lama Snackbar "tersimpan" tampil sebelum layar ditutup kembali ke Riwayat. */
private const val SAVE_FEEDBACK_MS = 1_200L

/**
 * State form Data Diri. Tiap field punya nilai + error sendiri (String?), dan
 * [canSave] baru true kalau semua field terisi dan lolos validasi — ini yang
 * menahan tombol Simpan tetap disabled sampai form benar.
 */
data class KontakFormState(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val fullNameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val addressError: String? = null,
) {
    val canSave: Boolean
        get() = fullName.isNotBlank() && phone.isNotBlank() &&
            email.isNotBlank() && address.isNotBlank() &&
            fullNameError == null && phoneError == null &&
            emailError == null && addressError == null
}

/**
 * ViewModel form Data Diri. Mengikuti pola [com.example.siaga.ui.faskes.FaskesViewModel]
 * (AndroidViewModel + StateFlow), plus SharedFlow untuk event "tersimpan" sekali
 * jalan seperti `effects` di ChatViewModel.
 *
 * Scoped ke NavBackStackEntry layar ini, jadi state-nya selamat dari rotasi dan
 * hilang otomatis saat layar ditutup.
 */
class KontakViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = ContactPreferences(application)

    private val _uiState = MutableStateFlow(KontakFormState())
    val uiState: StateFlow<KontakFormState> = _uiState.asStateFlow()

    /** Event sekali jalan: dipancarkan setelah data berhasil disimpan. */
    private val _saved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val saved: SharedFlow<Unit> = _saved

    init {
        // Pre-fill (skenario edit): muat data tersimpan sekali saja. Kalau belum
        // ada, field dibiarkan kosong. Error tidak disetel di sini supaya form
        // hasil muatan tidak langsung menampilkan error.
        viewModelScope.launch {
            val data = prefs.contact.first()
            _uiState.update {
                if (data == null) it
                else it.copy(
                    fullName = data.fullName,
                    phone = data.phone,
                    email = data.email,
                    address = data.address,
                )
            }
        }
    }

    fun onFullNameChange(value: String) =
        _uiState.update { it.copy(fullName = value, fullNameError = validateName(value)) }

    fun onPhoneChange(value: String) {
        // Tolak karakter non-numerik saat mengetik; hanya digit (dan '+' di awal
        // untuk format +62) yang diterima.
        val filtered = filterPhone(value)
        _uiState.update { it.copy(phone = filtered, phoneError = validatePhone(filtered)) }
    }

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = validateEmail(value)) }

    fun onAddressChange(value: String) =
        _uiState.update { it.copy(address = value, addressError = validateAddress(value)) }

    /** Simpan hanya jalan kalau form valid; lalu kabari UI untuk menutup layar. */
    fun save() {
        val s = _uiState.value
        if (!s.canSave) return
        viewModelScope.launch {
            prefs.save(
                ContactData(
                    fullName = s.fullName.trim(),
                    phone = s.phone.trim(),
                    email = s.email.trim(),
                    address = s.address.trim(),
                )
            )
            _saved.tryEmit(Unit)
        }
    }

    // ===== Validasi (dijalankan real-time tiap perubahan field) =====

    private fun validateName(value: String): String? =
        if (value.isBlank()) "Nama lengkap wajib diisi." else null

    private fun validatePhone(value: String): String? = when {
        value.isBlank() -> "Nomor HP wajib diisi."
        !PHONE_REGEX.matches(value) ->
            "Format tidak valid. Contoh: 081234567890 atau +6281234567890."
        else -> null
    }

    private fun validateEmail(value: String): String? = when {
        value.isBlank() -> "Email wajib diisi."
        !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Format email tidak valid."
        else -> null
    }

    private fun validateAddress(value: String): String? =
        if (value.isBlank()) "Alamat wajib diisi." else null

    /** Sisakan digit, dan satu '+' di awal untuk format internasional (+62...). */
    private fun filterPhone(raw: String): String {
        val prefix = if (raw.startsWith("+")) "+" else ""
        return prefix + raw.filter { it.isDigit() }
    }

    private companion object {
        /** Nomor Indonesia: 08xx... atau +628xx... */
        val PHONE_REGEX = Regex("^(08[1-9][0-9]{6,10}|\\+628[1-9][0-9]{6,10})$")
    }
}

/**
 * Layar form Data Diri (kontak darurat), dibuka dari tombol "Data Diri" di
 * RiwayatScreen.
 *
 * Bukan tab di nav bar bawah — layar ini di-push di atas Riwayat seperti layar
 * Detail, jadi punya tombol "← Kembali" dan tidak menampilkan nav bar.
 *
 * Gayanya mengikuti sistem SIAGA: backdrop yang sama, header SIAGA-03, dan kartu
 * kaca. Field-nya menyalin gaya OutlinedTextField di ChatScreen (fungsi TypedInput).
 *
 * @param onBack dipanggil untuk kembali ke layar sebelumnya (Riwayat).
 */
@Composable
fun KontakDaruratScreen(
    onBack: () -> Unit,
    viewModel: KontakViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Setelah simpan sukses: tampilkan feedback singkat, lalu kembali ke Riwayat.
    LaunchedEffect(Unit) {
        viewModel.saved.collect {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Data diri tersimpan.",
                    duration = SnackbarDuration.Short,
                )
            }
            delay(SAVE_FEEDBACK_MS)
            onBack()
        }
    }

    SiagaBackdrop(designHeight = DESIGN_HEIGHT) {
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
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
                    title = "Data Diri",
                    subtitle = "Data ini dipakai saat keadaan darurat.",
                )
            }

            Spacer(Modifier.height(21.128.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 25.354.dp),
            ) {
                SiagaGlassCard {
                    KontakField(
                        value = uiState.fullName,
                        onValueChange = viewModel::onFullNameChange,
                        label = "Nama Lengkap",
                        error = uiState.fullNameError,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    )
                    Spacer(Modifier.height(16.902.dp))
                    KontakField(
                        value = uiState.phone,
                        onValueChange = viewModel::onPhoneChange,
                        label = "No. HP",
                        error = uiState.phoneError,
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    )
                    Spacer(Modifier.height(16.902.dp))
                    KontakField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Email",
                        error = uiState.emailError,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    )
                    Spacer(Modifier.height(16.902.dp))
                    KontakField(
                        value = uiState.address,
                        onValueChange = viewModel::onAddressChange,
                        label = "Alamat",
                        error = uiState.addressError,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        singleLine = false,
                        minLines = 2,
                    )
                }

                Spacer(Modifier.height(21.128.dp))

                // Disabled selama ada field kosong/invalid (lihat KontakFormState.canSave).
                Button(
                    onClick = viewModel::save,
                    enabled = uiState.canSave,
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.482.dp),
                ) {
                    Text(
                        text = "Simpan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.79.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(Modifier.height(21.128.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .padding(bottom = 16.903.dp),
        )
    }
}

/**
 * Satu field form. Menyalin gaya OutlinedTextField dari ChatScreen (TypedInput):
 * RoundedCornerShape + border merah saat fokus, kursor merah. Error tampil sebagai
 * supportingText dengan isError = true.
 */
@Composable
private fun KontakField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.677.sp) },
        isError = error != null,
        supportingText = error?.let { { Text(it, fontSize = 10.564.sp) } },
        singleLine = singleLine,
        minLines = minLines,
        textStyle = TextStyle(fontSize = 12.677.sp, color = MaterialTheme.colorScheme.onSurface),
        shape = MaterialTheme.shapes.extraSmall,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = GlassBorder,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = SiagaTextNavIdle,
            errorBorderColor = MaterialTheme.colorScheme.primary,
            errorCursorColor = MaterialTheme.colorScheme.primary,
            errorLabelColor = MaterialTheme.colorScheme.primary,
            errorSupportingTextColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}
