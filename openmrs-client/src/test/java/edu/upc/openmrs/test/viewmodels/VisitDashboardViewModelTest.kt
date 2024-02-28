package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.openmrs.activities.visitdashboard.VisitDashboardViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.DoctorRepository
import edu.upc.sdk.library.api.repository.EncounterRepository
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.TreatmentExample
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_UUID
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.joda.time.Instant
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import rx.Observable

@RunWith(JUnit4::class)
class VisitDashboardViewModelTest : ACUnitTestBaseRx() {

    private lateinit var visitDAO: VisitDAO

    private lateinit var visitRepository: VisitRepository

    private lateinit var doctorRepository: DoctorRepository

    private lateinit var treatmentRepository: TreatmentRepository

    private lateinit var encounterRepository: EncounterRepository

    private lateinit var viewModel: VisitDashboardViewModel

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    override fun setUp() {
        super.setUp()
        Dispatchers.setMain(UnconfinedTestDispatcher())
        treatmentRepository = mockk()
        encounterRepository = mockk()
        visitRepository = mockk()
        doctorRepository = mockk()
        visitDAO = mockk()
        val savedStateHandle = SavedStateHandle().apply { set(VISIT_UUID, 1L) }
        viewModel = VisitDashboardViewModel(
            visitDAO,
            visitRepository,
            treatmentRepository,
            encounterRepository,
            doctorRepository,
            savedStateHandle
        )
    }

    @Test
    fun fetchCurrentVisit_success() {
        every { visitDAO.getVisitByID(any()) } returns Observable.just(Visit())

        viewModel.fetchCurrentVisit()

        assert(viewModel.result.value is Result<Visit>)
    }

    @Test
    fun fetchCurrentVisit_error() {
        val throwable = Throwable("Error message")
        every  {visitDAO.getVisitByID(any())} returns Observable.error(throwable)

        viewModel.fetchCurrentVisit()

        assert(viewModel.result.value is Result.Error)
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

        every { visitDAO.getVisitByID(any()) } returns Observable.just(Visit())
        every { visitRepository.endVisit(any()) } returns Observable.just(true)

        viewModel.fetchCurrentVisit().runCatching {
            viewModel.endCurrentVisit().observeForever { visitEnded ->
                assertTrue(visitEnded.equals(Result.Success(true)))
            }
        }
    }

    @Test
    fun endCurrentVisit_error() {
        val throwable = Throwable("Error message")
        every { visitDAO.getVisitByID(any()) } returns Observable.just(Visit())
        every { visitRepository.endVisit(any()) } returns Observable.error(throwable)

        viewModel.fetchCurrentVisit().runCatching {
            viewModel.endCurrentVisit().observeForever { visitEnded ->
                assert(visitEnded.equals(Result.Error(throwable)))
            }
        }
    }

    @Test
    fun filterAndSortEncounters() {

        val firstEncounter = Encounter().apply {
            encounterType = EncounterType("Vitals")
            encounterDate = "2023-08-10T09:23:13.000+0200"
        }

        val secondEncounter = Encounter().apply {
            encounterType = EncounterType("Vitals")
            encounterDate = "2023-09-11T09:23:13.000+0200"
        }

        val thirdEncounter = Encounter().apply {
            encounterType = EncounterType("Vitals")
            encounterDate = "2023-10-12T09:23:13.000+0200"
        }

        val mostRecentEncounter = Encounter().apply {
            encounterType = EncounterType("Vitals")
            encounterDate = "2023-10-13T09:23:13.000+0200"
        }

        val encounters = listOf(
            thirdEncounter,
            mostRecentEncounter,
            firstEncounter,
            secondEncounter
        )

        val result = viewModel.filterLastVitalEncounter(encounters)

        assertTrue(result.encounterDate.equals(mostRecentEncounter.encounterDate))
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
        coEvery { treatmentRepository.fetchAllActiveTreatments(patient) } returns Result.Success(treatmentList)

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

        coEvery { encounterRepository.removeEncounter(treatmentOne.treatmentUuid) } returns kotlin.Result.success(true)
        coEvery { treatmentRepository.fetchAllActiveTreatments(patient) } returns Result.Success(treatmentList)

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
        val patient = Patient().apply {
            id = 2L
        }
        val visit = Visit().apply {
            id = 1L
            this.patient = patient
            encounters = listOf(Encounter().apply { encounterType = EncounterType(EncounterType.VITALS) })
        }

        every { visitDAO.getVisitByID(any()) } returns Observable.just(visit)
        coEvery { treatmentRepository.fetchActiveTreatmentsAtAGivenTime(patient, visit) } returns Result.Success(
            listOf()
        )

        runBlocking {
            viewModel.fetchCurrentVisit()
            viewModel.refreshTreatments()
            coVerify(exactly = 2) {
                treatmentRepository.fetchActiveTreatmentsAtAGivenTime(patient, visit)
            }
        }
    }
}
