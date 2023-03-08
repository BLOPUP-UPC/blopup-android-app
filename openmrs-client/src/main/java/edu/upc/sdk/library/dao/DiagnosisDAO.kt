package edu.upc.sdk.library.dao

import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.databases.AppDatabase
import edu.upc.sdk.library.models.Diagnosis

class DiagnosisDAO {

    private val diagnosisRoomDAO = AppDatabase.getDatabase(
        OpenmrsAndroid.getInstance()!!.applicationContext).diagnosisRoomDAO()

    fun findDiagnosesByEncounterID(encounterId: Long): List<Diagnosis> =
        diagnosisRoomDAO.findDiagnosesByEncounterID(encounterId).blockingGet().map {
            val diagnosis = Diagnosis()
            diagnosis.id = it.id
            diagnosis.display = it.display
            diagnosis.uuid = it.uuid
            diagnosis.links = it.links
            diagnosis
        }
}
