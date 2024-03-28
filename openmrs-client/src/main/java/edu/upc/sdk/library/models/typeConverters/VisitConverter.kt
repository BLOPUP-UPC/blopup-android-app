package edu.upc.sdk.library.models.typeConverters

import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.Visit
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.library.models.Visit as OpenMRSVisit
import edu.upc.sdk.utilities.DateUtils
import java.time.LocalDateTime
import java.util.UUID

object VisitConverter {

    fun createVisitFromOpenMRSVisit(openMRSVisit: OpenMRSVisit?): Visit {
        val vitalsObservations =
            openMRSVisit?.encounters?.find { it.encounterType?.display == EncounterType.VITALS }?.observations

        val bloodPressure = vitalsObservations.let { vitals ->
            val systolic =
                vitals?.find { it.display?.contains("Systolic") == true }?.displayValue?.toDouble()
                    ?.toInt() ?: throw Exception("Systolic is null")
            val diastolic =
                vitals.find { it.display?.contains("Diastolic") == true }?.displayValue?.toDouble()
                    ?.toInt() ?: throw Exception("Diastolic is null")
            val pulse =
                vitals.find { it.display?.contains("Pulse") == true }?.displayValue?.toDouble()
                    ?.toInt() ?: throw Exception("Pulse is null")
            BloodPressure(systolic, diastolic, pulse)
        }

        val height =
            vitalsObservations?.find { it.display?.contains("Height") == true }?.displayValue?.toDouble()
                ?.toInt()

        val weight =
            vitalsObservations?.find { it.display?.contains("Weight") == true }?.displayValue?.toFloat()

        return Visit(
            UUID.fromString(openMRSVisit?.uuid),
            UUID.fromString(openMRSVisit?.patient?.uuid),
            openMRSVisit?.location?.display ?: "",
            openMRSVisit?.startDatetime?.let { DateUtils.parseLocalDateFromOpenmrsDate(it) }
                ?: LocalDateTime.now(),
            bloodPressure,
            height,
            weight
        ).apply {
            endDate =
                openMRSVisit?.stopDatetime?.let { DateUtils.parseLocalDateFromOpenmrsDate(it) }
        }
    }
}
