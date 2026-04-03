package com.google.android.vending.licensing

import android.content.SharedPreferences

/**
 * Booty: minimal stub replacing Google Play Licensing library.
 * Stores values in plain SharedPreferences without obfuscation,
 * since all licensing is bypassed (isEnabledFor always returns true).
 */
class PreferenceObfuscator(private val prefs: SharedPreferences) {
    fun getString(key: String, defValue: String?): String =
        prefs.getString(key, defValue) ?: defValue ?: ""

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun commit() {
        // No-op: individual operations already persist via apply()
    }
}
