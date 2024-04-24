package edu.upc.blopup.model

import java.time.Instant
import java.util.UUID

data class Visit(
    val id: UUID,
    val patientId: UUID,
    val location: String,
    val startDate: Instant,
    val bloodPressure: BloodPressure,
    val heightCm: Int? = null,
    val weightKg: Float? = null,
    var endDate: Instant? = null
) {
    fun bloodPressureType(): BloodPressureType {
        return bloodPressure.bloodPressureType()
    }

    fun isActive(): Boolean {
        return endDate == null
    }
}

