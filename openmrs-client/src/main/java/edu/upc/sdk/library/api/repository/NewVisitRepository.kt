package edu.upc.sdk.library.api.repository

import edu.upc.blopup.model.BloodPressure
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
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.VisitType
import edu.upc.sdk.library.models.typeConverters.VisitConverter
import edu.upc.sdk.utilities.ApplicationConstants.FACILITY_VISIT_TYPE_UUID
import edu.upc.sdk.utilities.DateUtils
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
    private val logger: OpenMRSLogger,
    private val oldVisitRepository: VisitRepository
) {
    suspend fun getVisitByUuid(visitUuid: UUID): Visit = withContext(Dispatchers.IO) {
        val openMRSVisit = restApi.getVisitByUuid(visitUuid.toString()).execute().body()
        return@withContext VisitConverter.createVisitFromOpenMRSVisit(openMRSVisit)
    }

    suspend fun getVisitsByPatientUuid(patientId: UUID): List<Visit> = withContext(Dispatchers.IO) {
        val result = restApi.findVisitsByPatientUUID(patientId.toString(), "custom:(uuid,patient:ref,location:ref,visitType:ref,startDatetime,stopDatetime,encounters:full)").execute()

        if (result.isSuccessful) {
            val visits = result.body()?.results
            return@withContext visits?.map { VisitConverter.createVisitFromOpenMRSVisit(it) } ?: emptyList()
        }

        throw IOException("Error getting visits by patient uuid: ${result.message()}")
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

    suspend fun startVisit(patient: Patient, bloodPressure: BloodPressure, heightCm: Int?, weightKg: Float?): Result<Visit> = withContext(Dispatchers.IO) {
        val now = Instant.now()
        val location = OpenmrsAndroid.getLocation()
        val openMRSVisit = OpenMRSVisit().apply {
            startDatetime = now.formatAsOpenMrsDate()
            this.patient = patient
            this.location = locationDAO.findLocationByName(location)
            visitType = VisitType("FACILITY", FACILITY_VISIT_TYPE_UUID)
        }

        restApi.startVisit(openMRSVisit).execute().run {
            if (!isSuccessful) {
                logger.e("Error starting visit: ${message()}")
                return@withContext Result.Error(IOException("Error starting visit ${message()}"))
            }

            val visitFromServer = this.body()!!

            val visit = Visit(
                UUID.fromString(visitFromServer.uuid),
                UUID.fromString(patient.uuid),
                location,
                visitFromServer.startDatetime.let { DateUtils.parseLocalDateFromOpenmrsDate(it) },
                bloodPressure,
                heightCm,
                weightKg
            )

            val result = addVisitEncounter(visit)
            if (result is Result.Success) {
                try {
                    oldVisitRepository.syncVisitsData(patient).toBlocking().first()
                } catch (e: Exception) {
                    logger.e("Error saving new visit in local database: ${e.javaClass.simpleName}: ${e.message}")
                }
            }

            return@withContext result
        }
    }

    private suspend fun addVisitEncounter(visit: Visit): Result<Visit> {
        val encounterCreate = Encountercreate().apply {
            this.visit = visit.id.toString()
            this.patient = visit.patientId.toString()
            encounterType = EncounterType.VITALS
            observations = listOfNotNull(
                Obscreate().apply {
                    concept = VitalsConceptType.SYSTOLIC_FIELD_CONCEPT
                    value = visit.bloodPressure.systolic.toString()
                    obsDatetime = visit.startDate.formatAsOpenMrsDate()
                    person = visit.patientId.toString()
                },
                Obscreate().apply {
                    concept = VitalsConceptType.DIASTOLIC_FIELD_CONCEPT
                    value = visit.bloodPressure.diastolic.toString()
                    obsDatetime = visit.startDate.formatAsOpenMrsDate()
                    person = visit.patientId.toString()
                },
                Obscreate().apply {
                    concept =
                        VitalsConceptType.HEART_RATE_FIELD_CONCEPT
                    value = visit.bloodPressure.pulse.toString()
                    obsDatetime = visit.startDate.formatAsOpenMrsDate()
                    person = visit.patientId.toString()
                },
                visit.heightCm?.let {
                    Obscreate().apply {
                        concept =
                            VitalsConceptType.HEIGHT_FIELD_CONCEPT
                        value = it.toString()
                        obsDatetime = visit.startDate.formatAsOpenMrsDate()
                        person = visit.patientId.toString()
                    }
                },
                visit.weightKg?.let {
                    Obscreate().apply {
                        concept =
                            VitalsConceptType.WEIGHT_FIELD_CONCEPT
                        value = it.toString()
                        obsDatetime = visit.startDate.formatAsOpenMrsDate()
                        person = visit.patientId.toString()
                    }
                }
            )
        }

        restApi.createEncounter(encounterCreate).execute().run {
            if (!isSuccessful) {
                logger.e("Error createing the encounter: ${message()}")
                deleteVisit(visit.id)
                return Result.Error(IOException("Error creating encounter ${message()}"))
            }
        }
        return Result.Success(visit)
    }

    private suspend fun deleteVisit(visitUuid: UUID): Boolean = withContext(Dispatchers.IO) {
        val call = restApi.deleteVisit(visitUuid.toString())
        val response = call.execute()
        if (response.isSuccessful) {
            return@withContext true
        }

        logger.e("Error deleting the visit with uuid: $visitUuid in the server $response")

        return@withContext false
    }

    suspend fun getActiveVisit(patientId: UUID) = withContext(Dispatchers.IO) {
        val result = restApi.findActiveVisitsByPatientUUID(patientId.toString()).execute()

        if (result.isSuccessful) {
            val visits = result.body()?.results
            if (visits != null) {
                when(visits.size) {
                    0 -> return@withContext null
                    1 -> return@withContext VisitConverter.createVisitFromOpenMRSVisit(visits[0])
                    else -> {
                        logger.i("More than one active visit for patient with id: $patientId")
                        logger.i("Returning the first active visit")

                        return@withContext VisitConverter.createVisitFromOpenMRSVisit(visits[0])
                    }
                }
            }
        }

        throw IOException("Error getting active visits by patient uuid: ${result.message()}")
    }

    suspend fun getLatestVisitWithHeight(patientId: UUID): Visit? {
        getVisitsByPatientUuid(patientId).let { visits ->
            return visits.filter { it.heightCm != null }.maxByOrNull { it.startDate }
        }
    }

    object VitalsConceptType {
        const val SYSTOLIC_FIELD_CONCEPT = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        const val DIASTOLIC_FIELD_CONCEPT = "5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        const val HEART_RATE_FIELD_CONCEPT = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        const val WEIGHT_FIELD_CONCEPT = "5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        const val HEIGHT_FIELD_CONCEPT = "5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    }

    object EncounterTypes {
        const val VITALS = "67a71486-1a54-468f-ac3e-7091a9a79584"

        @JvmField
        var ENCOUNTER_TYPES_DISPLAYS = arrayOf(
            EncounterType.VITALS
        )
    }
}
