package edu.upc.blopup.model

data class BloodPressure(val systolic: Int, val diastolic: Int, val pulse: Int)

object BloodPressureExample {
    fun random(): BloodPressure {
        return BloodPressure(120, 80, 70)
    }
}