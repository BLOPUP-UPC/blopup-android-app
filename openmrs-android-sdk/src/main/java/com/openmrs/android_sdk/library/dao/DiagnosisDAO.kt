package com.openmrs.android_sdk.library.dao

import com.openmrs.android_sdk.library.OpenmrsAndroid
import com.openmrs.android_sdk.library.databases.AppDatabase
import com.openmrs.android_sdk.library.models.Diagnosis
import io.reactivex.Single

class DiagnosisDAO {

    private val diagnosisRoomDAO = AppDatabase.getDatabase(OpenmrsAndroid.getInstance()!!.applicationContext).diagnosisRoomDAO()

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
