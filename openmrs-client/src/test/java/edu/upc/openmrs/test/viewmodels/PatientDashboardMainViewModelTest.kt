package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardMainViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
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


@RunWith(JUnit4::class)
class PatientDashboardMainViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var patientDAO: PatientDAO

    @Mock
    lateinit var visitDAO: VisitDAO

    @Mock
    lateinit var visitRepository: VisitRepository

    @Mock
    lateinit var patientRepository: PatientRepository

    lateinit var savedStateHandle: SavedStateHandle

    lateinit var viewModel: PatientDashboardMainViewModel

    lateinit var patient: Patient

    lateinit var visitList: List<Visit>

    @Before
    override fun setUp() {
        super.setUp()
        patient = createPatient(PATIENT_ID)
        Mockito.`when`(patientDAO.findPatientByID(PATIENT_ID.toString())).thenReturn(patient)
        savedStateHandle = SavedStateHandle().apply { set(PATIENT_ID_BUNDLE, PATIENT_ID) }
        viewModel = PatientDashboardMainViewModel(
            patientDAO, visitDAO, patientRepository,
            visitRepository, savedStateHandle
        )
        visitList = createVisitList()
    }

    @Test
    fun updateLocalPatientWhenSyncingData() {
        val serverPatient = Patient()
        Mockito.`when`(patientRepository.downloadPatientByUuid(any()))
            .thenReturn(Observable.just(serverPatient))
        Mockito.`when`(visitRepository.syncVisitsData(any()))
            .thenReturn(Observable.just(emptyList<Visit>()))
        Mockito.`when`(visitRepository.syncLastVitals(any()))
            .thenReturn(Observable.just(null))

        viewModel.syncPatientData()

        val inOrder: InOrder = inOrder(patientDAO)
        inOrder.verify(patientDAO).findPatientByID(PATIENT_ID.toString())
        inOrder.verify(patientDAO, atLeastOnce()).updatePatient(PATIENT_ID, serverPatient)
    }

    @Test
    fun notUpdateLocalPatientWithServerWhenItsEquals() {
        Mockito.`when`(patientRepository.downloadPatientByUuid(any()))
            .thenReturn(Observable.just(patient))
        Mockito.`when`(visitRepository.syncVisitsData(any()))
            .thenReturn(Observable.just(emptyList<Visit>()))
        Mockito.`when`(visitRepository.syncLastVitals(any()))
            .thenReturn(Observable.just(null))

        viewModel.syncPatientData()

        val inOrder: InOrder = inOrder(patientDAO)
        inOrder.verify(patientDAO).findPatientByID(PATIENT_ID.toString())
        inOrder.verify(patientDAO, never()).updatePatient(PATIENT_ID, patient)
    }
    companion object {
        const val PATIENT_ID = 1L
    }
}
