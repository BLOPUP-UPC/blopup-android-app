package edu.upc.sdk.library.api.repository

import edu.upc.blopup.model.MedicationType
import edu.upc.blopup.model.Treatment
import edu.upc.sdk.library.CrashlyticsLogger
import edu.upc.sdk.library.api.ObservationConcept
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.EncounterProviderCreate
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.library.models.Encountercreate
import edu.upc.sdk.library.models.Obscreate
import edu.upc.sdk.library.models.Observation
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.OpenMRSVisit
import edu.upc.sdk.utilities.DateUtils
import edu.upc.sdk.utilities.DateUtils.parseFromOpenmrsDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Result
import edu.upc.sdk.library.models.Result as OpenMRSResult

@Singleton
class TreatmentRepository @Inject constructor(
    private val restApi: RestApi,
    private val visitRepository: VisitRepository,
    private val encounterRepository: EncounterRepository,
    private val doctorRepository: DoctorRepository,
    private val crashlytics: CrashlyticsLogger
) {

    suspend fun saveTreatment(treatment: Treatment) {
        withContext(Dispatchers.IO) {
            val currentVisit = visitRepository.getVisitByUuid(UUID.fromString(treatment.visitUuid))

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
        treatmentUpdated: Treatment
    ): Result<Boolean> {
        val needsChanges = diffValues(treatmentToEdit, treatmentUpdated)

        if (!needsChanges) return Result.success(true)

        val response = encounterRepository.removeEncounter(treatmentToEdit.treatmentUuid)

        return if (response.isSuccess) {
            saveTreatment(treatmentUpdated)
            Result.success(true)
        } else {
            Result.failure(Exception("Failed to remove encounter"))
        }
    }

    suspend fun fetchAllActiveTreatments(patient: Patient): OpenMRSResult<List<Treatment>> {

        val result = fetchAllTreatments(UUID.fromString(patient.uuid))


        if (result is  OpenMRSResult.Success) {
            return OpenMRSResult.Success(result.data.filter { treatment -> treatment.isActive })
        }

        return result
    }

    suspend fun fetchActiveTreatmentsAtAGivenTime(
        patient: Patient,
        visit: OpenMRSVisit? = null,
        newVisit: edu.upc.blopup.model.Visit? = null
    ): OpenMRSResult<List<Treatment>> {
        if (visit == null && newVisit == null) {
            throw IllegalArgumentException("Visit or newVisit must be provided")
        }

        val visitId = visit?.uuid ?: newVisit?.id.toString()

        val visitDate = if (visit !== null) {
            parseFromOpenmrsDate(visit!!.startDatetime)
        } else {
            Instant.ofEpochMilli(newVisit!!.startDate.toInstant(ZoneOffset.UTC).toEpochMilli())
        }

        val result = fetchAllTreatments(UUID.fromString(patient.uuid))

        if (result is OpenMRSResult.Success) {
            return OpenMRSResult.Success(result.data.filter { treatment ->
                (treatment.creationDate.isBefore(visitDate) || treatment.visitUuid == visitId)
                        && treatment.isActive
                        || (!treatment.isActive && treatment.inactiveDate!!.isAfter(visitDate))
            })
        }
        return result
    }

    suspend fun fetchAllTreatments(patientId: UUID): OpenMRSResult<List<Treatment>> = withContext(Dispatchers.IO) {
        try {
            val result = restApi.findVisitsByPatientUUID(patientId.toString(), "custom:(uuid,visitType:ref,encounters:full)").execute()

            if (result.isSuccessful) {
                val treatments = result.body()?.results!!.flatMap { visit ->
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
                OpenMRSResult.Success(treatments)
            } else {
                OpenMRSResult.Error(Exception("Failed to fetch treatments"))
            }
        } catch (e: Exception) {
            OpenMRSResult.Error(e)
        }
    }

    private fun getTreatmentFromEncounter(encounter: Encounter): Treatment {
        val visitUuid = encounter.visit?.uuid
        val treatmentUuid = encounter.uuid
        val creationDate = parseFromOpenmrsDate(encounter.encounterDate!!)
        val doctor = encounter.encounterProviders.firstOrNull()?.provider?.display?.substringAfter("-")?.trim()

        val doctorRegisteredNumber = doctor?.let { doctorRepository.getDoctorRegistrationNumber(encounter.encounterProviders.firstOrNull()?.provider?.uuid) } ?: ""

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
                        inactiveDate = parseFromOpenmrsDate(observation.obsDatetime!!)
                    }
                }

                ObservationConcept.TREATMENT_ADHERENCE.uuid -> {
                    val adherence = observation.displayValue?.trim() == "1.0"
                    val date = LocalDate.parse(observation.dateCreated!!, DateTimeFormatter.ofPattern(DateUtils.OPEN_MRS_RESPONSE_FORMAT))
                    adherenceMap[date] = adherence
                }
            }
        }
        return Treatment(
            recommendedBy = recommendedBy,
            doctorUuid = doctor,
            doctorRegistrationNumber = doctorRegisteredNumber,
            medicationName = medicationName,
            medicationType = medicationType,
            notes = notes,
            isActive = isActive,
            visitUuid = visitUuid,
            treatmentUuid = treatmentUuid,
            observationStatusUuid = observationStatusUuid,
            inactiveDate = inactiveDate,
            creationDate = creationDate,
            adherence = adherenceMap
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
        currentVisit: edu.upc.blopup.model.Visit ,
        treatment: Treatment,
    ) : Encountercreate {

        var provider: EncounterProviderCreate? = null
        treatment.doctorUuid?.let { provider = EncounterProviderCreate(it, ENCOUNTER_ROLE_UUID) }

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
        const val ENCOUNTER_ROLE_UUID = "b09b056d-9eaf-41c9-a420-e2303e8f1c96"
    }
}
