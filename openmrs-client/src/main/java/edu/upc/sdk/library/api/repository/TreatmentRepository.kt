package edu.upc.sdk.library.api.repository

import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.library.models.Encountercreate
import edu.upc.sdk.library.models.MedicationType
import edu.upc.sdk.library.models.Obscreate
import edu.upc.sdk.library.models.Observation
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.library.models.Treatment
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.DateUtils.parseFromOpenmrsDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TreatmentRepository @Inject constructor(
    val visitRepository: VisitRepository,
    val encounterRepository: EncounterRepository
) :
    BaseRepository(null) {


    suspend fun saveTreatment(treatment: Treatment) {
        val currentVisit = visitRepository.getVisitById(treatment.visitId)
        val encounter = createEncounterFromTreatment(currentVisit, treatment)


        withContext(Dispatchers.IO) {
            try {
                val response = restApi.createEncounter(encounter).execute()
                if (response.isSuccessful) {
                    return@withContext response.body()
                } else {
                    throw Exception("Failed to create encounter: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                throw Exception("Failed to create encounter: ${e.message}", e)
            }
        }
    }

    suspend fun finalise(treatment: Treatment): ResultType {
        return try {
            val observation = getObservationByUuid(treatment.observationStatusUuid!!)

            val value = mapOf("value" to 0, "obsDatetime" to treatment.inactiveDate.toString())

            withContext(Dispatchers.IO) {
                val response = restApi.updateObservation(observation?.uuid, value).execute()
                if (response.isSuccessful) {
                    ResultType.FinalisedTreatmentSuccess
                } else {
                    throw Exception("Finalised treatment error: ${response.message()}")
                }
            }
            ResultType.FinalisedTreatmentSuccess
        } catch (e: Exception) {
            ResultType.FinalisedTreatmentError
        }
    }

    suspend fun fetchAllActiveTreatments(patient: Patient): List<Treatment> {
        val encounters = fetchAllTreatments(patient)

        return encounters
            .filter { treatment ->
                treatment.isActive
            }
    }

    suspend fun fetchActiveTreatmentsAtAGivenTime(patient: Patient, visit: Visit): List<Treatment> {
        val visitDate = parseFromOpenmrsDate(visit.startDatetime)

        val treatments = fetchAllTreatments(patient)
            .filter {
                it.creationDate.isBefore(visitDate) || it.visitUuid == visit.uuid
            }

        return treatments.filter { treatment ->
            (treatment.isActive) or (!treatment.isActive && treatment.inactiveDate!!.isAfter(
                visitDate
            ))
        }
    }

    private suspend fun fetchAllTreatments(patient: Patient): List<Treatment> {
        val visits: List<Visit>
        withContext(Dispatchers.IO) {
            visits = visitRepository.getAllVisitsForPatient(patient).toBlocking().first()
        }

        return visits.flatMap { visit ->
            visit.encounters.map {
                it.apply {
                    visitID = visit.id
                    visitUuid = visit.uuid
                }
            }
                .filter { encounter ->
                    encounter.encounterType?.display == EncounterType.TREATMENT
                }
                .map { encounter ->
                    getTreatmentFromEncounter(encounter)
                }
        }
    }

    private fun getTreatmentFromEncounter(encounter: Encounter): Treatment {
        val visitId = encounter.visitID ?: 0
        val visitUuid = encounter.visit?.uuid
        val treatmentUuid = encounter.uuid
        val creationDate = parseFromOpenmrsDate(encounter.encounterDate!!)

        var recommendedBy = ""
        var medicationName = ""
        var notes: String? = null
        var medicationType: Set<MedicationType> = emptySet()
        var isActive = false
        var observationStatusUuid: String? = null
        var inactiveDate: Instant? = null
        encounter.observations.map { observation ->
            when (observation.concept?.uuid) {
                RECOMMENDED_BY_CONCEPT_ID -> recommendedBy =
                    observation.displayValue ?: ""

                MEDICATION_NAME_CONCEPT_ID -> medicationName =
                    observation.displayValue?.trim() ?: ""

                TREATMENT_NOTES_CONCEPT_ID -> notes =
                    observation.displayValue?.trim() ?: ""

                MEDICATION_TYPE_CONCEPT_ID -> medicationType =
                    getMedicationTypesFromObservation(observation)

                ACTIVE_CONCEPT_ID -> {
                    observationStatusUuid = observation.uuid
                    isActive = observation.displayValue?.trim() == "1.0"
                    if (!isActive) {
                        inactiveDate = parseFromOpenmrsDate(observation.obsDatetime!!)
                    }
                }
            }
        }
        return Treatment(
            recommendedBy = recommendedBy,
            medicationName = medicationName,
            medicationType = medicationType,
            notes = notes,
            isActive = isActive,
            visitUuid = visitUuid,
            visitId = visitId,
            treatmentUuid = treatmentUuid,
            observationStatusUuid = observationStatusUuid,
            inactiveDate = inactiveDate,
            creationDate = creationDate
        )
    }

    private fun getMedicationTypesFromObservation(observation: Observation): Set<MedicationType> {
        return observation.groupMembers?.map { groupMember ->
            MedicationType.values().find { medicationType ->
                medicationType.conceptId == groupMember.valueCodedName
            }!!
        }?.toSet() ?: emptySet()
    }

    private fun createEncounterFromTreatment(currentVisit: Visit, treatment: Treatment) =
        Encountercreate().apply {
            encounterType = TREATMENT_ENCOUNTER_TYPE
            patient = currentVisit.patient.uuid
            visit = currentVisit.uuid
            observations = listOf(
                observation(
                    RECOMMENDED_BY_CONCEPT_ID,
                    treatment.recommendedBy,
                    currentVisit.patient.uuid!!
                ),
                observation(
                    MEDICATION_NAME_CONCEPT_ID,
                    treatment.medicationName,
                    currentVisit.patient.uuid!!
                ),
                observation(
                    ACTIVE_CONCEPT_ID,
                    if (treatment.isActive) "1.0" else "0.0",
                    currentVisit.patient.uuid!!
                ),
                drugFamiliesObservation(currentVisit.patient.uuid!!, treatment)
            )
        }.let { encounter ->
            addNotesIfPresent(treatment.notes, encounter)
        }

    private fun addNotesIfPresent(notes: String?, encounter: Encountercreate): Encountercreate {
        if (notes != null) {
            encounter.observations = encounter.observations.plus(
                observation(
                    TREATMENT_NOTES_CONCEPT_ID,
                    notes,
                    encounter.patient!!
                )
            )
        }
        return encounter
    }

    private fun drugFamiliesObservation(patientUuid: String, treatment: Treatment) =
        Obscreate().apply {
            concept = MEDICATION_TYPE_CONCEPT_ID
            person = patientUuid
            obsDatetime = Instant.now().toString()
            groupMembers = treatment.medicationType.map {
                observation(
                    MEDICATION_TYPE_CONCEPT_ID,
                    it.conceptId,
                    patientUuid
                )
            }
        }

    private fun observation(concept: String, value: String, patientId: String) =
        Obscreate().apply {
            this.concept = concept
            this.value = value
            obsDatetime = Instant.now().toString()
            person = patientId
        }

    private suspend fun getObservationByUuid(uuid: String): Observation? {
        return withContext(Dispatchers.IO) {
            val response = restApi.getObservationByUuid(uuid).execute()
            if (response.isSuccessful) {
                response.body()
            } else {
                throw Exception("Get observation error: ${response.message()}")
            }
        }
    }

    suspend fun updateTreatment(valuesToUpdate: Map<String, Any>, encounterUuid: String) : Result<Boolean> {
        var result: Result<Boolean> =
            Result.failure(Exception("Could not update Treatment"))

        val encounter = encounterRepository.getEncounterByUuid(encounterUuid)

        val obsToUpdate = encounter?.observations?.filter { observation ->
            valuesToUpdate.keys.any { key ->
                observation.display!!.contains(key)
            }
        }

        val obsMapToUpdate = obsToUpdate?.associate { observation ->
            observation.uuid!! to valuesToUpdate.entries.find { (key, _) ->
                observation.display?.contains(key) == true
            }?.value
        } ?: emptyMap()

        obsMapToUpdate.forEach {
            val value = mapOf("value" to it.value)

            withContext(Dispatchers.IO) {
                result = try {
                    val response = restApi.updateObservation(it.key, value).execute()
                    if (response.isSuccessful) {
                        Result.success(true)
                    } else {
                        throw Exception("Update treatment error: ${response.message()}")
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }
        return result
    }


    suspend fun saveTreatmentAdherence(
        treatmentAdherence: Map<String, Boolean>,
        patientUuid: String
    ): Result<Boolean> {
        var result: Result<Boolean> =
            Result.failure(Exception("Could not save treatment adherence"))
        treatmentAdherence.map {
            val treatment = it.key
            val adherence = it.value

            observation(
                TREATMENT_ADHERENCE_ID,
                if (adherence) "1.0" else "0.0",
                patientUuid
            ).apply { encounter = treatment }
        }.map {
            withContext(Dispatchers.IO) {
                result = try {
                    val response = restApi.createObs(it).execute()
                    if (response.isSuccessful) {
                        Result.success(true)
                    } else {
                        crashlytics.reportUnsuccessfulResponse(response, response.message())
                        Result.failure(Exception("Save treatment adherence error: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    crashlytics.reportException(e, "Save treatment adherence error")
                    Result.failure(e)
                }
            }
        }
        return result
    }

    companion object {
        const val MEDICATION_NAME_CONCEPT_ID = "a721776b-fd0f-41ea-821b-0d0df94d5560"
        const val RECOMMENDED_BY_CONCEPT_ID = "c1164da7-0b4f-490f-85da-0c4aac4ca8a1"
        const val ACTIVE_CONCEPT_ID = "81f60010-961e-4bc5-aa04-435c7ace1ee3"
        const val TREATMENT_NOTES_CONCEPT_ID = "dfa881a4-5c88-4057-958b-f583c8edbdef"
        const val MEDICATION_TYPE_CONCEPT_ID = "1a8f49cc-488b-4788-adb3-72c499108772"
        const val TREATMENT_ADHERENCE_ID = "87e51329-cc96-426d-bc71-ccef8892ce72"

        const val TREATMENT_ENCOUNTER_TYPE = "Treatment"
    }
}
