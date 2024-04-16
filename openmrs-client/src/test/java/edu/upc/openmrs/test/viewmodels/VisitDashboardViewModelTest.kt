package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.blopup.model.VisitExample
import edu.upc.blopup.ui.ResultUiState
import edu.upc.openmrs.activities.visitdashboard.VisitDashboardViewModel
import edu.upc.sdk.library.api.repository.DoctorRepository
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.TreatmentExample
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.joda.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.IOException
import java.util.UUID

@RunWith(JUnit4::class)
class VisitDashboardViewModelTest {

    @MockK
    private lateinit var patientDAO: PatientDAO

    @MockK
    private lateinit var visitRepository: VisitRepository

    @MockK
    private lateinit var doctorRepository: DoctorRepository

    @MockK
    private lateinit var treatmentRepository: TreatmentRepository

    @InjectMockKs
    private lateinit var viewModel: VisitDashboardViewModel

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        MockKAnnotations.init(this)
    }

    @Test
    fun `fetch current visit fails error`() = runTest {
        val visit = VisitExample.random()
        coEvery { visitRepository.getVisitByUuid(visit.id) } throws IOException()

        viewModel.fetchCurrentVisit(visit.id)

        assertEquals(ResultUiState.Error, viewModel.visit.value)
        assertNull(viewModel.patient.value)
        assertEquals(
            Pair(
                ResultUiState.Error,
                ResultUiState.Error
            ), viewModel.treatments.first()
        )
    }

    @Test
    fun `fetch current visit and treatments fails`() = runTest {
        val patient = Patient()
        val visit = VisitExample.random()
        coEvery { visitRepository.getVisitByUuid(visit.id) } returns visit
        every { patientDAO.findPatientByUUID(visit.patientId.toString()) } returns patient
        coEvery { treatmentRepository.fetchActiveTreatmentsAtAGivenTime(visit) } returns Result.Error(IOException())

        viewModel.fetchCurrentVisit(visit.id)

        assertEquals(ResultUiState.Success(visit), viewModel.visit.value)
        assertEquals(patient, viewModel.patient.value)
        assertEquals(
            Pair(
                ResultUiState.Success(visit),
                ResultUiState.Error
            ), viewModel.treatments.first()
        )
    }

    @Test
    fun `fetch current visit and treatments`() = runTest {
        val patient = Patient()
        val visit = VisitExample.random()
        val treatment = TreatmentExample.activeTreatment()
        coEvery { visitRepository.getVisitByUuid(visit.id) } returns visit
        every { patientDAO.findPatientByUUID(visit.patientId.toString()) } returns patient
        coEvery { treatmentRepository.fetchActiveTreatmentsAtAGivenTime(visit) } returns Result.Success(
            listOf(treatment)
        )

        viewModel.fetchCurrentVisit(visit.id)

        assertEquals(ResultUiState.Success(visit), viewModel.visit.value)
        assertEquals(patient, viewModel.patient.value)
        assertEquals(
            Pair(
                ResultUiState.Success(visit),
                ResultUiState.Success(listOf(treatment))
            ), viewModel.treatments.first()
        )
    }

    @Test
    fun `should send a message to the doctor`() {
        val message = "Message"

        coEvery { doctorRepository.sendMessageToDoctor(message) } returns kotlin.Result.success(true)

        runBlocking { viewModel.sendMessageToDoctor(message) }

        coVerify { doctorRepository.sendMessageToDoctor(message) }
    }

    @Test
    fun endCurrentVisit_success() {
        val visit = VisitExample.random()

        coEvery { visitRepository.endVisit(any()) } returns true

        runBlocking {
            viewModel.endCurrentVisit(visit.id).observeForever { visitEnded ->
                assertTrue(visitEnded.equals(Result.Success(true)))
            }
        }
    }

    @Test
    fun endCurrentVisit_error() {
        val visit = VisitExample.random()
        val throwable = IOException()
        coEvery { visitRepository.endVisit(any()) } throws throwable

        runBlocking {
            viewModel.endCurrentVisit(visit.id).observeForever { visitEnded ->
                assert(visitEnded.equals(Result.Error(throwable)))
            }
        }
    }

    @Test
    fun `should mark a treatment as inactive and return the treatments list with the update`() = runTest {
        val patient = Patient()
        val treatment = TreatmentExample.activeTreatment()
        val visit = VisitExample.random(id = UUID.fromString(treatment.visitUuid))
        val treatmentUpdated = treatment.apply {
            isActive = false
            inactiveDate = Instant.now()
        }
        val treatmentList = listOf(treatmentUpdated)

        coEvery { visitRepository.getVisitByUuid(visit.id) } returns visit
        every { patientDAO.findPatientByUUID(visit.patientId.toString()) } returns patient
        coEvery { treatmentRepository.fetchActiveTreatmentsAtAGivenTime(visit) } returns Result.Success(
            listOf(treatment)
        )

        coEvery { treatmentRepository.finalise(treatment) } returns kotlin.Result.success(true)

        viewModel.fetchCurrentVisit(UUID.fromString(treatment.visitUuid))
        viewModel.finaliseTreatment(treatment)

        coVerify { treatmentRepository.finalise(treatment) }

        val pair = viewModel.treatments.first()
        assertEquals(ResultUiState.Success(treatmentList), pair.second)
    }

    @Test
    fun `should remove a treatment and return the treatments list without it`() = runTest {
        val patient = Patient()
        val treatmentOne = TreatmentExample.activeTreatment()
        val treatmentTwo = TreatmentExample.activeTreatment()
        val treatmentList = listOf(treatmentTwo)
        val visit = VisitExample.random(id = UUID.fromString(treatmentOne.visitUuid))

        coEvery { visitRepository.getVisitByUuid(UUID.fromString(treatmentOne.visitUuid)) } returns visit
        every { patientDAO.findPatientByUUID(any()) } returns patient
        coEvery { treatmentRepository.fetchActiveTreatmentsAtAGivenTime(any()) } returns Result.Success(
            listOf(treatmentOne, treatmentTwo)
        )
        coEvery { treatmentRepository.deleteTreatment(treatmentOne.treatmentUuid!!) } returns kotlin.Result.success(
            true
        )

        viewModel.fetchCurrentVisit(UUID.fromString(treatmentOne.visitUuid))
        viewModel.removeTreatment(treatmentOne)

        coVerify {
            treatmentRepository.deleteTreatment(treatmentOne.treatmentUuid!!)
        }

        val pair = viewModel.treatments.first()
        assertEquals(ResultUiState.Success(treatmentList), pair.second)
    }

    @Test
    fun `should refresh the treatments for the current visit`() = runTest {
        val visit = VisitExample.random()
        val patient = Patient().apply {
            id = 2L
            uuid = visit.patientId.toString()
        }

        coEvery { visitRepository.getVisitByUuid(visit.id) } returns visit
        every { patientDAO.findPatientByUUID(visit.patientId.toString()) } returns patient
        coEvery {
            treatmentRepository.fetchActiveTreatmentsAtAGivenTime(visit)
        } returns Result.Success(
            listOf()
        )

        viewModel.fetchCurrentVisit(visit.id)
        viewModel.refreshTreatments()
        coVerify(exactly = 2) {
            treatmentRepository.fetchActiveTreatmentsAtAGivenTime(any())
        }
    }
}
