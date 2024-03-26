package edu.upc.sdk.library.api.repository

import androidx.work.WorkManager
import edu.upc.blopup.model.Doctor
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.api.RestServiceBuilder
import edu.upc.sdk.library.api.repository.DoctorRepository.Companion.DOCTOR_PROVIDER_UUID
import edu.upc.sdk.library.api.repository.DoctorRepository.ContactDoctorRequest
import edu.upc.sdk.library.databases.AppDatabase
import edu.upc.sdk.library.models.Person
import edu.upc.sdk.library.models.Provider
import edu.upc.sdk.library.models.ProviderAttribute
import edu.upc.sdk.library.models.Results
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
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
            assertTrue(result.exceptionOrNull()?.cause is UnknownHostException)
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
            assert(Result.success(listOf(doctor)) == result)
        }
    }

    @Test
    fun `should return failure when get all doctors fails`() {
        val call = mockk<Call<Results<Provider>>>()

        every { restApi.providerList } returns call
        every { call.execute() } throws UnknownHostException("No internet connection")


        runBlocking {
            val result = doctorRepository.getAllDoctors()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.cause is UnknownHostException)
        }
    }

    @Test
    fun `should return doctors registration number`() {

        val providerList = listOf(
            Provider().apply {
                uuid = "providerUuid1"
                person = Person().apply {
                    display = "Xavier de las Cuevas"
                }
                identifier = TreatmentRepository.DOCTOR
                attributes = listOf(
                    ProviderAttribute().apply {
                        display = "Registration Number: 123456"
                    }
                )
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

        coEvery { restApi.getProviderAttributes("providerUuid1") } returns call
        coEvery { call.execute() } returns response


        val result = doctorRepository.getDoctorRegistrationNumber("providerUuid1")
        assert("123456" == result)
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