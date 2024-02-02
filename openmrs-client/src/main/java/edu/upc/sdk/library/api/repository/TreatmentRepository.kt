package edu.upc.sdk.library.api.repository

import edu.upc.sdk.library.api.ObservationConcept
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.EncounterProviderCreate
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.library.models.Encountercreate
import edu.upc.sdk.library.models.MedicationType
import edu.upc.sdk.library.models.Obscreate
import edu.upc.sdk.library.models.Observation
import edu.upc.sdk.library.models.Patient
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
    private val encounterRepository: EncounterRepository,
) :
    BaseRepository(null) {

    suspend fun saveTreatment(treatment: Treatment) {
        withContext(Dispatchers.IO) {
            val currentVisit = visitRepository.getVisitByUuid(treatment.visitUuid)

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
    }

    suspend fun finalise(treatment: Treatment): Result<Boolean> {
        return try {
            val observation = getObservationByUuid(treatment.observationStatusUuid!!)

            val value = mapOf("value" to 0, "obsDatetime" to treatment.inactiveDate.toString())

            withContext(Dispatchers.IO) {
                val response = restApi.updateObservation(observation?.uuid, value).execute()
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Finalised treatment error: ${response.message()}"))
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Finalised treatment error: ${e.message}"))
        }
    }

    suspend fun updateTreatment(
        treatmentToEdit: Treatment,
        treatmentToUpdate: Treatment
    ): Result<Boolean> {
        val needsChanges = diffValues(treatmentToEdit, treatmentToUpdate)

        if (!needsChanges) return Result.success(true)

        val response = encounterRepository.removeEncounter(treatmentToEdit.treatmentUuid)

        return if (response.isSuccess) {
            saveTreatment(treatmentToUpdate)
            Result.success(true)
        } else {
            Result.failure(Exception("Failed to remove encounter"))
        }
    }

    suspend fun fetchAllActiveTreatments(patient: Patient): Result<List<Treatment>> {

        val result = fetchAllTreatments(patient)

        if (result.isSuccess) {
            return Result.success(result.getOrNull()!!.filter { treatment -> treatment.isActive })
        }

        return result
    }

    suspend fun fetchActiveTreatmentsAtAGivenTime(
        patient: Patient,
        visit: Visit
    ): Result<List<Treatment>> {
        val visitDate = parseFromOpenmrsDate(visit.startDatetime)

        val result = fetchAllTreatments(patient)

        if (result.isSuccess) {
            return Result.success(result.getOrNull()!!.filter { treatment ->
                (treatment.creationDate.isBefore(visitDate) || treatment.visitUuid == visit.uuid)
                        && treatment.isActive
                        || (!treatment.isActive && treatment.inactiveDate!!.isAfter(visitDate))
            })
        }
        return result
    }

    private suspend fun fetchAllTreatments(patient: Patient): Result<List<Treatment>> =
        try {
            withContext(Dispatchers.IO) {
                val visits = visitRepository.getAllVisitsForPatient(patient).toBlocking().first()
                val treatments = visits.flatMap { visit ->
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
                Result.success(treatments)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun getTreatmentFromEncounter(encounter: Encounter): Treatment {
        val visitUuid = encounter.visit?.uuid
        val treatmentUuid = encounter.uuid
        val creationDate = parseFromOpenmrsDate(encounter.encounterDate!!)
        val doctor = encounter.encounterProviders.firstOrNull()?.provider?.display?.substringAfter("-")?.trim()

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
                    observation.displayValue?.trim() ?: ""

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
            doctorUuid = doctor,
            medicationName = medicationName,
            medicationType = medicationType,
            notes = notes,
            isActive = isActive,
            visitUuid = visitUuid,
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

    private fun createEncounterFromTreatment(
        currentVisit: Visit,
        treatment: Treatment,
    ) : Encountercreate {

        var provider: EncounterProviderCreate? = null
        treatment.doctorUuid?.let { provider = EncounterProviderCreate(it, ENCOUNTER_ROLE_UUID) }

        return Encountercreate().apply {
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
            provider?.let { encounterProvider = listOf(it) }
        }.let { encounter ->
            addNotesIfPresent(treatment.notes, encounter)
        }
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

    private fun diffValues(
        treatmentToEdit: Treatment,
        treatmentUpdated: Treatment
    ): Boolean {
        if (treatmentToEdit.recommendedBy != treatmentUpdated.recommendedBy ||
            treatmentToEdit.medicationName != treatmentUpdated.medicationName ||
            treatmentToEdit.medicationType != treatmentUpdated.medicationType ||
            treatmentToEdit.notes != treatmentUpdated.notes
        ) {
            return true
        }
        return false
    }

    companion object {
        const val TREATMENT_ENCOUNTER_TYPE = "Treatment"
        const val DOCTOR = "registered doctor"
        const val ENCOUNTER_ROLE_UUID = "b09b056d-9eaf-41c9-a420-e2303e8f1c96"
    }
}
