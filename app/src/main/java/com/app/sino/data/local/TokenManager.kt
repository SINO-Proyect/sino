package com.app.sino.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString("refresh_token", token).apply()
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    fun saveEmail(email: String) {
        sharedPreferences.edit().putString("user_email", email).apply()
    }

    fun getEmail(): String? {
        return sharedPreferences.getString("user_email", null)
    }

    fun saveUserId(userId: Int) {
        sharedPreferences.edit().putInt("user_id", userId).apply()
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt("user_id", -1)
    }

    fun saveEmailVerified(isVerified: Boolean) {
        sharedPreferences.edit().putBoolean("email_verified", isVerified).apply()
    }

    fun isEmailVerified(): Boolean {
        return sharedPreferences.getBoolean("email_verified", false)
    }

    fun clearTokens() {
        sharedPreferences.edit().clear().apply()
    }
}
