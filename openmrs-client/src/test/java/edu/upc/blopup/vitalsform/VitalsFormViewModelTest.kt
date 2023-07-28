package edu.upc.blopup.vitalsform

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.EncounterRepository
import edu.upc.sdk.library.api.repository.FormRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.dao.VisitDAO
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
    lateinit var visitDAO: VisitDAO

    @Mock
    lateinit var savedStateHandle: SavedStateHandle

    lateinit var subjectUnderTest: VitalsFormViewModel

    private val DEFAULT_PATIENT_ID: Long = 88L
    private val vital = Vital("weight", "50")
    private val testVitalForm = listOf(vital)
    private val testPatient = Patient().apply {
        id = DEFAULT_PATIENT_ID
        uuid = "d384d23a-a91b-11ed-afa1-0242ac120002"
    }

    @Before
    override fun setUp() {
        super.setUp()
        savedStateHandle = SavedStateHandle().apply { set(PATIENT_ID_BUNDLE, DEFAULT_PATIENT_ID) }

        `when`(patientDAO.findPatientByID(ArgumentMatchers.anyString())).thenReturn(testPatient)

        subjectUnderTest = VitalsFormViewModel(
            patientDAO,
            formRepository,
            encounterRepository,
            visitDAO,
            savedStateHandle
        )
    }

    @Test
    fun `when empty list of vitals is sent check that encounterError is returned `() {

        val actualResult = subjectUnderTest.submitForm(emptyList())

        assertEquals(ResultType.EncounterSubmissionError, actualResult.value)

        verify(encounterRepository, never()).saveEncounter(any())
    }

    @Test
    fun `when we pass vitals, vitals are sent`() {

        `when`(formRepository.fetchFormResourceByName("Vitals")).thenReturn(
            Observable.just(FormResourceEntity().apply {
                uuid = "c384d23a-a91b-11ed-afa1-0242ac120003"
            })
        )
        `when`(encounterRepository.saveEncounter(any())).thenReturn(Observable.just(ResultType.EncounterSubmissionSuccess))

        val actualResult = subjectUnderTest.submitForm(testVitalForm)

        assertEquals(ResultType.EncounterSubmissionSuccess, actualResult.value)

        verify(encounterRepository).saveEncounter(any())
    }


    @Test
    fun `when patient had previous visits, we are able to grab the already existing height value`() {

        val visitList = createVisitListWithHeightObservation()

        `when`(visitDAO.getVisitsByPatientID(DEFAULT_PATIENT_ID)).thenReturn(
            Observable.just(visitList)
        )

        val actualResult = subjectUnderTest.getLastHeightFromVisits()

        val expectedResult = "190"

        if (actualResult is Result.Success<*>) {
            assertEquals(expectedResult, actualResult.data)
        }
    }

    @Test
    fun `when patient had previous visits, we are able to grab the last height value`() {

        val visitList = createVisitListWithHeightObservation()

        `when`(visitDAO.getVisitsByPatientID(DEFAULT_PATIENT_ID)).thenReturn(
            Observable.just(visitList)
        )

        val actualResult = subjectUnderTest.getLastHeightFromVisits()

        val expectedResult = "180"

        if (actualResult is Result.Success<*>) {
            assertEquals(expectedResult, actualResult.data)
        }
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

