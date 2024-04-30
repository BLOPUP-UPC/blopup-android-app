package edu.upc.sdk.library.api.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.Visit
import edu.upc.blopup.model.VisitExample
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.dao.LocationDAO
import edu.upc.sdk.library.databases.entities.LocationEntity
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.OpenMRSVisit
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.Results
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.SECONDS
import java.util.UUID
import edu.upc.sdk.library.models.OpenMrsVisitExample as OpenMRSVisitExample


@RunWith(AndroidJUnit4::class)
class VisitRepositoryTest {

    @MockK
    private lateinit var restApi: RestApi

    @MockK
    private lateinit var locationDAO: LocationDAO

    @MockK(relaxed = true)
    private lateinit var logger: OpenMRSLogger

    @InjectMockKs
    private lateinit var visitRepository: VisitRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should get visit with Vitals by uuid`() = runTest {
        val visitUuid = UUID.randomUUID()
        val patientUuid = UUID.randomUUID()
        val expectedVisit = Visit(
            visitUuid,
            patientUuid,
            "La casa de Ale",
            Instant.now().truncatedTo(SECONDS),
            BloodPressure(120, 80, 70),
            177,
            70.0f
        )

        val openMRSVisit = OpenMRSVisitExample.withVitals(
            visitUuid.toString(),
            patientUuid.toString(),
            expectedVisit.startDate,
            expectedVisit.location,
            expectedVisit.bloodPressure.systolic,
            expectedVisit.bloodPressure.diastolic,
            expectedVisit.bloodPressure.pulse,
            expectedVisit.weightKg,
            expectedVisit.heightCm
        )
        val response = Response.success(openMRSVisit)

        val call = mockk<Call<OpenMRSVisit>>(relaxed = true)
        coEvery { restApi.getVisitByUuid(visitUuid.toString()) } returns call
        coEvery { call.execute() } returns response

        val result = visitRepository.getVisitByUuid(visitUuid)

        assertEquals(expectedVisit, result)
    }

    @Test
    fun `should get all visits by uuid`() = runTest {
        val visit1 = VisitExample.random()
        val visit2 = VisitExample.random(patientId = visit1.patientId)

        val openMRSVisit1 = OpenMRSVisitExample.withVitals(
            visit1.id.toString(),
            visit1.patientId.toString(),
            visit1.startDate,
            visit1.location,
            visit1.bloodPressure.systolic,
            visit1.bloodPressure.diastolic,
            visit1.bloodPressure.pulse,
            visit1.weightKg,
            visit1.heightCm
        )
        val openMRSVisit2 = OpenMRSVisitExample.withVitals(
            visit2.id.toString(),
            visit2.patientId.toString(),
            visit2.startDate,
            visit2.location,
            visit2.bloodPressure.systolic,
            visit2.bloodPressure.diastolic,
            visit2.bloodPressure.pulse,
            visit2.weightKg,
            visit2.heightCm
        )

        val response = Response.success(Results<OpenMRSVisit>().apply{
            results = listOf(openMRSVisit1, openMRSVisit2)
        })

        val call = mockk<Call<Results<OpenMRSVisit>>>(relaxed = true)
        coEvery { restApi.findVisitsByPatientUUID(visit1.patientId.toString(), EXPECTED_VISIT_API_REPRESENTATION) } returns call
        coEvery { call.execute() } returns response

        val result = visitRepository.getVisitsByPatientUuid(visit1.patientId)

        assertEquals(listOf(visit1, visit2), result)
    }

    @Test
    fun `should return empty list when no visits found`() = runTest {
        val patientId = UUID.randomUUID()
        val response = Response.success(Results<OpenMRSVisit>().apply{
            results = emptyList()
        })

        val call = mockk<Call<Results<OpenMRSVisit>>>(relaxed = true)
        coEvery { restApi.findVisitsByPatientUUID(patientId.toString(), EXPECTED_VISIT_API_REPRESENTATION) } returns call
        coEvery { call.execute() } returns response

        val result = visitRepository.getVisitsByPatientUuid(patientId)

        assertEquals(emptyList<Visit>(), result)
    }

    @Test(expected = IOException::class)
    fun `should throw exception with not successful network response`() = runTest {
        val patientId = UUID.randomUUID()

        val call = mockk<Call<Results<OpenMRSVisit>>>(relaxed = true)
        coEvery { restApi.findVisitsByPatientUUID(patientId.toString(), EXPECTED_VISIT_API_REPRESENTATION) } returns call
        every { call.execute() } returns Response.error(500, "{\"error\":[\"Generic error\"]}"
            .toResponseBody("application/json".toMediaTypeOrNull()))

        visitRepository.getVisitsByPatientUuid(patientId)
    }

    @Test
    fun `should end visit`() {
        val visitUuid = UUID.randomUUID()
        val patientUuid = UUID.randomUUID()

        val visitId = 123L
        val patientId = 456L

        val openMRSVisit = OpenMRSVisit().apply {
            stopDatetime = Instant.now().toString()
            uuid = visitUuid.toString()
            id = visitId
            patient = Patient().apply { uuid = patientUuid.toString(); id = patientId }
        }

        val response = Response.success(openMRSVisit)

        val call = mockk<Call<OpenMRSVisit>>(relaxed = true)
        coEvery { restApi.endVisitByUUID(visitUuid.toString(), any()) } returns call
        coEvery { call.execute() } returns response

        runBlocking {
            val result = visitRepository.endVisit(visitUuid)

            assertEquals(result, true)
        }
    }

    @Test(expected = Exception::class)
    fun `should throw exception if ending visit fails`() {
        val visitUuid = UUID.randomUUID()

        val response = Response.error<OpenMRSVisit>(500, mockk())

        val call = mockk<Call<OpenMRSVisit>>(relaxed = true)
        coEvery { restApi.endVisitByUUID(visitUuid.toString(), any()) } returns call
        coEvery { call.execute() } returns response

        runBlocking {
            visitRepository.endVisit(visitUuid)
        }
    }

    @Test
    fun `end visit should log an error and continue if updating local database fails`() {
        val visitUuid = UUID.randomUUID()
        val patientUuid = UUID.randomUUID()

        val visitId = 123L
        val patientId = 456L

        val openMRSVisit = OpenMRSVisit().apply {
            stopDatetime = Instant.now().toString()
            uuid = visitUuid.toString()
            id = visitId
            patient = Patient().apply { uuid = patientUuid.toString(); id = patientId }
        }

        val response = Response.success(openMRSVisit)

        val call = mockk<Call<OpenMRSVisit>>(relaxed = true)
        coEvery { restApi.endVisitByUUID(visitUuid.toString(), any()) } returns call
        coEvery { call.execute() } returns response

        runBlocking {
            val result = visitRepository.endVisit(visitUuid)

            assertEquals(result, true)
        }
    }

    @Test
    fun `should start new visit`() {
        val patientId = UUID.randomUUID()
        val visitId = UUID.randomUUID()
        val patient = Patient().apply { uuid = patientId.toString(); id = 123L }
        val location = ""
        val visit = Visit(
            visitId,
            patientId,
            location,
            Instant.now().truncatedTo(SECONDS),
            BloodPressure(120, 80, 70),
            177,
            70.0f
        )

        val expectedOpenMRSVisit = OpenMRSVisitExample.withVitals(
            visitId.toString(),
            patientId.toString(),
            visit.startDate,
            visit.location,
            visit.bloodPressure.systolic,
            visit.bloodPressure.diastolic,
            visit.bloodPressure.pulse,
            visit.weightKg,
            visit.heightCm
        )

        every { locationDAO.findLocationByName(any()) } returns LocationEntity(location)

        val callStartVisit = mockk<Call<OpenMRSVisit>>(relaxed = true)
        every { restApi.startVisit(any()) } returns callStartVisit
        every { callStartVisit.execute() } returns Response.success(expectedOpenMRSVisit)

        val callCreateEncounter = mockk<Call<Encounter>>(relaxed = true)
        every { restApi.createEncounter(any()) } returns callCreateEncounter
        every { callCreateEncounter.execute() } returns Response.success(Encounter())

        runBlocking {
            val result = visitRepository.startVisit(patient, visit.bloodPressure, visit.heightCm, visit.weightKg)
            assertEquals(result, Result.Success(visit))
        }

        verify(exactly = 1) { restApi.startVisit(any()) }
        verify(exactly = 1) { restApi.createEncounter(any()) }
    }

    @Test
    fun `should throw exception when starting visit in server fails`() {
        val patient = Patient().apply { uuid = UUID.randomUUID().toString(); id = 123L }
        val visit = VisitExample.random()

        val call = mockk<Call<OpenMRSVisit>>(relaxed = true)
        every { locationDAO.findLocationByName(any()) } returns LocationEntity("La casa de Ale")
        every { restApi.startVisit(any()) } returns call

        every { call.execute() } returns Response.error(500, "{\"error\":[\"Generic error\"]}"
            .toResponseBody("application/json".toMediaTypeOrNull()))

        runBlocking {
            val result = visitRepository.startVisit(patient, visit.bloodPressure, visit.heightCm, visit.weightKg)
            assertThat(result, instanceOf(Result.Error::class.java))
            when(result) {
                is Result.Error -> assertEquals(result.throwable.message, "Error starting visit Response.error()")
                else -> {}
            }
        }

        verify { restApi.startVisit(any()) }
    }

    @Test
    fun `should log error and not fail when error saving new visit in local db`() {
        val patientId = UUID.randomUUID()
        val visitId = UUID.randomUUID()
        val patient = Patient().apply { uuid = patientId.toString(); id = 123L }
        val expectedVisit = Visit(
            visitId,
            patientId,
            "",
            Instant.now().truncatedTo(SECONDS),
            BloodPressure(120, 80, 70),
            177,
            70.0f
        )

        val openMRSVisit = OpenMRSVisitExample.withVitals(
            visitId.toString(),
            patientId.toString(),
            expectedVisit.startDate,
            expectedVisit.location,
            expectedVisit.bloodPressure.systolic,
            expectedVisit.bloodPressure.diastolic,
            expectedVisit.bloodPressure.pulse,
            expectedVisit.weightKg,
            expectedVisit.heightCm
        )

        every { locationDAO.findLocationByName(any()) } returns LocationEntity("La casa de Ale")

        val call = mockk<Call<OpenMRSVisit>>(relaxed = true)
        every { restApi.startVisit(any()) } returns call
        every { call.execute() } returns Response.success(openMRSVisit)

        val callCreateEncounter = mockk<Call<Encounter>>(relaxed = true)
        every { restApi.createEncounter(any()) } returns callCreateEncounter
        every { callCreateEncounter.execute() } returns Response.success(Encounter())

        runBlocking {
            val result = visitRepository.startVisit(patient, expectedVisit.bloodPressure, expectedVisit.heightCm, expectedVisit.weightKg)

            assertEquals(result, Result.Success(expectedVisit))
        }
        verify { restApi.startVisit(any()) }
    }

    @Test
    fun `should delete visit if creating visit encounters fails`() {
        val visit = VisitExample.random()
        val patient = Patient().apply { uuid = UUID.randomUUID().toString(); id = 123L }

        every { locationDAO.findLocationByName(any()) } returns LocationEntity("La casa de Aleh")

        val callStartVisit = mockk<Call<OpenMRSVisit>>(relaxed = true)
        every { restApi.startVisit(any()) } returns callStartVisit
        every { callStartVisit.execute() } returns
                Response.success(OpenMRSVisit().apply {
                    uuid = visit.id.toString()
                    startDatetime = visit.startDate.toString()
                })

        val callCreateEncounter = mockk<Call<Encounter>>(relaxed = true)
        every { restApi.createEncounter(any()) } returns callCreateEncounter
        every { callCreateEncounter.execute() } returns Response.error(500, "{\"error\":[\"Generic error\"]}"
            .toResponseBody("application/json".toMediaTypeOrNull()))

        val callDeleteVisit = mockk<Call<ResponseBody>>(relaxed = true)
        every { restApi.deleteVisit(visit.id.toString()) } returns callDeleteVisit
        every { callDeleteVisit.execute() } returns Response.error<ResponseBody>(500, "".toResponseBody(null))

        runBlocking {
            val result = visitRepository.startVisit(patient, visit.bloodPressure, visit.heightCm, visit.weightKg)
            assertThat(result, instanceOf(Result.Error::class.java))
            when(result) {
                is Result.Error -> assertEquals(result.throwable.message, "Error creating encounter Response.error()")
                else -> {}
            }
        }

        verify { restApi.startVisit(any()) }
        verify(exactly = 1) { restApi.deleteVisit(any()) }
    }

    @Test(expected = IOException::class)
    fun `should throw exception if network fails`() = runTest {
        val patientUuid = UUID.randomUUID()

        val call = mockk<Call<Results<OpenMRSVisit>>>(relaxed = true)
        coEvery { restApi.findActiveVisitsByPatientUUID(patientUuid.toString()) } returns call
        coEvery { call.execute() } returns Response.error(500, "{\"error\":[\"Generic error\"]}"
            .toResponseBody("application/json".toMediaTypeOrNull()))

        visitRepository.getActiveVisit(patientUuid)
    }

    @Test
    fun `should return null if no active visit are found`() = runTest {
        val patientUuid = UUID.randomUUID()
        val response = Response.success(Results<OpenMRSVisit>().apply{
            results = emptyList()
        })

        val call = mockk<Call<Results<OpenMRSVisit>>>(relaxed = true)
        coEvery { restApi.findActiveVisitsByPatientUUID(patientUuid.toString()) } returns call
        coEvery { call.execute() } returns response

        val result = visitRepository.getActiveVisit(patientUuid)

        assertNull(result)
    }

    @Test
    fun `should return the visit if active`() = runTest {
        val activeVisit = VisitExample.random()

        val openMRSVisit = OpenMRSVisitExample.withVitals(
            activeVisit.id.toString(),
            activeVisit.patientId.toString(),
            activeVisit.startDate,
            activeVisit.location,
            activeVisit.bloodPressure.systolic,
            activeVisit.bloodPressure.diastolic,
            activeVisit.bloodPressure.pulse,
            activeVisit.weightKg,
            activeVisit.heightCm
        )

        val response = Response.success(Results<OpenMRSVisit>().apply{
            results = listOf(openMRSVisit)
        })

        val call = mockk<Call<Results<OpenMRSVisit>>>(relaxed = true)
        coEvery { restApi.findActiveVisitsByPatientUUID(activeVisit.patientId.toString()) } returns call
        coEvery { call.execute() } returns response

        val result = visitRepository.getActiveVisit(activeVisit.patientId)

        assertEquals(activeVisit, result)
    }

    @Test
    fun `should return the last active visit last if more than one exists`() = runTest {
        val activeVisit = VisitExample.random()
        val activeVisit2 = VisitExample.random(patientId = activeVisit.patientId)

        val openMRSVisit = OpenMRSVisitExample.withVitals(
            activeVisit.id.toString(),
            activeVisit.patientId.toString(),
            activeVisit.startDate,
            activeVisit.location,
            activeVisit.bloodPressure.systolic,
            activeVisit.bloodPressure.diastolic,
            activeVisit.bloodPressure.pulse,
            activeVisit.weightKg,
            activeVisit.heightCm
        )

        val openMRSVisit2 = OpenMRSVisitExample.withVitals(
            activeVisit2.id.toString(),
            activeVisit2.patientId.toString(),
            activeVisit2.startDate,
            activeVisit2.location,
            activeVisit2.bloodPressure.systolic,
            activeVisit2.bloodPressure.diastolic,
            activeVisit2.bloodPressure.pulse,
            activeVisit2.weightKg,
            activeVisit2.heightCm
        )

        val response = Response.success(Results<OpenMRSVisit>().apply{
            results = listOf(openMRSVisit, openMRSVisit2)
        })

        val call = mockk<Call<Results<OpenMRSVisit>>>(relaxed = true)
        coEvery { restApi.findActiveVisitsByPatientUUID(activeVisit.patientId.toString()) } returns call
        coEvery { call.execute() } returns response

        val result = visitRepository.getActiveVisit(activeVisit.patientId)

        assertEquals(activeVisit, result)
    }

    @Test
    fun `latest visit with height should return null if no visit exists`() = runTest {
        val patientId = UUID.randomUUID()
        val response = Response.success(Results<OpenMRSVisit>().apply{
            results = listOf()
        })

        val call = mockk<Call<Results<OpenMRSVisit>>>(relaxed = true)
        coEvery { restApi.findVisitsByPatientUUID(patientId.toString(), EXPECTED_VISIT_API_REPRESENTATION) } returns call
        coEvery { call.execute() } returns response

        assertNull(visitRepository.getLatestVisitWithHeight(patientId))
    }

    @Test
    fun `latest visit with height should return null if no visit exist with height`() = runTest {
        val openMRSVisit1 = OpenMRSVisitExample.withVitals(
            height = null
        )
        val openMRSVisit2 = OpenMRSVisitExample.withVitals(
            patientUuid = openMRSVisit1.patient.uuid!!,
            height = null
        )
        val response = Response.success(Results<OpenMRSVisit>().apply{
            results = listOf(openMRSVisit1, openMRSVisit2)
        })

        val call = mockk<Call<Results<OpenMRSVisit>>>(relaxed = true)
        coEvery { restApi.findVisitsByPatientUUID(openMRSVisit1.patient.uuid, EXPECTED_VISIT_API_REPRESENTATION) } returns call
        coEvery { call.execute() } returns response

        assertNull(visitRepository.getLatestVisitWithHeight(UUID.fromString(openMRSVisit1.patient.uuid)))
    }

    @Test
    fun `latest visit with height should return the latest visit with height`() = runTest {
        val openMRSVisit1 = OpenMRSVisitExample.withVitals(
            height = 180,
            visitStartDate = Instant.now().minus(1, ChronoUnit.DAYS)
        )
        val openMRSVisit2 = OpenMRSVisitExample.withVitals(
            patientUuid = openMRSVisit1.patient.uuid!!,
            height = 190,
            visitStartDate = Instant.now()
        )
        val response = Response.success(Results<OpenMRSVisit>().apply{
            results = listOf(openMRSVisit1, openMRSVisit2)
        })

        val call = mockk<Call<Results<OpenMRSVisit>>>(relaxed = true)
        coEvery { restApi.findVisitsByPatientUUID(openMRSVisit1.patient.uuid, EXPECTED_VISIT_API_REPRESENTATION) } returns call
        coEvery { call.execute() } returns response

        val visit = visitRepository.getLatestVisitWithHeight(UUID.fromString(openMRSVisit1.patient.uuid))
        assertEquals(visit?.heightCm, 190)
    }

    companion object {
        const val EXPECTED_VISIT_API_REPRESENTATION = "custom:(uuid,patient:ref,location:ref,visitType:ref,startDatetime,stopDatetime,encounters:custom:(encounterType:custom:(display),obs:custom:(uuid,concept:custom:(uuid),display,value)))"
    }
}