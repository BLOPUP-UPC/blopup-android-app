package edu.upc.sdk.library.api.repository

import edu.upc.blopup.model.Doctor
import edu.upc.sdk.library.CrashlyticsLogger
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.api.repository.DoctorRepository.Companion.DOCTOR_PROVIDER_UUID
import edu.upc.sdk.library.api.repository.DoctorRepository.ContactDoctorRequest
import edu.upc.sdk.library.models.Person
import edu.upc.sdk.library.models.Provider
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import java.net.UnknownHostException

class DoctorRepositoryTest {

    @MockK
    private lateinit var restApi: RestApi

    @MockK(relaxed = true)
    private lateinit var crashlyticsLogger: CrashlyticsLogger

    @InjectMockKs
    private lateinit var doctorRepository: DoctorRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
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
            assertTrue(result is Result.Success)
        }
    }

    @Test
    fun `should return failure when sending message response is error`() {
        val call = mockk<Call<ResponseBody>>()
        val exception = Exception("Bad request")

        every { restApi.contactDoctor(any()) } returns call
        every { call.execute() } returns Response.error(
            400,
            "Bad request".toResponseBody("text/plain".toMediaType())
        )

        runBlocking {
            val result = doctorRepository.sendMessageToDoctor("message") as Result.Error
            result.throwable

            assertEquals("Failed to message doctor: ${exception.message}", result.throwable.message)
        }
    }

    @Test
    fun `should return failure when sending message fails`() {
        val call = mockk<Call<ResponseBody>>()
        val exception = UnknownHostException("No internet connection")

        every { restApi.contactDoctor(any()) } returns call
        every { call.execute() } throws exception

        runBlocking {
            val result = doctorRepository.sendMessageToDoctor("message") as Result.Error

            assertEquals("Failed to message doctor: No internet connection", result.throwable.message)
        }
    }

    @Test
    fun `should return list with all the doctors`() {

        val doctor = Doctor("providerUuid1", "Xavier de las Cuevas")

        val providerList = listOf(
            Provider().apply {
                uuid = "providerUuid1"
                person = Person().apply {
                    display = "Xavier de las Cuevas"
                }
                identifier = TreatmentRepository.DOCTOR
            },
            Provider().apply {
                uuid = "providerUuid2"
                person = Person().apply {
                    display = "Alejandro de las Cuevas"
                }
                identifier = "nurse"
            },
            Provider().apply {
                uuid = "providerUuid2"
                person = Person().apply {
                    display = "Rosa de las Cuevas"
                }
                identifier = "nurse"
            }
        )

        val response = Response.success(Results<Provider>().apply { results = providerList })
        val call = mockk<Call<Results<Provider>>>(relaxed = true)

        coEvery { restApi.providerList } returns call
        coEvery { call.execute() } returns response


        runBlocking {
            val result = doctorRepository.getAllDoctors()
            assertEquals(result, Result.Success(listOf(doctor)))
        }
    }

    @Test
    fun `should return failure when get all doctors fails`() {
        val call = mockk<Call<Results<Provider>>>()
        val exception = UnknownHostException("No internet connection")

        every { restApi.providerList } returns call
        every { call.execute() } throws exception


        runBlocking {
            val result = doctorRepository.getAllDoctors()

            assertEquals(edu.upc.sdk.library.models.Result.Error(exception), result)
        }
    }
}