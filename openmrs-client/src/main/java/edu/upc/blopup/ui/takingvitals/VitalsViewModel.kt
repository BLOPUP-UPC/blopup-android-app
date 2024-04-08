package edu.upc.blopup.ui.takingvitals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.CheckTreatment
import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.Treatment
import edu.upc.blopup.toggles.check
import edu.upc.blopup.toggles.hardcodeBluetoothDataToggle
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.api.repository.BloodPressureViewState
import edu.upc.sdk.library.api.repository.NewVisitRepository
import edu.upc.sdk.library.api.repository.ReadBloodPressureRepository
import edu.upc.sdk.library.api.repository.ReadScaleRepository
import edu.upc.sdk.library.api.repository.ScaleViewState
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

@HiltViewModel
open class VitalsViewModel @Inject constructor(
    private val readBloodPressureRepository: ReadBloodPressureRepository,
    private val readScaleRepository: ReadScaleRepository,
    private val visitRepository: VisitRepository,
    private val newVisitRepository: NewVisitRepository,
    patientDAO: PatientDAO,
    private val treatmentRepository: TreatmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patientId: Long =
        savedStateHandle[ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE]!!

    private val _bloodPressureUiState = MutableStateFlow<BloodPressure?>(null)
    val bloodPressureUiState: StateFlow<BloodPressure?> = _bloodPressureUiState.asStateFlow()

    private val _weightUiState = MutableStateFlow<String?>(null)
    val weightUiState: StateFlow<String?> = _weightUiState.asStateFlow()

    private val _heightUiState = MutableStateFlow<String?>(null)

    val patient: Patient = patientDAO.findPatientByID(patientId.toString())

    private val _createVisitResultUiState: MutableStateFlow<ResultUiState<Unit>?> = MutableStateFlow(null)
    val createVisitResultUiState: StateFlow<ResultUiState<Unit>?> =
        _createVisitResultUiState.asStateFlow()

    private val _treatmentsResultUiState: MutableStateFlow<ResultUiState<List<Treatment>>> =
        MutableStateFlow(ResultUiState.Loading)
    var treatmentsResultUiState: StateFlow<ResultUiState<List<Treatment>>> = _treatmentsResultUiState.asStateFlow()

    private val _bpBluetoothConnectionResultUiState: MutableStateFlow<ResultUiState<Unit>> =
        MutableStateFlow(ResultUiState.Loading)
    var bpBluetoothConnectionResultUiState: StateFlow<ResultUiState<Unit>> =
        _bpBluetoothConnectionResultUiState.asStateFlow()

    private val _scaleBluetoothConnectionResultUiState: MutableStateFlow<ResultUiState<Unit>> =
        MutableStateFlow(ResultUiState.Loading)
    var scaleBluetoothConnectionResultUiState: StateFlow<ResultUiState<Unit>> =
        _scaleBluetoothConnectionResultUiState.asStateFlow()

    suspend fun addTreatmentAdherence(checkTreatmentList: List<CheckTreatment>) {
        val treatmentAdherenceInfo = checkTreatmentList.map {
            return@map Pair<String, Boolean>(it.treatmentId, it.selected)
        }.toMap()
        treatmentRepository.saveTreatmentAdherence(treatmentAdherenceInfo, patient.uuid!!)
    }

    suspend fun fetchActiveTreatment() {
        _treatmentsResultUiState.value = ResultUiState.Loading

        val response = treatmentRepository.fetchAllActiveTreatments(patient)

        _treatmentsResultUiState.value =
            when (response) {
                is Result.Success -> ResultUiState.Success(response.data)
                else -> ResultUiState.Error
            }
    }

    fun receiveWeightData() {
        _scaleBluetoothConnectionResultUiState.value = ResultUiState.Loading

        hardcodeBluetoothDataToggle.check(

            { hardcodeWeightData() },
            {
                viewModelScope.launch {
                    readScaleRepository.start { state: ScaleViewState ->
                        when (state) {
                            is ScaleViewState.Content -> {
                                _weightUiState.value = state.weightMeasurement.weight.toString()
                                _scaleBluetoothConnectionResultUiState.value =
                                    ResultUiState.Success(Unit)
                            }

                            is ScaleViewState.Error -> {
                                _scaleBluetoothConnectionResultUiState.value = ResultUiState.Error
                            }
                        }
                        readScaleRepository.disconnect()
                    }
                }
            }
        )
    }

    fun removeWeightData() {
        _weightUiState.value = null
    }

    fun receiveBloodPressureData() {
        _bpBluetoothConnectionResultUiState.value = ResultUiState.Loading

        hardcodeBluetoothDataToggle.check(
            { hardcodeBloodPressureBluetoothData() },

            {
                viewModelScope.launch {
                    readBloodPressureRepository.start(
                        { },
                        { state: BloodPressureViewState ->
                            when (state) {
                                is BloodPressureViewState.Content -> {
                                    _bloodPressureUiState.value = BloodPressure(
                                        state.measurement.systolic,
                                        state.measurement.diastolic,
                                        state.measurement.heartRate
                                    )
                                    _bpBluetoothConnectionResultUiState.value =
                                        ResultUiState.Success(Unit)
                                }

                                is BloodPressureViewState.Error -> {
                                    _bpBluetoothConnectionResultUiState.value = ResultUiState.Error
                                }
                            }
                            readBloodPressureRepository.disconnect()
                        }
                    )
                }
            }
        )
    }

    fun saveHeight(height: String) {
        _heightUiState.value = height
    }

    fun getLastHeightFromVisits() =
        visitRepository.getLatestVisitWithHeight(patientId).getOrNull()
            ?.getLatestHeight() ?: ""

    fun createVisit() {
        _createVisitResultUiState.value = ResultUiState.Loading

        viewModelScope.launch {
            when (newVisitRepository.startVisit(
                patient,
                _bloodPressureUiState.value!!,
                _heightUiState.value?.toInt(),
                _weightUiState.value?.toFloat(),
            )) {
                is Result.Success -> _createVisitResultUiState.value = ResultUiState.Success(Unit)
                is Result.Error -> _createVisitResultUiState.value = ResultUiState.Error
                is Result.Loading -> _createVisitResultUiState.value = ResultUiState.Loading
            }
        }
    }

    private fun hardcodeBloodPressureBluetoothData() {
        _bloodPressureUiState.value = BloodPressure(
            (80..139).random(),
            (50..89).random(),
            (55..120).random()
        )
        _bpBluetoothConnectionResultUiState.value = ResultUiState.Success(Unit)
    }

    private fun hardcodeWeightData() {
        _weightUiState.value = (50..150).random().toString()
        _scaleBluetoothConnectionResultUiState.value = ResultUiState.Success(Unit)
    }
}
