package com.example.leafy.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.userDataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val EMAIL_KEY = stringPreferencesKey("user_email")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    suspend fun setLoggedIn(email: String, status: Boolean) {
        context.userDataStore.edit { prefs ->
            prefs[EMAIL_KEY] = email
            prefs[IS_LOGGED_IN] = status
        }
    }

    suspend fun getEmail(): String? {
        val prefs = context.userDataStore.data.first()
        return prefs[EMAIL_KEY]
    }

    suspend fun isUserLoggedIn(): Boolean {
        val prefs = context.userDataStore.data.first()
        return prefs[IS_LOGGED_IN] ?: false
    }

    suspend fun clear() {
        context.userDataStore.edit { it.clear() }
    }
}
