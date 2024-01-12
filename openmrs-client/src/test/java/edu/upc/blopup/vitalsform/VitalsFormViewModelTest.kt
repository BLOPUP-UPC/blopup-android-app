package edu.upc.blopup.vitalsform

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.EncounterRepository
import edu.upc.sdk.library.api.repository.FormRepository
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.databases.entities.FormResourceEntity
import edu.upc.sdk.library.models.*
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import rx.Observable
import java.io.IOException
import java.util.Optional
import java.util.UUID


@RunWith(JUnit4::class)
class VitalsFormViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var patientDAO: PatientDAO

    @MockK
    lateinit var formRepository: FormRepository

    @MockK
    lateinit var encounterRepository: EncounterRepository

    @MockK
    lateinit var visitRepository: VisitRepository

    @MockK
    lateinit var treatmentRepository: TreatmentRepository

    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: VitalsFormViewModel

    private lateinit var treatmentAdherence: Map<Treatment, Boolean>

    private val patientId: Long = 88L
    private val vital = Vital("weight", "50")
    private val vitalsList = listOf(vital)
    private val testPatient = Patient().apply {
        id = patientId
        uuid = "d384d23a-a91b-11ed-afa1-0242ac120002"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    override fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        super.setUp()
        MockKAnnotations.init(this, relaxUnitFun = true)
        savedStateHandle = SavedStateHandle().apply { set(PATIENT_ID_BUNDLE, patientId) }

        treatmentAdherence = mapOf(Treatment() to true)

        every { patientDAO.findPatientByID(any()) } returns testPatient

        viewModel = VitalsFormViewModel(
            patientDAO,
            formRepository,
            visitRepository,
            encounterRepository,
            treatmentRepository,
            savedStateHandle
        )
    }

    @Test
    fun `when no open visit, a visit should be created`() {
        every { visitRepository.getActiveVisitByPatientId(patientId) } returns null
        every { visitRepository.startVisit(testPatient) } returns Observable.just(Visit())
        every { formRepository.fetchFormResourceByName("Vitals") } returns
                Observable.just(FormResourceEntity().apply {
                    uuid = "c384d23a-a91b-11ed-afa1-0242ac120003"
                })

        every { encounterRepository.saveEncounter(any()) } returns
                Observable.just(
                    Result.Success(
                        true
                    )
                )

        runBlocking {
            viewModel.submitForm(vitalsList, treatmentAdherence)

            verify { visitRepository.startVisit(testPatient) }

        }
    }

    @Test
    fun `when empty list of vitals is sent check that error with IllegalArgumentException is returned `() {

        runBlocking {
            val actualResult = viewModel.submitForm(emptyList(), treatmentAdherence)

            val actualError = actualResult.value as Result.Error

            assert(actualError.throwable is IllegalArgumentException)

            verify { encounterRepository wasNot Called }
        }
    }


    @Test
    fun `when we pass vitals, vitals are sent`() {
        every { visitRepository.getActiveVisitByPatientId(patientId) } returns Visit()
        every { formRepository.fetchFormResourceByName("Vitals") } returns
                Observable.just(FormResourceEntity().apply {
                    uuid = "c384d23a-a91b-11ed-afa1-0242ac120003"
                })

        every { encounterRepository.saveEncounter(any()) } returns
                Observable.just(
                    Result.Success(
                        true
                    )
                )

        coEvery { treatmentRepository.saveTreatmentAdherence(any(), any()) } returns kotlin.Result.success(true)

            val actualResult = viewModel.submitForm(vitalsList, treatmentAdherence)

            assertEquals(Result.Success(true), actualResult.value)

            verify { encounterRepository.saveEncounter(any()) }

    }

    @Test
    fun `when patient had previous visits, we are able to grab the already existing height value`() {
        val visitList = createVisitListWithHeightObservation()

        every { visitRepository.getLatestVisitWithHeight(patientId) } returns Optional.of(
            visitList[0]
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

        every { visitRepository.getActiveVisitByPatientId(patientId) } returns null
        every { visitRepository.startVisit(viewModel.patient) } returns Observable.just(visit)

        every { formRepository.fetchFormResourceByName("Vitals") } returns
                Observable.just(FormResourceEntity().apply {
                    uuid = "c384d23a-a91b-11ed-afa1-0242ac120003"
                })

        every { encounterRepository.saveEncounter(any()) } returns
                Observable.just(
                    Result.Error(
                        IOException()
                    )
                )


      runBlocking {    viewModel.submitForm(vitalsList, treatmentAdherence) }

        verify { visitRepository.deleteVisitByUuid(visit.uuid) }
    }

    @Test
    fun `should get active treatments from repository`() {
        val treatment = Treatment()
        val treatmentList = listOf(treatment)

        coEvery { treatmentRepository.fetchAllActiveTreatments(any()) } returns treatmentList

        runBlocking { assertEquals(treatmentList, viewModel.getActiveTreatments()) }
    }

    @Test
    fun `should send adherence data`() {
        every { visitRepository.getActiveVisitByPatientId(any()) } returns Visit()
        every { formRepository.fetchFormResourceByName("Vitals") } returns
                Observable.just(FormResourceEntity().apply {
                    uuid = "c384d23a-a91b-11ed-afa1-0242ac120003"
                })

        every { encounterRepository.saveEncounter(any()) } returns
                Observable.just(
                    Result.Success(
                        true
                    )
                )

       runBlocking {   viewModel.submitForm(vitalsList, treatmentAdherence) }

        coVerify { treatmentRepository.saveTreatmentAdherence(treatmentAdherence, any()) }
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
