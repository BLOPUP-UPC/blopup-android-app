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

package edu.upc.sdk.library.databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import edu.upc.sdk.library.dao.LocationRoomDAO;
import edu.upc.sdk.library.dao.PatientRoomDAO;
import edu.upc.sdk.library.databases.entities.LocationEntity;
import edu.upc.sdk.library.databases.entities.PatientEntity;
import edu.upc.sdk.utilities.ApplicationConstants;

/**
 * The type App database.
 */
@Database(entities = {
            LocationEntity.class,
            PatientEntity.class
        },
        version = 14
)

public abstract class AppDatabase extends RoomDatabase {


    private static volatile AppDatabase INSTANCE;

    /**
     * Gets database.
     *
     * @param context the context
     * @return the database
     */
//TODO remove this public and refactor the packages of classes to incorporate allDAOs under this repository
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, ApplicationConstants.DB_NAME)
                            .allowMainThreadQueries().fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Location room dao location room dao.
     *
     * @return the location room dao
     */
    public abstract LocationRoomDAO locationRoomDAO();

    /**
     * Patient room dao patient room dao.
     *
     * @return the patient room dao
     */
    public abstract PatientRoomDAO patientRoomDAO();
}
