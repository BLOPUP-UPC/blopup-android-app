package edu.upc.sdk.library.api.repository

import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.databases.entities.LocationEntity
import edu.upc.sdk.library.models.Results
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import edu.upc.sdk.library.models.Result as OpenMRSResult

class LocationRepositoryTest{

    @InjectMockKs
    private lateinit var locationRepository: LocationRepository

    @MockK
    private lateinit var restApi: RestApi

    @Before
    fun setUp() {
        mockStaticMethodsNeededToInstantiateBaseRepository()
        MockKAnnotations.init(this)
    }


    @Test
    fun `should get all locations`() {

        val hospitalLocation = LocationEntity(display = "Hospital")
        val nurseryLocation = LocationEntity(display = "Nursery")

        val locationsList = listOf(hospitalLocation, nurseryLocation)

        val response = Response.success(Results<LocationEntity>().apply { results = locationsList })

        val call = mockk<Call<Results<LocationEntity>>>(relaxed = true)
        coEvery { restApi.getLocations(null) } returns call
        coEvery { call.execute() } returns response

        runBlocking {
            val result = locationRepository.getAllLocations()
           if(result is OpenMRSResult.Success){
               assert(result.data == locationsList)
           }
        }
    }

    @Test
    fun `should return failure when get all locations fails`() {
        val errorMessage = "Error fetching locations"
        val call = mockk<Call<Results<LocationEntity>>>(relaxed = true)

        coEvery { restApi.getLocations(null) } returns call
        coEvery { call.execute() } throws Exception(errorMessage)

        runBlocking {
            runCatching {
                val result = locationRepository.getAllLocations()

                assert(result is OpenMRSResult.Error)
                assert((result as OpenMRSResult.Error).throwable.message == errorMessage)
            }
        }
    }

    @Test
    fun `should return success if returns the current location`() {

        val location = "Hospital"

        every { OpenmrsAndroid.getLocation() } returns location

        val result = locationRepository.getCurrentLocation()

        assert(result is OpenMRSResult.Success)
        assert((result as OpenMRSResult.Success).data == location)
    }

    @Test
    fun `should return error if it does not return the current location`() {

        every { OpenmrsAndroid.getLocation() } throws Exception("Error fetching location")

        val result = locationRepository.getCurrentLocation()

        assert(result is OpenMRSResult.Error)
    }

    @Test
    fun `should return success if the new location is saved`() {

        val location = "Hospital"

        every { OpenmrsAndroid.setLocation(location) } returns Unit

        val result = locationRepository.setLocation(location)

        assert(result is OpenMRSResult.Success)
    }

    @Test
    fun `should return error if it does not save the new location`() {
        val location = "Hospital"

        every { OpenmrsAndroid.setLocation(location) } throws Exception("Error saving location")

        val result = locationRepository.setLocation(location)

        assert(result is OpenMRSResult.Error)
    }


    private fun mockStaticMethodsNeededToInstantiateBaseRepository() {
        mockkStatic(OpenmrsAndroid::class)
        every { OpenmrsAndroid.getServerUrl() } returns "http://localhost:8080/openmrs"
    }
}