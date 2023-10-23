package edu.upc.sdk.library.api.repository

import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.utilities.ApplicationConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepositoryKotlin @Inject constructor() : BaseRepository() {
    /**
     * Find patients.
     *
     * @param query patient query string
     * @return observable list of patients with matching query
     */
    suspend fun findPatients(query: String?): List<Patient> = withContext(Dispatchers.IO) {
        try {
            val call = restApi.getPatients(query, ApplicationConstants.API.FULL)
            val response = call.execute()
            if (response.isSuccessful) {
                return@withContext response.body()?.results.orEmpty()
            } else {
                throw Exception("Failed to find patients: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Error finding patients: ${e.message}")
        }
    }
}
