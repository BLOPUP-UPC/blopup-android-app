package edu.upc.openmrs.activities.visitdashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.model.Treatment
import edu.upc.blopup.model.Visit
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.api.repository.DoctorRepository
import edu.upc.sdk.library.api.repository.EncounterRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.ResultType
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.joda.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VisitDashboardViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val visitRepository: VisitRepository,
    private val treatmentRepository: TreatmentRepository,
    private val encounterRepository: EncounterRepository,
    private val doctorRepository: DoctorRepository,
) : ViewModel() {
    private val _visit = MutableLiveData<ResultUiState<Visit>>()
    val visit: LiveData<ResultUiState<Visit>> get() = _visit

    private val _patient = MutableLiveData<Patient>()
    val patient: LiveData<Patient> get() = _patient

    private val _treatments = MutableLiveData<ResultUiState<List<Treatment>>>()

    val treatments = visit.asFlow().combine(_treatments.asFlow()){ visit, treatments ->
        Pair(visit, treatments)
    }

    private val _treatmentOperationsLiveData = MutableLiveData<ResultType>()
    val treatmentOperationsLiveData: LiveData<ResultType> get() = _treatmentOperationsLiveData

    private val _doctorHasBeenContacted: MutableLiveData<Boolean> = MutableLiveData(false)
    val doctorHasBeenContacted: LiveData<Boolean> get() = _doctorHasBeenContacted

    fun doctorHasBeenContacted(value: Boolean) {
        _doctorHasBeenContacted.value = value
    }

    fun fetchCurrentVisit(visitId: UUID) {
        viewModelScope.launch {
            _visit.value = ResultUiState.Loading
            try {
                val visit = visitRepository.getVisitByUuid(visitId)
                val patient = patientDAO.findPatientByUUID(visit.patientId.toString())
                _patient.value = patientDAO.findPatientByUUID(visit.patientId.toString())

                fetchActiveTreatments(patient, visit)
                _visit.value = ResultUiState.Success(visit)
            } catch (exception: Exception) {
                Log.i("VisitDashboardViewModel", "Error fetching visit", exception)
                _visit.value = ResultUiState.Error
                _treatments.value = ResultUiState.Error
            }
        }
    }

    private suspend fun fetchActiveTreatments(patient: Patient, visit: Visit) {
        when (val result = treatmentRepository.fetchActiveTreatmentsAtAGivenTime(patient, null, visit)) {
            is Result.Success -> {
                _treatments.value = ResultUiState.Success(result.data)
            }
            is Result.Error -> _treatments.value = ResultUiState.Error
            is Result.Loading -> _treatments.value = ResultUiState.Loading
        }
    }

    suspend fun endCurrentVisit(visitId: UUID): LiveData<Result<Boolean>> {
        val endVisitResult = MutableLiveData<Result<Boolean>>()

        try {
            endVisitResult.value = Result.Success(visitRepository.endVisit(visitId))
        } catch (e: Exception) {
            endVisitResult.value = Result.Error(e)
        }

        return endVisitResult
    }

    suspend fun finaliseTreatment(treatment: Treatment) {
        treatment.inactiveDate = Instant.now()
        treatment.isActive = false

        val response = treatmentRepository.finalise(treatment)

        if (response.isSuccess) {
            _treatmentOperationsLiveData.value = ResultType.FinalisedTreatmentSuccess
            refreshTreatments()
        } else {
            _treatmentOperationsLiveData.value = ResultType.FinalisedTreatmentError
        }
    }

    suspend fun removeTreatment(treatment: Treatment) {
        val response = encounterRepository.removeEncounter(treatment.treatmentUuid)

        if (response.isSuccess && _treatments.value is ResultUiState.Success) {
            _treatments.value =
                (_treatments.value as ResultUiState.Success<List<Treatment>>).data.toMutableList().apply {
                    remove(treatment)
                }.let { ResultUiState.Success(it) }
            _treatmentOperationsLiveData.value = ResultType.RemoveTreatmentSuccess
        } else {
            _treatmentOperationsLiveData.value = ResultType.RemoveTreatmentError
        }
    }

    suspend fun refreshTreatments() {
        visit.let {
            if (it.value is ResultUiState.Success) {
                val visit = (it.value as ResultUiState.Success<Visit>).data
                val patient = patientDAO.findPatientByUUID(visit.patientId.toString())
                fetchActiveTreatments(patient, visit)
            }
        }
    }

    suspend fun sendMessageToDoctor(message: String): kotlin.Result<Boolean> =
        doctorRepository.sendMessageToDoctor(message)
}