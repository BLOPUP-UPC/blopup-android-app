package edu.upc.blopup.ui.searchpatient

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.api.repository.PatientRepositoryCoroutines
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.PersonName
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(org.mockito.junit.MockitoJUnitRunner::class)
class SearchPatientViewModelTest{

    @InjectMockKs
    private lateinit var viewModel: SearchPatientViewModel

    @MockK
    private lateinit var patientRepositoryCoroutines: PatientRepositoryCoroutines

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp(){
        Dispatchers.setMain(UnconfinedTestDispatcher())
        MockKAnnotations.init(this)
    }

    @Test
    fun `getAllPatientsLocally should return all patients saved locally`(){
        val patientList = listOf(Patient())

        coEvery { patientRepositoryCoroutines.getAllPatientsLocally() } returns edu.upc.sdk.library.models.Result.Success(patientList)

        runBlocking {
            viewModel.getAllPatientsLocally()

            assertEquals(ResultUiState.Success(patientList), viewModel.patientListResultUiState.value)
        }
    }

    @Test
    fun `if there is an error fetching the patients locally, should return error`(){
        coEvery { patientRepositoryCoroutines.getAllPatientsLocally() } returns edu.upc.sdk.library.models.Result.Error(Exception())

        runBlocking {
            viewModel.getAllPatientsLocally()


            assertEquals(ResultUiState.Error, viewModel.patientListResultUiState.value)
        }
    }

    @Test
    fun `when patient doesn't exist then return null`() {
        val patient = Patient().apply {
            uuid = "def218bb-a25a-4b40-9b77-b7c26628f0c9"
            names = emptyList()
        }
        coEvery { patient.uuid?.let { patientRepositoryCoroutines.downloadPatientByUuid(it) } } returns patient

        runBlocking {
            viewModel.retrieveOrDownloadPatient(patient.uuid)
            assertEquals(ResultUiState.Success(null), viewModel.retrievePatientResult.value)
        }
    }

    @Test
    fun `when patient exists in remote and in local database then return the local patient`() {
        val patient = Patient().apply {
            id = 1L
            uuid = "def218bb-a25a-4b40-9b77-b7c26628f0c6"
            names = PersonName().let {
                it.givenName = "Cristina"
                it.familyName = "Aguilera"
                listOf(it)
            }
        }

        coEvery { patientRepositoryCoroutines.downloadPatientByUuid(patient.uuid!!) } returns patient
        coEvery { patient.uuid?.let { patientRepositoryCoroutines.findPatientByUUID(it) } } returns patient

        runBlocking {
            viewModel.retrieveOrDownloadPatient(patient.uuid)
            assertEquals(ResultUiState.Success(patient), viewModel.retrievePatientResult.value)
        }
    }
}