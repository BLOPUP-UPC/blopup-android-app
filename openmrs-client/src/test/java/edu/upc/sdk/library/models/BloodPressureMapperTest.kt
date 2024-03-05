package edu.upc.sdk.library.models

import com.ideabus.model.data.CurrentAndMData
import edu.upc.sdk.library.api.repository.Measurement
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class BloodPressureMapperTest(private val case: TestCase) {

    @Test
    fun `given valid bluetooth data then it is mapped properly`() {
        val bluetoothData = CurrentAndMData().apply {
            systole = case.systolic
            dia = case.diastolic
            hr = case.heartRate
        }
        val mappedFrom = Measurement.from(bluetoothData)
        val expectedMeasurement = Measurement(case.systolic, case.diastolic, case.heartRate)

        assertEquals(expectedMeasurement, mappedFrom)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun testCases(): List<TestCase> = listOf(
            TestCase(107, 63, 69),
            TestCase(80, 70, 70)
        )
    }

    data class TestCase(
        val systolic: Int,
        val diastolic: Int,
        val heartRate: Int
    )
}
