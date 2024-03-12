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

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.upc.R;
import edu.upc.openmrs.application.OpenMRS;
import edu.upc.sdk.library.OpenMRSLogger;
import edu.upc.sdk.library.OpenmrsAndroid;
import edu.upc.sdk.library.api.RestApi;
import edu.upc.sdk.library.api.RestServiceBuilder;
import edu.upc.sdk.library.databases.AppDatabase;
import edu.upc.sdk.library.models.Session;
import edu.upc.sdk.utilities.ApplicationConstants;
import edu.upc.sdk.utilities.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthenticateCheckService extends Service {
    private final IBinder mBinder = new SocketServerBinder();

    private final OpenMRSLogger logger = new OpenMRSLogger();
    private boolean mRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mRunning) {
                    String username = OpenmrsAndroid.getUsername();
                    String password = OpenmrsAndroid.getPassword();
                    if ((!username.isEmpty()) &&
                            (!password.isEmpty())) {
                        authenticateCheck(username, password);
                    }
                }
            }
        }, 10000, 100000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mRunning = true;
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        mRunning = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mRunning = false;
        return super.onUnbind(intent);
    }

    private void authenticateCheck(String username, String password) {
        RestApi restApi = RestServiceBuilder.createService(RestApi.class, username, password);
        Call<Session> call = restApi.getSession();
        call.enqueue(new Callback<Session>() {
            @Override
            public void onResponse(@NonNull Call<Session> call, @NonNull Response<Session> response) {
                if (response.isSuccessful()) {
                    Session session = response.body();
                    if (session.isAuthenticated()) {
                        logger.i("Service Task: user authenticated");
                    } else {
                        logger.e("Service Task: User Credentials Changed");
                        if (isForeground(OpenMRS.getInstance().getPackageName())) {
                            Intent broadcastIntent = new Intent();
                            broadcastIntent.setAction(ApplicationConstants.BroadcastActions.AUTHENTICATION_CHECK_BROADCAST_ACTION);
                            sendBroadcast(broadcastIntent);
                        } else {
                            AppDatabase.getDatabase(getApplicationContext()).close();
                            OpenmrsAndroid.clearUserPreferencesData();
                            OpenmrsAndroid.clearCurrentLoggedInUserInfo();
                        }
                    }
                } else {
                    ToastUtil.error(getString(R.string.authenticate_check_service_error_response_message));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Session> call, @NonNull Throwable t) {
                if (t instanceof UnknownHostException) {
                    ToastUtil.error(getString(R.string.no_internet_connection));
                    logger.e("Service Task: No Network");
                } else {
                    ToastUtil.error(getString(R.string.authenticate_service_error_message));
                }
            }
        });
    }

    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }

    public class SocketServerBinder extends Binder {
        public AuthenticateCheckService getService() {
            return AuthenticateCheckService.this;
        }
    }
}
