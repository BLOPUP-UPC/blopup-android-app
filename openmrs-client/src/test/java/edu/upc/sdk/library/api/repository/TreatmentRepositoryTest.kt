package edu.upc.sdk.library.api.repository

import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.MedicationType
import edu.upc.blopup.model.Treatment
import edu.upc.blopup.model.VisitExample
import edu.upc.sdk.library.CrashlyticsLogger
import edu.upc.sdk.library.api.ObservationConcept
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.api.repository.TreatmentRepository.Companion.TREATMENT_ENCOUNTER_TYPE
import edu.upc.sdk.library.databases.entities.ConceptEntity
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.Encountercreate
import edu.upc.sdk.library.models.Obscreate
import edu.upc.sdk.library.models.Observation
import edu.upc.sdk.library.models.OpenMRSVisit
import edu.upc.sdk.library.models.OpenMrsVisitExample
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Results
import edu.upc.sdk.library.models.TreatmentExample
import edu.upc.sdk.utilities.DateUtils.formatToApiRequest
import edu.upc.sdk.utilities.DateUtils.parseInstantFromOpenmrsDate
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import retrofit2.Call
import retrofit2.Response
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class TreatmentRepositoryTest {

    @MockK
    private lateinit var restApi: RestApi

    @MockK
    private lateinit var visitRepository: VisitRepository

    @MockK
    private lateinit var doctorRepository: DoctorRepository

    @MockK(relaxed = true)
    private lateinit var crashlyticsLogger: CrashlyticsLogger

    @InjectMockKs
    private lateinit var treatmentRepository: TreatmentRepository

    private val treatmentsApiRepresentation = "custom:(" +
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

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should get all treatments`() = runTest {
        val patientUuid = UUID.randomUUID()

        val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val activeTreatment = TreatmentExample.activeTreatment(now)
        val inactiveTreatment = TreatmentExample.inactiveTreatment()

        val visitWithActiveTreatment = OpenMrsVisitExample.withTreatment(activeTreatment)
        val visitWithInactiveTreatment = OpenMrsVisitExample.withTreatment(inactiveTreatment)

        val visitList = listOf(visitWithActiveTreatment, visitWithInactiveTreatment)

        val call = mockk<Call<Results<OpenMRSVisit>>>(relaxed = true)
        coEvery { restApi.findVisitsByPatientUUID(patientUuid.toString(), treatmentsApiRepresentation) } returns call
        coEvery { call.execute() } returns Response.success(Results<OpenMRSVisit>().apply {
            results = visitList
        })

        val result = treatmentRepository.fetchAllTreatments(patientUuid)
        assertEquals(edu.upc.sdk.library.models.Result.Success(listOf(activeTreatment, inactiveTreatment)), result)
    }

    @Test
    fun `should get all treatments including doctor registration number`() = runTest {
        val patientUuid = UUID.randomUUID()

        val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val activeTreatment = TreatmentExample.activeTreatment(now)

        val visitWithActiveTreatment = OpenMrsVisitExample.withTreatment(activeTreatment)
        val visitList = listOf(visitWithActiveTreatment)

        val call = mockk<Call<Results<OpenMRSVisit>>>(relaxed = true)
        coEvery { restApi.findVisitsByPatientUUID(patientUuid.toString(), treatmentsApiRepresentation) } returns call
        coEvery { call.execute() } returns Response.success(Results<OpenMRSVisit>().apply {
            results = visitList
        })

        val result = treatmentRepository.fetchAllTreatments(patientUuid) as edu.upc.sdk.library.models.Result.Success<List<Treatment>>
        assertEquals(activeTreatment.doctor!!.registrationNumber, result.data[0].doctor!!.registrationNumber)
        assertEquals(activeTreatment.doctor!!.uuid, result.data[0].doctor!!.uuid)
        assertEquals(activeTreatment.doctor!!.name, result.data[0].doctor!!.name)
    }

    @Test
    fun `should get all active treatments`() {

        val patient = Patient().apply { uuid = UUID.randomUUID().toString() }

        val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val activeTreatment = TreatmentExample.activeTreatment(now)
        val inactiveTreatment = TreatmentExample.inactiveTreatment()

        val visitWithActiveTreatment = OpenMrsVisitExample.withTreatment(activeTreatment)
        val visitWithInactiveTreatment = OpenMrsVisitExample.withTreatment(inactiveTreatment)

        val visitList = listOf(visitWithActiveTreatment, visitWithInactiveTreatment)

        val call = mockk<Call<Results<OpenMRSVisit>>>(relaxed = true)
        coEvery { restApi.findVisitsByPatientUUID(patient.uuid, treatmentsApiRepresentation) } returns call
        coEvery { call.execute() } returns Response.success(Results<OpenMRSVisit>().apply {
            results = visitList
        })

        runBlocking {
            val result = treatmentRepository.fetchAllActiveTreatments(UUID.fromString(patient.uuid))
            assertEquals(edu.upc.sdk.library.models.Result.Success(listOf(activeTreatment)), result)
        }
    }

    @Test
    fun `should get all treatments recommended until visit date and in that visit`() {

        val patient = Patient().apply { uuid = UUID.randomUUID().toString() }

        val beforeVisit = parseInstantFromOpenmrsDate("2023-12-20T10:10:10.000+0000")
        val visitDate = beforeVisit.plus(1, ChronoUnit.DAYS)
        val afterVisit = beforeVisit.plus(2, ChronoUnit.DAYS)

        val previousTreatment = TreatmentExample.activeTreatment(beforeVisit)
        val actualVisitTreatment = TreatmentExample.activeTreatment(visitDate)
        val futureDateTreatment = TreatmentExample.activeTreatment(afterVisit)
        val previousAndInactiveTreatment = TreatmentExample.inactiveTreatment(beforeVisit)

        val visitWithPreviousTreatment = OpenMrsVisitExample.withTreatment(previousTreatment, beforeVisit)
        val visitWithTreatment = OpenMrsVisitExample.withTreatment(actualVisitTreatment, visitDate)
        val visitWithFutureTreatment = OpenMrsVisitExample.withTreatment(futureDateTreatment, afterVisit)
        val visitWithPreviousAndInactiveTreatment =
            OpenMrsVisitExample.withTreatment(previousAndInactiveTreatment, beforeVisit)

        val visitList = listOf(
            visitWithPreviousTreatment,
            visitWithTreatment,
            visitWithFutureTreatment,
            visitWithPreviousAndInactiveTreatment
        )
        val visit = VisitExample.random(
            id = UUID.fromString(visitWithTreatment.uuid),
            patientId = UUID.fromString(patient.uuid),
            startDateTime = visitDate,
        )

        val call = mockk<Call<Results<OpenMRSVisit>>>(relaxed = true)
        coEvery { restApi.findVisitsByPatientUUID(patient.uuid, treatmentsApiRepresentation) } returns call
        coEvery { call.execute() } returns Response.success(Results<OpenMRSVisit>().apply {
            results = visitList
        })

        runBlocking {
            val result =
                treatmentRepository.fetchActiveTreatmentsAtAGivenTime(visit)
            assertEquals(
                edu.upc.sdk.library.models.Result.Success(
                    listOf(
                        previousTreatment,
                        actualVisitTreatment,
                        previousAndInactiveTreatment
                    )
                ), result
            )
        }
    }

    @Test
    fun `should save the Treatment in OpenMRS with medication for one drug family`() {
        val patientUuid = UUID.randomUUID()
        val visitUuid = UUID.randomUUID()

        val treatment = Treatment(
            "Other",
            "hidroclorotiazida",
            setOf(MedicationType.DIURETIC),
            "25mg/dia",
            true,
            visitUuid.toString()
        )

        val expectedTreatmentEncounter = Encountercreate().apply {
            patient = patientUuid.toString()
            visit = visitUuid.toString()
            encounterType = TREATMENT_ENCOUNTER_TYPE
            observations = listOf(
                Obscreate().apply {
                    concept = ObservationConcept.RECOMMENDED_BY.uuid
                    value = "Other"
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
        coEvery { visitRepository.getVisitByUuid(any()) } returns edu.upc.blopup.model.Visit(
            visitUuid,
            patientUuid,
            "Casa del Aleh",
            java.time.Instant.now(),
            BloodPressure(120, 80, 80)
        )
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
        treatment.inactiveDate = Instant.now()
        treatment.isActive = false

        val observation = Observation().apply {
            concept = ConceptEntity().apply { uuid = ObservationConcept.ACTIVE.uuid }
            displayValue = " 1"
            display = "Active:$displayValue"
            uuid = UUID.randomUUID().toString()
            dateCreated = treatment.creationDate.formatToApiRequest()
        }

        coEvery { restApi.getObservationByUuid(treatment.observationStatusUuid!!) } returns createCall(
            response = Response.success(observation)
        )
        coEvery {
            restApi.updateObservation(
                observation.uuid,
                mapOf("value" to 0, "obsDatetime" to treatment.inactiveDate!!.formatToApiRequest())
            )
        } returns mockk(relaxed = true)


        runBlocking {
            treatmentRepository.finalise(treatment)
        }

        coVerify { restApi.getObservationByUuid(treatment.observationStatusUuid!!) }
        coVerify {
            restApi.updateObservation(
                observation.uuid,
                mapOf("value" to 0, "obsDatetime" to treatment.inactiveDate!!.formatToApiRequest())
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

        val treatmentToEdit = TreatmentExample.activeTreatment()

        val treatmentUpdated = Treatment(
            "Other",
            "Paracetamol",
            treatmentToEdit.medicationType,
            "hello",
            visitUuid = treatmentToEdit.visitUuid
        )
        val visit = VisitExample.random(
            id = UUID.fromString(treatmentToEdit.visitUuid),
        )

        val callDelete = mockk<Call<ResponseBody>>(relaxed = true)
        coEvery { restApi.deleteEncounter(any()) } returns callDelete
        coEvery { callDelete.execute() } returns Response.success("".toResponseBody())

        val call = mockk<Call<Encounter>>(relaxed = true)
        coEvery { restApi.createEncounter(any()) } returns call
        coEvery { call.execute() } returns Response.success(Encounter().apply {
            uuid = "encounterUuid"
            patient = Patient().apply { uuid = "18y3774283999cs" }
        })
        coEvery { visitRepository.getVisitByUuid(UUID.fromString(treatmentToEdit.visitUuid)) } returns visit

        runBlocking {
            val result = treatmentRepository.updateTreatment(treatmentToEdit, treatmentUpdated)
            assertEquals(Result.success(true), result)
        }
    }

    @Test
    fun `should returns true if no value changed to update a treatment`() = runTest {
        val treatmentToEdit = TreatmentExample.activeTreatment()
        val result = treatmentRepository.updateTreatment(treatmentToEdit, treatmentToEdit)

        assertEquals(Result.success(true), result)
    }

    private inline fun <reified T> createCall(response: Response<T>): Call<T> = object : Call<T> {
        override fun enqueue(callback: retrofit2.Callback<T>) = TODO()
        override fun isExecuted() = TODO()
        override fun clone(): Call<T> = TODO()
        override fun isCanceled() = TODO()
        override fun request(): Request = TODO()
        override fun timeout(): Timeout { TODO()
        }

        override fun cancel() = TODO()
        override fun execute(): Response<T> = response
    }
}