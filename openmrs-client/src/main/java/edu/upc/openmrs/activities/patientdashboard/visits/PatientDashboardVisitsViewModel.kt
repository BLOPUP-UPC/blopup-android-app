package edu.upc.openmrs.activities.patientdashboard.visits

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.model.Visit
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.NewVisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.OperationType.PatientVisitStarting
import edu.upc.sdk.library.models.OperationType.PatientVisitsFetching
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.typeConverters.VisitConverter
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class PatientDashboardVisitsViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val visitDAO: VisitDAO,
    private val newVisitRepository: NewVisitRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<List<Visit>>() {

    private val patientId: String = savedStateHandle[PATIENT_ID_BUNDLE]!!

    fun getPatient(): Patient = patientDAO.findPatientByID(patientId)

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

    fun hasActiveVisit(): LiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        addSubscription(visitDAO.getActiveVisitByPatientId(patientId.toLong())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { visit -> liveData.value = visit != null })
        return liveData
    }

    suspend fun startVisit() {
        setLoading(PatientVisitStarting)
        val patient = patientDAO.findPatientByID(patientId)
        try {
            setContent(listOf(newVisitRepository.startVisit(patient)))
        } catch (e: Exception) {
            setError(e, PatientVisitStarting)
        }
    }
}
