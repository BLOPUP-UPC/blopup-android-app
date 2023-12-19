package edu.upc.sdk.library.api.repository

import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.library.models.Encountercreate
import edu.upc.sdk.library.models.MedicationType
import edu.upc.sdk.library.models.Obscreate
import edu.upc.sdk.library.models.Observation
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Treatment
import edu.upc.sdk.library.models.Visit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TreatmentRepository @Inject constructor(val visitRepository: VisitRepository) :
    BaseRepository(null) {

    suspend fun saveTreatment(treatment: Treatment) {
        val currentVisit = visitRepository.getVisitById(treatment.visitId)
        val encounter = createEncounterFromTreatment(currentVisit, treatment)

        withContext(Dispatchers.IO) {
            restApi.createEncounter(encounter).execute()
        }

    }

    suspend fun fetchActiveTreatments(patient: Patient): List<Treatment> {
        val visits: List<Visit>
        withContext(Dispatchers.IO) {
            visits = visitRepository.getAllVisitsForPatient(patient).toBlocking().first()
        }

        val encounters = visits.flatMap { visit ->
            visit.encounters.filter { encounter ->
                encounter.encounterType?.display == EncounterType.TREATMENT
            }
        }

        return encounters.map { encounter -> getTreatmentFromEncounter(encounter) }
    }

    private fun getTreatmentFromEncounter(encounter: Encounter): Treatment {
        val treatment = Treatment()
        treatment.visitId = encounter.visitID ?: 0
        encounter.observations.map { observation ->
            when (observation.concept?.uuid) {
                RECOMMENDED_BY_CONCEPT_ID -> treatment.recommendedBy =
                    observation.displayValue ?: ""

                MEDICATION_NAME_CONCEPT_ID -> treatment.medicationName =
                    observation.displayValue ?: ""

                TREATMENT_NOTES_CONCEPT_ID -> treatment.notes =
                    observation.displayValue ?: ""

                MEDICATION_TYPE_CONCEPT_ID -> treatment.medicationType =
                    getMedicationTypesFromObservation(observation)

                ACTIVE_CONCEPT_ID -> treatment.isActive =
                    observation.displayValue == "1"
            }
        }
        return treatment
    }

    private fun getMedicationTypesFromObservation(observation: Observation): Set<MedicationType> {
        return observation.groupMembers?.map {
            MedicationType.values().find { medicationType ->
                medicationType.conceptId == it.concept?.name
            } ?: throw IllegalStateException("Medication type not found")
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
                    TREATMENT_NOTES_CONCEPT_ID,
                    treatment.notes,
                    currentVisit.patient.uuid!!
                ),
                observation(
                    ACTIVE_CONCEPT_ID,
                    if (treatment.isActive) 1F.toString() else 0F.toString(),
                    currentVisit.patient.uuid!!
                ),
                drugFamiliesObservation(currentVisit.patient.uuid!!, treatment)
            )
        }

    private fun drugFamiliesObservation(patientUuid: String, treatment: Treatment) =
        Obscreate().apply {
            concept = MEDICATION_TYPE_CONCEPT_ID
            person = patientUuid
            obsDatetime = Instant.now().toString()
            groupMembers = treatment.medicationType.map {
                observation(
                    MEDICATION_TYPE_CONCEPT_ID,
                    it.name,
                    patientUuid
                )
            }
        }

    private fun observation(concept: String, value: String, patientId: String) = Obscreate().apply {
        this.concept = concept
        this.value = value
        obsDatetime = Instant.now().toString()
        person = patientId
    }

    companion object {
        const val MEDICATION_NAME_CONCEPT_ID = "a721776b-fd0f-41ea-821b-0d0df94d5560"
        const val RECOMMENDED_BY_CONCEPT_ID = "c1164da7-0b4f-490f-85da-0c4aac4ca8a1"
        const val ACTIVE_CONCEPT_ID = "81f60010-961e-4bc5-aa04-435c7ace1ee3"
        const val TREATMENT_NOTES_CONCEPT_ID = "dfa881a4-5c88-4057-958b-f583c8edbdef"
        const val MEDICATION_TYPE_CONCEPT_ID = "1a8f49cc-488b-4788-adb3-72c499108772"

        const val TREATMENT_ENCOUNTER_TYPE = "Treatment"
    }
}
