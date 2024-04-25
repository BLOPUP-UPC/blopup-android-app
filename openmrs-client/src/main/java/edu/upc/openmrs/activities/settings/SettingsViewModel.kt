package edu.upc.openmrs.activities.settings

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.utilities.ApplicationConstants.OpenMRSlanguage.LANGUAGE_CODE
import edu.upc.sdk.utilities.ApplicationConstants.PACKAGE_NAME
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class SettingsViewModel @Inject constructor(
        private val openMRSLogger: OpenMRSLogger
) : edu.upc.openmrs.activities.BaseViewModel<Unit>() {

    val appMarketUri: Uri = Uri.parse("market://details?id=${PACKAGE_NAME}")
    val appLinkUri: Uri = Uri.parse("http://play.google.com/store/apps/details?id=$PACKAGE_NAME")
    var languageListPosition: Int = 0
        get() {
            val language = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()
            return max(LANGUAGE_CODE.indexOf(language.language), 0)
        }
        set(position) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(LANGUAGE_CODE[position]))
            field = position
        }

    fun getBuildVersionInfo(context: Context): String {
        var versionName = ""
        val packageManager = context.packageManager
        val packageName = context.packageName
        try {
            versionName = packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("Failed to load meta-data", "NameNotFound: ${e.message}")
        } catch (e: NullPointerException) {
            Log.e("Failed to load meta-data", "NullPointer: ${e.message}")
        }

        return versionName
    }

    companion object {
        private const val ONE_KB = 1024
    }
}
