package edu.upc.sdk.library.api.repository

import edu.upc.blopup.model.Visit
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.dao.LocationDAO
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.VisitType
import edu.upc.sdk.library.models.typeConverters.VisitConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
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
        return VisitConverter.createVisitFromOpenMRSVisit(openMRSVisit)
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

    suspend fun startVisit(patient: Patient): Visit =
        withContext(Dispatchers.IO) {
            val openMRSVisit = OpenMRSVisit().apply {
                startDatetime = LocalDateTime.now().truncatedTo(SECONDS).toInstant(ZoneOffset.UTC).toString()
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
                return@withContext VisitConverter.createVisitFromOpenMRSVisit(newVisit)
            } else {
                throw IOException(response.message())
            }
        }

    suspend fun deleteVisit(visitUuid: UUID): Boolean = withContext(Dispatchers.IO) {
        val call = restApi.deleteVisit(visitUuid.toString())
        val response = call.execute()
        if (response.isSuccessful) {
            visitDAO.deleteVisitByUuid(visitUuid.toString()).toBlocking().first()

            return@withContext true
        }

        logger.e("Error deleting the visit with uuid: $visitUuid in the server $response")

        return@withContext false
    }
}
