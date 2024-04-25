package edu.upc.openmrs.activities.visit

import android.graphics.Color
import com.github.anastr.speedviewlib.SpeedView
import com.github.anastr.speedviewlib.components.Section
import com.github.anastr.speedviewlib.components.indicators.Indicator.Indicators
import edu.upc.blopup.model.BloodPressureType
import edu.upc.openmrs.utilities.ChartColor

class BloodPressureChart {
    fun createChart(speedometer: SpeedView, bpType: BloodPressureType) {
        setIndicator(speedometer)
        setSections(speedometer)
        setValue(speedometer, bpType)
    }

    private fun setValue(speedometer: SpeedView, bpType: BloodPressureType) {

        val bpLevel = when (bpType) {
            BloodPressureType.NORMAL -> 12.5f
            BloodPressureType.STAGE_I -> 37.5f
            BloodPressureType.STAGE_II_A -> 62.5f
            BloodPressureType.STAGE_II_B -> 62.5f
            BloodPressureType.STAGE_II_C -> 87.5f
        }

        speedometer.speedTo(bpLevel,0)
        speedometer.speedTextColor = Color.TRANSPARENT
        speedometer.textColor = Color.TRANSPARENT
        speedometer.unit = ""
    }

    private fun setIndicator(speedometer: SpeedView) {
        speedometer.setIndicator(Indicators.HalfLineIndicator)
        speedometer.indicator.color = Color.BLACK
        speedometer.centerCircleRadius = 0f
        speedometer.withTremble = false
    }

    private fun setSections(speedometer: SpeedView) {
        val width = speedometer.speedometerWidth
        speedometer.setStartDegree(205)
        speedometer.setEndDegree(335)
        speedometer.clearSections()
        speedometer.addSections(
            Section(0f, .25f, ChartColor.BP_NORMAL.value, width),
            Section(.25f, .50f, ChartColor.BP_HT_STAGE_I.value, width),
            Section(.50f, .75f, ChartColor.BP_HT_STAGE_II_A_AND_B.value, width),
            Section(.75f, 1f, ChartColor.BP_HT_STAGE_II_C.value, width)
        )
    }
}
