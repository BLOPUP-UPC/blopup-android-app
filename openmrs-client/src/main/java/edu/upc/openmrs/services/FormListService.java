/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package edu.upc.openmrs.services;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.List;

import edu.upc.sdk.library.OpenmrsAndroid;
import edu.upc.sdk.library.api.RestApi;
import edu.upc.sdk.library.api.RestServiceBuilder;
import edu.upc.sdk.library.dao.EncounterTypeRoomDAO;
import edu.upc.sdk.library.dao.FormResourceDAO;
import edu.upc.sdk.library.databases.AppDatabase;
import edu.upc.sdk.library.databases.entities.FormResourceEntity;
import edu.upc.sdk.library.models.EncounterType;
import edu.upc.sdk.library.models.Results;
import edu.upc.sdk.utilities.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormListService extends IntentService {
    private final RestApi apiService = RestServiceBuilder.createService(RestApi.class);
    private List<FormResourceEntity> formresourcelist;

    public FormListService() {
        super("Sync Form List");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        FormResourceDAO formResourceDAO = AppDatabase.getDatabase(OpenmrsAndroid.getInstance().getApplicationContext()).formResourceDAO();
        EncounterTypeRoomDAO encounterTypeRoomDAO = AppDatabase.getDatabase(OpenmrsAndroid.getInstance().getApplicationContext()).encounterTypeRoomDAO();

            Call<Results<FormResourceEntity>> call = apiService.getForms();
            call.enqueue(new Callback<Results<FormResourceEntity>>() {
                @Override
                public void onResponse(@NonNull Call<Results<FormResourceEntity>> call, @NonNull Response<Results<FormResourceEntity>> response) {
                    if (response.isSuccessful()) {
                        formResourceDAO.deleteAllForms();
                        formresourcelist = response.body().getResults();
                        int size = formresourcelist.size();
                        for (int i = 0; i < size; i++) {
                            formResourceDAO.addFormResource(formresourcelist.get(i));
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Results<FormResourceEntity>> call, @NonNull Throwable t) {
                    ToastUtil.error(t.getMessage());
                }
            });

            Call<Results<EncounterType>> call2 = apiService.getEncounterTypes();
            call2.enqueue(new Callback<Results<EncounterType>>() {
                @Override
                public void onResponse(@NonNull Call<Results<EncounterType>> call, @NonNull Response<Results<EncounterType>> response) {
                    if (response.isSuccessful()) {
                        encounterTypeRoomDAO.deleteAllEncounterTypes();
                        Results<EncounterType> encounterTypeList = response.body();
                        for (EncounterType encounterType : encounterTypeList.getResults())
                            encounterTypeRoomDAO.addEncounterType(encounterType);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Results<EncounterType>> call, @NonNull Throwable t) {
                    ToastUtil.error(t.getMessage());
                }
            });
        }
}