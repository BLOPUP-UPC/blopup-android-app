package edu.upc.openmrs.activities.syncedpatients

import com.openmrs.android_sdk.library.dao.PatientDAO
import com.openmrs.android_sdk.library.dao.VisitDAO
import com.openmrs.android_sdk.library.models.OperationType
import com.openmrs.android_sdk.library.models.Patient
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.utilities.FilterUtil
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class SyncedPatientsViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val visitDAO: VisitDAO
) : edu.upc.openmrs.activities.BaseViewModel<List<Patient>>() {

    fun fetchSyncedPatients() {
        setLoading()
        addSubscription(patientDAO.allPatients
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { patients: List<Patient> -> setContent(patients) },
                { setError(it, OperationType.PatientFetching) }
            ))
    }

    fun fetchSyncedPatients(query: String) {
        setLoading()
        addSubscription(patientDAO.allPatients
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { patients: List<Patient> ->
                    val filteredPatients = FilterUtil.getPatientsFilteredByQuery(patients, query)
                    setContent(filteredPatients)
                },
                { setError(it, OperationType.PatientSearching) }
            ))
    }

    fun deleteSyncedPatient(patient: Patient) {
        setLoading()
        patientDAO.deletePatient(patient.id!!)
        addSubscription(
            visitDAO.deleteVisitPatient(patient)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        )
    }
}
