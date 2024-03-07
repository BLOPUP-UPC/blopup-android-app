package edu.upc.blopup.ui.takingvitals.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import edu.upc.sdk.library.models.Vital
import edu.upc.sdk.utilities.ApplicationConstants

class DataScreenParameters : PreviewParameterProvider<MutableList<Vital>> {
    override val values: Sequence<MutableList<Vital>>
        get() = sequenceOf(
            mutableListOf(
                Vital(ApplicationConstants.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT, "120"),
                Vital(ApplicationConstants.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT, "80"),
                Vital(ApplicationConstants.VitalsConceptType.HEART_RATE_FIELD_CONCEPT, "60"),
                Vital(ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT, "70")
            ),
            mutableListOf(
                Vital(ApplicationConstants.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT, "200"),
                Vital(ApplicationConstants.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT, "100"),
                Vital(ApplicationConstants.VitalsConceptType.HEART_RATE_FIELD_CONCEPT, "80"),
                Vital(ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT, "150")
            )
        )

}
