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

import android.content.Context;

import androidx.work.WorkManager;

import edu.upc.sdk.library.CrashlyticsLogger;
import edu.upc.sdk.library.CrashlyticsLoggerImpl;
import edu.upc.sdk.library.OpenMRSLogger;
import edu.upc.sdk.library.OpenmrsAndroid;
import edu.upc.sdk.library.api.RestApi;
import edu.upc.sdk.library.api.RestServiceBuilder;
import edu.upc.sdk.library.databases.AppDatabase;

/**
 * The type Base repository.
 */
public abstract class BaseRepository {
    /**
     * The Context.
     *
     * @see Context
     */
    protected Context context;
    /**
     * The Rest api
     *
     * @see RestApi
     */
    protected RestApi restApi;
    /**
     * The Database instance
     *
     * @see AppDatabase
     */
    protected AppDatabase db;
    /**
     * The Work manager.
     *
     * @see WorkManager
     */
    protected WorkManager workManager;
    /**
     * The Logger.
     *
     * @see OpenMRSLogger
     */
    protected OpenMRSLogger logger;

    protected CrashlyticsLogger crashlytics;

    /**
     * Instantiates a new Base repository.
     */
    public BaseRepository(CrashlyticsLogger crashlytics) {
        {
            {
                this.context = OpenmrsAndroid.getInstance();
                this.restApi = RestServiceBuilder.createService(RestApi.class);
                this.db = AppDatabase.getDatabase(context);
                this.workManager = WorkManager.getInstance(context);
                this.logger = new OpenMRSLogger();
                this.crashlytics = crashlytics == null? new CrashlyticsLoggerImpl() : crashlytics;
            }
        }
    }

    /**
     * Instantiates a new Base repository.
     *
     * @param restApi the rest api
     * @param logger  the logger
     */
    public BaseRepository(RestApi restApi, OpenMRSLogger logger) {
        this.logger = logger;
        this.restApi = restApi;
        this.context = OpenmrsAndroid.getInstance();
    }
}
