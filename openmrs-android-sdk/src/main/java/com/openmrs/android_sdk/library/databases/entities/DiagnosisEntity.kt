package com.openmrs.android_sdk.library.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.openmrs.android_sdk.library.models.Resource

@Entity(tableName = "diagnoses")
class DiagnosisEntity : Resource(){
    @ColumnInfo(name = "encounter_id")
    var encounterId: Long? = null
}