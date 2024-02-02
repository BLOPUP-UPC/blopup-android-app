/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package edu.upc.sdk.library.api.workers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import edu.upc.R;
import edu.upc.sdk.library.OpenMRSLogger;
import edu.upc.sdk.library.api.RestApi;
import edu.upc.sdk.library.api.RestServiceBuilder;
import edu.upc.sdk.library.dao.PatientDAO;
import edu.upc.sdk.library.listeners.retrofitcallbacks.DefaultResponseCallback;
import edu.upc.sdk.library.models.Patient;
import edu.upc.sdk.library.models.PatientDto;
import edu.upc.sdk.library.models.PatientDtoUpdate;
import edu.upc.sdk.utilities.ApplicationConstants;
import edu.upc.sdk.utilities.NetworkUtils;
import edu.upc.sdk.utilities.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The type Update patient worker.
 */
public class UpdatePatientWorker extends Worker {
    private static final int ON_SUCCESS = 1;
    private static final int ON_FAILURE = 2;
    private final RestApi restApi;
    private final OpenMRSLogger logger;
    private final Handler mHandler;

    /**
     * Instantiates a new Update patient worker.
     *
     * @param appContext   the app context
     * @param workerParams the worker params
     */
    public UpdatePatientWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        restApi = RestServiceBuilder.createService(RestApi.class);
        logger = new OpenMRSLogger();

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String responseMessage;
                switch (msg.what) {
                    case ON_SUCCESS:
                        String updateSuccessPatientName = (String) msg.obj;
                        ToastUtil.success(getApplicationContext().getString(R.string.patient_update_successful, updateSuccessPatientName));
                        break;
                    case ON_FAILURE:
                        String updateFailedPatientName = (String) msg.obj;
                        ToastUtil.error(getApplicationContext().getString(R.string.patient_update_unsuccessful, updateFailedPatientName));
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    @NonNull
    @Override
    public Result doWork() {
        final boolean[] result = new boolean[1];
        String patientIdTobeUpdated = getInputData().getString(ApplicationConstants.PRIMARY_KEY_ID);
        PatientDAO patientDAO = new PatientDAO();
        Patient patientTobeUpdated = patientDAO.findPatientByID(patientIdTobeUpdated);

        updatePatient(patientTobeUpdated, new DefaultResponseCallback() {
            @Override
            public void onResponse() {
                result[0] = true;
                Message msg = new Message();
                msg.obj = patientTobeUpdated.getPerson().getName().getNameString();
                msg.what = ON_SUCCESS;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onErrorResponse(String errorMessage) {
                result[0] = false;
                Message msg = new Message();
                msg.obj = patientTobeUpdated.getPerson().getName().getNameString();
                msg.what = ON_FAILURE;
                mHandler.sendMessage(msg);
            }
        });
        return result[0] ? Result.success() : Result.retry();
    }

    /**
     * Update patient.
     *
     * @param patient          the patient
     * @param callbackListener the callback listener
     */
    public void updatePatient(final Patient patient, @Nullable final DefaultResponseCallback callbackListener) {
        PatientDtoUpdate patientDto = patient.getUpdatedPatientDto();
        if (NetworkUtils.isOnline()) {
            Call<PatientDto> call = restApi.updatePatient(patientDto, patient.getUuid(), "full");
            call.enqueue(new Callback<PatientDto>() {
                @Override
                public void onResponse(@NonNull Call<PatientDto> call, @NonNull Response<PatientDto> response) {
                    if (response.isSuccessful()) {
                        PatientDto patientDto = response.body();
                        patient.setBirthdate(patientDto.getPerson().getBirthdate());

                        patient.setUuid(patient.getUuid());

                        new PatientDAO().updatePatient(patient.getId(), patient);

                        if (callbackListener != null) {
                            callbackListener.onResponse();
                        }
                    } else {
                        if (callbackListener != null) {
                            callbackListener.onErrorResponse(response.message());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PatientDto> call, @NonNull Throwable t) {
                    if (callbackListener != null) {
                        callbackListener.onErrorResponse(t.getMessage());
                    }
                }
            });
        }
    }
}
