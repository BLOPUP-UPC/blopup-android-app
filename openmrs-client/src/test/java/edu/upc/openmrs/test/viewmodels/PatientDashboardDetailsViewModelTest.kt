package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.blopup.model.Treatment
import edu.upc.openmrs.activities.patientdashboard.details.PatientDetailsViewModel
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.api.repository.PatientRepositoryCoroutines
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.UUID

@RunWith(JUnit4::class)
class PatientDashboardDetailsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK(relaxed = true)
    lateinit var patientDAO: PatientDAO

    @MockK
    lateinit var patientRepositoryCoroutines: PatientRepositoryCoroutines

    @MockK(relaxed = true)
    lateinit var logger: OpenMRSLogger

    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: PatientDetailsViewModel

    @MockK
    private lateinit var treatmentRepository: TreatmentRepository

    lateinit var patient: Patient

    val patientUuid = UUID.randomUUID()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        MockKAnnotations.init(this)
        savedStateHandle = SavedStateHandle().apply {
            set(PATIENT_ID_BUNDLE, PATIENT_ID)
            set(PATIENT_UUID_BUNDLE, patientUuid.toString())
        }
        viewModel = PatientDetailsViewModel(patientDAO, treatmentRepository, patientRepositoryCoroutines, logger, savedStateHandle)
        patient = Patient().apply {
            id = PATIENT_ID.toLong()
            uuid = patientUuid.toString()
        }
    }

    @Test
    fun fetchPatientData_success() = runTest {
        every { patientDAO.findPatientByID(PATIENT_ID) } returns patient

        viewModel.fetchPatientData()

        assert(viewModel.result.value is Result.Success)
    }

    @Test
    fun fetchPatientData_error() = runTest {
        every { patientDAO.findPatientByID(PATIENT_ID) } returns null

        viewModel.fetchPatientData()

        assert(viewModel.result.value is Result.Error)
    }

    @Test
    fun `should get all active treatments`() {
        val treatmentList = listOf<Treatment>()

        coEvery { treatmentRepository.fetchAllActiveTreatments(patientUuid) } returns Result.Success(treatmentList)

        runBlocking {
            viewModel.fetchActiveTreatments()
            coVerify { treatmentRepository.fetchAllActiveTreatments(patientUuid) }
            assertEquals(viewModel.activeTreatments.value, Result.Success(treatmentList))
        }
    }

    @Test
    fun `should refresh treatments with previous patient`() = runTest {
        val treatmentList = listOf<Treatment>()

        coEvery { treatmentRepository.fetchAllActiveTreatments(patientUuid) } returns Result.Success(treatmentList)

        viewModel.refreshActiveTreatments()

        coVerify { treatmentRepository.fetchAllActiveTreatments(patientUuid) }
        assertEquals(viewModel.activeTreatments.value, Result.Success(treatmentList))
    }

    companion object {
        const val PATIENT_ID = "1"
    }
}
