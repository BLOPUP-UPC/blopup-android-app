package edu.upc.sdk.library.models.typeConverters

import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.Visit
import edu.upc.sdk.library.api.repository.VisitRepository.Companion.VITALS_ENCOUNTER_TYPE
import edu.upc.sdk.library.api.repository.VisitRepository.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT
import edu.upc.sdk.library.api.repository.VisitRepository.VitalsConceptType.HEART_RATE_FIELD_CONCEPT
import edu.upc.sdk.library.api.repository.VisitRepository.VitalsConceptType.HEIGHT_FIELD_CONCEPT
import edu.upc.sdk.library.api.repository.VisitRepository.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT
import edu.upc.sdk.library.api.repository.VisitRepository.VitalsConceptType.WEIGHT_FIELD_CONCEPT
import edu.upc.sdk.library.models.OpenMRSVisit
import edu.upc.sdk.utilities.DateUtils.parseInstantFromOpenmrsDate
import java.time.Instant
import java.util.UUID

object VisitConverter {

    fun createVisitFromOpenMRSVisit(openMRSVisit: OpenMRSVisit?): Visit {
        val vitalsObservations =
            openMRSVisit?.encounters?.find { it.encounterType?.display == VITALS_ENCOUNTER_TYPE }?.observations

        val bloodPressure = vitalsObservations.let { vitals ->
            val systolic =
                vitals?.find { it.concept?.uuid == SYSTOLIC_FIELD_CONCEPT }?.value?.toDouble()?.toInt()
                    ?: throw Exception("Systolic is null")
            val diastolic =
                vitals.find { it.concept?.uuid == DIASTOLIC_FIELD_CONCEPT }?.value?.toDouble()?.toInt()
                    ?: throw Exception("Diastolic is null")
            val pulse =
                vitals.find { it.concept?.uuid == HEART_RATE_FIELD_CONCEPT }?.value?.toDouble()?.toInt()
                    ?: throw Exception("Pulse is null")
            BloodPressure(systolic, diastolic, pulse)
        }

        val height =
            vitalsObservations?.find { it.concept?.uuid == HEIGHT_FIELD_CONCEPT }?.value?.toDouble()?.toInt()

        val weight =
            vitalsObservations?.find { it.concept?.uuid == WEIGHT_FIELD_CONCEPT }?.value?.toFloat()

        return Visit(
            UUID.fromString(openMRSVisit?.uuid),
            UUID.fromString(openMRSVisit?.patient?.uuid),
            openMRSVisit?.location?.display ?: "el servei assistencial",
            openMRSVisit?.startDatetime?.let { parseInstantFromOpenmrsDate(it) }
                ?: Instant.now(),
            bloodPressure,
            height,
            weight
        ).apply {
            endDate =
                openMRSVisit?.stopDatetime?.let { parseInstantFromOpenmrsDate(it) }
        }
    }
}
