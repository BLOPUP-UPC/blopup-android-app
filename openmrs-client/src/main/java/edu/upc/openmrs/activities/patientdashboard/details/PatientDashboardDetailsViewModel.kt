package edu.upc.openmrs.activities.patientdashboard.details

import androidx.lifecycle.SavedStateHandle
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.OperationType.PatientFetching
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PatientDashboardDetailsViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val savedStateHandle: SavedStateHandle
) : edu.upc.openmrs.activities.BaseViewModel<Patient>() {

    private val patientId: String = savedStateHandle.get(PATIENT_ID_BUNDLE)!!

    fun fetchPatientData() {
        setLoading(PatientFetching)
        val patient = patientDAO.findPatientByID(patientId)
        if (patient != null) setContent(patient, PatientFetching)
        else setError(IllegalStateException("Error fetching patient"), PatientFetching)
    }
}
