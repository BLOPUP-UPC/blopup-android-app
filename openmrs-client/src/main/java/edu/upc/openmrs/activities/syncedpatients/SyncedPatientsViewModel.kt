package edu.upc.openmrs.activities.syncedpatients

import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.api.repository.PatientRepositoryCoroutines
import edu.upc.sdk.library.api.repository.VisitRepository
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

    fun retrieveOrDownloadPatient(patientUuid: String?): Patient? {
        val patientDAO = PatientDAO()
        val patientRepository = PatientRepository()

        val patient = patientRepository.downloadPatientByUuid(patientUuid!!)
            .single()
            .toBlocking()
            .first()

        return if (patient.names.isEmpty()) {
            null
        } else {
            val patientLocal = patientDAO.findPatientByUUID(patientUuid)
            if (patientLocal != null) {
                return patientLocal
            } else {
                val id = patientDAO.savePatient(patient)
                    .single()
                    .toBlocking()
                    .first()
                patient.id = id
                VisitRepository().syncVisitsData(patient)
                VisitRepository().syncLastVitals(patientUuid)
                patient
            }
        }
    }

    fun deletePatient(patientId: Long) {
        patientDAO.deletePatient(patientId)
    }
}
