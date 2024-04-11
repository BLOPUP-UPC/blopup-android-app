package edu.upc.blopup.model

import edu.upc.sdk.library.models.BloodPressureType

data class BloodPressure(val systolic: Int, val diastolic: Int, val pulse: Int) {
    fun bloodPressureType(): BloodPressureType {
        return when {
            systolic >= 180 || diastolic >= 110 -> BloodPressureType.STAGE_II_C
            systolic >= 160 || diastolic >= 100 -> BloodPressureType.STAGE_II_B
            systolic >= 140 || diastolic >= 90  -> BloodPressureType.STAGE_II_A
            systolic >= 130 || diastolic >= 80  -> BloodPressureType.STAGE_I
            else -> BloodPressureType.NORMAL
        }
    }
}

