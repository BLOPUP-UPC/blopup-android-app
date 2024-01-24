package edu.upc.sdk.library.api.repository

import androidx.work.WorkManager
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.api.ObservationConcept
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.api.RestServiceBuilder
import edu.upc.sdk.library.api.repository.TreatmentRepository.Companion.TREATMENT_ENCOUNTER_TYPE
import edu.upc.sdk.library.databases.AppDatabase
import edu.upc.sdk.library.databases.entities.ConceptEntity
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.Encountercreate
import edu.upc.sdk.library.models.MedicationType
import edu.upc.sdk.library.models.Obscreate
import edu.upc.sdk.library.models.Observation
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Treatment
import edu.upc.sdk.library.models.TreatmentExample
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.library.models.VisitExample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.joda.time.Instant
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import rx.Observable
import java.util.UUID

class TreatmentRepositoryTest {

    private lateinit var treatmentRepository: TreatmentRepository

    private lateinit var restApi: RestApi

    private lateinit var visitRepository: VisitRepository

    private lateinit var encounterRepository: EncounterRepository

    @Before
    fun setUp() {
        restApi = mockk(relaxed = true)
        visitRepository = mockk(relaxed = true)
        encounterRepository = mockk(relaxed = true)

        mockStaticMethodsNeededToInstantiateBaseRepository()
        treatmentRepository = TreatmentRepository(visitRepository, encounterRepository)
    }

    @Test
    fun `should get all active treatments`() {

        val patient = Patient().apply { uuid = UUID.randomUUID().toString() }

        val now = Instant.now().withMillis(0)
        val activeTreatment = TreatmentExample.activeTreatment(now)
        val inactiveTreatment = TreatmentExample.inactiveTreatment()

        val visitWithActiveTreatment = VisitExample.random(activeTreatment)
        val visitWithInactiveTreatment = VisitExample.random(inactiveTreatment)

        val visitList = listOf(visitWithActiveTreatment, visitWithInactiveTreatment)

        coEvery { visitRepository.getAllVisitsForPatient(patient) } returns Observable.just(
            visitList
        )

        runBlocking {
            val result = treatmentRepository.fetchAllActiveTreatments(patient)
            assertEquals(Result.success(listOf(activeTreatment)), result)
        }
    }

