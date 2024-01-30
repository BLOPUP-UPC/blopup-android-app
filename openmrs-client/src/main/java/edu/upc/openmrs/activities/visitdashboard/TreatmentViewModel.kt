package edu.upc.openmrs.activities.visitdashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.models.OperationType
import edu.upc.sdk.library.models.Provider
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

    val treatment: MutableLiveData<Treatment> = MutableLiveData<Treatment>().apply {
        value = Treatment(
            recommendedBy = "",
            medicationName = "",
            medicationType = emptySet()
        )
    }

    val treatmentToEdit: MutableLiveData<Treatment> =
        MutableLiveData<Treatment>().apply {
            value = Treatment(
                recommendedBy = "",
                medicationName = "",
                medicationType = emptySet()
            )
        }

    private val _doctors = MutableLiveData<List<Provider>>()
    val doctors: LiveData<List<Provider>> get() = _doctors

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
        try {
            treatmentRepository.updateTreatment(treatmentToEdit.value!!, treatment.value!!)
            setContent(treatment.value!!, OperationType.TreatmentUpdated)
        } catch (e: Exception) {
            setError(e)
        }
    }

    suspend fun getAllDoctors() {
        try {
            val result = treatmentRepository.getAllDoctors()
            _doctors.value = result
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
