package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppearanceSettings
import com.example.data.ImageStorageManager
import com.example.data.PreferencesStore
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.AppButtonSize
import com.example.ui.theme.AppFontChoice
import com.example.ui.theme.AppFontScale
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.BackgroundGradientPreset
import com.example.ui.theme.BackgroundMode
import com.example.ui.theme.BackgroundSolidPreset
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppearanceViewModel(
    private val preferencesStore: PreferencesStore,
    val imageStorageManager: ImageStorageManager
) : ViewModel() {

    val appearanceState: StateFlow<AppearanceSettings> = preferencesStore.appearanceSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppearanceSettings()
        )

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesStore.setLanguage(language)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            preferencesStore.setThemeMode(mode)
        }
    }

    fun setThemePreset(preset: AppThemePreset) {
        viewModelScope.launch {
            preferencesStore.setThemePreset(preset)
        }
    }

    fun setFontChoice(choice: AppFontChoice) {
        viewModelScope.launch {
            preferencesStore.setFontChoice(choice)
        }
    }

    fun setFontScale(scale: AppFontScale) {
        viewModelScope.launch {
            preferencesStore.setFontScale(scale)
        }
    }

    fun setButtonSize(size: AppButtonSize) {
        viewModelScope.launch {
            preferencesStore.setButtonSize(size)
        }
    }

    fun setBackgroundMode(mode: BackgroundMode) {
        viewModelScope.launch {
            preferencesStore.setBackgroundMode(mode)
        }
    }

    fun setGradientPreset(preset: BackgroundGradientPreset) {
        viewModelScope.launch {
            preferencesStore.setGradientPreset(preset)
        }
    }

    fun setSolidPreset(preset: BackgroundSolidPreset) {
        viewModelScope.launch {
            preferencesStore.setSolidPreset(preset)
        }
    }

    fun setCustomBackgroundPhoto(uri: Uri) {
        viewModelScope.launch {
            val previousName = appearanceState.value.customImageName
            val savedName = imageStorageManager.saveImageFromUri(uri)
            if (savedName != null) {
                preferencesStore.setCustomBackgroundImage(savedName)
                if (!previousName.isNullOrBlank() && previousName != savedName) {
                    imageStorageManager.deleteImageFile(previousName)
                }
            }
        }
    }

    fun setImageDimAlpha(alpha: Float) {
        viewModelScope.launch {
            preferencesStore.setImageDimAlpha(alpha)
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            val previousName = appearanceState.value.customImageName
            preferencesStore.resetAppearanceToDefault()
            if (!previousName.isNullOrBlank()) {
                imageStorageManager.deleteImageFile(previousName)
            }
        }
    }

    companion object {
        fun factory(
            preferencesStore: PreferencesStore,
            imageStorageManager: ImageStorageManager
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AppearanceViewModel(preferencesStore, imageStorageManager) as T
                }
            }
    }
}
