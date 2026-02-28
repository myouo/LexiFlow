package com.myouo.lexiflow.i18n

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual fun getSystemLanguage(): LanguageMode {
    val langCode = NSLocale.currentLocale.languageCode
    return if (langCode == "zh") LanguageMode.ZH_HANS else LanguageMode.EN
}
