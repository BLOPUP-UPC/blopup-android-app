package edu.upc.blopup.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.LocalDate

@Parcelize
data class Treatment(
    var recommendedBy: String,
    var medicationName: String,
    var medicationType: Set<MedicationType>,
    var notes: String? = null,
    var isActive: Boolean = true,
    var visitUuid: String? = null,
    var treatmentUuid: String? = null,
    var observationStatusUuid: String? = null,
    var creationDate: Instant = Instant.now(),
    var inactiveDate: Instant? = null,
    var adherence: Map<LocalDate, Boolean> = emptyMap(),
    var doctor: Doctor? = null
) : Parcelable {

    companion object {
        const val RECOMMENDED_BY_BLOPUP = "BlopUp"
        const val RECOMMENDED_BY_OTHER = "Other"
    }

}

