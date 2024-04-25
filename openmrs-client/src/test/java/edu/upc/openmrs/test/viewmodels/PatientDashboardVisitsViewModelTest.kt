package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.blopup.model.VisitExample
import edu.upc.openmrs.activities.patientdashboard.visits.PatientVisitsViewModel
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.IOException
import java.util.UUID

@RunWith(JUnit4::class)
class PatientDashboardVisitsViewModelTest() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var visitRepository: VisitRepository

    @InjectMockKs
    private lateinit var viewModel: PatientVisitsViewModel

    lateinit var patient: Patient

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        MockKAnnotations.init(this)
    }

    @Test
    fun `fetch visits should return list of visits by patient uuid`() = runTest {
        val patientUuid = UUID.randomUUID()
        val visits = listOf(
            VisitExample.random(), VisitExample.random()
        )

        coEvery { visitRepository.getVisitsByPatientUuid(patientUuid) } returns visits

        viewModel.fetchVisitsData(patientUuid)

        val actualResult = (viewModel.result.value as Result.Success).data
        assertIterableEquals(visits, actualResult)
    }

    @Test
    fun `fetch visits should return error if repository fails`() = runTest {
        val patientUuid = UUID.randomUUID()
        val exception = IOException()

        coEvery { visitRepository.getVisitsByPatientUuid(patientUuid) } throws exception

        viewModel.fetchVisitsData(patientUuid)

        assertEquals(Result.Error(exception), viewModel.result.value)
    }
}
