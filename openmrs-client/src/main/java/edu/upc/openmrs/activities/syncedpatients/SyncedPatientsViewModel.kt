package edu.upc.openmrs.activities.syncedpatients

import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.sdk.library.api.repository.PatientRepositoryCoroutines
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.OperationType.PatientFetching
import edu.upc.sdk.library.models.Patient
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class SyncedPatientsViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val patientRepositoryCoroutines: PatientRepositoryCoroutines
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
        patientRepositoryCoroutines.findPatients(query)
            .fold(
                { error -> setError(error, PatientFetching) },
                { patients -> setContent(patients) }
            )
    }

    suspend fun retrieveOrDownloadPatient(patientUuid: String?): Patient? {
        val patient = patientRepositoryCoroutines.downloadPatientByUuid(patientUuid!!)

        return if (patient.names.isEmpty()) {
            null
        } else {
            var localDBPatient = patientRepositoryCoroutines.findPatientByUUID(patientUuid)
            if (localDBPatient == null) {
                localDBPatient = patientRepositoryCoroutines.savePatientLocally(patient)
            }
            localDBPatient
        }
    }

    fun deletePatientLocally(patientId: Long) {
        patientRepositoryCoroutines.deletePatient(patientId)
    }
}
