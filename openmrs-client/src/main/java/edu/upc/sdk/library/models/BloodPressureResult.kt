package edu.upc.sdk.library.models

import edu.upc.blopup.model.BloodPressureType

data class BloodPressureResult(
    val bloodPressureType: BloodPressureType,
    val systolicValue: Double,
    val diastolicValue: Double
)
