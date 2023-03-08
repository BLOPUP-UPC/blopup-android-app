package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.openmrs.activities.patientdashboard.details.PatientDashboardDetailsViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
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

    lateinit var savedStateHandle: SavedStateHandle

    lateinit var viewModel: PatientDashboardDetailsViewModel

    lateinit var patient: Patient

    @Before
    override fun setUp() {
        super.setUp()
        savedStateHandle = SavedStateHandle().apply { set(PATIENT_ID_BUNDLE, ID) }
        viewModel = PatientDashboardDetailsViewModel(patientDAO, savedStateHandle)
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

    companion object {
        const val ID = "1"
    }
}
