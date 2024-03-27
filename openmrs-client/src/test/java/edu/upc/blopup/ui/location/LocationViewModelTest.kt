package edu.upc.blopup.ui.location

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.api.repository.LocationRepository
import edu.upc.sdk.library.databases.entities.LocationEntity
import edu.upc.sdk.library.models.Result
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LocationViewModelTest{

    @InjectMockKs
    private lateinit var locationViewModel: LocationViewModel

    @MockK
    private lateinit var locationRepository: LocationRepository

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should get locations from repository`() {

        val locationList = listOf(LocationEntity(display = "Hospital"), LocationEntity(display = "Nursery"))

        coEvery { locationRepository.getAllLocations() } returns Result.Success(
            locationList
        )

        runBlocking {
            locationViewModel.getAllLocations()

            assertEquals(ResultUiState.Success(locationList), locationViewModel.locationsListResultUiState.value)
        }
    }

    @Test
    fun `should return error if fetching locations fails`() {

        coEvery { locationRepository.getAllLocations() } returns Result.Error(
            Throwable("Error fetching locations")
        )

        runBlocking {
            locationViewModel.getAllLocations()

            assertEquals(ResultUiState.Error, locationViewModel.locationsListResultUiState.value)
        }
    }

    @Test
    fun `should get the current location`() {

        val location = "Hospital"

        every { locationRepository.getCurrentLocation() } returns Result.Success(location)

        assertEquals(ResultUiState.Success(location), locationViewModel.getLocation())
    }


    @Test
    fun `should return error if fetching current location fails`() {

        every { locationRepository.getCurrentLocation() } returns Result.Error(Exception("Error fetching current location"))

        val result = locationViewModel.getLocation()

        assertEquals(ResultUiState.Error, result)
    }

    @Test
    fun `should update the new location`() {

        val location = "Hospital"

        coEvery { locationRepository.setLocation(location) } returns Result.Success(true)

        locationViewModel.setLocation(location)

        verify { locationRepository.setLocation(location) }
    }

    @Test
    fun `should return error if saving the new location fails`() {
        val location = "Hospital"

        every {  locationRepository.setLocation(location)} returns Result.Error(Exception("Error saving current location"))

        val result = locationViewModel.setLocation(location)

        assertEquals(ResultUiState.Error, result)
    }
}

