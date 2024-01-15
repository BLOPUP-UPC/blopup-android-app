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

    val treatment: MutableLiveData<Treatment> = MutableLiveData<Treatment>().apply { value = Treatment(recommendedBy = "", medicationName = "", medicationType = emptySet(), visitId = 0L) }

    val treatmentToEdit: MutableLiveData<Treatment> =
        MutableLiveData<Treatment>().apply { value = Treatment(recommendedBy = "", medicationName = "", medicationType = emptySet(), visitId = 0L)}

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

    suspend fun updateTreatment() {

        val valuesToUpdate = mutableMapOf<String, Any>()

        if(treatmentToEdit.value?.recommendedBy != treatment.value?.recommendedBy) {
            valuesToUpdate["Recommended By"] = treatment.value?.recommendedBy!!
        }
        if(treatmentToEdit.value?.medicationName != treatment.value?.medicationName) {
            valuesToUpdate["Medication Name"] = treatment.value?.medicationName!!
        }
        if(treatmentToEdit.value?.medicationType != treatment.value?.medicationType) {
            valuesToUpdate["Medication Type"] = treatment.value?.medicationType!!
        }
        if(treatmentToEdit.value?.notes != treatment.value?.notes) {
            valuesToUpdate["Treatment Notes"] = treatment.value?.notes!!
        }

        try {
            treatmentRepository.updateTreatment(valuesToUpdate, treatmentToEdit.value?.treatmentUuid!!)
            setContent(treatment.value!!)
        } catch (e: Exception) {
            setError(e)
        }
    }

    companion object {
        const val RECOMMENDED_BY = "recommendedBy"
        const val MEDICATION_NAME = "medicationName"
        const val MEDICATION_TYPE = "medicationType"
    }
}
