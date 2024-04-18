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
import edu.upc.blopup.ui.takingvitals.components.LatestHeightResultUiState
import edu.upc.blopup.ui.takingvitals.screens.CreateVisitResultUiState
import edu.upc.sdk.library.api.repository.BloodPressureViewState
import edu.upc.sdk.library.api.repository.ReadBloodPressureRepository
import edu.upc.sdk.library.api.repository.ReadScaleRepository
import edu.upc.sdk.library.api.repository.ScaleViewState
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
open class VitalsViewModel @Inject constructor(
    private val readBloodPressureRepository: ReadBloodPressureRepository,
    private val readScaleRepository: ReadScaleRepository,
    private val visitRepository: VisitRepository,
    patientDAO: PatientDAO,
    private val treatmentRepository: TreatmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patientId: Long =
        savedStateHandle[ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE]!!
    private val patientUuid: String =
        savedStateHandle[ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE]!!

    private val _bloodPressureUiState = MutableStateFlow<ResultUiState<BloodPressure>>(ResultUiState.Loading)
    val bloodPressureUiState: StateFlow<ResultUiState<BloodPressure>> = _bloodPressureUiState.asStateFlow()

    private val _weightUiState = MutableStateFlow<ResultUiState<String>>(ResultUiState.Loading)
    val weightUiState: StateFlow<ResultUiState<String>> = _weightUiState.asStateFlow()

    private val _latestHeightUiState = MutableStateFlow<LatestHeightResultUiState>(LatestHeightResultUiState.Loading)
    val latestHeightUiState: StateFlow<LatestHeightResultUiState> = _latestHeightUiState.asStateFlow()

    private val _heightUiState = MutableStateFlow<String?>(null)

    val patient: Patient = patientDAO.findPatientByID(patientId.toString())

    private val _createVisitResultUiState: MutableStateFlow<CreateVisitResultUiState> = MutableStateFlow(CreateVisitResultUiState.NotStarted)
    val createVisitResultUiState: StateFlow<CreateVisitResultUiState> =
        _createVisitResultUiState.asStateFlow()

    private val _treatmentsResultUiState: MutableStateFlow<ResultUiState<List<Treatment>>> =
        MutableStateFlow(ResultUiState.Loading)
    var treatmentsResultUiState: StateFlow<ResultUiState<List<Treatment>>> = _treatmentsResultUiState.asStateFlow()

    fun isScaleAvailable() = readScaleRepository.isBluetoothAvailable()

    suspend fun addTreatmentAdherence(checkTreatmentList: List<CheckTreatment>) {
        val treatmentAdherenceInfo = checkTreatmentList.map {
            return@map Pair<String, Boolean>(it.treatmentId, it.selected)
        }.toMap()
        treatmentRepository.saveTreatmentAdherence(treatmentAdherenceInfo, patient.uuid!!)
    }

    suspend fun fetchActiveTreatment() {
        _treatmentsResultUiState.value = ResultUiState.Loading

        val response = treatmentRepository.fetchAllActiveTreatments(UUID.fromString(patientUuid))

        _treatmentsResultUiState.value =
            when (response) {
                is Result.Success -> ResultUiState.Success(response.data)
                else -> ResultUiState.Error
            }
    }

    fun receiveWeightData() {
        _weightUiState.value = ResultUiState.Loading

        hardcodeBluetoothDataToggle.check(

            { hardcodeWeightData() },
            {
                viewModelScope.launch {
                    readScaleRepository.start { state: ScaleViewState ->
                        when (state) {
                            is ScaleViewState.Content -> {
                                _weightUiState.value = ResultUiState.Success(state.weightMeasurement.weight.toString())
                            }

                            is ScaleViewState.Error -> {
                                _weightUiState.value = ResultUiState.Error
                            }
                        }
                        readScaleRepository.disconnect()
                    }
                }
            }
        )
    }

    fun receiveBloodPressureData() {
        _bloodPressureUiState.value = ResultUiState.Loading

        hardcodeBluetoothDataToggle.check(
            { hardcodeBloodPressureBluetoothData() },

            {
                viewModelScope.launch {
                    readBloodPressureRepository.start(
                        { },
                        { state: BloodPressureViewState ->
                            when (state) {
                                is BloodPressureViewState.Content -> {
                                    _bloodPressureUiState.value = ResultUiState.Success(BloodPressure(
                                        state.measurement.systolic,
                                        state.measurement.diastolic,
                                        state.measurement.heartRate
                                    ))
                                }

                                is BloodPressureViewState.Error -> {
                                    _bloodPressureUiState.value = ResultUiState.Error
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

    fun saveWeight(weight: String) {
        _weightUiState.value = ResultUiState.Success(weight)
    }

    fun getLastHeightFromVisits() = viewModelScope.launch {
        visitRepository.getLatestVisitWithHeight(UUID.fromString(patientUuid)).let {
            if (it == null) {
                _latestHeightUiState.value = LatestHeightResultUiState.NotFound
                return@let
            } else {
                _latestHeightUiState.value = LatestHeightResultUiState.Success(it.heightCm!!)
            }
        }
    }

    fun createVisit() {
        _createVisitResultUiState.value = CreateVisitResultUiState.Loading

        val bloodPressure = _bloodPressureUiState.value.let {
            when (it) {
                is ResultUiState.Success -> it.data
                else -> throw IllegalStateException("Blood pressure data not available")
            }
        }

        val weight = _weightUiState.value.let {
            when (it) {
                is ResultUiState.Success -> it.data.toFloat()
                else -> null
            }
        }

        viewModelScope.launch {
            when (val result = visitRepository.startVisit(
                patient,
                bloodPressure,
                _heightUiState.value?.toInt(),
                weight,
            )) {
                is Result.Success -> _createVisitResultUiState.value = CreateVisitResultUiState.Success(result.data)
                is Result.Error -> _createVisitResultUiState.value = CreateVisitResultUiState.Error
                is Result.Loading -> _createVisitResultUiState.value = CreateVisitResultUiState.Loading
            }
        }
    }

    private fun hardcodeBloodPressureBluetoothData() {
        viewModelScope.launch {
            delay(1000)
            _bloodPressureUiState.value = ResultUiState.Success(BloodPressure(
                (80..139).random(),
                (50..89).random(),
                (55..120).random()
            ))
        }
    }

    private fun hardcodeWeightData() {
        viewModelScope.launch {
            delay(1000)
            _weightUiState.value = ResultUiState.Success((50..150).random().toString())
        }
    }
}
