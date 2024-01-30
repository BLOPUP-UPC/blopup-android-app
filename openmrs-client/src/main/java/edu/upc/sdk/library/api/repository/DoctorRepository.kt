package edu.upc.sdk.library.api.repository

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoctorRepository @Inject constructor() : BaseRepository(null) {
    suspend fun sendMessageToDoctor(message: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            val contactRequest = ContactDoctorRequest(DOCTOR_PROVIDER_UUID, message)

            try {
                val response = restApi.contactDoctor(contactRequest).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Error sending message to doctor: ${response.errorBody()?.string()}"))
                }
                return@withContext Result.success(true)
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }


    companion object {
        const val DOCTOR_PROVIDER_UUID = "2775ad68-1a28-450f-a270-6ac5d0120636"
    }

    data class ContactDoctorRequest(
        @SerializedName("providerUuid") @Expose val providerUuid: String,
        @SerializedName("message") @Expose val message: String
    )
}
