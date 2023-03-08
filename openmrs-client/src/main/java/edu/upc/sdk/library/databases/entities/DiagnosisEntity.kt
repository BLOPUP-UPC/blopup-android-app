package edu.upc.sdk.library.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import edu.upc.sdk.library.models.Resource

@Entity(tableName = "diagnoses")
class DiagnosisEntity : Resource(){
    @ColumnInfo(name = "encounter_id")
    var encounterId: Long? = null
}