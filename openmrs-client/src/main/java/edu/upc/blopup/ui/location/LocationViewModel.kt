package edu.upc.blopup.ui.location

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.api.repository.LocationRepository
import edu.upc.sdk.library.databases.entities.LocationEntity
import edu.upc.sdk.library.models.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
open class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _locationsListResultUiState: MutableStateFlow<ResultUiState<List<LocationEntity>>> =
        MutableStateFlow(ResultUiState.Loading)
    var locationsListResultUiState: StateFlow<ResultUiState<List<LocationEntity>>> = _locationsListResultUiState.asStateFlow()

    fun getLocation() : ResultUiState<String> {
        return try {
            when(val response = locationRepository.getCurrentLocation()) {
                is Result.Success -> ResultUiState.Success(response.data)
                else -> ResultUiState.Error
            }
        } catch (e: Exception) {
            ResultUiState.Error
        }
    }

    fun setLocation(location: String) : ResultUiState<Boolean> {
        return try {
            when(val response = locationRepository.setLocation(location)) {
                is Result.Success -> ResultUiState.Success(response.data)
                else -> ResultUiState.Error
            }
        } catch (e: Exception) {
            ResultUiState.Error
        }
    }

    suspend fun getAllLocations() {
        val response = locationRepository.getAllLocations()

        _locationsListResultUiState.value =
            when(response) {
                is Result.Success -> ResultUiState.Success(response.data)
                else -> ResultUiState.Error
            }
    }
}
