package edu.upc.openmrs.activities.syncedpatients

import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.utilities.FilterUtil
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.OperationType
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.utilities.NetworkUtils.isOnline
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class SyncedPatientsViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val visitDAO: VisitDAO,
    private val patientRepository: PatientRepository
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
        if (isOnline()) {
            addSubscription(patientRepository.findPatients(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { patientList -> setContent(patientList) },
                    { setError(it, OperationType.PatientSearching) }
                )
            )
        } else {
            addSubscription(patientDAO.allPatients
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { patients: List<Patient> ->
                        val filteredPatients =
                            FilterUtil.getPatientsFilteredByQuery(patients, query)
                        setContent(filteredPatients)
                    },
                    { setError(it, OperationType.PatientSearching) }
                ))
        }
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
