package com.myouo.lexiflow.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class LanguageMode {
    SYSTEM,
    ZH_HANS,
    EN
}

expect fun getSystemLanguage(): LanguageMode

class LocalizationManager {
    private val _currentLanguage = MutableStateFlow(LanguageMode.ZH_HANS)
    val currentLanguage: StateFlow<LanguageMode> = _currentLanguage
    
    fun setLanguage(mode: LanguageMode) {
        _currentLanguage.value = mode
    }
    
    fun getString(key: String): String {
        val activeLang = if (_currentLanguage.value == LanguageMode.SYSTEM) {
           getSystemLanguage()
        } else {
            _currentLanguage.value
        }
        
        return when (activeLang) {
            LanguageMode.ZH_HANS -> zhStrings[key] ?: enStrings[key] ?: key
            else -> enStrings[key] ?: key
        }
    }
    
    private val enStrings = mapOf(
        "settings.language" to "Language",
        "settings.theme" to "Theme",
        "review.again" to "Again",
        "review.hard" to "Hard",
        "review.good" to "Good",
        "error.network" to "Network error occurred."
    )
    
    private val zhStrings = mapOf(
        "settings.language" to "语言",
        "settings.theme" to "主题",
        "review.again" to "不会",
        "review.hard" to "模糊",
        "review.good" to "会",
        "error.network" to "网络错误。"
    )
}
