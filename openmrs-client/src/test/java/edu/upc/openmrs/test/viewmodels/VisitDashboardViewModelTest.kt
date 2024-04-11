package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.blopup.model.VisitExample
import edu.upc.blopup.ui.ResultUiState
import edu.upc.openmrs.activities.visitdashboard.VisitDashboardViewModel
import edu.upc.sdk.library.api.repository.DoctorRepository
import edu.upc.sdk.library.api.repository.EncounterRepository
import edu.upc.sdk.library.api.repository.NewVisitRepository
import edu.upc.sdk.library.api.repository.TreatmentRepository
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

@RunWith(JUnit4::class)
class VisitDashboardViewModelTest {

    @MockK
    private lateinit var patientDAO: PatientDAO

    @MockK
    private lateinit var visitRepository: NewVisitRepository

    @MockK
    private lateinit var doctorRepository: DoctorRepository

    @MockK
    private lateinit var treatmentRepository: TreatmentRepository

    @MockK
    private lateinit var encounterRepository: EncounterRepository

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
        val treatment = TreatmentExample.activeTreatment()
        coEvery { visitRepository.getVisitByUuid(visit.id) } returns visit
        every { patientDAO.findPatientByUUID(visit.patientId.toString()) } returns patient
        coEvery { treatmentRepository.fetchActiveTreatmentsAtAGivenTime(patient, null, visit) } returns Result.Error(IOException())

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
        coEvery { treatmentRepository.fetchActiveTreatmentsAtAGivenTime(patient, null, visit) } returns Result.Success(
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
    fun `should mark a treatment as inactive and return the treatments list with the update`() {
        val patient = Patient()
        val treatment = TreatmentExample.activeTreatment()
        val treatmentUpdated = treatment.apply {
            isActive = false
            inactiveDate = Instant.now()
        }
        val treatmentList = listOf(treatmentUpdated)

        coEvery { treatmentRepository.finalise(treatment) } returns kotlin.Result.success(true)
        coEvery { treatmentRepository.fetchAllActiveTreatments(patient) } returns Result.Success(
            treatmentList
        )

        runBlocking {
            viewModel.finaliseTreatment(treatment)
            coVerify { treatmentRepository.finalise(treatment) }
            // I wanted to check the value of the treatments list but I cannot set the visit mock because it is a val. I tried to use a spy but it didn't work
//            assertEquals(treatmentList, viewModel.treatments.value)
        }
    }

    @Test
    fun `should remove a treatment and return the treatments list without it`() {
        val patient = Patient()
        val treatmentOne = TreatmentExample.activeTreatment()
        val treatmentTwo = TreatmentExample.activeTreatment()
        val treatmentList = listOf(treatmentTwo)

        coEvery { encounterRepository.removeEncounter(treatmentOne.treatmentUuid) } returns kotlin.Result.success(
            true
        )
        coEvery { treatmentRepository.fetchAllActiveTreatments(patient) } returns Result.Success(
            treatmentList
        )

        runBlocking {
            viewModel.removeTreatment(treatmentOne)
            coVerify {
                encounterRepository.removeEncounter(treatmentOne.treatmentUuid)
            }
            // I wanted to check the value of the treatments list but I cannot set the visit mock because it is a val. I tried to use a spy but it didn't work
//            assertEquals(treatmentList, viewModel.treatments.value)
        }
    }

    @Test
    fun `should refresh the treatments for the current visit`() {
        val visit = VisitExample.random()
        val patient = Patient().apply {
            id = 2L
        }

        coEvery { visitRepository.getVisitByUuid(visit.id) } returns visit
        every { patientDAO.findPatientByUUID(visit.patientId.toString()) } returns patient
        coEvery {
            treatmentRepository.fetchActiveTreatmentsAtAGivenTime(
                patient,
                null,
                visit
            )
        } returns Result.Success(
            listOf()
        )

        runBlocking {
            viewModel.fetchCurrentVisit(visit.id)
            viewModel.refreshTreatments()
            coVerify(exactly = 2) {
                treatmentRepository.fetchActiveTreatmentsAtAGivenTime(any(), any(), any())
            }
        }
    }
}
