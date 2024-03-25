package edu.upc.blopup.ui.location

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.api.repository.LocationRepository
import edu.upc.sdk.library.databases.entities.LocationEntity
import edu.upc.sdk.library.models.Result
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
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
}