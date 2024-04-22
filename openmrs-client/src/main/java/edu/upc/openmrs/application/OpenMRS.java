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

package edu.upc.openmrs.application;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import dagger.hilt.android.HiltAndroidApp;
import edu.upc.blopup.hilt.CurrentActivityProvider;
import edu.upc.openmrs.services.AuthenticateCheckService;
import edu.upc.sdk.library.OpenmrsAndroid;

@HiltAndroidApp
public class OpenMRS extends Application {
    private static String mExternalDirectoryPath;
    private static OpenMRS instance;
    private final MBCActivityLifecycleCallbacks mCallbacks = new MBCActivityLifecycleCallbacks();

    public static OpenMRS getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(mCallbacks);

        instance = this;
        OpenmrsAndroid.initializeSdk(this);

        if (mExternalDirectoryPath == null) {
            mExternalDirectoryPath = this.getExternalFilesDir(null).toString();
        }
        Intent intent = new Intent(this, AuthenticateCheckService.class);
        startService(intent);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static class MBCActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            CurrentActivityProvider.Companion.onActivityCreated(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            CurrentActivityProvider.Companion.onActivityDestroyed(activity);
        }
    }
}
