package edu.upc.sdk.library.api.repository

import edu.upc.sdk.library.CrashlyticsLogger
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.execute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepositoryCoroutines @Inject constructor(
    private val restApi: RestApi,
    private val patientDAO: PatientDAO,
    private var crashlytics: CrashlyticsLogger
) {
    suspend fun getAllPatientsLocally(): Result<List<Patient>> = withContext(Dispatchers.IO) {
        try {
            val patientList = patientDAO.allPatients.execute()
            Result.Success(patientList)
        } catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }

    suspend fun findPatients(query: String?): Result<List<Patient>> =
        withContext(Dispatchers.IO) {
            try {
                val call = restApi.getPatientsDto(query, ApplicationConstants.API.FULL)
                val response = call.execute()
                if (response.isSuccessful) {
                    val patientDtos = response.body()?.results.orEmpty()
                    Result.Success(patientDtos.map { it.patient })
                } else {
                    crashlytics.reportUnsuccessfulResponse(response, "Failed to find patients")
                    Result.Error(Exception("Failed to find patients: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                crashlytics.reportException(e, "Failed to find patients")
                Result.Error(Exception("Failed to find patients: ${e.message}", e))
            }
        }

    suspend fun downloadPatientByUuid(patientUuid: String): Patient =
        withContext(Dispatchers.IO) {
            try {
                val call = restApi.getPatientByUUID(patientUuid, ApplicationConstants.API.FULL)
                val response = call.execute()
                if (response.isSuccessful) {
                    val newPatientDto = response.body()
                    if (newPatientDto?.patient?.isVoided!!) {
                        throw IOException("Patient has been removed from remote database")
                    } else {
                        return@withContext newPatientDto.patient
                    }
                } else {
                    crashlytics.reportUnsuccessfulResponse(response, "Failed to download patient")
                    throw IOException("Error with downloading patient: " + response.message())
                }
            } catch (e: Exception) {
                crashlytics.reportException(e, "Failed to download patient")
                throw IOException("Error with downloading patient: " + e.message, e)
            }
        }

    fun deletePatient(patientId: UUID) {
        patientDAO.deletePatient(patientId)
    }

    suspend fun savePatientLocally(patient: Patient): Patient =
        withContext(Dispatchers.IO) {
            try {
                val id = patientDAO.savePatient(patient)
                    .single()
                    .toBlocking()
                    .first()
                patient.id = id
                return@withContext patient
            } catch (e: Exception) {
                crashlytics.reportException(e, "Failed to save patient locally")
                throw IOException("Error with saving patient locally: " + e.message)
            }
        }

    fun findPatientByUUID(patientUuid: String): Patient? = patientDAO.findPatientByUUID(patientUuid)
}
