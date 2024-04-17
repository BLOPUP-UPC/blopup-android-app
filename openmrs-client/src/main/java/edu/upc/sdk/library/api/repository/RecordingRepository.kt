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

import edu.upc.openmrs.utilities.FileUtils
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.api.RestApi
import edu.upc.sdk.library.databases.AppDatabaseHelper.createObservableIO
import edu.upc.sdk.library.models.LegalConsent
import edu.upc.sdk.library.models.LegalConsentRequest
import edu.upc.sdk.library.models.ResultType
import rx.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingRepository @Inject constructor(
    private val restApi: RestApi,
    private val logger: OpenMRSLogger
) {

    fun saveRecording(legalConsent: LegalConsent): Observable<ResultType> {

        val byteString = FileUtils.getByteArrayStringFromAudio(legalConsent.filePath)

        val request = LegalConsentRequest(legalConsent.patientIdentifier!!, byteString!!)

        return createObservableIO {
            try {
                val response = restApi.uploadLegalConsent(request).execute()

                if (response.isSuccessful) {
                    //save to local DB
                    return@createObservableIO ResultType.RecordingSuccess
                }
                else {
                    return@createObservableIO ResultType.RecordingError
                }
            } catch (exception: Exception) {
                logger.e("${javaClass.name}:  ${exception.message}", exception)
                throw exception
            }
        }
    }
}