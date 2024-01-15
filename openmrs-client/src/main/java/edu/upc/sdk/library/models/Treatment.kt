package edu.upc.sdk.library.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.Instant

@Parcelize
data class Treatment(
    var recommendedBy: String,
    var medicationName: String,
    var medicationType: Set<MedicationType>,
    var notes: String? = null,
    var isActive: Boolean = true,
    var visitId: Long,
    var visitUuid: String? = null,
    var treatmentUuid: String? = null,
    var observationStatusUuid: String? = null,
    var creationDate: Instant = Instant.now(),
    var inactiveDate: Instant? = null
) : Parcelable {

    companion object {
        const val RECOMMENDED_BY_BLOPUP = "BlopUp"
        const val RECOMMENDED_BY_OTHER = "Other"
    }

}

