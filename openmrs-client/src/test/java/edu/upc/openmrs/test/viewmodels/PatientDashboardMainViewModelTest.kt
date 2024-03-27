package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardMainViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.NewVisitRepository
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.InOrder
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.inOrder
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.never
import rx.Observable
import java.util.UUID


@RunWith(JUnit4::class)
class PatientDashboardMainViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var patientDAO: PatientDAO

    @MockK
    lateinit var visitDAO: VisitDAO

    @MockK
    lateinit var visitRepository: VisitRepository

    @MockK
    lateinit var patientRepository: PatientRepository

    @MockK
    lateinit var newVisitRepository: NewVisitRepository

    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: PatientDashboardMainViewModel

    lateinit var patient: Patient

    private lateinit var visitList: List<Visit>

    @Before
    override fun setUp() {
        super.setUp()
        MockKAnnotations.init(this)
        patient = createPatient(PATIENT_ID)
        every { patientDAO.findPatientByID(PATIENT_ID.toString()) } returns patient

        savedStateHandle = SavedStateHandle().apply { set(PATIENT_ID_BUNDLE, PATIENT_ID) }
        viewModel = PatientDashboardMainViewModel(
            patientDAO, visitDAO, patientRepository,
            visitRepository, newVisitRepository, savedStateHandle
        )
        visitList = createVisitList()
    }

    @Test
    fun activeVisitEndsSuccessfully() {

        val patient = Patient().apply {
            id = PATIENT_ID
            uuid = "d384d23a-a91b-11ed-afa1-0242ac120002"
        }

        val visit = Visit().apply {
            startDatetime = "2023-08-31T10:44:10.000+0000"
            id = 5
            stopDatetime = null
            uuid = "e4cc001c-884e-4cc1-b55d-30c49a48dcc5"
        }

        every { visitDAO.getActiveVisitByPatientId(patient.id) } returns Observable.just(visit)
        coEvery { newVisitRepository.endVisit(UUID.fromString(visit.uuid)) } returns true

        runBlocking {
            val actual = viewModel.endActiveVisit()
            assertEquals(true, actual.value)
        }

    }

    @Test
    fun updateLocalPatientWhenSyncingData() {
        val serverPatient = Patient()
        every { patientRepository.downloadPatientByUuid(any()) } returns Observable.just(
            serverPatient
        )
        every { visitRepository.syncVisitsData(any()) } returns Observable.just(emptyList<Visit>())
        every { visitRepository.syncLastVitals(any()) } returns Observable.just(null)

        viewModel.syncPatientData()

        verify(Ordering.ORDERED) {
            patientDAO.findPatientByID(PATIENT_ID.toString())
            patientDAO.updatePatient(PATIENT_ID, serverPatient)
        }
    }

    @Test
    fun notUpdateLocalPatientWithServerWhenItsEquals() {
        every { patientRepository.downloadPatientByUuid(any()) } returns Observable.just(patient)
        every { visitRepository.syncVisitsData(any()) } returns Observable.just(emptyList<Visit>())
        every { visitRepository.syncLastVitals(any()) } returns Observable.just(null)

        viewModel.syncPatientData()

        verify { patientDAO.findPatientByID(PATIENT_ID.toString()) }
        verify(exactly = 0) { patientDAO.updatePatient(PATIENT_ID, patient) }
    }

    companion object {
        const val PATIENT_ID = 1L
    }
}
