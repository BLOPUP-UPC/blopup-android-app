package edu.upc.blopup.model

import java.time.LocalDateTime
import java.util.UUID

data class Visit(
    val id: UUID,
    val patientId: UUID,
    val location: String,
    val startDate: LocalDateTime,
    val bloodPressure: BloodPressure,
    val heightCm: Int? = null,
    val weightKg: Float? = null,
    var endDate: LocalDateTime? = null
)

