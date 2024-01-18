package edu.upc.sdk.library.api.repository

import edu.upc.sdk.library.api.ObservationConcept
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
                ObservationConcept.RECOMMENDED_BY.uuid -> recommendedBy =
                    observation.displayValue ?: ""

                ObservationConcept.MEDICATION_NAME.uuid -> medicationName =
                    observation.displayValue?.trim() ?: ""

                ObservationConcept.TREATMENT_NOTES.uuid -> notes =
                    observation.displayValue?.trim() ?: ""

                ObservationConcept.MEDICATION_TYPE.uuid -> medicationType =
                    getMedicationTypesFromObservation(observation)

                ObservationConcept.ACTIVE.uuid -> {
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
                    ObservationConcept.RECOMMENDED_BY.uuid,
                    treatment.recommendedBy,
                    currentVisit.patient.uuid!!
                ),
                observation(
                    ObservationConcept.MEDICATION_NAME.uuid,
                    treatment.medicationName,
                    currentVisit.patient.uuid!!
                ),
                observation(
                    ObservationConcept.ACTIVE.uuid,
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
                    ObservationConcept.TREATMENT_NOTES.uuid,
                    notes,
                    encounter.patient!!
                )
            )
        }
        return encounter
    }

    private fun drugFamiliesObservation(patientUuid: String, treatment: Treatment) =
        Obscreate().apply {
            concept = ObservationConcept.MEDICATION_TYPE.uuid
            person = patientUuid
            obsDatetime = Instant.now().toString()
            groupMembers = treatment.medicationType.map {
                observation(
                    ObservationConcept.MEDICATION_TYPE.uuid,
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
                ObservationConcept.TREATMENT_ADHERENCE.uuid,
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

    suspend fun updateTreatment(
        treatmentToEdit: Treatment,
        treatmentUpdated: Treatment
    ): Result<Boolean> {
        val valuesToUpdate = updateValues(treatmentToEdit, treatmentUpdated)

        if (valuesToUpdate.isEmpty()) return Result.success(true)

        val encounterToUpdate =
            encounterRepository.getEncounterByUuid(treatmentToEdit.treatmentUuid!!)

        val observationsToUpdate = encounterToUpdate?.observations?.filter { observation ->
            valuesToUpdate.keys.any { key -> observation.display!!.contains(key) }
        }

        if (valuesToUpdate.keys.contains(ObservationConcept.MEDICATION_TYPE.display)) {
            removeOldObservationAndCreateNewOne(
                observationsToUpdate,
                valuesToUpdate,
                treatmentToEdit,
                encounterToUpdate!!
            )
        }

        if (valuesToUpdate.isEmpty()) return Result.success(true)

        val mapWithObservationsToUpdate = mapObservationWithNewValues(observationsToUpdate, valuesToUpdate)

        val requestBody = mapOf("obs" to mapWithObservationsToUpdate)

        return try {
            withContext(Dispatchers.IO) {
                val response =
                    restApi.updateEncounter(treatmentToEdit.treatmentUuid!!, requestBody)
                        .execute()
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Update treatment error: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapObservationWithNewValues(
        observationsToUpdate: List<Observation>?,
        valuesToUpdate: MutableMap<String, Any>
    ) = observationsToUpdate?.map {
        val observationUuid = it.uuid!!
        val value = valuesToUpdate.entries.find { (key, _) ->
            it.display?.contains(key) == true
        }?.value
        return@map mapOf("uuid" to observationUuid, "value" to value)
    }?.toList()

    private suspend fun removeOldObservationAndCreateNewOne(
        obsToUpdate: List<Observation>?,
        valuesToUpdate: MutableMap<String, Any>,
        treatmentToEdit: Treatment,
        encounterFound: Encounter
    ) {
        val medicationTypeObservation = obsToUpdate?.find { observation ->
            observation.display!!.contains(ObservationConcept.MEDICATION_TYPE.display)
        }
        withContext(Dispatchers.IO) {
            val response = restApi.deleteObservation(medicationTypeObservation?.uuid!!).execute()
            if (response.isSuccessful) {
                val groupMembersToCreate =
                    valuesToUpdate[ObservationConcept.MEDICATION_TYPE.display] as Set<MedicationType>

                val newMedicationTypeObservation = Obscreate().apply {
                    encounter = treatmentToEdit.treatmentUuid
                    concept = ObservationConcept.MEDICATION_TYPE.uuid
                    person = encounterFound.patient?.uuid
                    obsDatetime = Instant.now().toString()
                    groupMembers = groupMembersToCreate.map {
                        observation(
                            ObservationConcept.MEDICATION_TYPE.uuid,
                            it.conceptId,
                            encounterFound.patient?.uuid!!
                        )
                    }
                }

                withContext(Dispatchers.IO) {
                    try {
                        val createObservationResponse =
                            restApi.createObs(newMedicationTypeObservation).execute()
                        if (createObservationResponse.isSuccessful) {
                            valuesToUpdate.remove(ObservationConcept.MEDICATION_TYPE.display)
                            Result.success(true)
                        } else {
                            crashlytics.reportUnsuccessfulResponse(
                                createObservationResponse,
                                createObservationResponse.message()
                            )
                            Result.failure(Exception("Create new medication type observation error: ${createObservationResponse.message()}"))
                        }
                    } catch (e: Exception) {
                        crashlytics.reportException(
                            e,
                            "Create new medication type observation error"
                        )
                        Result.failure(e)
                    }
                }

            } else {
                crashlytics.reportUnsuccessfulResponse(response, response.message())
                Result.failure(Exception("Delete medication type observation error: ${response.message()}"))
            }
        }
    }

    private fun updateValues(
        treatmentToEdit: Treatment,
        treatmentUpdated: Treatment
    ): MutableMap<String, Any> {
        val valuesToUpdate = mutableMapOf<String, Any>()

        if (treatmentToEdit.recommendedBy != treatmentUpdated.recommendedBy && treatmentUpdated.recommendedBy.isNotEmpty()) {
            valuesToUpdate[ObservationConcept.RECOMMENDED_BY.display] =
                treatmentUpdated.recommendedBy
        }
        if (treatmentToEdit.medicationName != treatmentUpdated.medicationName) {
            valuesToUpdate[ObservationConcept.MEDICATION_NAME.display] =
                treatmentUpdated.medicationName
        }
        if (treatmentToEdit.medicationType != treatmentUpdated.medicationType) {
            valuesToUpdate[ObservationConcept.MEDICATION_TYPE.display] =
                treatmentUpdated.medicationType
        }
        if (treatmentToEdit.notes != treatmentUpdated.notes) {
            valuesToUpdate[ObservationConcept.TREATMENT_NOTES.display] =
                treatmentUpdated.notes.toString()
        }
        return valuesToUpdate
    }

    companion object {
        const val TREATMENT_ENCOUNTER_TYPE = "Treatment"
    }
}
