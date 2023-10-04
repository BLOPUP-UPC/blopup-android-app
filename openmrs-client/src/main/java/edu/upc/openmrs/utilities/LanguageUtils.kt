/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package edu.upc.openmrs.utilities

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import edu.upc.BuildConfig
import edu.upc.R
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.utilities.ApplicationConstants
import java.util.*

object LanguageUtils {

    @JvmStatic
    fun getLanguage(): String? {
        val defaultSharedPref = OpenmrsAndroid.getOpenMRSSharedPreferences()
        return defaultSharedPref.getString(
            ApplicationConstants.OpenMRSlanguage.KEY_LANGUAGE_MODE,
            "en"
        )
    }

    @JvmStatic
    fun setLanguage(language: String?) {
        val editor = OpenmrsAndroid.getOpenMRSSharedPreferences().edit()
        editor.putString(ApplicationConstants.OpenMRSlanguage.KEY_LANGUAGE_MODE, language)
        editor.apply()
    }

    @JvmStatic
    fun setupLanguage(resources: Resources) {
        val myLocale = getLanguage()?.let { Locale(it) }
        val dm = resources.displayMetrics
        val conf = resources.configuration
        conf.locale = myLocale
        resources.updateConfiguration(conf, dm)
    }

    @JvmStatic
    fun getLocaleStringResource(
        requestedLocale: Locale?,
        resourceId: Int,
        context: Context
    ): String {
        val result: String
        // use latest api
        val config = Configuration(context.resources.configuration)
        config.setLocale(requestedLocale)
        result = context.createConfigurationContext(config).getText(resourceId).toString()
        return result
    }

    @JvmStatic
    fun getLanguageCode(language: String?, context: Context): String? {
        val currentLocale = Locale.getDefault()
        val languageMap = mapOf(
            context.getString(R.string.english) to "en",
            context.getString(R.string.spanish) to "es",
            context.getString(R.string.catalan) to "ca",
            context.getString(R.string.italian) to "it",
            context.getString(R.string.portuguese) to "pt",
            context.getString(R.string.german) to "de",
            context.getString(R.string.french) to "fr",
            context.getString(R.string.moroccan) to "ar",
            context.getString(R.string.russian) to "ru",
            context.getString(R.string.ukrainian) to "uk",
        )
        return languageMap[language ?: "English"]?.lowercase(currentLocale)
    }

    @JvmStatic
    fun setAppToDeviceLanguage(context: Context) {
        val sharedPreferences = context.getSharedPreferences(ApplicationConstants.OpenMRSSharedPreferenceNames.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val isFirstTimeLaunch = sharedPreferences.getBoolean(ApplicationConstants.PREF_FIRST_TIME_LAUNCH, true)

        if (isFirstTimeLaunch) {
            val deviceLanguage = Locale.getDefault().language
            val supportedLanguages = setOf("ca", "es", "en")

            if (supportedLanguages.contains(deviceLanguage)) {
                setLanguage(deviceLanguage)
                setupLanguage(context.resources)
            }
            sharedPreferences.edit().putBoolean(ApplicationConstants.PREF_FIRST_TIME_LAUNCH, false).apply()
        }
    }
}
