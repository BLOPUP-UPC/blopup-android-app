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
import edu.upc.sdk.library.databases.AppDatabaseHelper.createObservableIO
import retrofit2.Response
import rx.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingRepository @Inject constructor() : BaseRepository() {
    fun saveRecording(recording: String): Observable<String> {
        return createObservableIO {
            try {
                val response = Response.success("OK")
                return@createObservableIO response.body()!!
            } catch (exception: Exception) {
                Log.e(javaClass.name, exception.message, exception)
                throw exception
            }
        }
    }
}