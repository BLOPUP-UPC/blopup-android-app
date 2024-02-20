package edu.upc.blopup.ui.takingvitals

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.BloodPressureViewState
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.Measurement
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.ReadBloodPressureRepository
import edu.upc.blopup.scale.readScaleMeasurement.ReadScaleRepository
import edu.upc.blopup.scale.readScaleMeasurement.ScaleViewState
import edu.upc.blopup.scale.readScaleMeasurement.WeightMeasurement
import edu.upc.blopup.vitalsform.Vital
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.Observation
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.TreatmentExample
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.library.models.VisitExample
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.HEART_RATE_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.Observable
import java.util.Optional

@RunWith(org.mockito.junit.MockitoJUnitRunner::class)
class VitalsViewModelTest {

    @InjectMockKs
    private lateinit var viewModel: VitalsViewModel

    @MockK
    private lateinit var readBloodPressureRepository: ReadBloodPressureRepository

    @MockK
    private lateinit var readScaleRepository: ReadScaleRepository

    @MockK
    private lateinit var treatmentRepository: TreatmentRepository

    @MockK(relaxUnitFun = true)
    private lateinit var visitRepository: VisitRepository

    @MockK
    private lateinit var patientDAO: PatientDAO

    private val patientId = 1L

    private var savedStateHandle: SavedStateHandle = SavedStateHandle().apply {
        set(
            ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, patientId
        )
    }

    private lateinit var testPatient: Patient

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)


        testPatient = Patient().apply {
            id = patientId
            uuid = "patientUuid"
        }
    }

    @Test
    fun `should receive Blood Pressure data`() = runTest {
        val measurements = BloodPressureViewState.Content(
            Measurement(
                120,
                80,
                70
            )
        )

        every {
            readBloodPressureRepository.start(
                captureLambda(),
                captureLambda()
            )
        } answers {
            val bloodPressureCallback = secondArg<(BloodPressureViewState) -> Unit>()
            bloodPressureCallback(measurements)
        }

        every {
            readBloodPressureRepository.disconnect()
        } answers {}

        viewModel.receiveBloodPressureData()

        val expectedResult = mutableListOf(
            Vital(SYSTOLIC_FIELD_CONCEPT, "120"),
            Vital(DIASTOLIC_FIELD_CONCEPT, "80"),
            Vital(HEART_RATE_FIELD_CONCEPT, "70")
        )

        val result = viewModel.vitalsUiState.first()

        assertEquals(expectedResult, result)

        verify { readBloodPressureRepository.disconnect() }
    }

    @Test
    fun `should receive Weight data`() = runTest {
        val weightMeasurement = ScaleViewState.Content(
            WeightMeasurement(
                70f
            )
        )

        every {
            readScaleRepository.start(
                captureLambda()
            )
        } answers {
            val weightCallback = firstArg<(ScaleViewState) -> Unit>()
            weightCallback(weightMeasurement)
        }

        every {
            readScaleRepository.disconnect()
        } answers {}

        viewModel.receiveWeightData()

        val expectedResult = mutableListOf(
            Vital(WEIGHT_FIELD_CONCEPT, "70.0")
        )

        val result = viewModel.vitalsUiState.first()

        assertEquals(expectedResult, result)
    }

    @Test
    fun `should get latest height data when present`() = runTest {
        val height = "170"
        val visit = VisitExample.random().apply {
            encounters = listOf(
                Encounter().apply {
                    observations = listOf(
                        Observation().apply {
                            display = "Height: $height"
                            displayValue = "$height.0"
                        }
                    )
                }
            )
        }

        every {
            visitRepository.getLatestVisitWithHeight(patientId)
        } returns Optional.of(visit)

        val result = viewModel.getLastHeightFromVisits()

        assertEquals(height, result)
    }

    @Test
    fun `should get empty height data when visit has no height`() = runTest {
        val visit = VisitExample.random()

        every {
            visitRepository.getLatestVisitWithHeight(patientId)
        } returns Optional.of(visit)

        val result = viewModel.getLastHeightFromVisits()

        assertEquals("", result)
    }

    @Test
    fun `should get empty height data without visit`() = runTest {
        every {
            visitRepository.getLatestVisitWithHeight(patientId)
        } returns Optional.empty()

        val result = viewModel.getLastHeightFromVisits()

        assertEquals("", result)
    }

    @Test
    fun `should create new visit and add vitals information`() {
        val visit = Visit().apply {
            uuid = "visitUuid"
            patient = testPatient
        }
        viewModel.saveHeight("170")

        every { patientDAO.findPatientByID(patientId.toString()) } returns testPatient
        every { visitRepository.startVisit(testPatient) } returns Observable.just(visit)
        every {
                visitRepository.createVisitWithVitals(testPatient, viewModel.vitalsUiState.value)
              } returns Observable.just(Result.Success(true))

        viewModel.createVisit()

        verify {
            visitRepository.createVisitWithVitals(
                testPatient,
                viewModel.vitalsUiState.value
            )
        }
    }

    @Test
    fun `should get active treatments from repository`() {
        val treatment = TreatmentExample.activeTreatment().apply { treatmentUuid = "treatmentUuid" }
        val treatmentList = listOf(treatment)

        every { patientDAO.findPatientByID(patientId.toString()) } returns testPatient

        coEvery { treatmentRepository.fetchAllActiveTreatments(any()) } returns kotlin.Result.success(treatmentList)


        runBlocking {
            val result = viewModel.fetchActiveTreatment()

            assertEquals(treatmentList, result)
        }
    }

}
