package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.blopup.RecordingHelper
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.ConceptRepository
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.*
import edu.upc.sdk.library.models.ResultType.PatientUpdateSuccess
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.COUNTRIES_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.PatientValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
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
    lateinit var conceptRepository: ConceptRepository

    @Mock
    lateinit var savedStateHandle: SavedStateHandle

    @Mock
    lateinit var recordingHelper: RecordingHelper

    lateinit var viewModel: AddEditPatientViewModel

    private val countries = listOf("country1", "country2", "country3")

    @Before
    override fun setUp() {
        super.setUp()
        `when`(patientDAO.findPatientByID(anyString())).thenReturn(Patient())
        savedStateHandle = SavedStateHandle().apply { set(COUNTRIES_BUNDLE, countries) }
    }

    @Test
    fun `resetPatient should clear all states and patient related data`() {
        viewModel = AddEditPatientViewModel(patientDAO, patientRepository, conceptRepository, recordingHelper, savedStateHandle)
        updatePatientData(0L, viewModel.patient)

        viewModel.resetPatient()

        with(viewModel) {
            // ViewModel state holding
            assertFalse(isUpdatePatient)
            assertNull(capturedPhotoFile)
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
        viewModel = AddEditPatientViewModel(patientDAO, patientRepository, conceptRepository, recordingHelper, savedStateHandle)
        `when`(patientRepository.registerPatient(any<Patient>())).thenReturn(Observable.just(
            Patient()
        ))
        with(viewModel) {
            patientValidator = mock(PatientValidator::class.java)
            `when`(patientValidator.validate()).thenReturn(true)

            confirmPatient()

            assert(viewModel.result.value is Result.Success)
        }
    }

    @Test
    fun `confirmPatient should update existing patient when its id is passed`() {
        savedStateHandle.apply { set(PATIENT_ID_BUNDLE, "1L") }
        viewModel = AddEditPatientViewModel(patientDAO, patientRepository, conceptRepository, recordingHelper, savedStateHandle)
        `when`(patientRepository.updatePatient(any<Patient>())).thenReturn(Observable.just(PatientUpdateSuccess))
        with(viewModel) {
            patientValidator = mock(PatientValidator::class.java)
            `when`(patientValidator.validate()).thenReturn(true)

            confirmPatient()

            assertEquals(PatientUpdateSuccess, patientUpdateLiveData.value)
        }
    }

    @Test
    fun `confirmPatient should do nothing when patient data is invalid`() {
        viewModel = AddEditPatientViewModel(patientDAO, patientRepository, conceptRepository, recordingHelper, savedStateHandle)
        `when`(patientRepository.registerPatient(any<Patient>())).thenReturn(Observable.just(
            Patient()
        ))
        with(viewModel) {
            patientValidator = mock(PatientValidator::class.java)
            `when`(patientValidator.validate()).thenReturn(false)

            confirmPatient()

            assertNull(result.value)
        }
    }

    @Test
    fun fetchSimilarPatients() {
        viewModel = AddEditPatientViewModel(patientDAO, patientRepository, conceptRepository, recordingHelper, savedStateHandle)
        with(viewModel) {
            val similarPatients = listOf(createPatient(1L), createPatient(2L), createPatient(3L))
            patientValidator = mock(PatientValidator::class.java)
            `when`(patientValidator.validate()).thenReturn(true)
            `when`(patientRepository.fetchSimilarPatients(any<Patient>())).thenReturn(Observable.just(similarPatients))

            fetchSimilarPatients()

            assertIterableEquals(similarPatients, similarPatientsLiveData.value)
        }
    }

    @Test
    fun fetchCausesOfDeath() {
        viewModel = AddEditPatientViewModel(patientDAO, patientRepository, conceptRepository, recordingHelper, savedStateHandle)
        `when`(patientRepository.getCauseOfDeathGlobalConceptID()).thenReturn(Observable.just(String()))
        `when`(conceptRepository.getConceptByUuid(anyString())).thenReturn(Observable.just(ConceptAnswers()))

        viewModel.fetchCausesOfDeath().observeForever {
            assert(it is ConceptAnswers)
        }
    }

    @Test
    fun `should save legal consent recording when new patient is added (with toggle on)`() {
        val patient = Patient()
        viewModel = AddEditPatientViewModel(patientDAO, patientRepository, conceptRepository, recordingHelper, savedStateHandle)
        `when`(patientRepository.registerPatient(any())).thenReturn(Observable.just(patient))

        with(viewModel) {
            patientValidator = mock(PatientValidator::class.java)
            `when`(patientValidator.validate()).thenReturn(true)

            confirmPatient()

            verify(recordingHelper).saveLegalConsent(patient)
        }
    }

    @Test
    @Ignore("need to mock the patient already existing")
    fun `should not save legal consent recording when updating existing patient`() {
        val patient = Patient()
        viewModel = AddEditPatientViewModel(patientDAO, patientRepository, conceptRepository, recordingHelper, savedStateHandle)
        `when`(patientRepository.registerPatient(any())).thenReturn(Observable.just(patient))

        with(viewModel) {
            patientValidator = mock(PatientValidator::class.java)
            `when`(patientValidator.validate()).thenReturn(true)

            confirmPatient()

            verify(recordingHelper, times(0)).saveLegalConsent(patient)
        }
    }
}
