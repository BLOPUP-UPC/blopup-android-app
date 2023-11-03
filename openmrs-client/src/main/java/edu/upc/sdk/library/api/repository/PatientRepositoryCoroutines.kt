package edu.upc.sdk.library.api.repository

import arrow.core.Either
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.utilities.ApplicationConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepositoryCoroutines @Inject constructor() : BaseRepository() {
    /**
     * Find patients.
     *
     * @param query patient query string
     * @return observable list of patients with matching query
     */
    suspend fun findPatients(query: String?): Either<Error, List<Patient>> =
        withContext(Dispatchers.IO) {
            try {
                val call = restApi.getPatients(query, ApplicationConstants.API.FULL)
                val response = call.execute()
                if (response.isSuccessful) {
                    Either.Right(response.body()?.results.orEmpty())
                } else {
                    crashlytics.reportUnsuccessfulResponse(response, "Failed to find patients")
                    Either.Left(Error("Failed to find patients: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                crashlytics.reportException(e, "Failed to find patients")
                Either.Left(Error("Failed to find patients: ${e.message}"))
            }
        }
}
