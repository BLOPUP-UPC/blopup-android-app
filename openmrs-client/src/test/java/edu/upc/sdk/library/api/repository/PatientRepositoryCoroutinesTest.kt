package edu.upc.sdk.library.api.repository

import edu.upc.sdk.library.CrashlyticsLogger
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.PatientDto
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.Results
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import retrofit2.Call
import retrofit2.Response
import rx.Observable

class PatientRepositoryCoroutinesTest {

    @MockK
    private lateinit var patientDAOMock: PatientDAO

    @MockK
    private lateinit var restApi: RestApi

    @MockK(relaxed = true)
    private lateinit var crashlytics: CrashlyticsLogger

    @InjectMockKs
    private lateinit var patientRepositoryCoroutines: PatientRepositoryCoroutines

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should return list of patients when call response is successful`() {
        val patient1 = Patient().apply {
            uuid = "uuid1"; isDeceased = false; birthdate = "1990-01-01"; gender = "M"
        }

        val patient2: Patient = Patient().apply {
            uuid = "uuid2"; isDeceased = false; birthdate = "1990-01-01"; gender = "M"
        }
        val patients = listOf(patient1.patientDto, patient2.patientDto)
        val response = Response.success(Results<PatientDto>().apply { results = patients })
        val call = mockk<Call<Results<PatientDto>>>(relaxed = true)

        coEvery { restApi.getPatientsDto(any(), any()) } returns call
        coEvery { call.execute() } returns response

        runBlocking {
            val result = patientRepositoryCoroutines.findPatients("query")

            assert(result.isRight())
            assert(patient1.uuid == result.fold({ }, { it[0].uuid }))
            assert(patient2.uuid == result.fold({ }, { it[1].uuid }))
        }
    }

    @Test
    fun `should return empty list when call response is successful but no matches found`() {
        val response = Response.success(Results<PatientDto>())
        val call = mockk<Call<Results<PatientDto>>>(relaxed = true)

        coEvery { restApi.getPatientsDto(any(), any()) } returns call
        coEvery { call.execute() } returns response

        runBlocking {
            val result = patientRepositoryCoroutines.findPatients("query")

            assert(result.isRight())
            assertEquals(emptyList<Patient>(), result.fold({ }, { it }))
        }

    }

    @Test
    fun `should return Error when call response is not successful`() {
        val errorResponse = mockk<Response<Results<PatientDto>>>()
        every { errorResponse.isSuccessful } returns false
        every { errorResponse.code() } returns 400
        every { errorResponse.message() } returns "Client Error"
        val call = mockk<Call<Results<PatientDto>>>(relaxed = true)

        val expected = Error("Failed to find patients: 400 - Client Error")

        coEvery { restApi.getPatientsDto(any(), any()) } returns call
        coEvery { call.execute() } returns errorResponse

        runBlocking {
            val result = patientRepositoryCoroutines.findPatients("query")

            assert(result.isLeft())
            assertEquals(expected.message, result.fold({ it.message }, { }))
        }
    }

    @Test
    fun `should return Error when call fails`() {
        val call = mockk<Call<Results<PatientDto>>>(relaxed = true)

        val expected = Error("Failed to find patients: RuntimeException")

        coEvery { restApi.getPatientsDto(any(), any()) } returns call
        coEvery { call.execute() } throws RuntimeException("RuntimeException")

        runBlocking {
            val result = patientRepositoryCoroutines.findPatients("query")

            assert(result.isLeft())
            assert(expected.message == result.fold({ it.message }, { }))
        }
    }

    @Test
    fun `should return all patients locally`() {

        val patientList = listOf(Patient())

        coEvery {patientDAOMock.allPatients  } returns Observable.just(patientList)


        runBlocking {
            val result = patientRepositoryCoroutines.getAllPatientsLocally()

            assertEquals(Result.Success(patientList), result)
        }
    }

    @Test
    fun `should return Error when get all patients locally fails`() {

        coEvery {patientDAOMock.allPatients  } returns Observable.just(emptyList())


        runBlocking {
            val result = patientRepositoryCoroutines.getAllPatientsLocally()

            assertTrue(result is Result.Error)
        }
    }
}
