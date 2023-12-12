package edu.upc.sdk.library.api.repository

import androidx.work.WorkManager
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.api.RestServiceBuilder
import edu.upc.sdk.library.api.repository.TreatmentRepository.Companion.ACTIVE_CONCEPT_ID
import edu.upc.sdk.library.api.repository.TreatmentRepository.Companion.MEDICATION_NAME_CONCEPT_ID
import edu.upc.sdk.library.api.repository.TreatmentRepository.Companion.MEDICATION_TYPE_CONCEPT_ID
import edu.upc.sdk.library.api.repository.TreatmentRepository.Companion.RECOMMENDED_BY_CONCEPT_ID
import edu.upc.sdk.library.api.repository.TreatmentRepository.Companion.TREATMENT_ENCOUNTER_TYPE
import edu.upc.sdk.library.api.repository.TreatmentRepository.Companion.TREATMENT_NOTES_CONCEPT_ID
import edu.upc.sdk.library.databases.AppDatabase
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.Encountercreate
import edu.upc.sdk.library.models.MedicationType
import edu.upc.sdk.library.models.Obscreate
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Treatment
import edu.upc.sdk.library.models.Visit
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import java.util.UUID

class TreatmentRepositoryTest {

    private lateinit var treatmentRepository: TreatmentRepository

    private lateinit var restApi: RestApi

    private lateinit var visitRepository: VisitRepository

    @Before
    fun setUp() {
        restApi = mockk(relaxed = true)
        visitRepository = mockk(relaxed = true)

        mockStaticMethodsNeededToInstantiateBaseRepository()
        treatmentRepository = TreatmentRepository(visitRepository)
    }

    @Test
    fun `should save the Treatment in OpenMRS with medication for one drug family`() {
        val patientUuid = UUID.randomUUID().toString()
        val visitUuid = UUID.randomUUID().toString()

        val treatment = Treatment(
            "BlopUp",
            "hidroclorotiazida",
            setOf(MedicationType.DIURETIC),
            "25mg/dia",
            true,
            19
        )

        val expectedTreatmentEncounter = Encountercreate().apply {
            patient = patientUuid
            visit = visitUuid
            encounterType = TREATMENT_ENCOUNTER_TYPE
            observations = listOf(
                Obscreate().apply {
                    concept = RECOMMENDED_BY_CONCEPT_ID
                    value = "BlopUp"
                },
                Obscreate().apply {
                    concept = MEDICATION_NAME_CONCEPT_ID
                    value = "hidroclorotiazida"
                },
                Obscreate().apply {
                    concept = TREATMENT_NOTES_CONCEPT_ID
                    value = "25mg/dia"
                },
                Obscreate().apply {
                    concept = ACTIVE_CONCEPT_ID
                    value = "1"
                },
                Obscreate().apply {
                    concept = MEDICATION_TYPE_CONCEPT_ID
                    groupMembers = listOf(Obscreate().apply {
                        concept = MEDICATION_TYPE_CONCEPT_ID
                        value = MedicationType.DIURETIC.conceptId
                    })
                }
            )
        }

        val capturedTreatmentEncounter = slot<Encountercreate>()

        val call = mockk<Call<Encounter>>(relaxed = true)
        every { visitRepository.getVisitById(any()) } returns Visit().apply {
            uuid = visitUuid; patient = Patient().apply { uuid = patientUuid }
        }
        coEvery { restApi.createEncounter(any()) } returns call
        coEvery { call.execute() } returns Response.success(Encounter().apply {
            uuid = "encounterUuid"
        })

        runBlocking {
            treatmentRepository.saveTreatment(treatment)

            coVerify { restApi.createEncounter(capture(capturedTreatmentEncounter)) }

            assertEquals(expectedTreatmentEncounter.patient, capturedTreatmentEncounter.captured.patient)
            assertEquals(expectedTreatmentEncounter.visit, capturedTreatmentEncounter.captured.visit)
            assertEquals(expectedTreatmentEncounter.encounterType, capturedTreatmentEncounter.captured.encounterType)
            assertEquals(expectedTreatmentEncounter.observations[0].concept, capturedTreatmentEncounter.captured.observations[0].concept)
            assertEquals(expectedTreatmentEncounter.observations[4].groupMembers!![0].value, capturedTreatmentEncounter.captured.observations[4].groupMembers!![0].value)
        }
    }

    private fun mockStaticMethodsNeededToInstantiateBaseRepository() {
        mockkStatic(OpenmrsAndroid::class)
        every { OpenmrsAndroid.getServerUrl() } returns "http://localhost:8080/openmrs"
        mockkConstructor(Retrofit.Builder::class)
        mockkStatic(RestServiceBuilder::class)
        mockkConstructor(RestServiceBuilder::class)
        every { RestServiceBuilder.createService(RestApi::class.java) } returns restApi
        mockkStatic(AppDatabase::class)
        every { AppDatabase.getDatabase(any()) } returns mockk(relaxed = true)
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns mockk(relaxed = true)
    }
}