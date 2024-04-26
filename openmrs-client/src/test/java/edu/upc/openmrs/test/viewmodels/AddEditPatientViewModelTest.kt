package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import edu.upc.openmrs.activities.editpatient.EditPatientViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.PersonAddress
import edu.upc.sdk.library.models.PersonName
import edu.upc.sdk.library.models.ResultType.PatientUpdateSuccess
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import rx.Observable

@RunWith(JUnit4::class)
class AddEditPatientViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var patientDAO: PatientDAO

    @Mock
    lateinit var patientRepository: PatientRepository

    @Mock
    lateinit var savedStateHandle: SavedStateHandle

    @Mock
    private lateinit var pairValidationObserver: Observer<Pair<Boolean, Int?>>

    @Mock
    private lateinit var booleanValidationObserver: Observer<Boolean>

    private lateinit var viewModel: EditPatientViewModel

    @Before
    override fun setUp() {
        super.setUp()
        `when`(patientDAO.findPatientByID(anyString())).thenReturn(Patient())
        savedStateHandle = SavedStateHandle()
    }

    @Test
    fun `resetPatient should clear all states and patient related data`() {
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )
        updatePatientData(0L, viewModel.patient)

        viewModel.resetPatient()

        with(viewModel) {
            // ViewModel state holding
            assertNull(dateHolder)
        }
        with(viewModel.patient) {
            // ViewModel's patient object state
            assertNull(id)
            assertNull(gender)
            assertFalse(birthdateEstimated)
            assertNull(birthdate)
            assertNull(isDeceased)
            assertNull(causeOfDeath)
            assertIterableEquals(emptyList<PersonName>(), names)
            assertIterableEquals(emptyList<PersonAddress>(), addresses)
        }
    }

    @Test
    fun `confirmPatient should update existing patient when its id is passed`() {
        savedStateHandle.apply { set(PATIENT_ID_BUNDLE, "1L") }
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )
        `when`(patientRepository.updatePatient(any())).thenReturn(
            Observable.just(
                PatientUpdateSuccess
            )
        )
        with(viewModel) {

            confirmPatient()

            assertEquals(PatientUpdateSuccess, patientUpdateLiveData.value)
        }
    }

    @Test
    fun fetchCausesOfDeath() {
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )
    }

    @Test
    fun `when name is valid, the isNameValidLiveData is true`() {
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )

        viewModel.isNameValidLiveData.observeForever(pairValidationObserver)

        viewModel.validateFirstName("Pilar")

        assertEquals(viewModel.isNameValidLiveData.value?.first, true)
    }
    @Test
    fun `when name is null, the isNameValidLiveData is false`() {
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )

        viewModel.isNameValidLiveData.observeForever(pairValidationObserver)

        viewModel.validateFirstName(null)

        assertEquals(viewModel.isNameValidLiveData.value?.first, false)
        assertEquals(viewModel.isNameValidLiveData.value?.second, edu.upc.R.string.empty_value)
    }

    @Test
    fun `when name is invalid, the isNameValidLiveData is false`() {
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )

        viewModel.isNameValidLiveData.observeForever(pairValidationObserver)

        viewModel.validateFirstName("*")

        assertEquals(viewModel.isNameValidLiveData.value?.first, false)
        assertEquals(viewModel.isNameValidLiveData.value?.second, edu.upc.R.string.fname_invalid_error)
    }

    @Test
    fun `when last name is valid, the isNameValidLiveData is true`() {
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )

        viewModel.isSurnameValidLiveData.observeForever(pairValidationObserver)

        viewModel.validateSurname("Alonso")

        assertEquals(viewModel.isSurnameValidLiveData.value?.first, true)
    }

    @Test
    fun `when last name is null, the isNameValidLiveData is false`() {
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )

        viewModel.isSurnameValidLiveData.observeForever(pairValidationObserver)

        viewModel.validateSurname(null)

        assertEquals(viewModel.isSurnameValidLiveData.value?.first, false)
        assertEquals(viewModel.isSurnameValidLiveData.value?.second, edu.upc.R.string.empty_value)
    }

    @Test
    fun `when last name is invalid, the isNameValidLiveData is false`() {
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )

        viewModel.isSurnameValidLiveData.observeForever(pairValidationObserver)

        viewModel.validateSurname("*")

        assertEquals(viewModel.isSurnameValidLiveData.value?.first, false)
        assertEquals(viewModel.isSurnameValidLiveData.value?.second, edu.upc.R.string.fname_invalid_error)
    }

    @Test
    fun `when country of birth is valid, then is true`() {
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )

        viewModel.isCountryOfBirthValidLiveData.observeForever(booleanValidationObserver)

        viewModel.validateCountryOfBirth("Argentina")

        assertEquals(viewModel.isCountryOfBirthValidLiveData.value, true)
    }

    @Test
    fun `when country of birth is the default option, then is false`() {
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )

        viewModel.isCountryOfBirthValidLiveData.observeForever(booleanValidationObserver)

        viewModel.validateCountryOfBirth("Select country of birth")

        assertEquals(viewModel.isCountryOfBirthValidLiveData.value, false)
    }

    @Test
    fun `when gender is selected, then it is true`() {
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )

        viewModel.isGenderValidLiveData.observeForever(booleanValidationObserver)

        viewModel.validateGender(true)

        assertEquals(viewModel.isGenderValidLiveData.value, true)
    }

    @Test
    fun `when gender is not selected, then it is false`() {
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )

        viewModel.isGenderValidLiveData.observeForever(booleanValidationObserver)

        viewModel.validateGender(false)

        assertEquals(viewModel.isGenderValidLiveData.value, false)
    }

    @Test
    fun `when all fields are completed, then everything is valid`() {
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )

        viewModel.isNameValidLiveData.observeForever(pairValidationObserver)
        viewModel.isSurnameValidLiveData.observeForever(pairValidationObserver)
        viewModel.isGenderValidLiveData.observeForever(booleanValidationObserver)
        viewModel.isCountryOfBirthValidLiveData.observeForever(booleanValidationObserver)
        viewModel.isBirthDateValidLiveData.observeForever(pairValidationObserver)

        viewModel.validateFirstName("Pilar")
        viewModel.validateSurname("Alonso")
        viewModel.validateGender(true)
        viewModel.validateCountryOfBirth("Argentina")
        viewModel.validateBirthDate("01/01/2020")

        assertEquals(viewModel.isPatientValidLiveData.value, true)
    }

    @Test
    fun `when all fields are not completed, then patient is not valid`() {
        viewModel = EditPatientViewModel(
            patientDAO,
            patientRepository,
            savedStateHandle
        )

        viewModel.isNameValidLiveData.observeForever(pairValidationObserver)
        viewModel.isSurnameValidLiveData.observeForever(pairValidationObserver)
        viewModel.isGenderValidLiveData.observeForever(booleanValidationObserver)
        viewModel.isCountryOfBirthValidLiveData.observeForever(booleanValidationObserver)
        viewModel.isBirthDateValidLiveData.observeForever(pairValidationObserver)

        viewModel.validateFirstName("*")
        viewModel.validateSurname(null)
        viewModel.validateGender(true)
        viewModel.validateCountryOfBirth("Argentina")
        viewModel.validateBirthDate("01/01/2020")

        assertEquals(viewModel.isPatientValidLiveData.value, false)
    }
}
