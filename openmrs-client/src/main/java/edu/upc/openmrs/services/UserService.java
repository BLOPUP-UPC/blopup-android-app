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

package edu.upc.openmrs.services;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.upc.sdk.library.OpenmrsAndroid;
import edu.upc.sdk.library.api.RestApi;
import edu.upc.sdk.library.api.RestServiceBuilder;
import edu.upc.sdk.library.models.Results;
import edu.upc.sdk.library.models.User;
import edu.upc.sdk.utilities.ApplicationConstants;
import edu.upc.sdk.utilities.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserService {
    public void updateUserInformation(final String username) {
        RestApi restApi = RestServiceBuilder.createService();
        Call<Results<User>> call = restApi.getUserInfo(username);
        call.enqueue(new Callback<Results<User>>() {
            @Override
            public void onResponse(@NonNull Call<Results<User>> call, @NonNull Response<Results<User>> response) {
                if (response.isSuccessful()) {
                    List<User> resultList = response.body().getResults();
                    boolean matchFound = false;
                    if (resultList.size() > 0) {
                        for (User user : resultList) {
                            if (user.getDisplay().toUpperCase().equals(username.toUpperCase())) {
                                matchFound = true;
                                fetchFullUserInformation(user.getUuid());
                            }
                        }
                        if (!matchFound) {
                            //string resource and translation added "error_fetching_user_data_message"
                            ToastUtil.error("Couldn't fetch user data");
                        }
                    }
                } else {
                    ToastUtil.error(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Results<User>> call, @NonNull Throwable t) {
                ToastUtil.error(t.getMessage());
            }
        });
    }

    private void fetchFullUserInformation(String uuid) {
        RestApi restApi = RestServiceBuilder.createService();
        Call<User> call = restApi.getFullUserInfo(uuid);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    Map<String, String> userInfo = new HashMap<>();
                    userInfo.put(ApplicationConstants.UserKeys.USER_PERSON_NAME, response.body().getPerson().getDisplay());
                    userInfo.put(ApplicationConstants.UserKeys.USER_UUID, response.body().getPerson().getUuid());
                    OpenmrsAndroid.setCurrentUserInformation(userInfo);
                } else {
                    ToastUtil.error(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                ToastUtil.error(t.getMessage());
            }
        });
    }
}
