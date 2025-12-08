package com.example.leafy.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_NAME = stringPreferencesKey("name")
        private val KEY_LOGGED_IN = booleanPreferencesKey("logged_in")
    }

    /** Simpan status login */
    suspend fun setLoggedIn(email: String, name: String, loggedIn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMAIL] = email
            prefs[KEY_NAME] = name
            prefs[KEY_LOGGED_IN] = loggedIn
        }
    }

    /** Cek apakah user sudah login (dipakai di MainActivity) */
    suspend fun isUserLoggedIn(): Boolean {
        return context.dataStore.data
            .map { it[KEY_LOGGED_IN] ?: false }
            .first()
    }

    /** Ambil email (dipakai HomeScreen & ProfileScreen) */
    suspend fun getEmail(): String? {
        return context.dataStore.data
            .map { it[KEY_EMAIL] }
            .first()
    }

    /** Ambil nama (dipakai HomeScreen & ProfileScreen) */
    suspend fun getName(): String? {
        return context.dataStore.data
            .map { it[KEY_NAME] }
            .first()
    }

    /** Hapus semua data (dipakai saat logout) */
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
