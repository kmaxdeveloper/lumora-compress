package uz.kmax.compress.core.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import uz.kmax.compress.core.compressor.CompressionFormat
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object PreferencesKeys {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val LANGUAGE = stringPreferencesKey("language")
        val DEFAULT_FORMAT = stringPreferencesKey("default_format")
        val DEFAULT_QUALITY = intPreferencesKey("default_quality")
        val KEEP_METADATA = booleanPreferencesKey("keep_metadata")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val BATCH_COUNT = intPreferencesKey("batch_count")
    }

    val preferencesFlow: Flow<AppPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AppPreferences(
                isFirstLaunch = preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true,
                themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
                dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true,
                language = preferences[PreferencesKeys.LANGUAGE] ?: "en",
                defaultFormat = preferences[PreferencesKeys.DEFAULT_FORMAT] ?: CompressionFormat.AUTO.name,
                defaultQuality = preferences[PreferencesKeys.DEFAULT_QUALITY] ?: 80,
                keepMetadata = preferences[PreferencesKeys.KEEP_METADATA] ?: true,
                isPremium = preferences[PreferencesKeys.IS_PREMIUM] ?: false,
                batchCount = preferences[PreferencesKeys.BATCH_COUNT] ?: 0
            )
        }

    suspend fun setFirstLaunchCompleted() {
        dataStore.edit { it[PreferencesKeys.IS_FIRST_LAUNCH] = false }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { it[PreferencesKeys.LANGUAGE] = language }
    }

    suspend fun setDefaultFormat(format: String) {
        dataStore.edit { it[PreferencesKeys.DEFAULT_FORMAT] = format }
    }

    suspend fun setDefaultQuality(quality: Int) {
        dataStore.edit { it[PreferencesKeys.DEFAULT_QUALITY] = quality }
    }

    suspend fun setKeepMetadata(keep: Boolean) {
        dataStore.edit { it[PreferencesKeys.KEEP_METADATA] = keep }
    }

    suspend fun setPremiumState(isPremium: Boolean) {
        dataStore.edit { it[PreferencesKeys.IS_PREMIUM] = isPremium }
    }

    suspend fun incrementBatchCount() {
        dataStore.edit { 
            val current = it[PreferencesKeys.BATCH_COUNT] ?: 0
            it[PreferencesKeys.BATCH_COUNT] = current + 1
        }
    }
}

data class AppPreferences(
    val isFirstLaunch: Boolean,
    val themeMode: String,
    val dynamicColor: Boolean,
    val language: String,
    val defaultFormat: String,
    val defaultQuality: Int,
    val keepMetadata: Boolean,
    val isPremium: Boolean,
    val batchCount: Int
)
