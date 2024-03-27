package edu.upc.sdk.library.api.repository

import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.Visit
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.utilities.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.SECONDS
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import edu.upc.sdk.library.models.Visit as OpenMRSVisit

@Singleton
class NewVisitRepository @Inject constructor(val restApi: RestApi, val visitDAO: VisitDAO) {

    private val logger = OpenMRSLogger()

    fun getVisitByUuid(visitUuid: UUID): Visit {

        val openMRSVisit = restApi.getVisitByUuid(visitUuid.toString()).execute().body()

        val vitalsObservations =
            openMRSVisit?.encounters?.find { it.encounterType?.display == EncounterType.VITALS }?.observations

        val systolic =
            vitalsObservations?.find { it.display?.contains("Systolic") == true }?.displayValue?.toDouble()
                ?.toInt() ?: throw Exception("Systolic is null")
        val diastolic =
            vitalsObservations.find { it.display?.contains("Diastolic") == true }?.displayValue?.toDouble()
                ?.toInt() ?: throw Exception("Diastolic is null")
        val pulse =
            vitalsObservations.find { it.display?.contains("Pulse") == true }?.displayValue?.toDouble()
                ?.toInt() ?: throw Exception("Pulse is null")
        val weight =
            vitalsObservations.find { it.display?.contains("Weight") == true }?.displayValue?.toFloat()
        val height =
            vitalsObservations.find { it.display?.contains("Height") == true }?.displayValue?.toDouble()
                ?.toInt()

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

    suspend fun endVisit(visitId: UUID): Boolean {
        var result = false

        val endVisitDateTime = LocalDateTime.now().truncatedTo(SECONDS)
        val visitWithEndDate = OpenMRSVisit().apply {
            stopDatetime = endVisitDateTime.toInstant(ZoneOffset.UTC).toString()
        }

        withContext(Dispatchers.IO) {
            val call = restApi.endVisitByUUID(visitId.toString(), visitWithEndDate)
            val response = call.execute()

            if (response.isSuccessful) {
                try {
                    val localVisitId =
                        visitDAO.getVisitsIDByUUID(visitId.toString()).single().toBlocking().first()
                    val localVisit =
                        visitDAO.getVisitByID(localVisitId).single().toBlocking().first()
                    localVisit.patient.id?.let {
                        visitDAO.saveOrUpdate(localVisit, it).single().toBlocking().first()
                    }
                } catch (e: Exception) {
                    logger.e("Error updating visit end date in local database: ${e.javaClass.simpleName}: ${e.message}")
                }
                result = true
            } else {
                throw Exception("endVisitByUuid error: " + response.message())
            }
        }
        return result
    }
}
