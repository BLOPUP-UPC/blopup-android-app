package edu.upc.sdk.library.api.repository

import edu.upc.blopup.model.Visit
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.dao.LocationDAO
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.library.models.Encountercreate
import edu.upc.sdk.library.models.Obscreate
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.VisitType
import edu.upc.sdk.library.models.typeConverters.VisitConverter
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.DateUtils.formatAsOpenMrsDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.io.IOException
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import edu.upc.sdk.library.models.Visit as OpenMRSVisit

@Singleton
class NewVisitRepository @Inject constructor(
    val restApi: RestApi,
    val visitDAO: VisitDAO,
    private val locationDAO: LocationDAO,
    private val logger: OpenMRSLogger
) {
    fun getVisitByUuid(visitUuid: UUID): Visit {
        val openMRSVisit = restApi.getVisitByUuid(visitUuid.toString()).execute().body()
        return VisitConverter.createVisitFromOpenMRSVisit(openMRSVisit)
    }

    suspend fun endVisit(visitId: UUID): Boolean = withContext(Dispatchers.IO) {
        val endVisitDateTime = Instant.now().formatAsOpenMrsDate()
        val visitWithEndDate = OpenMRSVisit().apply {
            stopDatetime = endVisitDateTime
        }

        val call: Call<OpenMRSVisit> = restApi.endVisitByUUID(visitId.toString(), visitWithEndDate)
        val response = call.execute()

        if (response.isSuccessful) {
            try {
                val localVisitId =
                    visitDAO.getVisitsIDByUUID(visitId.toString()).single().toBlocking().first()
                val localVisit = visitDAO.getVisitByID(localVisitId).single().toBlocking().first()
                localVisit.patient.id?.let {
                    localVisit.stopDatetime = endVisitDateTime
                    visitDAO.saveOrUpdate(localVisit, it).single().toBlocking().first()
                }
            } catch (e: Exception) {
                logger.e("Error updating visit end date in local database: ${e.javaClass.simpleName}: ${e.message}")
            }
            true
        } else {
            throw Exception("endVisitByUuid error: " + response.message())
        }
    }

    suspend fun startVisit(patient: Patient, visit: Visit): Visit = withContext(Dispatchers.IO) {
        val now = Instant.now()
        val openMRSVisit = OpenMRSVisit().apply {
            startDatetime = now.formatAsOpenMrsDate()
            this.patient = patient
            location = locationDAO.findLocationByName(OpenmrsAndroid.getLocation())
            visitType = VisitType("FACILITY", OpenmrsAndroid.getVisitTypeUUID())
        }
        val encounterCreate = Encountercreate().apply {
            this.visit = visit.id.toString()
            this.patient = patient.uuid
            encounterType = EncounterType.VITALS
            observations = listOfNotNull(
                Obscreate().apply {
                    concept = ApplicationConstants.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT
                    value = visit.bloodPressure.systolic.toString()
                    obsDatetime = now.toString()
                    person = patient.uuid
                },
                Obscreate().apply {
                    concept = ApplicationConstants.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT
                    value = visit.bloodPressure.diastolic.toString()
                    obsDatetime = now.toString()
                    person = patient.uuid
                },
                Obscreate().apply {
                    concept =
                        ApplicationConstants.VitalsConceptType.HEART_RATE_FIELD_CONCEPT
                    value = visit.bloodPressure.pulse.toString()
                    obsDatetime = now.toString()
                    person = patient.uuid
                },
                visit.heightCm?.let {
                    Obscreate().apply {
                        concept =
                            ApplicationConstants.VitalsConceptType.HEIGHT_FIELD_CONCEPT
                        value = it.toString()
                        obsDatetime = now.toString()
                        person = patient.uuid
                    }
                },
                visit.weightKg?.let {
                    Obscreate().apply {
                        concept =
                            ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT
                        value = it.toString()
                        obsDatetime = now.toString()
                        person = patient.uuid
                    }
                }
            )
        }

        restApi.startVisit(openMRSVisit).execute().run {
            if (!isSuccessful) {
                throw IOException("Error starting visit ${message()}")
            }

            try {
                val newVisit = body()
                restApi.createEncounter(encounterCreate).execute().run {
                    if (!isSuccessful) {
                        throw IOException("syncEncounter error: ${message()}")
                    }
                }

                try {
                    visitDAO.saveOrUpdate(newVisit, patient.id!!).toBlocking()
                    // In the old impl we are updating the visit with the encounters and storing the encounter in Room
                } catch (e: Exception) {
                    logger.e("Error saving new visit in local database: ${e.javaClass.simpleName}: ${e.message}")
                }
                return@withContext VisitConverter.createVisitFromOpenMRSVisit(newVisit)
            } catch (e: Exception) {
                logger.e("Error starting visit: ${e.javaClass.simpleName}: ${e.message}")
                logger.i("Deleting visit with uuid: ${visit.id}")
                deleteVisit(visit.id)
                throw e
            }
        }
    }

    suspend fun deleteVisit(visitUuid: UUID): Boolean = withContext(Dispatchers.IO) {
        val call = restApi.deleteVisit(visitUuid.toString())
        val response = call.execute()
        if (response.isSuccessful) {
            visitDAO.deleteVisitByUuid(visitUuid.toString()).toBlocking().first()

            return@withContext true
        }

        logger.e("Error deleting the visit with uuid: $visitUuid in the server $response")

        return@withContext false
    }
}
