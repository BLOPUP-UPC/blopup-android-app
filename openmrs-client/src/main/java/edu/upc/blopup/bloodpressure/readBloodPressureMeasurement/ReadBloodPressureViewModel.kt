package edu.upc.blopup.bloodpressure.readBloodPressureMeasurement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReadBloodPressureViewModel @Inject constructor(
    private val readBloodPressureRepository: ReadBloodPressureRepository
) : ViewModel() {

    private val _connectionViewState = MutableLiveData<ConnectionViewState>()
    val connectionViewState: LiveData<ConnectionViewState>
        get() = _connectionViewState

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