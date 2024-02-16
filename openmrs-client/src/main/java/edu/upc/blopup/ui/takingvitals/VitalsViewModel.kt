package edu.upc.blopup.ui.takingvitals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.BloodPressureViewState
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.ConnectionViewState
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.ReadBloodPressureRepository
import edu.upc.blopup.scale.readScaleMeasurement.ReadScaleRepository
import edu.upc.blopup.scale.readScaleMeasurement.ScaleViewState
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
    private val readBloodPressureRepository: ReadBloodPressureRepository,
    private val readScaleRepository: ReadScaleRepository
) : ViewModel() {

    private val _vitalsUiState = MutableStateFlow((mutableListOf<Vital>()))
    val vitalsUiState: StateFlow<MutableList<Vital>> = _vitalsUiState.asStateFlow()

    private val _connectionViewState = MutableLiveData<ConnectionViewState>()
    val connectionViewState: LiveData<ConnectionViewState>
        get() = _connectionViewState

    private val _bpViewState = MutableLiveData<BloodPressureViewState>()
    private val bpViewState: LiveData<BloodPressureViewState>
        get() = _bpViewState

    private val _scaleViewState = MutableLiveData<ScaleViewState>()
    val scaleViewState: LiveData<ScaleViewState>
        get() = _scaleViewState

    fun receiveWeightData() {
        hardcodeBluetoothDataToggle.check(onToggleEnabled = { hardcodeWeightData() })

        readScaleRepository.start { state: ScaleViewState ->
            _scaleViewState.postValue(state)
            if (state is ScaleViewState.Content) {
                _vitalsUiState.value.removeIf { it.concept == ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT }
                _vitalsUiState.value.add(
                    Vital(
                        ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT,
                        state.weightMeasurement.weight.toString()
                    )
                )
            }
        }
    }

    fun disconnect() = readScaleRepository.disconnect()

    fun receiveBloodPressureData() {
        hardcodeBluetoothDataToggle.check(onToggleEnabled = { hardcodeBloodPressureBluetoothData() })

        readBloodPressureRepository.start(
            { state: ConnectionViewState -> _connectionViewState.postValue(state) },
            { state: BloodPressureViewState ->
                _bpViewState.postValue(state)
                if (state is BloodPressureViewState.Content) {
                    _vitalsUiState.value =
                        mutableListOf(
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


    private fun hardcodeBloodPressureBluetoothData() {
        _vitalsUiState.value =
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

    private fun hardcodeWeightData() {
        _vitalsUiState.value.removeIf { it.concept == ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT }
        _vitalsUiState.value.add(
            Vital(
                ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT,
                (50..150).random().toString()
            )
        )
    }

    fun saveHeight(height: String) {
        _vitalsUiState.value.removeIf { it.concept == ApplicationConstants.VitalsConceptType.HEIGHT_FIELD_CONCEPT }
        _vitalsUiState.value.add(
            Vital(
                ApplicationConstants.VitalsConceptType.HEIGHT_FIELD_CONCEPT,
                height
            )
        )
    }
}
