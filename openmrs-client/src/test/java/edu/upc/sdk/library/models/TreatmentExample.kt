package edu.upc.sdk.library.models

import org.joda.time.Instant
import java.util.UUID
import kotlin.random.Random

object TreatmentExample {

    fun activeTreatment(creationDate: Instant = Instant.now()): Treatment {
        return Treatment().apply {
            medicationName = "Oxycontin"
            medicationType = setOf(MedicationType.DIURETIC)
            notes = "25mg/dia"
            recommendedBy = "BlopUp"
            isActive = true
            visitId = Random.nextLong(1, 100)
            visitUuid = UUID.randomUUID().toString()
            encounterUuid = UUID.randomUUID().toString()
            this.creationDate = creationDate
        }
    }

    fun inactiveTreatment(creationDate: Instant = Instant.now()): Treatment {
        return Treatment().apply {
            medicationName = "Tylenol"
            medicationType = setOf(MedicationType.ARA_II, MedicationType.CALCIUM_CHANNEL_BLOCKER)
            notes = "50mg/dia"
            recommendedBy = "Other"
            isActive = false
            visitId = Random.nextLong(1, 100)
            visitUuid = UUID.randomUUID().toString()
            encounterUuid = UUID.randomUUID().toString()
            this.creationDate = creationDate
            inactiveDate = Instant("2023-12-22T10:10:10Z")
        }
    }
}
