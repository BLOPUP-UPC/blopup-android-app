package edu.upc.openmrs.activities.patientdashboard.visits

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.model.Visit
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.NewVisitRepository
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PatientDashboardVisitsViewModel @Inject constructor(
    private val visitRepository: NewVisitRepository
) : BaseViewModel<List<Visit>>() {

    fun fetchVisitsData(patientId: UUID) {
        setLoading()
        viewModelScope.launch {
            try {
                val visits = visitRepository.getVisitsByPatientUuid(patientId)
                setContent(visits)
            } catch (exception: IOException) {
                setError(exception)
            }
        }
    }
}
