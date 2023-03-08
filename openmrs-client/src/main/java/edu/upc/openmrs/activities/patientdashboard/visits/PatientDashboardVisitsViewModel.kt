package edu.upc.openmrs.activities.patientdashboard.visits

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.OperationType.PatientVisitStarting
import edu.upc.sdk.library.models.OperationType.PatientVisitsFetching
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import dagger.hilt.android.lifecycle.HiltViewModel
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class PatientDashboardVisitsViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val visitDAO: VisitDAO,
    private val visitRepository: VisitRepository,
    private val savedStateHandle: SavedStateHandle
) : edu.upc.openmrs.activities.BaseViewModel<List<Visit>>() {

    private val patientId: String = savedStateHandle.get(PATIENT_ID_BUNDLE)!!

    fun getPatient(): Patient = patientDAO.findPatientByID(patientId)

    fun fetchVisitsData() {
        setLoading(PatientVisitsFetching)
        addSubscription(visitDAO.getVisitsByPatientID(patientId.toLong())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { visits: List<Visit> -> setContent(visits, PatientVisitsFetching) },
                        { setError(it, PatientVisitsFetching) }
                ))
    }

    fun hasActiveVisit(): LiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        addSubscription(visitDAO.getActiveVisitByPatientId(patientId.toLong())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { visit: Visit? -> liveData.value = visit != null })
        return liveData
    }

    fun startVisit() {
        setLoading(PatientVisitStarting)
        val patient = patientDAO.findPatientByID(patientId)
        addSubscription(visitRepository.startVisit(patient)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { visit: Visit -> setContent(listOf(visit), PatientVisitStarting) },
                        { setError(it, PatientVisitStarting) }
                ))
    }
}
