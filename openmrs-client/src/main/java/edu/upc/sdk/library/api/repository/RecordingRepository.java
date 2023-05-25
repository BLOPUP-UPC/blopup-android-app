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

package edu.upc.sdk.library.api.repository;

import android.util.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.upc.sdk.library.databases.AppDatabaseHelper;
import edu.upc.sdk.library.databases.entities.PatientEntity;
import edu.upc.sdk.library.models.EmailRequest;
import edu.upc.sdk.library.models.RecordingRequest;
import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;

@Singleton
public class RecordingRepository extends BaseRepository {

    @Inject
    public RecordingRepository(){}

    public Observable<String> saveRecording(RecordingRequest recordingRequest) {
        return AppDatabaseHelper.createObservableIO(() -> {
            try{
                if (Math.random() < 0.9){
                    return "OK";
                } else {
                    throw new Exception("Error saving recording:");
                }
            } catch (Exception exception){
                Log.e(getClass().getName(), exception.getMessage(), exception);
                throw exception;
            }
        });
    }
}