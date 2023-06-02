/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package edu.upc.sdk.library.api.repository

import android.util.Log
import edu.upc.sdk.library.dao.LegalConsentDAO
import edu.upc.sdk.library.databases.AppDatabaseHelper.createObservableIO
import edu.upc.sdk.library.models.LegalConsent
import edu.upc.sdk.library.models.ResultType
import okhttp3.MediaType
import okhttp3.MultipartBody
import rx.Observable
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingRepository @Inject constructor(val legalConsentDAO: LegalConsentDAO) :
    BaseRepository() {

    fun saveRecording(legalConsent: LegalConsent): Observable<ResultType> {
        val mediaType = MediaType.parse("audio")
        val file = File(legalConsent.filePath!!)
        val multipartBody = MultipartBody.create(mediaType, file)
        val multipartBodyPart = MultipartBody.Part.create(multipartBody)

        return createObservableIO {
            try {
                val response = restApi.uploadConsent(multipartBodyPart, legalConsent.patientId).execute()

                if (response.isSuccessful) {
                    //save to local DB
                    return@createObservableIO ResultType.RecordingSuccess
                }
                else {
                    return@createObservableIO ResultType.RecordingError
                }
            } catch (exception: Exception) {
                Log.e(javaClass.name, exception.message, exception)
                throw exception
            }
        }
    }
}