package edu.upc.openmrs.activities.visitdashboard

import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.models.Treatment
import javax.inject.Inject

@HiltViewModel
class TreatmentViewModel @Inject constructor(private val treatmentRepository: TreatmentRepository) :
    BaseViewModel<Result<Treatment>>() {

    val treatment: MutableLiveData<Treatment> =
        MutableLiveData<Treatment>().apply { value = Treatment() }

    suspend fun registerTreatment(): Result<Treatment>? {
        return try {
            treatmentRepository.saveTreatment(treatment.value!!)
            Result.success(treatment.value!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

