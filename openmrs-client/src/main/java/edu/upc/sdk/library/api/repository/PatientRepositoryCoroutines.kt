package edu.upc.sdk.library.api.repository

import arrow.core.Either
import edu.upc.sdk.library.CrashlyticsLogger
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.utilities.ApplicationConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepositoryCoroutines @Inject constructor() : BaseRepository(null) {
    /**
     * Find patients.
     *
     * @param query patient query string
     * @return observable list of patients with matching query
     */

    constructor(crashlyticsLogger: CrashlyticsLogger? = null) : this() {
        this.crashlytics = crashlyticsLogger
    }

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

    suspend fun downloadPatientByUuid(patientUuid: String): Patient =
        withContext(Dispatchers.IO) {
            try {
                val call = restApi.getPatientByUUID(patientUuid, ApplicationConstants.API.FULL)
                val response = call.execute()
                if (response.isSuccessful) {
                    val newPatientDto = response.body()
                    return@withContext newPatientDto!!.patient
                } else {
                    crashlytics.reportUnsuccessfulResponse(response, "Failed to download patient")
                    throw IOException("Error with downloading patient: " + response.message())
                }
            } catch (e: Exception) {
                crashlytics.reportException(e, "Failed to download patient")
                throw IOException("Error with downloading patient: " + e.message)
            }
        }
}
