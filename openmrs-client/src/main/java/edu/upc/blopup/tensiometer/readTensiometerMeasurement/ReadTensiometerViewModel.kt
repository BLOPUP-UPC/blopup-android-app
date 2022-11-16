package edu.upc.blopup.tensiometer.readTensiometerMeasurement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReadTensiometerViewModel @Inject constructor(
    private val readTensiometerRepository: ReadTensiometerRepository
) : ViewModel() {

    private val _connectionViewState = MutableLiveData<ConnectionViewState>()
    val connectionViewState: LiveData<ConnectionViewState>
        get() = _connectionViewState

    private val _viewState = MutableLiveData<TensiometerViewState>()
    val viewState: LiveData<TensiometerViewState>
        get() = _viewState

    fun startListeningBluetoothConnection() {
        readTensiometerRepository.start(
            { state: ConnectionViewState -> _connectionViewState.postValue(state) },
            { state: TensiometerViewState -> _viewState.postValue(state) }
        )
    }

    fun disconnect() = readTensiometerRepository.disconnect()
}