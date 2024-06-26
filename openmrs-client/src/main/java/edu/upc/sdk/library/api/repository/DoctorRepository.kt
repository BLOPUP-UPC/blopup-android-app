package edu.upc.sdk.library.api.repository

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import edu.upc.blopup.model.Doctor
import edu.upc.sdk.library.CrashlyticsLogger
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.models.Provider
import edu.upc.sdk.library.models.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoctorRepository @Inject constructor(
    private val restApi: RestApi,
    private val crashlytics: CrashlyticsLogger
) {
    suspend fun sendMessageToDoctor(message: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            val contactRequest = ContactDoctorRequest(DOCTOR_PROVIDER_UUID, message)

            try {
                val response = restApi.contactDoctor(contactRequest).execute()
                if (!response.isSuccessful) {
                    crashlytics.reportUnsuccessfulResponse(response, FAILED_MESSAGE_DOCTOR)
                    return@withContext Result.Error(
                        Exception(
                            "$FAILED_MESSAGE_DOCTOR: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
                return@withContext Result.Success(true)
            } catch (e: Exception) {
                crashlytics.reportException(e, FAILED_MESSAGE_DOCTOR)
                return@withContext Result.Error(
                    Exception(
                        "$FAILED_MESSAGE_DOCTOR: ${e.message}",
                        e
                    )
                )
            }
        }

    suspend fun getAllDoctors(): Result<List<Doctor>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = restApi.providerList.execute()
                if (response.isSuccessful) {
                    val result =
                        response.body()?.results?.filter<Provider> { it.identifier == TreatmentRepository.DOCTOR }
                            ?.filter {
                                it.person?.isVoided == false
                            }?.map { provider ->
                                val registrationNumber =
                                    provider.attributes.find { it.attributeType?.uuid == REGISTRATION_NUMBER_UUID }?.value
                                        ?: ""
                                Doctor(
                                    provider.uuid!!,
                                    provider.person?.display?.substringAfter("-")?.trim() ?: "",
                                    registrationNumber
                                )
                            } ?: emptyList()
                    Result.Success(result)
                } else {
                    Result.Error(Exception("Failed to get providers: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    companion object {
        const val DOCTOR_PROVIDER_UUID = "2775ad68-1a28-450f-a270-6ac5d0120636"
        private const val FAILED_MESSAGE_DOCTOR = "Failed to message doctor"
        const val REGISTRATION_NUMBER_UUID = "f4778183-b518-4842-946a-794fbff0e2e1"
    }

    data class ContactDoctorRequest(
        @SerializedName("providerUuid") @Expose val providerUuid: String,
        @SerializedName("message") @Expose val message: String
    )
}
