package edu.upc.blopup.vitalsform

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.BloodPressureViewState
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.ConnectionViewState
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.ReadBloodPressureRepository
import javax.inject.Inject

@HiltViewModel
class VitalsViewModel @Inject constructor(
    private val readBloodPressureRepository: ReadBloodPressureRepository
) : ViewModel() {

    //TODO: understand what the connectionViewState is and when and how it's used
    private val _connectionViewState = MutableLiveData<ConnectionViewState>()
    val connectionViewState: LiveData<ConnectionViewState>
        get() = _connectionViewState

    var vitals = MutableLiveData<MutableList<Vital>>()

    private val _viewState = MutableLiveData<BloodPressureViewState>()
    val viewState: LiveData<BloodPressureViewState>
        get() = _viewState

    fun startListeningBluetoothConnection() {
        readBloodPressureRepository.start(
            { state: ConnectionViewState -> _connectionViewState.postValue(state) },
            { state: BloodPressureViewState -> _viewState.postValue(state) }
        )
    }

    fun disconnect() = readBloodPressureRepository.disconnect()
}
