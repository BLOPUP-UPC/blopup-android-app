package edu.upc.sdk.library.api.repository

import androidx.work.WorkManager
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.api.RestServiceBuilder
import edu.upc.sdk.library.api.repository.DoctorRepository.*
import edu.upc.sdk.library.api.repository.DoctorRepository.Companion.DOCTOR_PROVIDER_UUID
import edu.upc.sdk.library.databases.AppDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import java.net.UnknownHostException

class DoctorRepositoryTest {

    private lateinit var restApi: RestApi
    private lateinit var doctorRepository: DoctorRepository

    @Before
    fun setUp() {
        restApi = mockk(relaxed = true)

        mockStaticMethodsNeededToInstantiateBaseRepository()
        doctorRepository = DoctorRepository()
    }

    @Test
    fun `should send message to doctor`() {
        val contactDoctorRequest = ContactDoctorRequest(DOCTOR_PROVIDER_UUID, "message")
        val call = mockk<Call<ResponseBody>>()
        every { restApi.contactDoctor(contactDoctorRequest) } returns call
        every { call.execute() } returns Response.success("Created".toResponseBody("text/plain".toMediaType()))

        runBlocking {
            val result = doctorRepository.sendMessageToDoctor("message")

            verify { restApi.contactDoctor(any()) }
            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun `should return failure when sending message response is error`() {
        val call = mockk<Call<ResponseBody>>()

        every { restApi.contactDoctor(any()) } returns call
        every { call.execute() } returns Response.error(
            400,
            "Bad request".toResponseBody("text/plain".toMediaType())
        )

        runBlocking {
            val result = doctorRepository.sendMessageToDoctor("message")

            assertTrue(result.isFailure)
        }
    }

    @Test
    fun `should return failure when sending message fails`() {
        val call = mockk<Call<ResponseBody>>()

        every { restApi.contactDoctor(any()) } returns call
        every { call.execute() } throws UnknownHostException("No internet connection")

        runBlocking {
            val result = doctorRepository.sendMessageToDoctor("message")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is UnknownHostException)
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