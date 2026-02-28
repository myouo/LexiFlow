package com.myouo.lexiflow.i18n

import java.util.Locale

actual fun getSystemLanguage(): LanguageMode {
    val locale = Locale.getDefault().language
    return if (locale == "zh") LanguageMode.ZH_HANS else LanguageMode.EN
}
