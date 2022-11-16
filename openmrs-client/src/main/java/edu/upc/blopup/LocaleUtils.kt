package edu.upc.blopup

import android.content.Context
import java.util.Locale

fun changeLocale(baseContext: Context, language: String): Context {
    val locale = Locale(language)
    Locale.setDefault(locale)

    val configuration = baseContext.resources.configuration.apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }

    return baseContext.createConfigurationContext(configuration)
}