package edu.upc.sdk.library.api.repository

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import edu.upc.sdk.library.CrashlyticsLoggerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoctorRepository @Inject constructor() : BaseRepository(CrashlyticsLoggerImpl()) {
    suspend fun sendMessageToDoctor(message: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            val contactRequest = ContactDoctorRequest(DOCTOR_PROVIDER_UUID, message)

            try {
                val response = restApi.contactDoctor(contactRequest).execute()
                if (!response.isSuccessful) {
                    crashlytics.reportUnsuccessfulResponse(response, FAILED_MESSAGE_DOCTOR)
                    return@withContext Result.failure(
                        Exception("$FAILED_MESSAGE_DOCTOR: ${response.errorBody()?.string()}")
                    )
                }
                return@withContext Result.success(true)
            } catch (e: Exception) {
                crashlytics.reportException(e, FAILED_MESSAGE_DOCTOR)
                return@withContext Result.failure(e)
            }
        }


    companion object {
        const val DOCTOR_PROVIDER_UUID = "2775ad68-1a28-450f-a270-6ac5d0120636"
        private const val FAILED_MESSAGE_DOCTOR = "Failed to message doctor"
    }

    data class ContactDoctorRequest(
        @SerializedName("providerUuid") @Expose val providerUuid: String,
        @SerializedName("message") @Expose val message: String
    )
}
