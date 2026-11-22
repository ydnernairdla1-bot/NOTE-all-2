package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.AppButtonSize
import com.example.ui.theme.AppFontChoice
import com.example.ui.theme.AppFontScale
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.BackgroundGradientPreset
import com.example.ui.theme.BackgroundMode
import com.example.ui.theme.BackgroundSolidPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cloudnote_prefs")

data class AppearanceSettings(
    val language: AppLanguage = AppLanguage.INDONESIAN,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val themePreset: AppThemePreset = AppThemePreset.EMERALD,
    val fontChoice: AppFontChoice = AppFontChoice.PLUS_JAKARTA,
    val fontScale: AppFontScale = AppFontScale.NORMAL,
    val buttonSize: AppButtonSize = AppButtonSize.COMPACT,
    val backgroundMode: BackgroundMode = BackgroundMode.DEFAULT,
    val gradientPreset: BackgroundGradientPreset = BackgroundGradientPreset.WARM_PAPER,
    val solidPreset: BackgroundSolidPreset = BackgroundSolidPreset.IVORY,
    val customImageName: String? = null,
    val imageDimAlpha: Float = 0.25f
)

class PreferencesStore(private val context: Context) {

    private object Keys {
        val IS_GRID_VIEW = booleanPreferencesKey("is_grid_view")
        val LAST_BACKUP_AT = longPreferencesKey("last_backup_at")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val THEME_PRESET = stringPreferencesKey("theme_preset")
        val FONT_CHOICE = stringPreferencesKey("font_choice")
        val FONT_SCALE = stringPreferencesKey("font_scale")
        val BUTTON_SIZE = stringPreferencesKey("button_size")
        val BACKGROUND_MODE = stringPreferencesKey("background_mode")
        val BACKGROUND_GRADIENT = stringPreferencesKey("background_gradient")
        val BACKGROUND_SOLID = stringPreferencesKey("background_solid")
        val BACKGROUND_IMAGE_NAME = stringPreferencesKey("background_image_name")
        val BACKGROUND_IMAGE_DIM = floatPreferencesKey("background_image_dim")
    }

    val isGridView: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_GRID_VIEW] ?: true
    }

    val lastBackupAt: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_BACKUP_AT]
    }

    val appearanceSettings: Flow<AppearanceSettings> = context.dataStore.data.map { prefs ->
        AppearanceSettings(
            language = AppLanguage.fromKey(prefs[Keys.APP_LANGUAGE] ?: AppLanguage.INDONESIAN.key),
            themeMode = AppThemeMode.fromKey(prefs[Keys.THEME_MODE] ?: AppThemeMode.SYSTEM.key),
            themePreset = AppThemePreset.fromKey(prefs[Keys.THEME_PRESET] ?: AppThemePreset.EMERALD.key),
            fontChoice = AppFontChoice.fromKey(prefs[Keys.FONT_CHOICE] ?: AppFontChoice.PLUS_JAKARTA.key),
            fontScale = AppFontScale.fromKey(prefs[Keys.FONT_SCALE] ?: AppFontScale.NORMAL.key),
            buttonSize = AppButtonSize.fromKey(prefs[Keys.BUTTON_SIZE] ?: AppButtonSize.COMPACT.key),
            backgroundMode = BackgroundMode.fromKey(prefs[Keys.BACKGROUND_MODE] ?: BackgroundMode.DEFAULT.key),
            gradientPreset = BackgroundGradientPreset.fromKey(
                prefs[Keys.BACKGROUND_GRADIENT] ?: BackgroundGradientPreset.WARM_PAPER.key
            ),
            solidPreset = BackgroundSolidPreset.fromKey(
                prefs[Keys.BACKGROUND_SOLID] ?: BackgroundSolidPreset.IVORY.key
            ),
            customImageName = prefs[Keys.BACKGROUND_IMAGE_NAME],
            imageDimAlpha = (prefs[Keys.BACKGROUND_IMAGE_DIM] ?: 0.25f).coerceIn(0f, 0.75f)
        )
    }

    suspend fun setGridView(isGrid: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_GRID_VIEW] = isGrid
        }
    }

    suspend fun setLastBackupAt(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_BACKUP_AT] = timestamp
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_LANGUAGE] = language.key
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.key
        }
    }

    suspend fun setThemePreset(preset: AppThemePreset) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_PRESET] = preset.key
        }
    }

    suspend fun setFontChoice(choice: AppFontChoice) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FONT_CHOICE] = choice.key
        }
    }

    suspend fun setFontScale(scale: AppFontScale) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FONT_SCALE] = scale.key
        }
    }

    suspend fun setButtonSize(size: AppButtonSize) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BUTTON_SIZE] = size.key
        }
    }

    suspend fun setBackgroundMode(mode: BackgroundMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BACKGROUND_MODE] = mode.key
        }
    }

    suspend fun setGradientPreset(preset: BackgroundGradientPreset) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BACKGROUND_MODE] = BackgroundMode.GRADIENT.key
            prefs[Keys.BACKGROUND_GRADIENT] = preset.key
        }
    }

    suspend fun setSolidPreset(preset: BackgroundSolidPreset) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BACKGROUND_MODE] = BackgroundMode.SOLID.key
            prefs[Keys.BACKGROUND_SOLID] = preset.key
        }
    }

    suspend fun setCustomBackgroundImage(fileName: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BACKGROUND_MODE] = BackgroundMode.IMAGE.key
            prefs[Keys.BACKGROUND_IMAGE_NAME] = fileName
        }
    }

    suspend fun setImageDimAlpha(alpha: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BACKGROUND_IMAGE_DIM] = alpha.coerceIn(0f, 0.75f)
        }
    }

    suspend fun resetAppearanceToDefault() {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_LANGUAGE] = AppLanguage.INDONESIAN.key
            prefs[Keys.THEME_MODE] = AppThemeMode.SYSTEM.key
            prefs[Keys.THEME_PRESET] = AppThemePreset.EMERALD.key
            prefs[Keys.FONT_CHOICE] = AppFontChoice.PLUS_JAKARTA.key
            prefs[Keys.FONT_SCALE] = AppFontScale.NORMAL.key
            prefs[Keys.BUTTON_SIZE] = AppButtonSize.COMPACT.key
            prefs[Keys.BACKGROUND_MODE] = BackgroundMode.DEFAULT.key
            prefs.remove(Keys.BACKGROUND_IMAGE_NAME)
            prefs[Keys.BACKGROUND_IMAGE_DIM] = 0.25f
        }
    }
}
