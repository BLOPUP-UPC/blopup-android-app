package edu.upc.openmrs.activities.visitdashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.models.Treatment
import javax.inject.Inject

@HiltViewModel
class TreatmentViewModel @Inject constructor(private val treatmentRepository: TreatmentRepository) :
    BaseViewModel<Treatment>() {

    private val _fieldValidation: MutableLiveData<MutableMap<String, Boolean>> =
        MutableLiveData<MutableMap<String, Boolean>>().apply {
            value =
                mutableMapOf(
                    RECOMMENDED_BY to false,
                    MEDICATION_NAME to false,
                    MEDICATION_TYPE to false
                )
        }

    val fieldValidation: LiveData<MutableMap<String, Boolean>> = _fieldValidation

    val treatment: MutableLiveData<Treatment> =
        MutableLiveData<Treatment>().apply { value = Treatment() }

    suspend fun registerTreatment() =
        try {
            treatmentRepository.saveTreatment(treatment.value!!)
            setContent(treatment.value!!)
        } catch (e: Exception) {
            setError(e)
        }

    fun updateFieldValidation(fieldName: String, isValid: Boolean) {
        _fieldValidation.value = _fieldValidation.value?.apply { this[fieldName] = isValid }
    }

    companion object {
        const val RECOMMENDED_BY = "recommendedBy"
        const val MEDICATION_NAME = "medicationName"
        const val MEDICATION_TYPE = "medicationType"
    }
}
