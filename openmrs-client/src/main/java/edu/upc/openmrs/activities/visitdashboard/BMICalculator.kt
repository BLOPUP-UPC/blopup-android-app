package edu.upc.openmrs.activities.visitdashboard

import edu.upc.sdk.library.models.Observation
import java.util.Locale
import kotlin.math.pow

class BMICalculator {
    fun execute(observations: List<Observation>): String {
        var weight = "0"
        var height = "0"

        for (obs in observations) {
            if (obs.display!!.contains("Weight")) {
                weight = obs.displayValue!!
            }
            if (obs.display!!.contains("Height")) {
                height = obs.displayValue!!
            }
        }
        if (weight == "0" || height == "0") {
            return "N/A"
        }

        val heightForBmi = (height.toDouble() / 100).pow(2.0)
        val bmi = weight.toDouble() / heightForBmi

        return String.format(Locale.US, "%.1f", bmi)
    }
}