package edu.upc.sdk.library.dao

import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.databases.AppDatabase
import edu.upc.sdk.library.databases.AppDatabaseHelper
import edu.upc.sdk.library.models.LegalConsent
import rx.Observable
import javax.inject.Inject

class LegalConsentDAO @Inject constructor(){

    private val legalConsentRoomDAO = AppDatabase.getDatabase(
        OpenmrsAndroid.getInstance()!!.applicationContext).legalConsentRoomDAO()

    fun saveLegalConsent(legalConsent: LegalConsent?): Observable<Long> {
        val entity = AppDatabaseHelper.convert(legalConsent!!)
        return AppDatabaseHelper.createObservableIO {
            legalConsentRoomDAO.saveLegalConsent(entity)
        }
    }
}
