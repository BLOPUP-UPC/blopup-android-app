package edu.upc

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.setCustomKeys
import com.google.firebase.ktx.Firebase
import edu.upc.sdk.library.OpenMRSLogger
import retrofit2.Response

private const val ERROR_TYPE = "error_type"
private const val OPENMRS_API_ERROR = "openmrs_api_error"
private const val EXCEPTION = "exception"
private const val RESPONSE_CODE = "response_code"

class CrashlyticsLoggerImpl: CrashlyticsLogger {

    override fun reportUnsuccessfulResponse(response: Response<*>, message: String) {
        Firebase.crashlytics.setCustomKeys {
            key(ERROR_TYPE, OPENMRS_API_ERROR)
            key(RESPONSE_CODE, response.code())
        }
        Firebase.crashlytics.log("Unsuccessful response from OpenMRS API")
        Firebase.crashlytics.recordException(Exception("${message}: ${response.code()} - ${response.message()}"))
        OpenMRSLogger().e("${message}: ${response.code()} - ${response.message()}", Exception())
    }

    override fun reportException(exception: Exception, message: String) {
        Firebase.crashlytics.setCustomKey(ERROR_TYPE, EXCEPTION)

        Firebase.crashlytics.log("${message}. Reason: ${exception.message}")
        Firebase.crashlytics.recordException(exception)
        OpenMRSLogger().e("${message}: ${exception.message}", exception)
    }
}
