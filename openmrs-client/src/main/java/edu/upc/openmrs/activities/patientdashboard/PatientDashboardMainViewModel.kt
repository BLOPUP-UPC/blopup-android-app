package edu.upc.openmrs.activities.patientdashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.ui.dashboard.ActiveVisitResultUiState
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.api.repository.NewVisitRepository
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.OperationType
import edu.upc.sdk.library.models.OperationType.PatientSynchronizing
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import kotlinx.coroutines.launch
import rx.android.schedulers.AndroidSchedulers
import java.io.IOException
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class PatientDashboardMainViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val visitDAO: VisitDAO,
    private val patientRepository: PatientRepository,
    private val visitRepository: VisitRepository,
    private val newVisitRepository: NewVisitRepository,
    private val openMRSLogger: OpenMRSLogger,
    savedStateHandle: SavedStateHandle
) : edu.upc.openmrs.activities.BaseViewModel<Unit>() {

    val patientId: String = savedStateHandle.get<Long>(PATIENT_ID_BUNDLE)?.toString()!!
    private val patient: Patient = patientDAO.findPatientByID(patientId)
    private val patientUuid: String = patient.uuid!!

    private val _activeVisit = MutableLiveData<ActiveVisitResultUiState>()
    val activeVisit: LiveData<ActiveVisitResultUiState> get() = _activeVisit

    private var runningSyncs = 0

    fun fetchActiveVisit() {
        _activeVisit.value = ActiveVisitResultUiState.Loading
        viewModelScope.launch {
            try {
                val result = newVisitRepository.getActiveVisit(UUID.fromString(patientUuid))
                if (result == null) {
                    _activeVisit.value = ActiveVisitResultUiState.NotFound
                } else {
                    _activeVisit.value = ActiveVisitResultUiState.Success(result)
                }
            } catch (e: IOException) {
                openMRSLogger.e("Error fetching active visit: ${e.message}", e)
                _activeVisit.value = ActiveVisitResultUiState.Error
            }
        }
    }

    suspend fun endActiveVisit(visitUUID: UUID): LiveData<Boolean> {
        val endVisitResult: MutableLiveData<Boolean> = MutableLiveData(false)

        endVisitResult.value = newVisitRepository.endVisit(visitUUID)

        return endVisitResult
    }

    fun syncPatientData() {
        setLoading(PatientSynchronizing)
        syncDetails()
        syncVisits()
        syncVitals()
    }

    private fun syncDetails() {
        runningSyncs++
        addSubscription(patientRepository.downloadPatientByUuid(patientUuid)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { handleSyncPatientDetails(it) },
                { setError(it, PatientSynchronizing) }
            )
        )
    }

    private fun handleSyncPatientDetails(serverPatient: Patient) {
        if (serverPatient != patient) {
            patientDAO.updatePatient(patientId.toLong(), serverPatient)
        }

        setContent(Unit, PatientSynchronizing)
    }

    private fun syncVisits() {
        runningSyncs++
        addSubscription(visitRepository.syncVisitsData(patient)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { setContent(Unit, PatientSynchronizing) },
                { setError(it, PatientSynchronizing) }
            ))
    }

    private fun syncVitals() {
        runningSyncs++
        addSubscription(visitRepository.syncLastVitals(patient.uuid)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { setContent(Unit, PatientSynchronizing) },
                { setError(it, PatientSynchronizing) }
            )
        )
    }

    override fun setContent(data: Unit, operationType: OperationType) {
        if (operationType == PatientSynchronizing) {
            runningSyncs--
            // Check if no syncs are still running
            if (runningSyncs == 0) super.setContent(data, operationType)
        } else {
            super.setContent(data, operationType)
        }
    }

    override fun setError(t: Throwable, operationType: OperationType) {
        OpenMRSLogger().d("GeneralLogKey: setError: ${t.message}")
        if (operationType == PatientSynchronizing) clearSubscriptions()
        super.setError(t, operationType)
    }
}
