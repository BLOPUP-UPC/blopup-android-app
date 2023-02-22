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

package com.openmrs.android_sdk.library.api.repository;

import static com.openmrs.android_sdk.library.databases.AppDatabaseHelper.createObservableIO;

import android.util.Log;

import com.openmrs.android_sdk.library.models.EmailRequest;
import com.openmrs.android_sdk.library.models.Session;

import javax.inject.Inject;
import javax.inject.Singleton;

import kotlin.Unit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;

@Singleton
public class EmailRepository extends BaseRepository {

    @Inject
    public  EmailRepository(){}

    public Observable<String> sendEmail(EmailRequest emailRequest) {
        return createObservableIO(() -> {
            try{
                Response<ResponseBody> response = restApi.sendEmail(emailRequest).execute();
                if (response.isSuccessful()){
                    return response.body().string();
                }
                throw new Exception("Error sending email:" + response.errorBody());
            } catch (Exception exception){
                Log.e(getClass().getName(), exception.getMessage(), exception);
                throw exception;
            }
        });
    }
}