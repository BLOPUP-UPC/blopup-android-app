package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import arrow.core.Either
import edu.upc.openmrs.activities.lastviewedpatients.LastViewedPatientsViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.api.repository.PatientRepositoryCoroutines
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Link
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.Results
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.AdditionalAnswers.returnsFirstArg
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.`when`
import rx.Observable


@RunWith(JUnit4::class)
class LastViewedPatientsViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var patientDAO: PatientDAO

    @Mock
    lateinit var patientRepository: PatientRepository

    private lateinit var patientRepositoryCoroutines: PatientRepositoryCoroutines

    lateinit var viewModel: LastViewedPatientsViewModel

    @BeforeEach
    override fun setUp() {
        super.setUp()
        patientRepositoryCoroutines = mockk(relaxed = true)
        viewModel =
            LastViewedPatientsViewModel(patientDAO, patientRepository, patientRepositoryCoroutines)
    }

    @Test
    fun `fetchLastViewedPatients when no patients saved & excluded`() {
        val limit = 2
        val patientList = listOf(createPatient(20L), createPatient(40L), createPatient(60L))
        val expectedList =
            if (limit > patientList.size) patientList else patientList.subList(0, limit)
        `when`(patientRepository.loadMorePatients(eq(limit), anyInt())).thenAnswer {
            val limitArg = it.arguments[0] as Int
            val startArg = it.arguments[1] as Int
            val result = Results<Patient>().apply {
                results =
                    if (limitArg > patientList.size) patientList else patientList.slice(startArg until limitArg)
            }
            return@thenAnswer Observable.just(result)
        }
        `when`(patientDAO.excludeSavedPatients(any<List<Patient>>())).then(returnsFirstArg<List<Patient>>())

        viewModel.fetchLastViewedPatients(limit)
        val actualResult = (viewModel.result.value as Result.Success).data

        assertIterableEquals(expectedList, actualResult)
    }

    @Test
    fun `fetchLastViewedPatients when one is already saved, fetch another`() {
        val limit = 1
        val listWithSavedPatient = listOf(createPatient(20L))
        val patientList = listOf(listWithSavedPatient[0], createPatient(40L), createPatient(60L))
        val expectedList = patientList.subList(1, 2)
        `when`(patientRepository.loadMorePatients(limit, 0)).thenAnswer {
            val result = Results<Patient>().apply {
                results = listWithSavedPatient
                links = listOf(Link().apply { rel = "next" })
            }
            return@thenAnswer Observable.just(result)
        }
        `when`(patientRepository.loadMorePatients(limit, 1)).thenAnswer {
            val result = Results<Patient>().apply {
                results = expectedList
                links = listOf(Link().apply { rel = "next" })
            }
            return@thenAnswer Observable.just(result)
        }
        `when`(patientDAO.excludeSavedPatients(listWithSavedPatient)).thenReturn(emptyList())
        `when`(patientDAO.excludeSavedPatients(expectedList)).thenReturn(expectedList)

        viewModel.fetchLastViewedPatients(limit)
        val actualResult = (viewModel.result.value as Result.Success).data

        assertIterableEquals(expectedList, actualResult)
    }

    @Test
    fun fetchPatients_success() {
        val patientList = listOf(createPatient(20L), createPatient(40L))

        coEvery {  patientRepositoryCoroutines.findPatients("query") } returns Either.Right(patientList)

        runBlocking { viewModel.fetchPatients("query") }

        assert(viewModel.result.value is Result.Success)
    }

    @Test
    fun fetchPatientData_error() {
        val errorMsg = "Error message!"
        val throwable = Throwable(errorMsg)
        coEvery {  patientRepositoryCoroutines.findPatients("query") } returns Either.Left(Error(throwable.message))

        runBlocking { viewModel.fetchPatients("query") }

        val actualResult = (viewModel.result.value as Result.Error).throwable.message

        assertEquals(errorMsg, actualResult)
    }
}
