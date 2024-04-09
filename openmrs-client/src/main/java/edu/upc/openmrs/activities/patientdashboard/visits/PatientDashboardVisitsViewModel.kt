package edu.upc.openmrs.activities.patientdashboard.visits

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.model.Visit
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.OperationType.PatientVisitsFetching
import edu.upc.sdk.library.models.typeConverters.VisitConverter
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class PatientDashboardVisitsViewModel @Inject constructor(
    private val visitDAO: VisitDAO,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<List<Visit>>() {

    private val patientId: String = savedStateHandle[PATIENT_ID_BUNDLE]!!

    fun fetchVisitsData() {
        setLoading(PatientVisitsFetching)
        addSubscription(visitDAO.getVisitsByPatientID(patientId.toLong())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { openMRSVisits ->
                    setContent(openMRSVisits.map {
                        VisitConverter.createVisitFromOpenMRSVisit(it)
                    }, PatientVisitsFetching)
                },
                { setError(it, PatientVisitsFetching) }
            ))
    }
}
