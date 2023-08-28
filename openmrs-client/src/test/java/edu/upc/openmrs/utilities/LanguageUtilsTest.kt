package edu.upc.openmrs.utilities

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.sdk.utilities.ApplicationConstants
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
internal class LanguageUtilsTest{

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        // Clear any shared preferences
        context.getSharedPreferences(ApplicationConstants.PREF_FIRST_TIME_LAUNCH, Context.MODE_PRIVATE).edit().clear().apply()

        //set the Locale language of the app
        setLocale("es")

        LanguageUtils.setAppToDeviceLanguage(context)
    }

    @Test
    fun `when opening the app for the first time, the app language should be the same as the device one`() {

        val appLanguage = context.resources.configuration.locales[0].language
        assertEquals("es", appLanguage)

        resetLocale()
    }

    @Test
    fun `when I change the app language, when I reopen the app should be still the same as I changed it`() {
        LanguageUtils.setLanguage("ca")

        val testContext = createTestContextWithLocale("ca")

        LanguageUtils.setAppToDeviceLanguage(testContext)

        val appLanguage = testContext.resources.configuration.locales[0].language
        assertEquals("ca", appLanguage)

        resetLocale()
    }


    private fun setLocale(language: String) {
        val config = context.resources.configuration
        val locale = Locale(language)
        Locale.setDefault(locale)
        config.setLocale(locale)
        context.createConfigurationContext(config)
    }

    private fun resetLocale() {
        val defaultLocale = Locale.getDefault()
        val config = context.resources.configuration
        config.setLocale(defaultLocale)
        context.createConfigurationContext(config)
    }

    private fun createTestContextWithLocale(language: String): Context {
        val config = android.content.res.Configuration()
        config.setLocale(Locale(language))
        return context.createConfigurationContext(config)
    }

}