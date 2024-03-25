package edu.upc.blopup.ui.location

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.sdk.library.api.repository.LocationRepository
import javax.inject.Inject

@HiltViewModel
open class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    fun getLocation() = locationRepository.getCurrentLocation()

}
