package edu.upc.blopup.bloodpressure

data class BloodPressureResult(
    val bloodPressureType: BloodPressureType,
    val systolicValue: Double,
    val diastolicValue: Double
)
