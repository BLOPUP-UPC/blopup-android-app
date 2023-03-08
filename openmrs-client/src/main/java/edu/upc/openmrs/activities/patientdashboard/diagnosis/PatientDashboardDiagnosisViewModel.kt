package edu.upc.openmrs.activities.patientdashboard.diagnosis

import androidx.lifecycle.SavedStateHandle
import edu.upc.sdk.library.dao.EncounterDAO
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.library.models.EncounterType.Companion.VISIT_NOTE
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import dagger.hilt.android.lifecycle.HiltViewModel
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class PatientDashboardDiagnosisViewModel @Inject constructor(
    private val encounterDAO: EncounterDAO,
    private val savedStateHandle: SavedStateHandle
) : edu.upc.openmrs.activities.BaseViewModel<List<String>>() {

    private val patientId: String = savedStateHandle.get(PATIENT_ID_BUNDLE)!!

    fun fetchDiagnoses() {
        setLoading()
        addSubscription(encounterDAO.getAllEncountersByType(patientId.toLong(), EncounterType(VISIT_NOTE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { encounters: List<Encounter> ->
                    val diagnosis = loadDiagnosesFromEncounters(encounters)
                    setContent(diagnosis)
                }
        )
    }

    private fun loadDiagnosesFromEncounters(encounters: List<Encounter>): List<String> {
        val diagnoses = ArrayList<String>()
        for (encounter in encounters) {
            for (diagnosis in encounter.diagnoses) {
                if(diagnosis.display != null)
                    diagnoses.add(diagnosis.display!!)
            }
        }
        return diagnoses
    }
}
