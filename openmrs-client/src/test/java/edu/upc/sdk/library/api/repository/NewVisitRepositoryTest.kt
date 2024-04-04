package edu.upc.sdk.library.api.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.Visit
import edu.upc.blopup.model.VisitExample
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.dao.LocationDAO
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.databases.entities.LocationEntity
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.library.models.Encountercreate
import edu.upc.sdk.library.models.Obscreate
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.utilities.ApplicationConstants
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
import okhttp3.ResponseBody.Companion.toResponseBody
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
import edu.upc.sdk.library.models.VisitExample as OpenMRSVisitExample

@RunWith(AndroidJUnit4::class)
class NewVisitRepositoryTest {

    @MockK
    private lateinit var restApi: RestApi

    @MockK
    private lateinit var visitDAO: VisitDAO

    @MockK
    private lateinit var locationDAO: LocationDAO

    @MockK(relaxed = true)
    private lateinit var logger: OpenMRSLogger

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
        val location = "La casa de Ale"
        val visit = Visit(
            visitId,
            patientId,
            location,
            LocalDateTime.now().truncatedTo(SECONDS),
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

        every { visitDAO.saveOrUpdate(any(), any()) } returns Observable.just(1)

        runBlocking {
            val result = visitRepository.startVisit(patient, visit)
            assertEquals(result, visit)
        }

        verify(exactly = 1) { restApi.startVisit(any()) }
        verify(exactly = 1) { restApi.createEncounter(any()) }
    }

    @Test(expected = IOException::class)
    fun `should throw exception when starting visit in server fails`() {
        val patient = Patient().apply { uuid = UUID.randomUUID().toString(); id = 123L }

        val call = mockk<Call<OpenMRSVisit>>(relaxed = true)
        every { locationDAO.findLocationByName(any()) } returns LocationEntity("La casa de Ale")
        every { restApi.startVisit(any()) } returns call
        every { call.execute() } returns Response.error(500, mockk<ResponseBody>())

        runBlocking { visitRepository.startVisit(patient, visit = VisitExample.random()) }

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

        every { visitDAO.saveOrUpdate(any(), any()) } throws Exception()

        runBlocking {
            val result = visitRepository.startVisit(patient, visit = VisitExample.random())

            assertEquals(result, expectedVisit)
        }
        verify { restApi.startVisit(any()) }
    }

    @Test(expected = IOException::class)
    fun `should delete visit if creating visit encounters fails`() {
        val visit = VisitExample.random()
        val patient = Patient().apply { uuid = UUID.randomUUID().toString(); id = 123L }

        every { locationDAO.findLocationByName(any()) } returns LocationEntity("La casa de Aleh")

        val callStartVisit = mockk<Call<OpenMRSVisit>>(relaxed = true)
        every { restApi.startVisit(any()) } returns callStartVisit
        every { callStartVisit.execute() } returns Response.success(edu.upc.sdk.library.models.Visit())

        val callCreateEncounter = mockk<Call<Encounter>>(relaxed = true)
        every { restApi.createEncounter(any()) } returns callCreateEncounter
        every { callCreateEncounter.execute() } returns Response.error(500, mockk<ResponseBody>())

        val callDeleteVisit = mockk<Call<ResponseBody>>(relaxed = true)
        every { restApi.deleteVisit(visit.id.toString()) } returns callDeleteVisit
        every { callDeleteVisit.execute() } returns Response.error<ResponseBody>(500, "".toResponseBody(null))
        every { visitDAO.deleteVisitByUuid(visit.id.toString()) } returns Observable.just(true)

        runBlocking { visitRepository.startVisit(patient, visit) }

        verify { restApi.startVisit(any()) }
        verify { visitDAO wasNot Called }
    }

    @Test
    fun `should delete visit`() {
        val visitUuid = UUID.randomUUID()

        val response = Response.success("".toResponseBody(null))

        val call = mockk<Call<ResponseBody>>(relaxed = true)
        every { restApi.deleteVisit(visitUuid.toString()) } returns call
        every { call.execute() } returns response
        every { visitDAO.deleteVisitByUuid(visitUuid.toString()) } returns Observable.just(true)

        runBlocking {
            val result = visitRepository.deleteVisit(visitUuid)

            assertEquals(result, true)
        }
    }

    @Test
    fun `should return false if delete visit fails and local visit is not deleted`() {
        val visitUuid = UUID.randomUUID()

        val response = Response.error<ResponseBody>(500, "".toResponseBody(null))

        val call = mockk<Call<ResponseBody>>(relaxed = true)
        every { restApi.deleteVisit(visitUuid.toString()) } returns call
        every { call.execute() } returns response

        runBlocking {
            val result = visitRepository.deleteVisit(visitUuid)

            verify { visitDAO wasNot Called }
            assertEquals(result, false)
        }
    }

    private fun givenEncounterCreate(visit: Visit) = Encountercreate().apply {
        this.visit = visit.id.toString()
        this.patient = visit.patientId.toString()
        encounterType = EncounterType.VITALS
        observations = listOfNotNull(
            Obscreate().apply {
                concept = ApplicationConstants.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT
                value = visit.bloodPressure.systolic.toString()
                obsDatetime = visit.startDate.toString()
                person = visit.patientId.toString()
            },
            Obscreate().apply {
                concept = ApplicationConstants.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT
                value = visit.bloodPressure.diastolic.toString()
                obsDatetime = visit.startDate.toString()
                person = visit.patientId.toString()
            },
            Obscreate().apply {
                concept = ApplicationConstants.VitalsConceptType.HEART_RATE_FIELD_CONCEPT
                value = visit.bloodPressure.pulse.toString()
                obsDatetime = visit.startDate.toString()
                person = visit.patientId.toString()
            },
            visit.heightCm?.let {
                Obscreate().apply {
                    concept = ApplicationConstants.VitalsConceptType.HEIGHT_FIELD_CONCEPT
                    value = it.toString()
                    obsDatetime = visit.startDate.toString()
                    person = visit.patientId.toString()
                }
            },
            visit.weightKg?.let {
                Obscreate().apply {
                    concept = ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT
                    value = it.toString()
                    obsDatetime = visit.startDate.toString()
                    person = visit.patientId.toString()
                }
            }
        )
    }
}