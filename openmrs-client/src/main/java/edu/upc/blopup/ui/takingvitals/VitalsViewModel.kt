package edu.upc.blopup.ui.takingvitals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.BloodPressureViewState
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.ConnectionViewState
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.ReadBloodPressureRepository
import edu.upc.blopup.toggles.check
import edu.upc.blopup.toggles.hardcodeBluetoothDataToggle
import edu.upc.blopup.vitalsform.Vital
import edu.upc.sdk.utilities.ApplicationConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
open class VitalsViewModel @Inject constructor(
    private val readBloodPressureRepository: ReadBloodPressureRepository?
) : ViewModel() {

    private val _uiState = MutableStateFlow((mutableListOf<Vital>()))
    val uiState: StateFlow<MutableList<Vital>> = _uiState.asStateFlow()

    private val _connectionViewState = MutableLiveData<ConnectionViewState>()
    val connectionViewState: LiveData<ConnectionViewState>
        get() = _connectionViewState

    private val _viewState = MutableLiveData<BloodPressureViewState>()
    private val viewState: LiveData<BloodPressureViewState>
        get() = _viewState

    fun startListeningBluetoothConnection() {
        hardcodeBluetoothDataToggle.check(onToggleEnabled = { hardcodeBluetoothData() })

        readBloodPressureRepository?.start(
            { state: ConnectionViewState -> _connectionViewState.postValue(state) },
            { state: BloodPressureViewState -> _viewState.postValue(state)
                if (state is BloodPressureViewState.Content) {
                        _uiState.value = mutableListOf(
                            Vital(
                                ApplicationConstants.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT,
                                state.measurement.systolic.toString()
                            ),
                            Vital(
                                ApplicationConstants.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT,
                                state.measurement.diastolic.toString()
                            ),
                            Vital(
                                ApplicationConstants.VitalsConceptType.HEART_RATE_FIELD_CONCEPT,
                                state.measurement.heartRate.toString()
                            )
                        )
                    }
                readBloodPressureRepository.disconnect()
            }
        )
    }


    private fun hardcodeBluetoothData() {
        _uiState.value =
            mutableListOf(
                Vital(
                    ApplicationConstants.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT,
                    (80..250).random().toString()
                ),
                Vital(
                    ApplicationConstants.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT,
                    (50..99).random().toString()
                ),
                Vital(
                    ApplicationConstants.VitalsConceptType.HEART_RATE_FIELD_CONCEPT,
                    (55..120).random().toString()
                )
            )
    }

}
