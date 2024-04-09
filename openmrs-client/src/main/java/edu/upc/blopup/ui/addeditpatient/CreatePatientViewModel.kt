package edu.upc.blopup.ui.addeditpatient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.utilities.DateUtils
import edu.upc.sdk.utilities.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

@HiltViewModel
class CreatePatientViewModel @Inject constructor(private val patientRepository: PatientRepository) : ViewModel() {

    private val _createPatientUiState = MutableStateFlow<CreatePatientResultUiState>(CreatePatientResultUiState.NotCreated)
    val createPatientUiState: StateFlow<CreatePatientResultUiState> = _createPatientUiState.asStateFlow()


    fun createPatient  (name: String, familyName: String, dateOfBirth: String, estimatedYears: String, gender: String, countryOfBirth: String) {
        _createPatientUiState.value = CreatePatientResultUiState.Loading

        var birthdateEstimated: Boolean = false
        var birthdate = ""
        if (estimatedYears.isNotEmpty()) {
            birthdateEstimated = true
            val approximateBirthdate =
                DateUtils.getDateTimeFromDifference(estimatedYears.toInt())
            birthdate = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT)
                .print(approximateBirthdate)
        } else {
            birthdateEstimated = false
            val parsedBirthdate = DateTime.parse(dateOfBirth, DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT))
            birthdate = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT)
                .print(parsedBirthdate)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val response = patientRepository.registerPatient(
                name,
                familyName,
                birthdate,
                birthdateEstimated,
                gender,
                countryOfBirth
            )

            try {
                val patient = response?.toBlocking()?.first()
                if (patient?.id != null) {
                    _createPatientUiState.value = CreatePatientResultUiState.Success(patient.id!!)
                } else {
                    _createPatientUiState.value = CreatePatientResultUiState.Error
                }
            } catch (e: Exception) {
                _createPatientUiState.value = CreatePatientResultUiState.Error
            }
        }
    }

    fun isNameOrSurnameInvalidFormat(input: String): Boolean {
        return !StringUtils.validateText(input, StringUtils.ILLEGAL_CHARACTERS)
    }
}