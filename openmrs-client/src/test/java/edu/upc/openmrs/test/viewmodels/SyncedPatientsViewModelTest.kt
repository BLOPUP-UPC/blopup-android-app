package edu.upc.openmrs.test.viewmodels

import android.annotation.SuppressLint
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import arrow.core.Either
import edu.upc.openmrs.activities.syncedpatients.SyncedPatientsViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.api.repository.PatientRepositoryCoroutines
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import rx.Observable

@RunWith(JUnit4::class)
class SyncedPatientsViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var patientDAO: PatientDAO

    @MockK
    lateinit var patientRepository: PatientRepository

    @MockK
    lateinit var patientRepositoryCoroutines: PatientRepositoryCoroutines

    private lateinit var patientList: List<Patient>

    lateinit var viewModel: SyncedPatientsViewModel

    @SuppressLint("CheckResult")
    @Before
    override fun setUp() {
        super.setUp()
        MockKAnnotations.init(this)
        patientList = listOf(createPatient(1L), createPatient(2L), createPatient(3L))


        viewModel = SyncedPatientsViewModel(patientDAO, patientRepository, patientRepositoryCoroutines)
    }

    @Test
    fun fetchSyncedPatients_success() {

        every { patientDAO.allPatients } returns Observable.just(patientList)

        viewModel.fetchSyncedPatients()

        val actualResult = (viewModel.result.value as Result.Success).data

        assertIterableEquals(patientList, actualResult)
    }

    @Test
    fun fetchSyncedPatients_error() {
        val errorMsg = "Error message!"
        val throwable = Throwable(errorMsg)

        every { patientDAO.allPatients } returns Observable.error(throwable)

        viewModel.fetchSyncedPatients()

        val actualResult = (viewModel.result.value as Result.Error).throwable.message

        assertEquals(errorMsg, actualResult)
    }

    @Test
    fun fetchSyncedPatientsWithQuery_success() {
        val patient = patientList[0]
        val filteredPatients = listOf(patient)

        coEvery { patientRepositoryCoroutines.findPatients(patient.display!!) } returns Either.Right(
            filteredPatients
        )

        runBlocking {
            viewModel.fetchSyncedPatients(patient.display!!)
        }
        assertEquals(Result.Success(filteredPatients), viewModel.result.value)
    }

    @Test
    fun fetchSyncedPatientsWithQuery_noMatchingPatients() {
        coEvery { patientRepositoryCoroutines.findPatients("Patient99") } returns Either.Right(emptyList())

        runBlocking {
            viewModel.fetchSyncedPatients("Patient99")
        }

        val actualResult = (viewModel.result.value as Result.Success).data

        assertIterableEquals(emptyList<Patient>(), actualResult)
    }


    @Test
    fun fetchSyncedPatientsWithQuery_error() {
        val errorMsg = "Error message!"
        coEvery { patientRepositoryCoroutines.findPatients(any()) } returns Either.Left(Error(errorMsg))

        runBlocking {
            viewModel.fetchSyncedPatients(patientList[0].display!!)
        }

        val actualResult = (viewModel.result.value as Result.Error).throwable.message

        assertEquals(errorMsg, actualResult)
    }
}
