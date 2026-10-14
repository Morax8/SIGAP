package com.example.siaga.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Data diri user untuk keperluan darurat, diisi lewat layar Kontak Darurat. */
data class ContactData(
    val fullName: String,
    val phone: String,
    val email: String,
    val address: String,
)

/**
 * Menyimpan data diri user (nama, HP, email, alamat) di HP.
 *
 * Mengikuti pola [OnboardingPreferences]: satu DataStore bersama lewat
 * [siagaDataStore]. [contact] memancarkan null kalau belum pernah diisi, supaya
 * layar bisa membedakan "field kosong" dari "belum ada data tersimpan".
 */
class ContactPreferences(private val context: Context) {

    private val nameKey = stringPreferencesKey("contact_name")
    private val phoneKey = stringPreferencesKey("contact_phone")
    private val emailKey = stringPreferencesKey("contact_email")
    private val addressKey = stringPreferencesKey("contact_address")

    val contact: Flow<ContactData?> =
        context.siagaDataStore.data.map { prefs ->
            // Belum pernah disimpan kalau kunci nama tidak ada.
            val name = prefs[nameKey] ?: return@map null
            ContactData(
                fullName = name,
                phone = prefs[phoneKey].orEmpty(),
                email = prefs[emailKey].orEmpty(),
                address = prefs[addressKey].orEmpty(),
            )
        }

    suspend fun save(data: ContactData) {
        context.siagaDataStore.edit { prefs ->
            prefs[nameKey] = data.fullName
            prefs[phoneKey] = data.phone
            prefs[emailKey] = data.email
            prefs[addressKey] = data.address
        }
    }
}
