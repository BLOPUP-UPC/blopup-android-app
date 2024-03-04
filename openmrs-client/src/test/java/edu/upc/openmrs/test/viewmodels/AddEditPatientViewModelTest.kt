package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import edu.upc.blopup.RecordingHelper
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.api.repository.RecordingRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.PersonAddress
import edu.upc.sdk.library.models.PersonName
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.ResultType.PatientUpdateSuccess
import edu.upc.sdk.library.models.ResultType.RecordingSuccess
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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
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
    lateinit var recordingHelper: RecordingHelper

    @Mock
    lateinit var recordingRepository: RecordingRepository

    @Mock
    private lateinit var pairValidationObserver: Observer<Pair<Boolean, Int?>>

    @Mock
    private lateinit var booleanValidationObserver: Observer<Boolean>

    private lateinit var viewModel: AddEditPatientViewModel

    @Before
    override fun setUp() {
        super.setUp()
        `when`(patientDAO.findPatientByID(anyString())).thenReturn(Patient())
        savedStateHandle = SavedStateHandle()
    }

    @Test
    fun `resetPatient should clear all states and patient related data`() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )
        updatePatientData(0L, viewModel.patient)

        viewModel.resetPatient()

        with(viewModel) {
            // ViewModel state holding
            assertFalse(isUpdatePatient)
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
    fun `confirmPatient should create new patient when no patient id passed`() {

        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )

        viewModel.patient = createPatient(1, "1000A")

        `when`(patientRepository.registerPatient(any())).thenReturn(
            Observable.just(
                Patient()
            )
        )

        confirmPatient()

        assert(viewModel.result.value is Result.Success)
    }

    @Test
    fun `confirmPatient should update existing patient when its id is passed`() {
        savedStateHandle.apply { set(PATIENT_ID_BUNDLE, "1L") }
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
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
    fun fetchSimilarPatients() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )
        with(viewModel) {
            val similarPatients = listOf(createPatient(1L), createPatient(2L), createPatient(3L))
            `when`(patientRepository.fetchSimilarPatients(any())).thenReturn(
                Observable.just(
                    similarPatients
                )
            )

            fetchSimilarPatients()

            assertIterableEquals(similarPatients, similarPatientsLiveData.value)
        }
    }

    @Test
    fun fetchCausesOfDeath() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )
    }

    @Test
    fun `should save legal consent recording when new patient is added (with toggle on)`() {
        val patient = createPatient(1, "10009X3")

        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )
        `when`(patientRepository.registerPatient(any())).thenReturn(Observable.just(patient))
        `when`(recordingRepository.saveRecording(any())).thenReturn(Observable.just(RecordingSuccess))

        viewModel.confirmPatient()

        verify(recordingHelper, times(0)).saveLegalConsent(any())
    }

    @Test
    fun `should not save legal consent recording when updating existing patient`() {
        savedStateHandle = SavedStateHandle().apply { set(PATIENT_ID_BUNDLE, "patientId") }
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )

        `when`(patientRepository.updatePatient(any())).thenReturn(
            Observable.just(
                PatientUpdateSuccess
            )
        )

        confirmPatient()

        verify(recordingHelper, times(0)).saveLegalConsent(any())
    }

    @Test
    fun `when name is valid, the isNameValidLiveData is true`() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )

        viewModel.isNameValidLiveData.observeForever(pairValidationObserver)

        viewModel.validateFirstName("Pilar")

        assertEquals(viewModel.isNameValidLiveData.value?.first, true)
    }
    @Test
    fun `when name is null, the isNameValidLiveData is false`() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )

        viewModel.isNameValidLiveData.observeForever(pairValidationObserver)

        viewModel.validateFirstName(null)

        assertEquals(viewModel.isNameValidLiveData.value?.first, false)
        assertEquals(viewModel.isNameValidLiveData.value?.second, edu.upc.R.string.empty_value)
    }

    @Test
    fun `when name is invalid, the isNameValidLiveData is false`() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )

        viewModel.isNameValidLiveData.observeForever(pairValidationObserver)

        viewModel.validateFirstName("*")

        assertEquals(viewModel.isNameValidLiveData.value?.first, false)
        assertEquals(viewModel.isNameValidLiveData.value?.second, edu.upc.R.string.fname_invalid_error)
    }

    @Test
    fun `when last name is valid, the isNameValidLiveData is true`() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )

        viewModel.isSurnameValidLiveData.observeForever(pairValidationObserver)

        viewModel.validateSurname("Alonso")

        assertEquals(viewModel.isSurnameValidLiveData.value?.first, true)
    }

    @Test
    fun `when last name is null, the isNameValidLiveData is false`() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )

        viewModel.isSurnameValidLiveData.observeForever(pairValidationObserver)

        viewModel.validateSurname(null)

        assertEquals(viewModel.isSurnameValidLiveData.value?.first, false)
        assertEquals(viewModel.isSurnameValidLiveData.value?.second, edu.upc.R.string.empty_value)
    }

    @Test
    fun `when last name is invalid, the isNameValidLiveData is false`() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )

        viewModel.isSurnameValidLiveData.observeForever(pairValidationObserver)

        viewModel.validateSurname("*")

        assertEquals(viewModel.isSurnameValidLiveData.value?.first, false)
        assertEquals(viewModel.isSurnameValidLiveData.value?.second, edu.upc.R.string.fname_invalid_error)
    }

    @Test
    fun `when country of birth is valid, then is true`() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )

        viewModel.isCountryOfBirthValidLiveData.observeForever(booleanValidationObserver)

        viewModel.validateCountryOfBirth("Argentina")

        assertEquals(viewModel.isCountryOfBirthValidLiveData.value, true)
    }

    @Test
    fun `when country of birth is the default option, then is false`() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )

        viewModel.isCountryOfBirthValidLiveData.observeForever(booleanValidationObserver)

        viewModel.validateCountryOfBirth("Select country of birth")

        assertEquals(viewModel.isCountryOfBirthValidLiveData.value, false)
    }

    @Test
    fun `when gender is selected, then it is true`() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )

        viewModel.isGenderValidLiveData.observeForever(booleanValidationObserver)

        viewModel.validateGender(true)

        assertEquals(viewModel.isGenderValidLiveData.value, true)
    }

    @Test
    fun `when gender is not selected, then it is false`() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )

        viewModel.isGenderValidLiveData.observeForever(booleanValidationObserver)

        viewModel.validateGender(false)

        assertEquals(viewModel.isGenderValidLiveData.value, false)
    }

    @Test
    fun `when all fields are completed, then everything is valid`() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )

        viewModel.isNameValidLiveData.observeForever(pairValidationObserver)
        viewModel.isSurnameValidLiveData.observeForever(pairValidationObserver)
        viewModel.isGenderValidLiveData.observeForever(booleanValidationObserver)
        viewModel.isCountryOfBirthValidLiveData.observeForever(booleanValidationObserver)
        viewModel.isBirthDateValidLiveData.observeForever(pairValidationObserver)
        viewModel.isLegalConsentValidLiveData.observeForever(booleanValidationObserver)

        viewModel.validateFirstName("Pilar")
        viewModel.validateSurname("Alonso")
        viewModel.validateGender(true)
        viewModel.validateCountryOfBirth("Argentina")
        viewModel.validateBirthDate("01/01/2020")
        viewModel.validateLegalConsent(true)

        assertEquals(viewModel.isPatientValidLiveData.value, true)
    }

    @Test
    fun `when all fields are not completed, then patient is not valid`() {
        viewModel = AddEditPatientViewModel(
            patientDAO,
            patientRepository,
            recordingHelper,
            savedStateHandle
        )

        viewModel.isNameValidLiveData.observeForever(pairValidationObserver)
        viewModel.isSurnameValidLiveData.observeForever(pairValidationObserver)
        viewModel.isGenderValidLiveData.observeForever(booleanValidationObserver)
        viewModel.isCountryOfBirthValidLiveData.observeForever(booleanValidationObserver)
        viewModel.isBirthDateValidLiveData.observeForever(pairValidationObserver)
        viewModel.isLegalConsentValidLiveData.observeForever(booleanValidationObserver)

        viewModel.validateFirstName("*")
        viewModel.validateSurname(null)
        viewModel.validateGender(true)
        viewModel.validateCountryOfBirth("Argentina")
        viewModel.validateBirthDate("01/01/2020")
        viewModel.validateLegalConsent(true)

        assertEquals(viewModel.isPatientValidLiveData.value, false)
    }
}
