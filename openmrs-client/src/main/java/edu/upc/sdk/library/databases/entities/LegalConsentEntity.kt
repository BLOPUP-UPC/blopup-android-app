package edu.upc.sdk.library.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "legalConsents")
class LegalConsentEntity {

    @PrimaryKey
    @ColumnInfo(name = "filePath")
    var filePath: String = ""

    @ColumnInfo(name = "patientId")
    var patientId: String = ""

}