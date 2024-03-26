package edu.upc.sdk.library.api.repository

import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.Visit
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.HEART_RATE_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.HEIGHT_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT
import edu.upc.sdk.utilities.DateUtils
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewVisitRepository @Inject constructor(val restApi: RestApi){

    fun getVisitByUuid(visitUuid: UUID): Visit {

        val openMRSVisit = restApi.getVisitByUuid(visitUuid.toString()).execute().body()

        val vitalsEncounter = openMRSVisit?.encounters?.find { it.encounterType?.display == EncounterType.VITALS }

        val systolic = vitalsEncounter?.observations?.find { it.concept?.uuid  == SYSTOLIC_FIELD_CONCEPT }?.displayValue?.toInt() ?: throw Exception("Systolic is null")
        val diastolic = vitalsEncounter.observations.find { it.concept?.uuid  == DIASTOLIC_FIELD_CONCEPT }?.displayValue?.toInt() ?: throw Exception("Diastolic is null")
        val pulse = vitalsEncounter.observations.find { it.concept?.uuid  == HEART_RATE_FIELD_CONCEPT }?.displayValue?.toInt() ?: throw Exception("Pulse is null")
        val weight = vitalsEncounter.observations.find { it.concept?.uuid  == WEIGHT_FIELD_CONCEPT }?.displayValue?.toFloat()
        val height = vitalsEncounter.observations.find { it.concept?.uuid  == HEIGHT_FIELD_CONCEPT }?.displayValue?.toInt()

        val visitStartDate = openMRSVisit.startDatetime.let { DateUtils.parseLocalDateFromOpenmrsDate(it) }

        return Visit(
            visitUuid,
            openMRSVisit.location.display ?: "",
            visitStartDate,
            BloodPressure(systolic, diastolic, pulse),
            height,
            weight
        )
    }

}
