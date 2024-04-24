package edu.upc.sdk.library.api.repository

import edu.upc.blopup.model.Doctor
import edu.upc.blopup.model.MedicationType
import edu.upc.blopup.model.Treatment
import edu.upc.blopup.model.Visit
import edu.upc.sdk.library.CrashlyticsLogger
import edu.upc.sdk.library.api.ObservationConcept
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.EncounterProviderCreate
import edu.upc.sdk.library.models.Encountercreate
import edu.upc.sdk.library.models.Obscreate
import edu.upc.sdk.library.models.Observation
import edu.upc.sdk.utilities.DateUtils
import edu.upc.sdk.utilities.DateUtils.parseInstantFromOpenmrsDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Result
import edu.upc.sdk.library.models.Result as OpenMRSResult

@Singleton
class TreatmentRepository @Inject constructor(
    private val restApi: RestApi,
    private val visitRepository: VisitRepository,
    private val crashlytics: CrashlyticsLogger
) {

    suspend fun saveTreatment(treatment: Treatment) = withContext(Dispatchers.IO) {
        val currentVisit = visitRepository.getVisitByUuid(UUID.fromString(treatment.visitUuid))
        val encounter = createEncounterFromTreatment(currentVisit, treatment)

        try {
            val response = restApi.createEncounter(encounter).execute()
            if (response.isSuccessful) {
                return@withContext
            } else {
                throw Exception("Failed to create encounter: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to create encounter: ${e.message}", e)
        }
    }

    suspend fun deleteTreatment(treatmentUuid: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = restApi.deleteEncounter(treatmentUuid).execute()

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Remove treatment error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Remove encounter error: ${e.message}"))
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
        treatmentUpdated: Treatment
    ): Result<Boolean> {
        val needsChanges = diffValues(treatmentToEdit, treatmentUpdated)

        if (!needsChanges) return Result.success(true)

        if (treatmentToEdit.treatmentUuid == null) {
            throw IllegalArgumentException("Treatment UUID must be provided")
        }

        val response = deleteTreatment(treatmentToEdit.treatmentUuid!!)

        return if (response.isSuccess) {
            saveTreatment(treatmentUpdated)
            Result.success(true)
        } else {
            Result.failure(Exception("Failed to remove encounter"))
        }
    }

    suspend fun fetchAllActiveTreatments(patientId: UUID): OpenMRSResult<List<Treatment>> {
        val result = fetchAllTreatments(patientId)

        if (result is  OpenMRSResult.Success) {
            return OpenMRSResult.Success(result.data.filter { treatment -> treatment.isActive })
        }

        return result
    }

    suspend fun fetchActiveTreatmentsAtAGivenTime(visit: Visit): OpenMRSResult<List<Treatment>> {
        val result = fetchAllTreatments(visit.patientId)

        if (result is OpenMRSResult.Success) {
            return OpenMRSResult.Success(result.data.filter { treatment ->
                (treatment.creationDate.isBefore(visit.startDate) || treatment.visitUuid == visit.id.toString())
                        && treatment.isActive
                        || (!treatment.isActive && treatment.inactiveDate!!.isAfter(visit.startDate))
            })
        }
        return result
    }

    suspend fun fetchAllTreatments(patientId: UUID): OpenMRSResult<List<Treatment>> = withContext(Dispatchers.IO) {
        try {
            val result = restApi.findVisitsByPatientUUID(patientId.toString(), API_TREATMENT_REPRESENTATION).execute()

            if (result.isSuccessful) {
                val treatments = result.body()?.results!!.flatMap { visit ->
                    visit.encounters
                        .filter { encounter ->
                            encounter.encounterType?.display == TREATMENT_ENCOUNTER_TYPE
                        }
                        .map { encounter ->
                            getTreatmentFromEncounter(visit.uuid!!, encounter)
                        }
                }
                OpenMRSResult.Success(treatments)
            } else {
                OpenMRSResult.Error(Exception("Failed to fetch treatments"))
            }
        } catch (e: Exception) {
            OpenMRSResult.Error(e)
        }
    }

    private fun getTreatmentFromEncounter(visitUuid: String, encounter: Encounter): Treatment {
        val treatmentUuid = encounter.uuid
        val creationDate = parseInstantFromOpenmrsDate(encounter.encounterDate!!)
        val doctor = encounter.encounterProviders.find { it.encounterRole?.uuid == ENCOUNTER_DOCTOR_ROLE_UUID }?.provider.let {
            it?.uuid?.let { doctorUuid ->
                it.person?.display?.let { doctorName ->
                    it.attributes?.find { it.attributeType?.uuid == DoctorRepository.REGISTRATION_NUMBER_UUID }?.value?.let { doctorRegistrationNumber ->
                        Doctor(doctorUuid, doctorName, doctorRegistrationNumber)
                    }
                }
            }
        }

        var recommendedBy = ""
        var medicationName = ""
        var notes: String? = null
        var medicationType: Set<MedicationType> = emptySet()
        var isActive = false
        var observationStatusUuid: String? = null
        var inactiveDate: Instant? = null
        val adherenceMap = mutableMapOf<LocalDate, Boolean>()
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
                        inactiveDate = parseInstantFromOpenmrsDate(observation.obsDatetime!!)
                    }
                }

                ObservationConcept.TREATMENT_ADHERENCE.uuid -> {
                    val adherence = observation.displayValue?.trim() == "1.0"
                    val date = DateUtils.parseLocalDateFromOpenmrsDate(observation.dateCreated!!)
                    adherenceMap[date] = adherence
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
            treatmentUuid = treatmentUuid,
            observationStatusUuid = observationStatusUuid,
            creationDate = creationDate,
            inactiveDate = inactiveDate,
            adherence = adherenceMap,
            doctor = doctor
        )
    }

    private fun getMedicationTypesFromObservation(observation: Observation): Set<MedicationType> {
        return observation.groupMembers?.map { groupMember ->
            MedicationType.entries.find { medicationType ->
                medicationType.conceptId == groupMember.valueCodedName
            }!!
        }?.toSet() ?: emptySet()
    }

    private fun createEncounterFromTreatment(
        currentVisit: Visit,
        treatment: Treatment,
    ) : Encountercreate {

        var provider: EncounterProviderCreate? = null
        treatment.doctor?.let { provider = EncounterProviderCreate(it.uuid, ENCOUNTER_DOCTOR_ROLE_UUID) }

        return Encountercreate().apply {
            encounterType = TREATMENT_ENCOUNTER_TYPE
            patient = currentVisit.patientId.toString()
            visit = currentVisit.id.toString()
            observations = listOf(
                observation(
                    ObservationConcept.RECOMMENDED_BY.uuid,
                    treatment.recommendedBy,
                    currentVisit.patientId.toString()
                ),
                observation(
                    ObservationConcept.MEDICATION_NAME.uuid,
                    treatment.medicationName,
                    currentVisit.patientId.toString()
                ),
                observation(
                    ObservationConcept.ACTIVE.uuid,
                    if (treatment.isActive) "1.0" else "0.0",
                    currentVisit.patientId.toString()
                ),
                drugFamiliesObservation(currentVisit.patientId.toString(), treatment)
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
        return treatmentToEdit.recommendedBy != treatmentUpdated.recommendedBy ||
                treatmentToEdit.medicationName != treatmentUpdated.medicationName ||
                treatmentToEdit.medicationType != treatmentUpdated.medicationType ||
                treatmentToEdit.notes != treatmentUpdated.notes
    }

    companion object {
        const val TREATMENT_ENCOUNTER_TYPE = "Treatment"
        const val DOCTOR = "registered doctor"
        const val ENCOUNTER_DOCTOR_ROLE_UUID = "b09b056d-9eaf-41c9-a420-e2303e8f1c96"

        // This is exactly the info we need to avoid fetching encounters:full
        // Doing encounters:full will bring all the obs that takes a lot of time and size
        const val API_TREATMENT_REPRESENTATION = "custom:(" +
                "uuid," +
                "visitType:custom:(uuid,display)," +
                "encounters:custom:(" +
                    "uuid," +
                    "encounterType:custom:(display)," +
                    "encounterDatetime," +
                    "encounterProviders:custom:(" +
                        "encounterRole:ref," +
                        "provider:custom:(" +
                            "uuid," +
                            "person:custom:(display)," +
                            "attributes:custom:(attributeType:custom:(uuid),display)))," +
                    "obs:custom:(" +
                        "uuid," +
                        "concept:custom:(uuid)," +
                        "display," +
                        "value," +
                        "obsDatetime," +
                        "dateCreated," +
                        "groupMembers:custom:(" +
                            "concept:custom:(display)," +
                            "value:custom:(uuid)))))"
    }
}
