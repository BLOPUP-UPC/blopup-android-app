package edu.upc.sdk.library.models

object TreatmentExample {

    fun activeTreatment(): Treatment {
        return Treatment().apply {
            medicationName = "Oxycontin"
            medicationType = setOf(MedicationType.DIURETIC)
            notes = "25mg/dia"
            recommendedBy = "BlopUp"
            isActive = true
            visitId = 14L
        }
    }

    fun inactiveTreatment(): Treatment {
        return Treatment().apply {
            medicationName = "Tylenol"
            medicationType = setOf(MedicationType.ARA_II, MedicationType.CALCIUM_CHANNEL_BLOCKER)
            notes = "50mg/dia"
            recommendedBy = "Other"
            isActive = false
            visitId = 15L
        }
    }
}
