package edu.upc.sdk.library.models

import edu.upc.blopup.model.MedicationType
import edu.upc.blopup.model.Treatment
import org.joda.time.Instant
import java.time.LocalDate
import java.util.UUID

object TreatmentExample {

    fun activeTreatment(creationDate: Instant = Instant.now()): Treatment {
        return Treatment(
            medicationName = "Oxycontin",
            medicationType = setOf(MedicationType.DIURETIC),
            notes = "25mg/dia",
            recommendedBy = "BlopUp",
            isActive = true,
            visitUuid = UUID.randomUUID().toString(),
            treatmentUuid = UUID.randomUUID().toString(),
            observationStatusUuid = UUID.randomUUID().toString(),
            creationDate = creationDate,
            adherence = mapOf(LocalDate.now() to true),
            doctorUuid = UUID.randomUUID().toString(),
            doctorRegistrationNumber = "12345",
            doctorName = "Dr. House"
        )
    }

    fun inactiveTreatment(creationDate: Instant = Instant.now()): Treatment {
        return Treatment(
            medicationName = "Tylenol",
            medicationType = setOf(MedicationType.ARA_II, MedicationType.CALCIUM_CHANNEL_BLOCKER),
            notes = "50mg/dia",
            recommendedBy = "Other",
            isActive = false,
            visitUuid = UUID.randomUUID().toString(),
            treatmentUuid = UUID.randomUUID().toString(),
            creationDate = creationDate,
            inactiveDate = Instant("2023-12-22T10:10:10Z"),
        )
    }
}
