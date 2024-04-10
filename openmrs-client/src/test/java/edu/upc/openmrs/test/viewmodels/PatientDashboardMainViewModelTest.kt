package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.blopup.model.VisitExample
import edu.upc.blopup.ui.dashboard.ActiveVisitResultUiState
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardMainViewModel
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.api.repository.NewVisitRepository
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Visit
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
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
import java.io.IOException
import java.util.UUID


@RunWith(JUnit4::class)
class PatientDashboardMainViewModelTest() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var newVisitRepository: NewVisitRepository

    @MockK(relaxed = true)
    lateinit var logger: OpenMRSLogger

    @InjectMockKs
    private lateinit var viewModel: PatientDashboardMainViewModel

    lateinit var patient: Patient

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        // Set dispatcher
        Dispatchers.setMain(UnconfinedTestDispatcher())
        MockKAnnotations.init(this)
    }

    @Test
    fun `fetch visit sets not found without active visit`() = runTest {
        val visitId = UUID.randomUUID()
        coEvery { newVisitRepository.getActiveVisit(visitId) } returns null

        viewModel.fetchActiveVisit(visitId)

        assertEquals(ActiveVisitResultUiState.NotFound, viewModel.activeVisit.value)
    }

    @Test
    fun `fetch visit sets the active visit`() = runTest {
        val visit = VisitExample.random()

        coEvery { newVisitRepository.getActiveVisit(visit.id) } returns visit

        viewModel.fetchActiveVisit(visit.id)

        assertEquals(ActiveVisitResultUiState.Success(visit), viewModel.activeVisit.value)
    }

    @Test
    fun `fetch visit sets error if network fails`() = runTest {
        val visitId = UUID.randomUUID()
        coEvery { newVisitRepository.getActiveVisit(visitId) } throws IOException()

        runBlocking {
            viewModel.fetchActiveVisit(visitId)
            assertEquals(ActiveVisitResultUiState.Error, viewModel.activeVisit.value)
        }
    }

    @Test
    fun activeVisitEndsSuccessfully() {
        val visit = Visit().apply {
            startDatetime = "2023-08-31T10:44:10.000+0000"
            id = 5
            stopDatetime = null
            uuid = "e4cc001c-884e-4cc1-b55d-30c49a48dcc5"
        }

        coEvery { newVisitRepository.endVisit(UUID.fromString(visit.uuid)) } returns true

        runBlocking {
            val actual = viewModel.endActiveVisit(UUID.fromString(visit.uuid))
            assertEquals(true, actual.value)
        }

    }
}
