package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppViewMode {
    GRID,
    LIST
}

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("device_test_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        try {
            AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
        } catch (_: Exception) { AppThemeMode.SYSTEM }
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _viewMode = MutableStateFlow(
        try {
            AppViewMode.valueOf(prefs.getString("view_mode", AppViewMode.GRID.name) ?: AppViewMode.GRID.name)
        } catch (_: Exception) { AppViewMode.GRID }
    )
    val viewMode: StateFlow<AppViewMode> = _viewMode.asStateFlow()

    private val _isFirstLaunch = MutableStateFlow(prefs.getBoolean("is_first_launch", true))
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()

    private val _currentLanguageTag = MutableStateFlow(prefs.getString("lang_tag", "system") ?: "system")
    val currentLanguageTag: StateFlow<String> = _currentLanguageTag.asStateFlow()

    private val _amoledMode = MutableStateFlow(prefs.getBoolean("amoled_mode", false))
    val amoledMode: StateFlow<Boolean> = _amoledMode.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.getBoolean("haptics_enabled", true))
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _strictMode = MutableStateFlow(prefs.getBoolean("strict_mode", false))
    val strictMode: StateFlow<Boolean> = _strictMode.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    fun setAmoledMode(enabled: Boolean) {
        prefs.edit().putBoolean("amoled_mode", enabled).apply()
        _amoledMode.value = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptics_enabled", enabled).apply()
        _hapticsEnabled.value = enabled
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setStrictMode(enabled: Boolean) {
        prefs.edit().putBoolean("strict_mode", enabled).apply()
        _strictMode.value = enabled
    }

    fun setViewMode(mode: AppViewMode) {
        prefs.edit().putString("view_mode", mode.name).apply()
        _viewMode.value = mode
    }

    fun setLanguage(tag: String) {
        prefs.edit().putString("lang_tag", tag).apply()
        _currentLanguageTag.value = tag
        when (tag) {
            "en" -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            "ru" -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ru"))
            else -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        }
    }

    fun completeFirstLaunch() {
        prefs.edit().putBoolean("is_first_launch", false).apply()
        _isFirstLaunch.value = false
    }

    companion object {
        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
