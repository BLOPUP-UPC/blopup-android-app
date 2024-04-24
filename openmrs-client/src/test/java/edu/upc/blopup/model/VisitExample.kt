package edu.upc.blopup.model

import java.time.Instant
import java.util.UUID

object VisitExample {
    fun random(
        heightCm: Int? = null,
        patientId: UUID = UUID.randomUUID(),
        startDateTime: Instant = Instant.now(),
        bloodPressure: BloodPressure = BloodPressureExample.random(),
        weightKg : Float? = null,
        id: UUID = UUID.randomUUID()
    ): Visit {
        return Visit(
            id = id,
            patientId = patientId,
            location = "location",
            startDate = startDateTime.truncatedTo(java.time.temporal.ChronoUnit.SECONDS),
            bloodPressure = bloodPressure,
            heightCm = heightCm,
            weightKg = weightKg
        )
    }
}