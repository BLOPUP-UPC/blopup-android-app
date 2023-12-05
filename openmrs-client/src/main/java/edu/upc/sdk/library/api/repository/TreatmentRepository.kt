package edu.upc.sdk.library.api.repository

import android.os.Build
import androidx.annotation.RequiresApi
import edu.upc.sdk.library.models.Encountercreate
import edu.upc.sdk.library.models.Obscreate
import edu.upc.sdk.library.models.Treatment
import edu.upc.sdk.library.models.Visit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TreatmentRepository @Inject constructor(val visitRepository: VisitRepository) : BaseRepository(null) {

    suspend fun saveTreatment(treatment: Treatment) {
        val currentVisit = visitRepository.getVisitById(treatment.visitId)
        val encounter = createEncounterFromTreatment(currentVisit, treatment)

        withContext(Dispatchers.IO) {

            val response = restApi.createEncounter(encounter).execute()
            val responseEncounter = if (response.isSuccessful) response.body() else null

            val drugFamilies = drugFamiliesObservation(
                responseEncounter!!.uuid!!,
                currentVisit.patient.uuid!!,
                treatment
            )
            restApi.createObs(drugFamilies).execute()
        }

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
                    if (treatment.isActive) 0F.toString() else 1F.toString(),
                    currentVisit.patient.uuid!!
                ),
            )
        }

    private fun drugFamiliesObservation(encounterUuid: String, patientUuid: String, treatment: Treatment) =
        Obscreate().apply {
            encounter = encounterUuid
            concept = DRUG_FAMILIES_CONCEPT_ID
            person = patientUuid
            obsDatetime = Instant.now().toString()
            groupMembers = treatment.drugFamilies.map { observation(DRUG_FAMILIES_CONCEPT_ID, it, patientUuid) }
        }

    private fun observation(concept: String, value: String, patientId: String) = Obscreate().apply {
        this.concept = concept
        this.value = value
        obsDatetime = Instant.now().toString()
        person = patientId
    }

    companion object {
        const val MEDICATION_NAME_CONCEPT_ID = "43f8a8f3-6cf9-4a1f-bba5-8ec979f6d0b6"
        const val RECOMMENDED_BY_CONCEPT_ID = "c1164da7-0b4f-490f-85da-0c4aac4ca8a1"
        const val ACTIVE_CONCEPT_ID = "81f60010-961e-4bc5-aa04-435c7ace1ee3"
        const val TREATMENT_NOTES_CONCEPT_ID = "dfa881a4-5c88-4057-958b-f583c8edbdef"
        const val DRUG_FAMILIES_CONCEPT_ID = "1a8f49cc-488b-4788-adb3-72c499108772"

        const val TREATMENT_ENCOUNTER_TYPE = "Treatment"
    }
}
