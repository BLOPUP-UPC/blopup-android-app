package edu.upc.sdk.library.api.repository

import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.model.Visit
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.dao.LocationDAO
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.databases.AppDatabaseHelper.createObservableIO
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.VisitType
import edu.upc.sdk.utilities.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.Instant
import retrofit2.Call
import rx.Observable
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit.SECONDS
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import edu.upc.sdk.library.models.Visit as OpenMRSVisit

@Singleton
class NewVisitRepository @Inject constructor(
    val restApi: RestApi, val visitDAO: VisitDAO, private val locationDAO: LocationDAO
) {

    private val logger = OpenMRSLogger()

    fun getVisitByUuid(visitUuid: UUID): Visit {
        val openMRSVisit = restApi.getVisitByUuid(visitUuid.toString()).execute().body()
        return createVisitFromOpenMRSVisit(openMRSVisit)
    }

    suspend fun endVisit(visitId: UUID): Boolean = withContext(Dispatchers.IO) {
        val endVisitDateTime =
            LocalDateTime.now().truncatedTo(SECONDS).toInstant(ZoneOffset.UTC).toString()
        val visitWithEndDate = OpenMRSVisit().apply {
            stopDatetime = endVisitDateTime
        }

        val call: Call<OpenMRSVisit> = restApi.endVisitByUUID(visitId.toString(), visitWithEndDate)
        val response = call.execute()

        if (response.isSuccessful) {
            try {
                val localVisitId =
                    visitDAO.getVisitsIDByUUID(visitId.toString()).single().toBlocking().first()
                val localVisit = visitDAO.getVisitByID(localVisitId).single().toBlocking().first()
                localVisit.patient.id?.let {
                    localVisit.stopDatetime = endVisitDateTime
                    visitDAO.saveOrUpdate(localVisit, it).single().toBlocking().first()
                }
            } catch (e: Exception) {
                logger.e("Error updating visit end date in local database: ${e.javaClass.simpleName}: ${e.message}")
            }
            true
        } else {
            throw Exception("endVisitByUuid error: " + response.message())
        }
    }

    fun startVisit(patient: Patient): Visit {
        val openMRSVisit = OpenMRSVisit().apply {
            startDatetime = Instant.now().toString()
            this.patient = patient
            location = locationDAO.findLocationByName(OpenmrsAndroid.getLocation())
            visitType = VisitType("FACILITY", OpenmrsAndroid.getVisitTypeUUID())
        }

        val call = restApi.startVisit(openMRSVisit)
        val response = call.execute()
        if (response.isSuccessful) {
            val newVisit = response.body()
            try {
                visitDAO.saveOrUpdate(newVisit, patient.id!!).toBlocking()
            } catch (e: Exception) {
                logger.e("Error saving new visit in local database: ${e.javaClass.simpleName}: ${e.message}")
            }
            return createVisitFromOpenMRSVisit(newVisit)
        } else {
            throw IOException(response.message())
        }
    }

    private fun createVisitFromOpenMRSVisit(openMRSVisit: OpenMRSVisit?): Visit {
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
        )
    }
}
