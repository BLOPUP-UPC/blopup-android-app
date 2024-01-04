package edu.upc.sdk.library.models

import org.joda.time.Instant

data class Treatment(
    var recommendedBy: String,
    var medicationName: String,
    var medicationType: Set<MedicationType>,
    var notes: String,
    var isActive: Boolean = true,
    var visitId: Long,
    var visitUuid: String? = null,
    var encounterUuid: String? = null,
    var observationStatusUuid: String? = null,
    var creationDate: Instant = Instant.now(),
    var inactiveDate: Instant? = null
) {


    companion object {
        const val RECOMMENDED_BY_BLOPUP = "BlopUp"
        const val RECOMMENDED_BY_OTHER = "Other"
    }

    constructor() : this("", "", emptySet(), "", true, 0)
}

