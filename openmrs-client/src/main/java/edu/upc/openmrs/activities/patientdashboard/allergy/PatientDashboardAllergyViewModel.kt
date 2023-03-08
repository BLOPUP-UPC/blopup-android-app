package edu.upc.openmrs.activities.patientdashboard.allergy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import edu.upc.sdk.library.api.repository.AllergyRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Allergy
import edu.upc.sdk.library.models.OperationType.PatientAllergyFetching
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import dagger.hilt.android.lifecycle.HiltViewModel
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class PatientDashboardAllergyViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val allergyRepository: AllergyRepository,
    private val savedStateHandle: SavedStateHandle
) : edu.upc.openmrs.activities.BaseViewModel<List<Allergy>>() {

    private val patientId: String = savedStateHandle.get(PATIENT_ID_BUNDLE)!!

    fun getPatient(): Patient = patientDAO.findPatientByID(patientId)

    fun fetchAllergies() {
        setLoading(PatientAllergyFetching)
        addSubscription(allergyRepository.getAllergyFromDatabase(patientId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { allergies: List<Allergy> -> setContent(allergies, PatientAllergyFetching) },
                        { setError(it, PatientAllergyFetching) }
                )
        )
    }

    fun deleteAllergy(allergyUuid: String): LiveData<ResultType> {
        setLoading()
        val liveData = MutableLiveData<ResultType>()
        addSubscription(allergyRepository.deleteAllergy(getPatient().uuid, allergyUuid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { deletionStatus -> liveData.value = deletionStatus },
                        { liveData.value = ResultType.AllergyDeletionError }
                )
        )
        return liveData
    }
}
