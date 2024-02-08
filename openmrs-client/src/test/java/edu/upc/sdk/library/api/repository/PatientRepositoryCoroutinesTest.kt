package edu.upc.sdk.library.api.repository

import androidx.work.WorkManager
import edu.upc.openmrs.MockCrashlyticsLogger
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.api.RestServiceBuilder
import edu.upc.sdk.library.databases.AppDatabase
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Results
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit

class PatientRepositoryCoroutinesTest {

    private lateinit var patientRepositoryCoroutines: PatientRepositoryCoroutines

    private lateinit var restApi: RestApi

    @Before
    fun setUp() {
        restApi = mockk(relaxed = true)
        mockStaticMethodsNeededToInstantiateBaseRepository()
        patientRepositoryCoroutines = PatientRepositoryCoroutines(MockCrashlyticsLogger())
    }

    @Test
    fun `should return list of patients when call response is successful`() {
        val patient1 = Patient().apply { uuid = "uuid1" }
        val patient2: Patient = Patient().apply { uuid = "uuid2" }
        val patients = listOf(patient1, patient2)
        val response = Response.success(Results<Patient>().apply { results = patients })
        val call = mockk<Call<Results<Patient>>>(relaxed = true)

        coEvery { restApi.getPatients(any(), any()) } returns call
        coEvery { call.execute() } returns response

        runBlocking {
            val result = patientRepositoryCoroutines.findPatients("query")

            assert(result.isRight())
            assertEquals(patients, result.fold({ }, { it }))
        }
    }

    @Test
    fun `should return empty list when call response is successful but no matches found`() {
        val response = Response.success(Results<Patient>())
        val call = mockk<Call<Results<Patient>>>(relaxed = true)

        coEvery { restApi.getPatients(any(), any()) } returns call
        coEvery { call.execute() } returns response

        runBlocking {
            val result = patientRepositoryCoroutines.findPatients("query")

            assert(result.isRight())
            assertEquals(emptyList<Patient>(), result.fold({ }, { it }))
        }

    }

    @Test
    fun `should return Error when call response is not successful`() {
        val errorResponse = mockk<Response<Results<Patient>>>()
        every { errorResponse.isSuccessful } returns false
        every { errorResponse.code() } returns 400
        every { errorResponse.message() } returns "Client Error"
        val call = mockk<Call<Results<Patient>>>(relaxed = true)

        val expected = Error("Failed to find patients: 400 - Client Error")

        coEvery { restApi.getPatients(any(), any()) } returns call
        coEvery { call.execute() } returns errorResponse

        runBlocking {
            val result = patientRepositoryCoroutines.findPatients("query")

            assert(result.isLeft())
            assertEquals(expected.message, result.fold({ it.message }, { }))
        }
    }

    @Test
    fun `should return Error when call fails`() {
        val call = mockk<Call<Results<Patient>>>(relaxed = true)

        val expected = Error("Failed to find patients: RuntimeException")

        coEvery { restApi.getPatients(any(), any()) } returns call
        coEvery { call.execute() } throws RuntimeException("RuntimeException")

        runBlocking {
            val result = patientRepositoryCoroutines.findPatients("query")

            assert(result.isLeft())
            assert(expected.message == result.fold({ it.message }, { }))
        }
    }

    private fun mockStaticMethodsNeededToInstantiateBaseRepository() {
        mockkStatic(OpenmrsAndroid::class)
        every { OpenmrsAndroid.getServerUrl() } returns "http://localhost:8080/openmrs"
        mockkConstructor(Retrofit.Builder::class)
        mockkStatic(RestServiceBuilder::class)
        every { RestServiceBuilder.createService() } returns restApi
        mockkStatic(AppDatabase::class)
        every { AppDatabase.getDatabase(any()) } returns mockk(relaxed = true)
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns mockk(relaxed = true)
    }
}
