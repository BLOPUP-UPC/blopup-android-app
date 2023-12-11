package edu.upc.sdk.library.models

data class Treatment(
    var recommendedBy: String,
    var medicationName: String,
    var medicationType: Set<MedicationType>,
    var notes: String,
    var isActive: Boolean = true,
    var visitId: Long
) {

    companion object {
        const val RECOMMENDED_BY_BLOPUP = "BlopUp"
        const val RECOMMENDED_BY_OTHER = "Other"
    }

    constructor() : this("", "", emptySet(), "", true, 0)
}

