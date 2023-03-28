package edu.upc.openmrs.activities.matchingpatients

import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.OperationType.PatientMerging
import edu.upc.sdk.library.models.OperationType.PatientRegistering
import edu.upc.sdk.library.models.Patient
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.utilities.PatientMerger
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class MatchingPatientsViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val patientRepository: PatientRepository
) : edu.upc.openmrs.activities.BaseViewModel<Patient>() {

    fun registerNewPatient(patient: Patient) {
        setLoading()
        addSubscription(patientRepository.syncPatient(patient)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { returnedPatient -> setContent(returnedPatient, PatientRegistering) },
                        { setError(it, PatientRegistering) })
        )
    }

    /**
     * Updates a selected patient with current patient data.
     *
     * @param selectedPatient the old patient to be updated
     * @param currentPatient the new patient to be merged to the old patient
     */
    fun mergePatients(selectedPatient: Patient, currentPatient: Patient) {
        setLoading()
        val mergedPatient = PatientMerger().mergePatient(selectedPatient, currentPatient)
        addSubscription(patientRepository.updateMatchingPatient(mergedPatient)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { returnedMergedPatient ->
                            val storedPatient = patientDAO.findPatientByUUID(selectedPatient.uuid)
                            if (storedPatient != null) {
                                // If the selected similar patient (from server) is already saved in local DB
                                patientDAO.updatePatient(storedPatient.id!!, returnedMergedPatient)
                                selectedPatient.id?.let { patientDAO.deletePatient(it) }
                            } else {
                                patientDAO.updatePatient(currentPatient.id!!, returnedMergedPatient)
                            }
                            setContent(returnedMergedPatient, PatientMerging)
                        },
                        { setError(it, PatientMerging) }
                ))
    }
}