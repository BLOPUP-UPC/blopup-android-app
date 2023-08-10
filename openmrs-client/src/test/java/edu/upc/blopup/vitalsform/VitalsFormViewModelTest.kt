package edu.upc.blopup.vitalsform

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.EncounterRepository
import edu.upc.sdk.library.api.repository.FormRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.databases.entities.FormResourceEntity
import edu.upc.sdk.library.models.*
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import rx.Observable
import java.util.Optional
import java.util.UUID


@RunWith(JUnit4::class)
class VitalsFormViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var patientDAO: PatientDAO

    @Mock
    lateinit var formRepository: FormRepository

    @Mock
    lateinit var encounterRepository: EncounterRepository

    @Mock
    lateinit var visitRepository: VisitRepository

    @Mock
    lateinit var savedStateHandle: SavedStateHandle

    lateinit var viewModel: VitalsFormViewModel

    private val patientId: Long = 88L
    private val vital = Vital("weight", "50")
    private val vitalsList = listOf(vital)
    private val testPatient = Patient().apply {
        id = patientId
        uuid = "d384d23a-a91b-11ed-afa1-0242ac120002"
    }

    @Before
    override fun setUp() {
        super.setUp()
        savedStateHandle = SavedStateHandle().apply { set(PATIENT_ID_BUNDLE, patientId) }

        `when`(patientDAO.findPatientByID(ArgumentMatchers.anyString())).thenReturn(testPatient)

        viewModel = VitalsFormViewModel(
            patientDAO,
            formRepository,
            visitRepository,
            encounterRepository,
            savedStateHandle
        )
    }

    @Test
    fun `when no open visit, a visit should be created`(){
        `when`(visitRepository.getActiveVisitByPatientId(patientId)).thenReturn(null)
        `when`(visitRepository.startVisit(testPatient)).thenReturn(Observable.just(Visit()))
        `when`(formRepository.fetchFormResourceByName("Vitals")).thenReturn(
            Observable.just(FormResourceEntity().apply {
                uuid = "c384d23a-a91b-11ed-afa1-0242ac120003"
            })
        )
        `when`(encounterRepository.saveEncounter(any())).thenReturn(Observable.just(ResultType.EncounterSubmissionSuccess))

        viewModel.submitForm(vitalsList)

        verify(visitRepository).startVisit(testPatient)
    }

    @Test
    fun `when empty list of vitals is sent check that encounterError is returned `() {

        val actualResult = viewModel.submitForm(emptyList())

        assertEquals(ResultType.EncounterSubmissionError, actualResult.value)

        verify(encounterRepository, never()).saveEncounter(any())
    }


    @Test
    fun `when we pass vitals, vitals are sent`() {
        `when`(visitRepository.getActiveVisitByPatientId(patientId)).thenReturn(Visit())
        `when`(formRepository.fetchFormResourceByName("Vitals")).thenReturn(
            Observable.just(FormResourceEntity().apply {
                uuid = "c384d23a-a91b-11ed-afa1-0242ac120003"
            })
        )
        `when`(encounterRepository.saveEncounter(any())).thenReturn(Observable.just(ResultType.EncounterSubmissionSuccess))

        val actualResult = viewModel.submitForm(vitalsList)

        assertEquals(ResultType.EncounterSubmissionSuccess, actualResult.value)

        verify(encounterRepository).saveEncounter(any())
    }

    @Test
    fun `when patient had previous visits, we are able to grab the already existing height value`() {
        val visitList = createVisitListWithHeightObservation()

        `when`(visitRepository.getLatestVisitWithHeight(patientId)).thenReturn(
            Optional.of(visitList[0])
        )

        val actualResult = viewModel.getLastHeightFromVisits().value as Result.Success<*>

        val expectedResult = "180"

        assertEquals(expectedResult, actualResult.data)
    }

    @Test
    fun `when no previous visit and error submitting vitals, no new visit is created`() {
        val visit = Visit().apply {
            uuid = UUID.randomUUID().toString()
        }

        `when`(visitRepository.getActiveVisitByPatientId(patientId)).thenReturn(null)
        `when`(visitRepository.startVisit(viewModel.patient)).thenReturn(Observable.just(visit))

        `when`(formRepository.fetchFormResourceByName("Vitals")).thenReturn(
            Observable.just(FormResourceEntity().apply {
                uuid = "c384d23a-a91b-11ed-afa1-0242ac120003"
            })
        )
        `when`(encounterRepository.saveEncounter(any())).thenReturn(Observable.just(ResultType.EncounterSubmissionError))

        viewModel.submitForm(vitalsList)

        verify(visitRepository).deleteVisitByUuid(visit.uuid!!)
    }

    private fun createVisitListWithHeightObservation(): List<Visit> {
        val observationOne = Observation().apply {
            display = "Height: 190"
            displayValue = "190.0"
        }
        val observationTwo = Observation().apply {
            display = "Height: 180"
            displayValue = "180.0"
        }
        val encounterOne = Encounter().apply {
            encounterDate = "2023-07-27T09:28:04.000+0200"
            observations = listOf(observationOne)
        }
        val encounterTwo = Encounter().apply {
            encounterDate = "2023-07-27T09:38:38.000+0200"
            observations = listOf(observationTwo)
        }
        val visit = Visit().apply {
            encounters = listOf(encounterOne, encounterTwo)
        }
        return listOf(visit)
    }
}
