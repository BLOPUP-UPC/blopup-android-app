package edu.upc.openmrs.activities.visitdashboard

import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.models.Treatment
import javax.inject.Inject
import edu.upc.sdk.library.models.Result


@HiltViewModel
class TreatmentViewModel @Inject constructor(private val treatmentRepository: TreatmentRepository) :
    BaseViewModel<Treatment>() {

    val treatment: MutableLiveData<Treatment> =
        MutableLiveData<Treatment>().apply { value = Treatment() }

    suspend fun registerTreatment() =
        try {
            treatmentRepository.saveTreatment(treatment.value!!)
            setContent(treatment.value!!)
        } catch (e: Exception) {
            setError(e)
        }
}
