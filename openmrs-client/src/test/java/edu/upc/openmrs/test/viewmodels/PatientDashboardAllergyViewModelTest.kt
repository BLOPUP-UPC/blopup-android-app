package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.sdk.library.api.repository.AllergyRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Allergy
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.ResultType.AllergyDeletionError
import edu.upc.sdk.library.models.ResultType.AllergyDeletionLocalSuccess
import edu.upc.sdk.library.models.ResultType.AllergyDeletionSuccess
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.openmrs.activities.patientdashboard.allergy.PatientDashboardAllergyViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import rx.Observable

@RunWith(JUnit4::class)
class PatientDashboardAllergyViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var patientDAO: PatientDAO

    @Mock
    lateinit var allergyRepository: AllergyRepository

    lateinit var savedStateHandle: SavedStateHandle

    lateinit var viewModel: PatientDashboardAllergyViewModel

    lateinit var patient: Patient

    lateinit var allergies: List<Allergy>

    @Before
    override fun setUp() {
        super.setUp()
        savedStateHandle = SavedStateHandle().apply { set(PATIENT_ID_BUNDLE, PATIENT_ID) }
        viewModel = PatientDashboardAllergyViewModel(patientDAO, allergyRepository, savedStateHandle)
        patient = createPatient(PATIENT_ID.toLong())
        allergies = listOf(createAllergy(1L, "doctor"), createAllergy(2L, "doctor"))
    }

    @Test
    fun fetchAllergies_success() {
        Mockito.`when`(allergyRepository.getAllergyFromDatabase(PATIENT_ID)).thenReturn(Observable.just(allergies))

        viewModel.fetchAllergies()

        assert(viewModel.result.value is Result.Success)
    }

    @Test
    fun fetchAllergies_error() {
        val errorMsg = "Error message!"
        val throwable = Throwable(errorMsg)
        Mockito.`when`(allergyRepository.getAllergyFromDatabase(PATIENT_ID)).thenReturn(Observable.error(throwable))

        viewModel.fetchAllergies()

        val actualResult = (viewModel.result.value as Result.Error).throwable.message

        assertEquals(errorMsg, actualResult)
    }

    @Test
    fun deleteAllergy_success() {
        val allergy = allergies[0]
        Mockito.`when`(patientDAO.findPatientByID(PATIENT_ID)).thenReturn(patient)
        Mockito.`when`(allergyRepository.deleteAllergy(patient.uuid, allergy.uuid))
                .thenReturn(Observable.just(AllergyDeletionSuccess))

        viewModel.deleteAllergy(allergy.uuid!!).observeForever { actualResultType ->
            assertEquals(AllergyDeletionSuccess, actualResultType)
        }
    }

    @Test
    fun deleteAllergy_LocalSuccess() {
        val allergy = allergies[0]
        Mockito.`when`(patientDAO.findPatientByID(PATIENT_ID)).thenReturn(patient)
        Mockito.`when`(allergyRepository.deleteAllergy(patient.uuid, allergy.uuid))
                .thenReturn(Observable.just(AllergyDeletionLocalSuccess))

        viewModel.deleteAllergy(allergy.uuid!!).observeForever { actualResultType ->
            assertEquals(AllergyDeletionLocalSuccess, actualResultType)
        }
    }

    @Test
    fun deleteAllergy_error() {
        val allergy = allergies[0]
        Mockito.`when`(patientDAO.findPatientByID(PATIENT_ID)).thenReturn(patient)
        Mockito.`when`(allergyRepository.deleteAllergy(patient.uuid, allergy.uuid))
                .thenReturn(Observable.just(AllergyDeletionError))

        viewModel.deleteAllergy(allergy.uuid!!).observeForever { actualResultType ->
            assertEquals(AllergyDeletionError, actualResultType)
        }
    }

    companion object {
        const val PATIENT_ID = "1"
    }
}
