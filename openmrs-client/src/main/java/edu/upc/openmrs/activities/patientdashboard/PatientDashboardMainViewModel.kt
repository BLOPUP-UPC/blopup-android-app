package edu.upc.openmrs.activities.patientdashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.ui.dashboard.ActiveVisitResultUiState
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.api.repository.VisitRepository
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PatientDashboardMainViewModel @Inject constructor(
    private val visitRepository: VisitRepository,
    private val openMRSLogger: OpenMRSLogger
) : ViewModel() {
    private val _activeVisit = MutableLiveData<ActiveVisitResultUiState>()
    val activeVisit: LiveData<ActiveVisitResultUiState> get() = _activeVisit

    fun fetchActiveVisit(patientUuid: UUID) {
        _activeVisit.value = ActiveVisitResultUiState.Loading
        viewModelScope.launch {
            try {
                val result = visitRepository.getActiveVisit(patientUuid)
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

        endVisitResult.value = visitRepository.endVisit(visitUUID)

        return endVisitResult
    }
}
