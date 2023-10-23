package edu.upc.openmrs.activities.syncedpatients

import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.utilities.FilterUtil
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.api.repository.PatientRepositoryKotlin
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.OperationType
import edu.upc.sdk.library.models.OperationType.*
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Results
import edu.upc.sdk.utilities.NetworkUtils.isOnline
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class SyncedPatientsViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val patientRepository: PatientRepository,
    private val patientRepositoryKotlin: PatientRepositoryKotlin
) : edu.upc.openmrs.activities.BaseViewModel<List<Patient>>() {

    fun fetchSyncedPatients() {
        setLoading()
        addSubscription(patientDAO.allPatients
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { patients: List<Patient> -> setContent(patients) },
                { setError(it, PatientFetching) }
            ))
    }

    fun fetchSyncedPatients(query: String) {
        setLoading()
        try {
            val patientList = patientRepositoryKotlin.findPatients(query)
            setContent(patientList)
        } catch (e: Exception) {
            setError(e, PatientFetching)
        }
    }
}
