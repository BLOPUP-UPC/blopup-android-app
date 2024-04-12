package edu.upc.blopup.ui.takingvitals

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import edu.upc.blopup.CheckTreatment
import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.MedicationType
import edu.upc.blopup.model.VisitExample
import edu.upc.blopup.toggles.BuildConfigWrapper
import edu.upc.blopup.ui.ResultUiState
import edu.upc.blopup.ui.takingvitals.components.LatestHeightResultUiState
import edu.upc.sdk.library.api.repository.BloodPressureViewState
import edu.upc.sdk.library.api.repository.BluetoothConnectionException
import edu.upc.sdk.library.api.repository.Measurement
import edu.upc.sdk.library.api.repository.NewVisitRepository
import edu.upc.sdk.library.api.repository.ReadBloodPressureRepository
import edu.upc.sdk.library.api.repository.ReadScaleRepository
import edu.upc.sdk.library.api.repository.ScaleViewState
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.api.repository.WeightMeasurement
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.TreatmentExample
import edu.upc.sdk.utilities.ApplicationConstants
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

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

    @MockK
    private lateinit var newVisitRepository: NewVisitRepository

    @MockK
    private lateinit var patientDAO: PatientDAO

    private val patientId = 1L
    private val patientUuid = UUID.randomUUID()

    private var savedStateHandle: SavedStateHandle = SavedStateHandle().apply {
        set(
            ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, patientId
        )
        set(
            ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE, patientUuid.toString()
        )
    }

    private lateinit var testPatient: Patient

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        testPatient = Patient().apply {
            id = patientId
            uuid = "patientUuid"
        }

        patientDAO = mockk()

        every { patientDAO.findPatientByID(patientId.toString()) } returns testPatient

        mockkObject(BuildConfigWrapper)

        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkObject(BuildConfigWrapper)
    }

    @Test
    fun `should receive Blood Pressure data`() = runTest {
        val bloodPressure = BloodPressure(120, 80, 70)
        val measurements = BloodPressureViewState.Content(
            Measurement(
                bloodPressure.systolic,
                bloodPressure.diastolic,
                bloodPressure.pulse
            )
        )

        mockkObject(BuildConfigWrapper)
        every { BuildConfigWrapper.hardcodeBluetoothDataToggle } returns false

        every {
            readBloodPressureRepository.start(
                captureLambda(),
                captureLambda()
            )
        } answers {
            val bloodPressureCallback = secondArg<(BloodPressureViewState) -> Unit>()
            bloodPressureCallback(measurements)
        }

        every { readBloodPressureRepository.disconnect() } answers {}

        viewModel.receiveBloodPressureData()

        val result = viewModel.bloodPressureUiState.first()

        assertEquals(ResultUiState.Success(bloodPressure), result)

        verify { readBloodPressureRepository.disconnect() }

    }

    @Test
    fun `should receive Weight data`() = runTest {
        val weight = 70f
        val weightMeasurement = ScaleViewState.Content(
            WeightMeasurement(weight)
        )

        mockkObject(BuildConfigWrapper)
        every { BuildConfigWrapper.hardcodeBluetoothDataToggle } returns false

        every {
            readScaleRepository.start(
                captureLambda()
            )
        } answers {
            val weightCallback = firstArg<(ScaleViewState) -> Unit>()
            weightCallback(weightMeasurement)
        }

        every { readScaleRepository.disconnect() } answers {}

        viewModel.receiveWeightData()

        val result = viewModel.weightUiState.first()

        assertEquals(ResultUiState.Success(weight.toString()), result)

        verify { readScaleRepository.disconnect() }
    }

    @Test
    fun `should get latest height data when present`() = runTest {
        val height = 170
        val visit = VisitExample.random(heightCm = height)

        coEvery { newVisitRepository.getLatestVisitWithHeight(patientUuid) } returns visit

        viewModel.getLastHeightFromVisits()

        assertEquals(LatestHeightResultUiState.Success(height), viewModel.latestHeightUiState.value)
    }

    @Test
    fun `should get empty height data when visit has no height`() = runTest {
        coEvery { newVisitRepository.getLatestVisitWithHeight(patientUuid) } returns null

        viewModel.getLastHeightFromVisits()

        assertEquals(LatestHeightResultUiState.NotFound, viewModel.latestHeightUiState.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should create new visit and add vitals information`() = runTest {
        val visit = VisitExample.random(heightCm = 170)

        every { patientDAO.findPatientByID(patientId.toString()) } returns testPatient
        coEvery {
            newVisitRepository.startVisit(testPatient, ofType(BloodPressure::class), visit.heightCm, ofType(Float::class))
        } returns Result.Success(visit)

        every { BuildConfigWrapper.hardcodeBluetoothDataToggle } returns true

        viewModel.receiveBloodPressureData()
        viewModel.receiveWeightData()
        viewModel.saveHeight(visit.heightCm.toString())
        advanceUntilIdle()
        viewModel.createVisit()

        coVerify {
            newVisitRepository.startVisit(
                testPatient,
                any(BloodPressure::class),
                visit.heightCm,
                ofType(Float::class)
            )
        }
    }

    @Test
    fun `should get active treatments from repository`() {
        val treatment = TreatmentExample.activeTreatment().apply { treatmentUuid = "treatmentUuid" }
        val treatmentList = listOf(treatment)

        coEvery { treatmentRepository.fetchAllActiveTreatments(any()) } returns Result.Success(
            treatmentList
        )


        runBlocking {
            viewModel.fetchActiveTreatment()

            assertEquals(
                ResultUiState.Success(treatmentList),
                viewModel.treatmentsResultUiState.value
            )
        }
    }

    @Test
    fun `should return error if fetching all treatments fails`() {

        coEvery { treatmentRepository.fetchAllActiveTreatments(any()) } returns Result.Error(
            Throwable("Error fetching treatments")
        )

        runBlocking {
            viewModel.fetchActiveTreatment()

            assertEquals(ResultUiState.Error, viewModel.treatmentsResultUiState.value)
        }
    }

    @Test
    fun `should save treatment adherence`() {

        val treatmentIdOne = "treatmentUuid"
        val treatmentIdTwo = "treatmentUuidTwo"

        val treatmentAdherenceInfo = mapOf(
            treatmentIdOne to true,
            treatmentIdTwo to false
        )

        val checkTreatmentList = listOf(
            CheckTreatment("Paracetamol", setOf(MedicationType.ARA_II), true, {}, treatmentIdOne),
            CheckTreatment("Ibuprofen", setOf(MedicationType.ARA_II), false, {}, treatmentIdTwo),
        )

        coEvery {
            treatmentRepository.saveTreatmentAdherence(
                treatmentAdherenceInfo,
                testPatient.uuid!!
            )
        } returns kotlin.Result.success(true)


        runBlocking {
            viewModel.addTreatmentAdherence(checkTreatmentList)

            coVerify {
                treatmentRepository.saveTreatmentAdherence(
                    treatmentAdherenceInfo,
                    testPatient.uuid!!
                )
            }

        }
    }

    @Test
    fun `should set bpBluetoothConnectionResultUiState to Error when bluetooth connection fails`() {
        every { BuildConfigWrapper.hardcodeBluetoothDataToggle } returns false

        every { readBloodPressureRepository.start(any(), any()) } answers {
            val connectionCallback = secondArg<(BloodPressureViewState) -> Unit>()
            connectionCallback(BloodPressureViewState.Error(BluetoothConnectionException.OnResponseReadHistory))
        }

        every { readBloodPressureRepository.disconnect() } answers {}

        viewModel.receiveBloodPressureData()

        assertEquals(ResultUiState.Error, viewModel.bloodPressureUiState.value)
    }

    @Test
    fun `should set scaleBluetoothConnectionResultUiState to Error when bluetooth connection fails`() {
        every { BuildConfigWrapper.hardcodeBluetoothDataToggle } returns false

        every { readScaleRepository.start(any()) } answers {
            val connectionCallback = firstArg<(ScaleViewState) -> Unit>()
            connectionCallback(ScaleViewState.Error(BluetoothConnectionException.OnResponseReadHistory))
        }

        every { readScaleRepository.disconnect() } answers {}

        viewModel.receiveWeightData()

        assertEquals(ResultUiState.Error, viewModel.weightUiState.value)
    }
}