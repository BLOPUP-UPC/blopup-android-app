package edu.upc.blopup.model

import java.time.LocalDateTime
import java.util.UUID

object VisitExample {
    fun random(heightCm: Int? = null, patientId: UUID = UUID.randomUUID(), startDateTime: LocalDateTime = LocalDateTime.now()): Visit {
        return Visit(
            id = UUID.randomUUID(),
            patientId = patientId,
            location = "location",
            startDate = startDateTime.truncatedTo(java.time.temporal.ChronoUnit.SECONDS),
            bloodPressure = BloodPressureExample.random(),
            heightCm = heightCm,
        )
    }
}