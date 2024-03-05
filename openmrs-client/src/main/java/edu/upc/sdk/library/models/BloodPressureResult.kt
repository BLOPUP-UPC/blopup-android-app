package edu.upc.sdk.library.models

data class BloodPressureResult(
    val bloodPressureType: BloodPressureType,
    val systolicValue: Double,
    val diastolicValue: Double
)
