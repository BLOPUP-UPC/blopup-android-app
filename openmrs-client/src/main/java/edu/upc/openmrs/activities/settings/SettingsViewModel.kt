package edu.upc.openmrs.activities.settings

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.R
import edu.upc.openmrs.utilities.LanguageUtils
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.utilities.ApplicationConstants.OpenMRSlanguage.LANGUAGE_CODE
import edu.upc.sdk.utilities.ApplicationConstants.PACKAGE_NAME
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
        private val openMRSLogger: OpenMRSLogger
) : edu.upc.openmrs.activities.BaseViewModel<Unit>() {

    val appMarketUri: Uri = Uri.parse("market://details?id=${PACKAGE_NAME}")
    val appLinkUri: Uri = Uri.parse("http://play.google.com/store/apps/details?id=$PACKAGE_NAME")
    var languageListPosition: Int = 0
        get() {
            val language = LanguageUtils.getLanguage()
            var i = 0
            while (i < LANGUAGE_CODE.size) {
                if (language == LANGUAGE_CODE[i]) {
                    return i
                }
                i++
            }
            return 0
        }
        set(position) {
            LanguageUtils.setLanguage(LANGUAGE_CODE[position])
            field = position
        }

    fun getBuildVersionInfo(context: Context): String {
        var versionName = ""
        var buildVersion = 0
        val packageManager = context.packageManager
        val packageName = context.packageName
        try {
            versionName = packageManager.getPackageInfo(packageName, 0).versionName
            val ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            buildVersion = ai.metaData.getInt("buildVersion")
        } catch (e: PackageManager.NameNotFoundException) {
            openMRSLogger.e("Failed to load meta-data, NameNotFound: ${e.message}")
        } catch (e: NullPointerException) {
            openMRSLogger.e("Failed to load meta-data, NullPointer: ${e.message}")
        }

        return versionName + context.getString(R.string.frag_settings_build) + buildVersion
    }

    companion object {
        private const val ONE_KB = 1024
    }
}
