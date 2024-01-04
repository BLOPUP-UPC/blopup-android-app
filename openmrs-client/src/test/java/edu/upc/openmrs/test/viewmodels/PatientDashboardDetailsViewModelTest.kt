package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.openmrs.activities.patientdashboard.details.PatientDashboardDetailsViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.Treatment
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito

@RunWith(JUnit4::class)
class PatientDashboardDetailsViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var patientDAO: PatientDAO

    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: PatientDashboardDetailsViewModel

    private lateinit var treatmentRepository: TreatmentRepository

    lateinit var patient: Patient

    @Before
    override fun setUp() {
        super.setUp()
        treatmentRepository = mockk()
        savedStateHandle = SavedStateHandle().apply { set(PATIENT_ID_BUNDLE, ID) }
        viewModel = PatientDashboardDetailsViewModel(patientDAO, treatmentRepository, savedStateHandle)
        patient = createPatient(ID.toLong())
    }

    @Test
    fun fetchPatientData_success() {
        Mockito.`when`(patientDAO.findPatientByID(ID)).thenReturn(patient)

        viewModel.fetchPatientData()

        assert(viewModel.result.value is Result.Success)
    }

    @Test
    fun fetchPatientData_error() {
        Mockito.`when`(patientDAO.findPatientByID(ID)).thenReturn(null)

        viewModel.fetchPatientData()

        assert(viewModel.result.value is Result.Error)
    }

    @Test
    fun `should get all active treatments`() {
        val patient = Patient()
        val treatmentList = listOf<Treatment>()

        coEvery { treatmentRepository.fetchActiveTreatments(patient) } returns treatmentList

        runBlocking {
            viewModel.fetchActiveTreatments(patient)
            coVerify { treatmentRepository.fetchActiveTreatments(patient) }
            assert(viewModel.activeTreatments.value == treatmentList)
        }
    }

    companion object {
        const val ID = "1"
    }
}
