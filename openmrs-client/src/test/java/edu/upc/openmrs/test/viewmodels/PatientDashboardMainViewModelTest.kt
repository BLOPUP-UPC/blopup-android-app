package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.blopup.model.VisitExample
import edu.upc.blopup.ui.dashboard.ActiveVisitResultUiState
import edu.upc.openmrs.activities.patientdashboard.PatientViewModel
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.models.Patient
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
    lateinit var visitRepository: VisitRepository

    @MockK(relaxed = true)
    lateinit var logger: OpenMRSLogger

    @InjectMockKs
    private lateinit var viewModel: PatientViewModel

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
        coEvery { visitRepository.getActiveVisit(visitId) } returns null

        viewModel.fetchActiveVisit(visitId)

        assertEquals(ActiveVisitResultUiState.NotFound, viewModel.activeVisit.value)
    }

    @Test
    fun `fetch visit sets the active visit`() = runTest {
        val visit = VisitExample.random()

        coEvery { visitRepository.getActiveVisit(visit.id) } returns visit

        viewModel.fetchActiveVisit(visit.id)

        assertEquals(ActiveVisitResultUiState.Success(visit), viewModel.activeVisit.value)
    }

    @Test
    fun `fetch visit sets error if network fails`() = runTest {
        val visitId = UUID.randomUUID()
        coEvery { visitRepository.getActiveVisit(visitId) } throws IOException()

        runBlocking {
            viewModel.fetchActiveVisit(visitId)
            assertEquals(ActiveVisitResultUiState.Error, viewModel.activeVisit.value)
        }
    }

    @Test
    fun activeVisitEndsSuccessfully() {
        val visit = VisitExample.random()

        coEvery { visitRepository.endVisit(visit.id) } returns true

        runBlocking {
            val actual = viewModel.endActiveVisit(visit.id)
            assertEquals(true, actual.value)
        }

    }
}
