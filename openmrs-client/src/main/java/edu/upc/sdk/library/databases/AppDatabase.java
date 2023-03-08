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

import edu.upc.sdk.library.dao.AllergyRoomDAO;
import edu.upc.sdk.library.dao.ConceptRoomDAO;
import edu.upc.sdk.library.dao.DiagnosisRoomDAO;
import edu.upc.sdk.library.dao.EncounterCreateRoomDAO;
import edu.upc.sdk.library.dao.EncounterRoomDAO;
import edu.upc.sdk.library.dao.EncounterTypeRoomDAO;
import edu.upc.sdk.library.dao.FormResourceDAO;
import edu.upc.sdk.library.dao.LocationRoomDAO;
import edu.upc.sdk.library.dao.ObservationRoomDAO;
import edu.upc.sdk.library.dao.PatientRoomDAO;
import edu.upc.sdk.library.dao.ProviderRoomDAO;
import edu.upc.sdk.library.dao.VisitRoomDAO;
import edu.upc.sdk.library.databases.entities.AllergyEntity;
import edu.upc.sdk.library.databases.entities.ConceptEntity;
import edu.upc.sdk.library.databases.entities.DiagnosisEntity;
import edu.upc.sdk.library.databases.entities.EncounterEntity;
import edu.upc.sdk.library.databases.entities.FormResourceEntity;
import edu.upc.sdk.library.databases.entities.LocationEntity;
import edu.upc.sdk.library.databases.entities.ObservationEntity;
import edu.upc.sdk.library.databases.entities.PatientEntity;
import edu.upc.sdk.library.databases.entities.VisitEntity;
import edu.upc.sdk.library.models.EncounterType;
import edu.upc.sdk.library.models.Encountercreate;
import edu.upc.sdk.library.models.Provider;
import edu.upc.sdk.utilities.ApplicationConstants;

import edu.upc.sdk.library.databases.entities.AllergyEntity;
import edu.upc.sdk.library.databases.entities.ConceptEntity;
import edu.upc.sdk.library.databases.entities.DiagnosisEntity;
import edu.upc.sdk.library.databases.entities.EncounterEntity;
import edu.upc.sdk.library.databases.entities.FormResourceEntity;
import edu.upc.sdk.library.databases.entities.LocationEntity;
import edu.upc.sdk.library.databases.entities.ObservationEntity;
import edu.upc.sdk.library.databases.entities.PatientEntity;
import edu.upc.sdk.library.databases.entities.VisitEntity;

/**
 * The type App database.
 */
@Database(entities = {ConceptEntity.class,
        EncounterEntity.class,
        LocationEntity.class,
        ObservationEntity.class,
        PatientEntity.class,
        VisitEntity.class,
        DiagnosisEntity.class,
        Provider.class,
        FormResourceEntity.class,
        EncounterType.class,
        Encountercreate.class,
        AllergyEntity.class},
        version = 2)

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
     * Visit room dao visit room dao.
     *
     * @return the visit room dao
     */
    public abstract VisitRoomDAO visitRoomDAO();

    /**
     * Patient room dao patient room dao.
     *
     * @return the patient room dao
     */
    public abstract PatientRoomDAO patientRoomDAO();

    /**
     * Observation room dao observation room dao.
     *
     * @return the observation room dao
     */
    public abstract ObservationRoomDAO observationRoomDAO();

    /**
     * Encounter room dao encounter room dao.
     *
     * @return the encounter room dao
     */
    public abstract EncounterRoomDAO encounterRoomDAO();

    /**
     * Concept room dao concept room dao.
     *
     * @return the concept room dao
     */
    public abstract ConceptRoomDAO conceptRoomDAO();

    /**
     * Provider room dao provider room dao.
     *
     * @return the provider room dao
     */
    public abstract ProviderRoomDAO providerRoomDAO();

    /**
     * Form resource dao form resource dao.
     *
     * @return the form resource dao
     */
    public abstract FormResourceDAO formResourceDAO();

    /**
     * Encounter type room dao encounter type room dao.
     *
     * @return the encounter type room dao
     */
    public abstract EncounterTypeRoomDAO encounterTypeRoomDAO();

    /**
     * Encounter create room dao encounter create room dao.
     *
     * @return the encounter create room dao
     */
    public abstract EncounterCreateRoomDAO encounterCreateRoomDAO();

    /**
     * Allergy room dao allergy room dao.
     *
     * @return the allergy room dao
     */
    public abstract AllergyRoomDAO allergyRoomDAO();


    /**
     * Diagnosis room dao.
     *
     * @return the diagnosis room dao
     */
    public abstract DiagnosisRoomDAO diagnosisRoomDAO();
}
