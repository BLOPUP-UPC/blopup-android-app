package edu.upc.blopup.ui.takingvitals

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.BloodPressureViewState
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.Measurement
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.ReadBloodPressureRepository
import edu.upc.blopup.scale.readScaleMeasurement.ReadScaleRepository
import edu.upc.blopup.scale.readScaleMeasurement.ScaleViewState
import edu.upc.blopup.scale.readScaleMeasurement.WeightMeasurement
import edu.upc.blopup.vitalsform.Vital
import edu.upc.sdk.utilities.ApplicationConstants
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(org.mockito.junit.MockitoJUnitRunner::class)
class VitalsViewModelTest{

    private lateinit var viewModel: VitalsViewModel
    private lateinit var readBloodPressureRepository: ReadBloodPressureRepository
    private lateinit var readScaleRepository: ReadScaleRepository

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        readBloodPressureRepository = mockk()
        readScaleRepository = mockk()
        viewModel = VitalsViewModel(readBloodPressureRepository, readScaleRepository)
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
            Vital(ApplicationConstants.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT, "120"),
            Vital(ApplicationConstants.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT, "80"),
            Vital(ApplicationConstants.VitalsConceptType.HEART_RATE_FIELD_CONCEPT, "70")
        )

        val result = viewModel.vitalsUiState.first()

        assertEquals(expectedResult, result)

        verify {readBloodPressureRepository.disconnect() }
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
                captureLambda())
        } answers {
            val weightCallback = firstArg<(ScaleViewState) -> Unit>()
            weightCallback(weightMeasurement)
        }

        every {
            readScaleRepository.disconnect()
        } answers {}

        viewModel.receiveWeightData()

        val expectedResult = mutableListOf(
            Vital(ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT, "70.0")
        )

        val result = viewModel.vitalsUiState.first()

        assertEquals(expectedResult, result)
    }
}
