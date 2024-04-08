package edu.upc.blopup.ui.takingvitals.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import edu.upc.sdk.library.api.repository.NewVisitRepository
import edu.upc.sdk.library.models.Vital

class DataScreenParameters : PreviewParameterProvider<MutableList<Vital>> {
    override val values: Sequence<MutableList<Vital>>
        get() = sequenceOf(
            mutableListOf(
                Vital(NewVisitRepository.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT, "120"),
                Vital(NewVisitRepository.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT, "80"),
                Vital(NewVisitRepository.VitalsConceptType.HEART_RATE_FIELD_CONCEPT, "60"),
                Vital(NewVisitRepository.VitalsConceptType.WEIGHT_FIELD_CONCEPT, "70")
            ),
            mutableListOf(
                Vital(NewVisitRepository.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT, "200"),
                Vital(NewVisitRepository.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT, "100"),
                Vital(NewVisitRepository.VitalsConceptType.HEART_RATE_FIELD_CONCEPT, "80"),
                Vital(NewVisitRepository.VitalsConceptType.WEIGHT_FIELD_CONCEPT, "150")
            )
        )

}
