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

import com.openmrs.android_sdk.R;
import com.openmrs.android_sdk.library.models.EmailRequest;
import com.openmrs.android_sdk.utilities.NetworkUtils;
import com.openmrs.android_sdk.utilities.StringUtils;
import com.openmrs.android_sdk.utilities.ToastUtil;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;
import rx.Observable;

@Singleton
public class EmailRepository extends BaseRepository {

    @Inject
    public  EmailRepository(){}

    public Observable<String> sendEmail(EmailRequest emailRequest) {
        return createObservableIO(() -> {
            // If not online, fetch providers locally
            if (!NetworkUtils.isOnline()) {
                ToastUtil.notify(context.getString(R.string.offline_provider_fetch));
                logger.e("offline providers fetched couldn't sync with the database device offline");
                return null;
            }
            // Otherwise (online), fetch remote providers
            Response<String> response = restApi.sendEmail(emailRequest).execute();
            if (response.isSuccessful()) {
                if (StringUtils.notEmpty(response.body())) {
                    return response.body();
                }
            } else {
                logger.e("Error sending email from the server: " + response.errorBody().string());
            }
            return null;
        });
    }
}