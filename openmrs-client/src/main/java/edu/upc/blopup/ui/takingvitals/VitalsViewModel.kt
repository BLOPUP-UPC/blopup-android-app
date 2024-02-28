package edu.upc.blopup.ui.takingvitals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.CheckTreatment
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.BloodPressureViewState
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.ConnectionViewState
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.ReadBloodPressureRepository
import edu.upc.blopup.scale.readScaleMeasurement.ReadScaleRepository
import edu.upc.blopup.scale.readScaleMeasurement.ScaleViewState
import edu.upc.blopup.toggles.check
import edu.upc.blopup.toggles.hardcodeBluetoothDataToggle
import edu.upc.blopup.vitalsform.Vital
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
    patientDAO: PatientDAO,
    private val treatmentRepository: TreatmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patientId: Long =
        savedStateHandle[ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE]!!

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

    val patient: Patient = patientDAO.findPatientByID(patientId.toString())

    private val _createVisitResultUiState: MutableStateFlow<ResultUiState> =
        MutableStateFlow(ResultUiState.Loading)
    var createVisitResultUiState: StateFlow<ResultUiState> =
        _createVisitResultUiState.asStateFlow()

    private val _treatmentsResultUiState: MutableStateFlow<ResultUiState> = MutableStateFlow(ResultUiState.Loading)
    var treatmentsResultUiState: StateFlow<ResultUiState> = _treatmentsResultUiState.asStateFlow()

    suspend fun addTreatmentAdherence(checkTreatmentList: List<CheckTreatment>) {
        val treatmentAdherenceInfo = checkTreatmentList.map {
            return@map Pair<String, Boolean>(it.treatmentId, it.selected)
        }.toMap()
        treatmentRepository.saveTreatmentAdherence(treatmentAdherenceInfo, patient.uuid!!)
    }

    suspend fun fetchActiveTreatment() {
        val response = treatmentRepository.fetchAllActiveTreatments(patient)

        _treatmentsResultUiState.value =
            when (response) {
                is Result.Success -> ResultUiState.Success(response.data)
                else -> ResultUiState.Error
            }
    }

    fun receiveWeightData() {
        hardcodeBluetoothDataToggle.check(

            { hardcodeWeightData() },
            {
                viewModelScope.launch {
                    readScaleRepository.start { state: ScaleViewState ->
                        _scaleViewState.postValue(state)
                        if (state is ScaleViewState.Content) {
                            val copy = _vitalsUiState.value.toMutableList()

                            copy.add(
                                Vital(
                                    ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT,
                                    state.weightMeasurement.weight.toString()
                                )
                            )
                            _vitalsUiState.value = copy
                        }
                        readScaleRepository.disconnect()
                    }
                }
            }
        )
    }

    fun disconnect() = readScaleRepository.disconnect()

    fun removeWeightData() {
        _vitalsUiState.value.removeIf { it.concept == ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT }
    }

    fun receiveBloodPressureData() {
        hardcodeBluetoothDataToggle.check(
            { hardcodeBloodPressureBluetoothData() },

            {
                viewModelScope.launch {
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
            }
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

    fun getLastHeightFromVisits() =
        visitRepository.getLatestVisitWithHeight(patientId).getOrNull()?.getLatestHeight() ?: ""

    fun createVisit() {
        _createVisitResultUiState.value = ResultUiState.Loading
        visitRepository.createVisitWithVitals(patient, _vitalsUiState.value).subscribe { result ->
            when (result) {
                is Result.Success -> {
                    _createVisitResultUiState.value = ResultUiState.Success(Unit)
                }

                is Result.Loading -> {
                    _createVisitResultUiState.value = ResultUiState.Loading
                }

                is Result.Error -> {
                    _createVisitResultUiState.value = ResultUiState.Error
                }
            }
        }
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
}
