package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.openmrs.activities.visitdashboard.VisitDashboardViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_ID
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

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var visitDAO: VisitDAO

    @Mock
    lateinit var visitRepository: VisitRepository

    @Mock
    lateinit var treatmentRepository: TreatmentRepository

    private lateinit var savedStateHandle: SavedStateHandle

    lateinit var viewModel: VisitDashboardViewModel

    @Before
    override fun setUp() {
        super.setUp()
        savedStateHandle = SavedStateHandle().apply { set(VISIT_ID, 1L) }
        viewModel = VisitDashboardViewModel(visitDAO, visitRepository, treatmentRepository, savedStateHandle)
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
}
