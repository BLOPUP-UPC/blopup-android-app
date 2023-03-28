package edu.upc.openmrs.activities.patientdashboard.vitals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import edu.upc.sdk.library.dao.EncounterDAO
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.OperationType.PatientVitalsFetching
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import dagger.hilt.android.lifecycle.HiltViewModel
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class PatientDashboardVitalsViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val encounterDAO: EncounterDAO,
    private val savedStateHandle: SavedStateHandle
) : edu.upc.openmrs.activities.BaseViewModel<Encounter>() {

    private val patientId: String = savedStateHandle.get(PATIENT_ID_BUNDLE)!!

    fun fetchLastVitals() {
        setLoading(PatientVitalsFetching)
        val patient = patientDAO.findPatientByID(patientId)
        addSubscription(encounterDAO.getLastVitalsEncounter(patient.uuid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { encounter -> setContent(encounter, PatientVitalsFetching) },
                        { setError(it) }
                )
        )
    }

    fun fetchLastVitalsEncounter(): LiveData<Encounter> {
        setLoading()
        val liveData = MutableLiveData<Encounter>()
        val patient = patientDAO.findPatientByID(patientId)
        addSubscription(encounterDAO.getLastVitalsEncounter(patient.uuid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { encounter -> liveData.value = encounter }
        )
        return liveData
    }
}