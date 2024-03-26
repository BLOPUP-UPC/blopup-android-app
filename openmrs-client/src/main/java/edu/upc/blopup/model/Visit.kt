package edu.upc.blopup.model

import java.time.LocalDate
import java.util.UUID

data class Visit(
    val id: UUID,
    val location: String,
    val startDate: LocalDate,
    val bloodPressure: BloodPressure,
    val heightCm: Int? = null,
    val weightKg: Float? = null,
    val treatments: List<Treatment> = emptyList(),
    val endDate: LocalDate? = null
)
