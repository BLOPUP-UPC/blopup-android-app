package edu.upc.openmrs.activities.visitdashboard

import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.Treatment
import javax.inject.Inject

@HiltViewModel
class TreatmentViewModel @Inject constructor(private val treatmentRepository: TreatmentRepository) : BaseViewModel<Result<Treatment>>() {

    val treatment: MutableLiveData<Treatment> = MutableLiveData<Treatment>().apply { value = Treatment() }

    val activeTreatments: MutableLiveData<List<Treatment>> = MutableLiveData<List<Treatment>>()

    suspend fun registerTreatment() {
        treatmentRepository.saveTreatment(treatment.value!!)
    }

    suspend fun fetchActiveTreatments(patient: Patient) {
        val activeTreatmentsList = treatmentRepository.fetchActiveTreatments(patient)
        activeTreatments.postValue(activeTreatmentsList)
    }
}

