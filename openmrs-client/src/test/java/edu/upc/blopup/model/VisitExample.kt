package edu.upc.blopup.model

import java.time.LocalDateTime
import java.util.UUID

object VisitExample {
    fun random(heightCm: Int? = null): Visit {
        return Visit(
            id = UUID.randomUUID(),
            patientId = UUID.randomUUID(),
            location = "location",
            startDate = LocalDateTime.now(),
            bloodPressure = BloodPressureExample.random(),
            heightCm = heightCm,
        )
    }
}