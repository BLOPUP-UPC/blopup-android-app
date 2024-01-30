package edu.upc.sdk.library.api.repository

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import edu.upc.sdk.library.CrashlyticsLoggerImpl
import edu.upc.sdk.library.models.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun getAllDoctors(): List<Provider> {
        return withContext(Dispatchers.IO) {
            try {
                val response = restApi.providerList.execute()
                if (response.isSuccessful) {
                    return@withContext response.body()?.results?.filter<Provider> { it.identifier == TreatmentRepository.DOCTOR }
                        ?.filter {
                            it.person?.isVoided == false
                        } ?: emptyList()
                } else {
                    throw Exception("Failed to get providers: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                throw Exception("Failed to get providers: ${e.message}", e)
            }
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
