package edu.upc.sdk.library.api.repository

import arrow.core.Either
import edu.upc.sdk.library.CrashlyticsLogger
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.utilities.ApplicationConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class PatientRepositoryCoroutines @Inject constructor() : BaseRepository(null) {

    @Inject
    lateinit var patientDAO: PatientDAO
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
                Either.Left(Error("Failed to find patients: ${e.message}", e))
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

    fun deletePatient(patientId: Long) {
        patientDAO.deletePatient(patientId)
    }

    suspend fun savePatientLocally(patient: Patient) : Patient =
        withContext(Dispatchers.IO) {
            try{
                val id = patientDAO.savePatient(patient)
                    .single()
                    .toBlocking()
                    .first()
                patient.id = id
                VisitRepository().syncVisitsData(patient)
                VisitRepository().syncLastVitals(patient.uuid)
                return@withContext patient
            } catch (e: Exception) {
                crashlytics.reportException(e, "Failed to save patient locally")
                throw IOException("Error with saving patient locally: " + e.message)
            }
    }

    fun findPatientByUUID(patientUuid: String): Patient? = patientDAO.findPatientByUUID(patientUuid)
}
