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

package org.openmrs.mobile.application;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.openmrs.android_sdk.library.OpenMRSLogger;
import com.openmrs.android_sdk.library.OpenmrsAndroid;

import org.openmrs.mobile.services.FormListService;
import org.openmrs.mobile.services.AuthenticateCheckService;

import java.io.File;

import dagger.hilt.android.HiltAndroidApp;
import edu.upc.blopup.hilt.CurrentActivityProvider;

@HiltAndroidApp
public class OpenMRS extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {
    private static final String OPENMRS_DIR_NAME = "OpenMRS";
    private static final String OPENMRS_DIR_PATH = File.separator + OPENMRS_DIR_NAME;
    private static String mExternalDirectoryPath;
    private static OpenMRS instance;
    private OpenMRSLogger mLogger;

    public static OpenMRS getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        OpenmrsAndroid.initializeSdk(this);

        if (mExternalDirectoryPath == null) {
            mExternalDirectoryPath = this.getExternalFilesDir(null).toString();
        }
        mLogger = new OpenMRSLogger();
        Intent i = new Intent(this, FormListService.class);
        startService(i);
        Intent intent = new Intent(this, AuthenticateCheckService.class);
        startService(intent);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public boolean isRunningKitKatVersionOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        CurrentActivityProvider.Companion.onActivityCreated(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        CurrentActivityProvider.Companion.onActivityDestroyed(activity);
    }
}
