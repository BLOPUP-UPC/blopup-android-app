package edu.upc.openmrs.activities.visit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.model.Doctor
import edu.upc.blopup.model.Treatment
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.api.repository.DoctorRepository
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.models.OperationType
import edu.upc.sdk.library.models.Result
import javax.inject.Inject

@HiltViewModel
class AddEditTreatmentViewModel @Inject constructor(
    private val treatmentRepository: TreatmentRepository,
    private val doctorRepository: DoctorRepository
) :
    BaseViewModel<Treatment>() {

    private val logger = OpenMRSLogger()

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

    private val _doctors = MutableLiveData<List<Doctor>>()
    val doctors: LiveData<List<Doctor>> get() = _doctors

    suspend fun registerTreatment() =
        try {
            treatmentRepository.saveTreatment(treatment.value!!)
            setContent(treatment.value!!)
        } catch (e: Exception) {
            logger.e("Error registering treatment. ${e.javaClass}: ${e.message})")
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
        when (val result = doctorRepository.getAllDoctors()) {
            is Result.Success -> _doctors.value = result.data
            is Result.Error -> setError(result.throwable)
            is Result.Loading -> { }
        }
    }

    companion object {
        const val RECOMMENDED_BY = "recommendedBy"
        const val MEDICATION_NAME = "medicationName"
        const val MEDICATION_TYPE = "medicationType"
    }
}
