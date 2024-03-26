package edu.upc.sdk.library.api.repository

import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.Visit
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.utilities.DateUtils
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewVisitRepository @Inject constructor(val restApi: RestApi) {

    fun getVisitByUuid(visitUuid: UUID): Visit {

        val openMRSVisit = restApi.getVisitByUuid(visitUuid.toString()).execute().body()

        val vitalsObservations =
            openMRSVisit?.encounters?.find { it.encounterType?.display == EncounterType.VITALS }?.observations

        val systolic =
            vitalsObservations?.find { it.display?.contains("Systolic") == true }?.displayValue?.toDouble()?.toInt()
                ?: throw Exception("Systolic is null")
        val diastolic =
            vitalsObservations.find {  it.display?.contains("Diastolic") == true }?.displayValue?.toDouble()?.toInt()
                ?: throw Exception("Diastolic is null")
        val pulse =
            vitalsObservations.find {  it.display?.contains("Pulse") == true }?.displayValue?.toDouble()?.toInt()
                ?: throw Exception("Pulse is null")
        val weight =
            vitalsObservations.find {  it.display?.contains("Weight") == true }?.displayValue?.toFloat()
        val height =
            vitalsObservations.find {  it.display?.contains("Height") == true }?.displayValue?.toDouble()?.toInt()

        val visitStartDate =
            openMRSVisit.startDatetime.let { DateUtils.parseLocalDateFromOpenmrsDate(it) }

        return Visit(
            visitUuid,
            UUID.fromString(openMRSVisit.patient.uuid),
            openMRSVisit.location.display ?: "",
            visitStartDate,
            BloodPressure(systolic, diastolic, pulse),
            height,
            weight
        )
    }
}