    @Test
    fun `should get all treatments recommended until visit date and in that visit`() {

        val patient = Patient().apply { uuid = UUID.randomUUID().toString() }

        val beforeVisit = Instant.parse("2023-12-20T10:10:10Z")
        val visitDate = Instant.parse("2023-12-21T10:10:10Z")
        val afterVisit = Instant.parse("2023-12-22T10:10:10Z")

        val previousTreatment = TreatmentExample.activeTreatment(beforeVisit)
        val actualVisitTreatment = TreatmentExample.activeTreatment(visitDate)
        val futureDateTreatment = TreatmentExample.activeTreatment(afterVisit)
        val previousAndInactiveTreatment = TreatmentExample.inactiveTreatment(beforeVisit)

        val visitWithPreviousTreatment = VisitExample.random(previousTreatment, beforeVisit)
        val visitWithTreatment = VisitExample.random(actualVisitTreatment, visitDate)
        val visitWithFutureTreatment = VisitExample.random(futureDateTreatment, afterVisit)
        val visitWithPreviousAndInactiveTreatment =
            VisitExample.random(previousAndInactiveTreatment, beforeVisit)

        val visitList = listOf(
            visitWithPreviousTreatment,
            visitWithTreatment,
            visitWithFutureTreatment,
            visitWithPreviousAndInactiveTreatment
        )

        coEvery { visitRepository.getAllVisitsForPatient(patient) } returns Observable.just(
            visitList
        )

        runBlocking {
            val result =
                treatmentRepository.fetchActiveTreatmentsAtAGivenTime(patient, visitWithTreatment)
            assertEquals(
               Result.success( listOf(
                    previousTreatment,
                    actualVisitTreatment,
                    previousAndInactiveTreatment
                )), result
            )
        }
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
                    concept = ObservationConcept.RECOMMENDED_BY.uuid
                    value = "BlopUp"
                },
                Obscreate().apply {
                    concept = ObservationConcept.MEDICATION_NAME.uuid
                    value = "hidroclorotiazida"
                },
                Obscreate().apply {
                    concept = ObservationConcept.TREATMENT_NOTES.uuid
                    value = "25mg/dia"
                },
                Obscreate().apply {
                    concept = ObservationConcept.ACTIVE.uuid
                    value = "1"
                },
                Obscreate().apply {
                    concept = ObservationConcept.MEDICATION_TYPE.uuid
                    groupMembers = listOf(Obscreate().apply {
                        concept = ObservationConcept.MEDICATION_TYPE.uuid
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

            assertEquals(
                expectedTreatmentEncounter.patient,
                capturedTreatmentEncounter.captured.patient
            )
            assertEquals(
                expectedTreatmentEncounter.visit,
                capturedTreatmentEncounter.captured.visit
            )
            assertEquals(
                expectedTreatmentEncounter.encounterType,
                capturedTreatmentEncounter.captured.encounterType
            )
            assertEquals(
                expectedTreatmentEncounter.observations[0].concept,
                capturedTreatmentEncounter.captured.observations[0].concept
            )
            assertEquals(
                expectedTreatmentEncounter.observations[4].groupMembers!![0].value,
                capturedTreatmentEncounter.captured.observations[3].groupMembers!![0].value
            )
        }
    }

    @Test
    fun `should return error if create encounter call fails`() {

        coEvery { visitRepository.getAllVisitsForPatient(any()) } returns Observable.just(
            listOf(
                Visit()
            )
        )
        coEvery { restApi.createEncounter(any()) } returns createCall(
            Response.error(
                400,
                mockk(relaxed = true)
            )
        )
        assertThrows<Exception> {
            runBlocking {
                treatmentRepository.saveTreatment(TreatmentExample.activeTreatment())
            }
        }
    }

    @Test
    fun `should update the observation as inactive`() {
        val treatment = TreatmentExample.activeTreatment()
        val observation = Observation().apply {
            concept = ConceptEntity().apply { uuid = ObservationConcept.ACTIVE.uuid }
            displayValue = " 0"
            display = "Active:$displayValue"
            uuid = UUID.randomUUID().toString()
            dateCreated = treatment.creationDate.toString()
            obsDatetime = treatment.inactiveDate.toString()
        }

        coEvery { restApi.getObservationByUuid(treatment.observationStatusUuid!!) } returns createCall(
            response = Response.success(observation)
        )
        coEvery {
            restApi.updateObservation(
                observation.uuid,
                mapOf("value" to 0, "obsDatetime" to treatment.inactiveDate.toString())
            )
        } returns mockk(relaxed = true)


        runBlocking {
            treatmentRepository.finalise(treatment)
        }

        coVerify { restApi.getObservationByUuid(treatment.observationStatusUuid!!) }
        coVerify {
            restApi.updateObservation(
                observation.uuid,
                mapOf("value" to 0, "obsDatetime" to treatment.inactiveDate.toString())
            )
        }
    }

    @Test
    fun `should return result success when treatment adherence is saved`() {
        val treatment1 = "treatmentUuid1"
        val treatment2 = "treatmentUuid2"
        val treatmentAdherenceData = mapOf(Pair(treatment1, true), Pair(treatment2, false))
        val mockCall = mockk<Call<Observation>>()


        coEvery { restApi.createObs(any()) } returns mockCall
        coEvery { mockCall.execute() } returns Response.success(Observation())

        runBlocking {
            val result =
                treatmentRepository.saveTreatmentAdherence(treatmentAdherenceData, "patientUuid")

            assertEquals(Result.success(true), result)
        }

        coVerify(exactly = 2) { restApi.createObs(any()) }
    }

    @Test
    fun `should return result failure if error occurs saving treatment adherence`() {
        val treatment1 = "treatmentUuid1"
        val treatment2 = "treatmentUuid2"
        val treatmentAdherenceData = mapOf(Pair(treatment1, true), Pair(treatment2, false))
        val mockCall = mockk<Call<Observation>>()
        val exception = Exception()


        coEvery { restApi.createObs(any()) } returns mockCall
        coEvery { mockCall.execute() } throws exception

        runBlocking {
            val result =
                treatmentRepository.saveTreatmentAdherence(treatmentAdherenceData, "patientUuid")

            assertEquals(Result.failure<Exception>(exception), result)
        }
    }

    @Test
    fun `should return result success when treatment is updated`() {
        val call = mockk<Call<Encounter>>(relaxed = true)

        val treatmentToEdit = TreatmentExample.activeTreatment()

        val treatmentUpdated = Treatment(
            "Other",
            "Paracetamol",
            treatmentToEdit.medicationType,
            "hello",
            visitId = treatmentToEdit.visitId
        )

        coEvery { encounterRepository.removeEncounter(treatmentToEdit.treatmentUuid) } returns Result.success(true)

        coEvery { visitRepository.getVisitByUuid(treatmentToEdit.visitUuid) } returns Visit().apply {
            uuid = treatmentToEdit.visitUuid
            patient = Patient().apply { uuid = "18y3774283999cs" }
        }

        coEvery { restApi.createEncounter(any()) } returns call
        coEvery { call.execute() } returns Response.success(Encounter().apply {
            uuid = "encounterUuid"
            patient = Patient().apply { uuid = "18y3774283999cs" }
        })

        runBlocking {
            val result = treatmentRepository.updateTreatment(treatmentToEdit, treatmentUpdated)
            assertEquals(Result.success(true), result)
        }
    }

    @Test
    fun `should set error if no value changed to update a treatment`() {
        val exceptionMessage = "No changes detected"

        runBlocking {
            runCatching {
                val result = treatmentRepository.updateTreatment(any(), any())

                assert(result.isFailure)
                assertEquals(exceptionMessage, result.exceptionOrNull()?.message)

            }
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

    private inline fun <reified T> createCall(response: Response<T>): Call<T> = object : Call<T> {
        override fun enqueue(callback: retrofit2.Callback<T>) = TODO()
        override fun isExecuted() = TODO()
        override fun clone(): Call<T> = TODO()
        override fun isCanceled() = TODO()
        override fun request(): Request = TODO()
        override fun cancel() = TODO()
        override fun execute(): Response<T> = response
    }
}