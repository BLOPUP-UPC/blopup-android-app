package edu.upc.openmrs.activities.syncedpatients

import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.api.repository.PatientRepositoryKotlin
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.OperationType.PatientFetching
import edu.upc.sdk.library.models.Patient
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

    suspend fun fetchSyncedPatients(query: String) {
        setLoading()
        try {
            val patientList = patientRepositoryKotlin.findPatients(query)
            setContent(patientList)
        } catch (e: Exception) {
            setError(e, PatientFetching)
        }
    }
}
