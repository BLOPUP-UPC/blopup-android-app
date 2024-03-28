package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.openmrs.activities.patientdashboard.visits.PatientDashboardVisitsViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.NewVisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.library.models.typeConverters.VisitConverter
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import rx.Observable

@RunWith(JUnit4::class)
class PatientDashboardVisitsViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var patientDAO: PatientDAO

    @MockK
    lateinit var visitDAO: VisitDAO

    @MockK
    lateinit var newVisitRepository: NewVisitRepository

    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: PatientDashboardVisitsViewModel

    lateinit var patient: Patient

    private lateinit var visitList: List<Visit>

    @Before
    override fun setUp() {
        super.setUp()
        MockKAnnotations.init(this)
        savedStateHandle = SavedStateHandle().apply { set(PATIENT_ID_BUNDLE, PATIENT_ID) }
        viewModel =
            PatientDashboardVisitsViewModel(
                patientDAO,
                visitDAO,
                newVisitRepository,
                savedStateHandle
            )
        patient = createPatient(PATIENT_ID.toLong())
        visitList = createVisitList()
    }

    @Test
    fun fetchVisitsData_success() {
        every { visitDAO.getVisitsByPatientID(PATIENT_ID.toLong()) } returns Observable.just(
            visitList
        )

        viewModel.fetchVisitsData()

        val parsedVisits = visitList.map { VisitConverter.createVisitFromOpenMRSVisit(it) }

        val actualResult = (viewModel.result.value as Result.Success).data

        assertIterableEquals(parsedVisits, actualResult)
    }

    @Test
    fun fetchVisitsData_error() {
        val errorMsg = "Error message!"
        val throwable = Throwable(errorMsg)
        every { visitDAO.getVisitsByPatientID(PATIENT_ID.toLong()) } returns Observable.error(
            throwable
        )

        viewModel.fetchVisitsData()

        val actualResult = (viewModel.result.value as Result.Error).throwable.message

        assertEquals(errorMsg, actualResult)
    }

    @Test
    fun hasActiveVisit_shouldBeTrue() {
        val visit = visitList[0]
        every { visitDAO.getActiveVisitByPatientId(PATIENT_ID.toLong()) } returns Observable.just(
            visit
        )

        viewModel.hasActiveVisit().observeForever { hasActiveVisit -> assertTrue(hasActiveVisit) }
    }

    @Test
    fun hasActiveVisit_shouldBeFalse() {
        val visit = null
        every { visitDAO.getActiveVisitByPatientId(PATIENT_ID.toLong()) } returns Observable.just(
            visit
        )

        viewModel.hasActiveVisit().observeForever { hasActiveVisit -> assertFalse(hasActiveVisit) }
    }

    @Test
    fun startVisit_success() {
        val visit = VisitConverter.createVisitFromOpenMRSVisit(visitList[0])

        coEvery { patientDAO.findPatientByID(PATIENT_ID) } returns patient
        coEvery { newVisitRepository.startVisit(patient) } returns visit

        runBlocking { viewModel.startVisit() }

        val actualResult = (viewModel.result.value as Result.Success).data[0]

        assertEquals(visit, actualResult)
    }

    companion object {
        const val PATIENT_ID = "1"
    }
}
