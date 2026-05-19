package com.splitmate.android.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Top-level extension to ensure a single instance of DataStore is created per application.
// This prevents multiple instances from throwing IllegalStateException during concurrent writes.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

/**
 * Manages secure storage of the JWT authentication token.
 * We use DataStore over SharedPreferences as it performs fully asynchronous I/O,
 * preventing UI thread blocking and ANRs during token reads/writes.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    /**
     * Exposes the token as a Flow. This allows the UI or networking interceptors
     * to reactively listen for login/logout events and update state automatically.
     */
    val tokenFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_KEY]
        }

    val userIdFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY]
        }

    /**
     * Saves the token asynchronously.
     */
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    /**
     * Clears the token asynchronously. Used during sign-out or when the token expires (401 response).
     */
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
        }
    }
}
