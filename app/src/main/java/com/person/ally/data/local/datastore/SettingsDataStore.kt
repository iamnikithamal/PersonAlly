package com.person.ally.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DAILY_BRIEFING_MORNING = booleanPreferencesKey("daily_briefing_morning")
        val DAILY_BRIEFING_MIDDAY = booleanPreferencesKey("daily_briefing_midday")
        val DAILY_BRIEFING_EVENING = booleanPreferencesKey("daily_briefing_evening")
        val BRIEFING_MORNING_TIME = stringPreferencesKey("briefing_morning_time")
        val BRIEFING_MIDDAY_TIME = stringPreferencesKey("briefing_midday_time")
        val BRIEFING_EVENING_TIME = stringPreferencesKey("briefing_evening_time")
        val AI_RESPONSE_LENGTH = stringPreferencesKey("ai_response_length")
        val AI_PERSONALITY = stringPreferencesKey("ai_personality")
        val AI_PROACTIVITY = intPreferencesKey("ai_proactivity")
        val MEMORY_AUTO_CAPTURE = booleanPreferencesKey("memory_auto_capture")
        val MEMORY_SUGGESTIONS = booleanPreferencesKey("memory_suggestions")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        val CRASH_REPORTING = booleanPreferencesKey("crash_reporting")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LAST_BACKUP_TIME = stringPreferencesKey("last_backup_time")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val TEXT_SIZE = stringPreferencesKey("text_size")
        val REDUCE_ANIMATIONS = booleanPreferencesKey("reduce_animations")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        ThemeMode.fromString(preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
    }

    val dynamicColorsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLORS] ?: true
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    val dailyBriefingMorning: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DAILY_BRIEFING_MORNING] ?: true
    }

    val dailyBriefingMidday: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DAILY_BRIEFING_MIDDAY] ?: false
    }

    val dailyBriefingEvening: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DAILY_BRIEFING_EVENING] ?: true
    }

    val briefingMorningTime: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BRIEFING_MORNING_TIME] ?: "08:00"
    }

    val briefingMiddayTime: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BRIEFING_MIDDAY_TIME] ?: "12:00"
    }

    val briefingEveningTime: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BRIEFING_EVENING_TIME] ?: "20:00"
    }

    val aiResponseLength: Flow<AIResponseLength> = context.dataStore.data.map { preferences ->
        AIResponseLength.fromString(preferences[PreferencesKeys.AI_RESPONSE_LENGTH] ?: AIResponseLength.BALANCED.name)
    }

    val aiPersonality: Flow<AIPersonality> = context.dataStore.data.map { preferences ->
        AIPersonality.fromString(preferences[PreferencesKeys.AI_PERSONALITY] ?: AIPersonality.WARM.name)
    }

    val aiProactivity: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AI_PROACTIVITY] ?: 5
    }

    val memoryAutoCapture: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MEMORY_AUTO_CAPTURE] ?: true
    }

    val memorySuggestions: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MEMORY_SUGGESTIONS] ?: true
    }

    val hapticFeedback: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true
    }

    val analyticsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ANALYTICS_ENABLED] ?: false
    }

    val crashReporting: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CRASH_REPORTING] ?: true
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    val lastBackupTime: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_BACKUP_TIME]
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.APP_LANGUAGE] ?: "en"
    }

    val textSize: Flow<TextSize> = context.dataStore.data.map { preferences ->
        TextSize.fromString(preferences[PreferencesKeys.TEXT_SIZE] ?: TextSize.MEDIUM.name)
    }

    val reduceAnimations: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REDUCE_ANIMATIONS] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLORS] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setDailyBriefingMorning(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_BRIEFING_MORNING] = enabled
        }
    }

    suspend fun setDailyBriefingMidday(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_BRIEFING_MIDDAY] = enabled
        }
    }

    suspend fun setDailyBriefingEvening(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_BRIEFING_EVENING] = enabled
        }
    }

    suspend fun setBriefingMorningTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BRIEFING_MORNING_TIME] = time
        }
    }

    suspend fun setBriefingMiddayTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BRIEFING_MIDDAY_TIME] = time
        }
    }

    suspend fun setBriefingEveningTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BRIEFING_EVENING_TIME] = time
        }
    }

    suspend fun setAIResponseLength(length: AIResponseLength) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_RESPONSE_LENGTH] = length.name
        }
    }

    suspend fun setAIPersonality(personality: AIPersonality) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_PERSONALITY] = personality.name
        }
    }

    suspend fun setAIProactivity(level: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_PROACTIVITY] = level.coerceIn(1, 10)
        }
    }

    suspend fun setMemoryAutoCapture(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MEMORY_AUTO_CAPTURE] = enabled
        }
    }

    suspend fun setMemorySuggestions(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MEMORY_SUGGESTIONS] = enabled
        }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] = enabled
        }
    }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANALYTICS_ENABLED] = enabled
        }
    }

    suspend fun setCrashReporting(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CRASH_REPORTING] = enabled
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setLastBackupTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_BACKUP_TIME] = time
        }
    }

    suspend fun setAppLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LANGUAGE] = language
        }
    }

    suspend fun setTextSize(size: TextSize) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEXT_SIZE] = size.name
        }
    }

    suspend fun setReduceAnimations(reduce: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REDUCE_ANIMATIONS] = reduce
        }
    }

    suspend fun clearAllSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM;

    companion object {
        fun fromString(value: String): ThemeMode = entries.find { it.name == value } ?: SYSTEM
    }
}

enum class AIResponseLength {
    CONCISE, BALANCED, DETAILED;

    companion object {
        fun fromString(value: String): AIResponseLength = entries.find { it.name == value } ?: BALANCED
    }

    fun getDisplayName(): String = when (this) {
        CONCISE -> "Concise"
        BALANCED -> "Balanced"
        DETAILED -> "Detailed"
    }
}

enum class AIPersonality {
    WARM, PROFESSIONAL, FRIENDLY, THOUGHTFUL;

    companion object {
        fun fromString(value: String): AIPersonality = entries.find { it.name == value } ?: WARM
    }

    fun getDisplayName(): String = when (this) {
        WARM -> "Warm & Supportive"
        PROFESSIONAL -> "Professional"
        FRIENDLY -> "Friendly & Casual"
        THOUGHTFUL -> "Thoughtful & Reflective"
    }
}

enum class TextSize {
    SMALL, MEDIUM, LARGE, EXTRA_LARGE;

    companion object {
        fun fromString(value: String): TextSize = entries.find { it.name == value } ?: MEDIUM
    }

    fun getDisplayName(): String = when (this) {
        SMALL -> "Small"
        MEDIUM -> "Medium"
        LARGE -> "Large"
        EXTRA_LARGE -> "Extra Large"
    }

    fun getScaleFactor(): Float = when (this) {
        SMALL -> 0.85f
        MEDIUM -> 1f
        LARGE -> 1.15f
        EXTRA_LARGE -> 1.3f
    }
}
