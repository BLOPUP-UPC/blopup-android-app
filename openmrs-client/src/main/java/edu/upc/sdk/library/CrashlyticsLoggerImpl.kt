package edu.upc.sdk.library

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.setCustomKeys
import com.google.firebase.ktx.Firebase
import edu.upc.BuildConfig

import retrofit2.Response

private const val ERROR_TYPE = "error_type"
private const val OPENMRS_API_ERROR = "openmrs_api_error"
private const val EXCEPTION = "exception"
private const val RESPONSE_CODE = "response_code"

class CrashlyticsLoggerImpl : CrashlyticsLogger {

    override fun reportUnsuccessfulResponse(response: Response<*>, message: String) {
        val exception = Exception("${message}: ${response.code()} - ${response.message()}")
        OpenMRSLogger().e("${message}: ${response.code()} - ${response.message()}", exception)
        if (BuildConfig.DEBUG) return

        Firebase.crashlytics.setCustomKeys {
            key(ERROR_TYPE, OPENMRS_API_ERROR)
            key(RESPONSE_CODE, response.code())
        }
        Firebase.crashlytics.log("Unsuccessful response from OpenMRS API")
        Firebase.crashlytics.recordException(exception)
    }

    override fun reportException(exception: Exception, message: String) {
        OpenMRSLogger().e("${message}: ${exception.message}", exception)
        if (BuildConfig.DEBUG) return

        Firebase.crashlytics.setCustomKey(ERROR_TYPE, EXCEPTION)
        Firebase.crashlytics.log("${message}. Reason: ${exception.message}")

        Firebase.crashlytics.log("Last 20 logs before exception occurred:")
        val logcat = Runtime.getRuntime().exec("logcat -d -t 20")
        logcat.inputStream.bufferedReader().forEachLine { line ->
            Firebase.crashlytics.log(line)
        }
        Firebase.crashlytics.log("End of logs")

        Firebase.crashlytics.recordException(exception)
    }
}