package org.openmrs.mobile.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.openmrs.android_sdk.library.api.repository.AllergyRepository
import com.openmrs.android_sdk.library.api.repository.PatientRepository
import com.openmrs.android_sdk.library.api.repository.VisitRepository
import com.openmrs.android_sdk.library.dao.PatientDAO
import com.openmrs.android_sdk.library.dao.VisitDAO
import com.openmrs.android_sdk.library.models.Allergy
import com.openmrs.android_sdk.library.models.Patient
import com.openmrs.android_sdk.library.models.Visit
import com.openmrs.android_sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
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
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardMainViewModel
import org.openmrs.mobile.test.ACUnitTestBaseRx
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

    @Mock
    lateinit var allergyRepository: AllergyRepository

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
            visitRepository, allergyRepository, savedStateHandle
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
        Mockito.`when`(allergyRepository.syncAllergies(any()))
            .thenReturn(Observable.just(emptyList<Allergy>()))
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
        Mockito.`when`(allergyRepository.syncAllergies(any()))
            .thenReturn(Observable.just(emptyList<Allergy>()))
        Mockito.`when`(visitRepository.syncLastVitals(any()))
            .thenReturn(Observable.just(null))

        viewModel.syncPatientData()

        val inOrder: InOrder = inOrder(patientDAO)
        inOrder.verify(patientDAO).findPatientByID(PATIENT_ID.toString())
        inOrder.verify(patientDAO, never()).updatePatient(PATIENT_ID, patient)
    }

    // TODO to be removed when implementing card #145
    @Test
    fun returnTrueWhenPatientIsDeletedInServer() {
        patient.isVoided = true
        Mockito.`when`(patientRepository.downloadPatientByUuid(any()))
            .thenReturn(Observable.just(patient))
        Mockito.doNothing().`when`(patientDAO).deletePatient(PATIENT_ID)
        Mockito.`when`(visitDAO.deleteVisitPatient(patient))
            .thenReturn(Observable.just(true))

        val result = viewModel.deleteLocalPatientIfDeletedInServer()

        assert(result)
    }

    // TODO to be removed when implementing card #145
    @Test
    fun returnFalseWhenPatientIsDeletedInServer() {
        patient.isVoided = false
        Mockito.`when`(patientRepository.downloadPatientByUuid(any()))
            .thenReturn(Observable.just(patient))
        Mockito.doNothing().`when`(patientDAO).deletePatient(PATIENT_ID)
        Mockito.`when`(visitDAO.deleteVisitPatient(patient))
            .thenReturn(Observable.just(true))

        val result = viewModel.deleteLocalPatientIfDeletedInServer()

        assert(!result)
    }

    companion object {
        const val PATIENT_ID = 1L
    }
}
