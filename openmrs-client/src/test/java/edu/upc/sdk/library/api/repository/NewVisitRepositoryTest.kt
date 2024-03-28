package edu.upc.sdk.library.api.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.Visit
import edu.upc.openmrs.application.OpenMRS
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.dao.LocationDAO
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.databases.entities.LocationEntity
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.Observation
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.VisitExample
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Call
import retrofit2.Response
import rx.Observable
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.SECONDS
import java.util.UUID
import edu.upc.sdk.library.models.Visit as OpenMRSVisit

@RunWith(AndroidJUnit4::class)
class NewVisitRepositoryTest {

    @MockK
    private lateinit var restApi: RestApi

    @MockK
    private lateinit var visitDAO: VisitDAO

    @MockK
    private lateinit var locationDAO: LocationDAO

    @InjectMockKs
    private lateinit var visitRepository: NewVisitRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should get visit with Vitals by uuid`() {
        val visitUuid = UUID.randomUUID()
        val patientUuid = UUID.randomUUID()
        val expectedVisit = Visit(
            visitUuid,
            patientUuid,
            "La casa de Ale",
            LocalDateTime.now().truncatedTo(SECONDS),
            BloodPressure(120, 80, 70),
            177,
            70.0f
        )

        val openMRSVisit = VisitExample.withVitals(
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

        coEvery { visitDAO.getVisitsIDByUUID(visitUuid.toString()) } returns Observable.just(visitId)
        coEvery { visitDAO.getVisitByID(visitId) } returns Observable.just(openMRSVisit)
        coEvery { visitDAO.saveOrUpdate(openMRSVisit, patientId) } returns Observable.just(visitId)

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
    fun `should log an error and continue if updating local database fails`() {
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

        coEvery { visitDAO.getVisitsIDByUUID(visitUuid.toString()) } throws Exception()

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
        val expectedVisit = Visit(
            visitId,
            patientId,
            "La casa de Ale",
            LocalDateTime.now().truncatedTo(SECONDS),
            BloodPressure(120, 80, 70),
            177,
            70.0f
        )

        val openMRSVisit = VisitExample.withVitals(
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

        val call = mockk<Call<OpenMRSVisit>>(relaxed = true)
        every { locationDAO.findLocationByName(any()) } returns LocationEntity("La casa de Ale")
        every { restApi.startVisit(any()) } returns call
        every { call.execute() } returns Response.success(openMRSVisit)
        every { visitDAO.saveOrUpdate(any(), any()) } returns Observable.just(1)

        val result = visitRepository.startVisit(patient)

        verify { restApi.startVisit(any()) }

        assertEquals(result, expectedVisit)
    }

    @Test(expected = IOException::class)
    fun `should throw exception when starting visit in server fails`() {
        val patient = Patient().apply { uuid = UUID.randomUUID().toString(); id = 123L }

        val call = mockk<Call<OpenMRSVisit>>(relaxed = true)
        every { locationDAO.findLocationByName(any()) } returns LocationEntity("La casa de Ale")
        every { restApi.startVisit(any()) } returns call
        every { call.execute() } returns Response.error(500, mockk<ResponseBody>())

        visitRepository.startVisit(patient)

        verify { restApi.startVisit(any()) }
        verify { visitDAO wasNot Called }
    }

    @Test
    fun `should log error and not fail when error saving new visit in local db`() {
        val patientId = UUID.randomUUID()
        val visitId = UUID.randomUUID()
        val patient = Patient().apply { uuid = patientId.toString(); id = 123L }
        val expectedVisit = Visit(
            visitId,
            patientId,
            "La casa de Ale",
            LocalDateTime.now().truncatedTo(SECONDS),
            BloodPressure(120, 80, 70),
            177,
            70.0f
        )

        val openMRSVisit = VisitExample.withVitals(
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

        val call = mockk<Call<OpenMRSVisit>>(relaxed = true)
        every { locationDAO.findLocationByName(any()) } returns LocationEntity("La casa de Ale")
        every { restApi.startVisit(any()) } returns call
        every { call.execute() } returns Response.success(openMRSVisit)
        every { visitDAO.saveOrUpdate(any(), any()) } throws Exception()

        val result = visitRepository.startVisit(patient)

        verify { restApi.startVisit(any()) }

        assertEquals(result, expectedVisit)
    }
}