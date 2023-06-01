package edu.upc.sdk.library.dao

import androidx.room.Dao
import androidx.room.Insert
import edu.upc.sdk.library.databases.entities.LegalConsentEntity

@Dao
interface LegalConsentRoomDAO {

    @Insert
    fun saveLegalConsent(legalConsent: LegalConsentEntity?): Long

}