package edu.upc.sdk.library.api.repository

import arrow.core.Either
import arrow.core.right
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
    suspend fun findPatients(query: String?): Either<Error, List<Patient>> =
        withContext(Dispatchers.IO) {
            val call = restApi.getPatients(query, ApplicationConstants.API.FULL)
            val response = call.execute()
            if (response.isSuccessful) {
                Either.Right(response.body()?.results.orEmpty())
            } else {
                Either.Left(Error("Failed to find patients: ${response.code()} - ${response.message()}"))
            }
        }
}
