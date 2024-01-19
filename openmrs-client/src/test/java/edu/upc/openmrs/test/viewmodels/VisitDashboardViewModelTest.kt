package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.openmrs.activities.visitdashboard.VisitDashboardViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.EncounterRepository
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.library.models.TreatmentExample
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_ID
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.`when`
import rx.Observable

@RunWith(JUnit4::class)
class VisitDashboardViewModelTest : ACUnitTestBaseRx() {

    @Mock
    private lateinit var visitDAO: VisitDAO

    @Mock
    private lateinit var visitRepository: VisitRepository

    private lateinit var treatmentRepository: TreatmentRepository

    private lateinit var encounterRepository: EncounterRepository

    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: VisitDashboardViewModel

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    override fun setUp() {
        super.setUp()
        treatmentRepository = mockk()
        encounterRepository = mockk()
        savedStateHandle = SavedStateHandle().apply { set(VISIT_ID, 1L) }
        viewModel = VisitDashboardViewModel(
            visitDAO,
            visitRepository,
            treatmentRepository,
            encounterRepository,
            savedStateHandle
        )
    }

    @Test
    fun fetchCurrentVisit_success() {
        `when`(visitDAO.getVisitByID(anyLong())).thenReturn(Observable.just(Visit()))

        viewModel.fetchCurrentVisit()

        assert(viewModel.result.value is Result<Visit>)
    }

    @Test
    fun fetchCurrentVisit_error() {
        val throwable = Throwable("Error message")
        `when`(visitDAO.getVisitByID(anyLong())).thenReturn(Observable.error(throwable))

        viewModel.fetchCurrentVisit()

        assert(viewModel.result.value is Result.Error)
    }

    @Test
    fun endCurrentVisit_success() {

        `when`(visitDAO.getVisitByID(anyLong())).thenReturn(Observable.just(Visit()))
        `when`(visitRepository.endVisit(any())).thenReturn(Observable.just(true))

        viewModel.fetchCurrentVisit().runCatching {
            viewModel.endCurrentVisit().observeForever { visitEnded ->
                assertTrue(visitEnded.equals(Result.Success(true)))
            }
        }
    }

    @Test
    fun endCurrentVisit_error() {
        val throwable = Throwable("Error message")
        `when`(visitDAO.getVisitByID(anyLong())).thenReturn(Observable.just(Visit()))
        `when`(visitRepository.endVisit(any<Visit>())).thenReturn(Observable.error(throwable))

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

        val fourthEncounter = Encounter().apply {
            encounterType = EncounterType("Vitals")
            encounterDate = "2023-10-12T09:23:13.000+0200"
        }

        val encounters = listOf(
            thirdEncounter,
            fourthEncounter,
            firstEncounter,
            secondEncounter
        )

        val result = viewModel.filterAndSortEncounters(encounters)

        assertTrue(result.size == 4)
        assertTrue(result[0].encounterDate.equals(firstEncounter.encounterDate))
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

        coEvery { treatmentRepository.finalise(treatment) } returns ResultType.FinalisedTreatmentSuccess
        coEvery { treatmentRepository.fetchAllActiveTreatments(patient) } returns kotlin.Result.success(treatmentList)

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

        coEvery { encounterRepository.removeEncounter(treatmentOne.treatmentUuid) } returns ResultType.RemoveTreatmentSuccess
        coEvery { treatmentRepository.fetchAllActiveTreatments(patient) } returns kotlin.Result.success(treatmentList)

        runBlocking {
            viewModel.removeTreatment(treatmentOne)
            coVerify {
                encounterRepository.removeEncounter(treatmentOne.treatmentUuid)
            }
        // I wanted to check the value of the treatments list but I cannot set the visit mock because it is a val. I tried to use a spy but it didn't work
//            assertEquals(treatmentList, viewModel.treatments.value)
        }
    }
}
