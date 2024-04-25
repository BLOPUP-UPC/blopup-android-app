package edu.upc.openmrs.activities.visit

import edu.upc.blopup.model.Visit
import kotlin.math.pow

class BMICalculator {
    fun execute(visit: Visit): Float? {
        if (visit.weightKg === null || visit.heightCm === null) {
            return null
        }

        val heightForBmi = (visit.heightCm.toDouble() / 100).pow(2.0)
        return (visit.weightKg.toDouble() / heightForBmi).toFloat()
    }
}