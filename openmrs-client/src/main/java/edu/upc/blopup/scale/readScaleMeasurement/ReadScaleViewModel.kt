package edu.upc.blopup.scale.readScaleMeasurement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReadScaleViewModel @Inject constructor(
    private val readScaleRepository: ReadScaleRepository
) : ViewModel() {

    private val _connectionViewState = MutableLiveData<ConnectionViewState>()
    val connectionViewState: LiveData<ConnectionViewState>
        get() = _connectionViewState

    private val _viewState = MutableLiveData<ScaleViewState>()
    val viewState: LiveData<ScaleViewState>
        get() = _viewState

    fun startListeningBluetoothConnection() {
        readScaleRepository.start(
            { state: ConnectionViewState -> _connectionViewState.postValue(state) },
            { state: ScaleViewState -> _viewState.postValue(state) }
        )
    }

    fun disconnect() = readScaleRepository.disconnect()
}