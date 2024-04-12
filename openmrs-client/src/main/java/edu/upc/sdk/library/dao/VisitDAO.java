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

package edu.upc.sdk.library.dao;

import android.content.Context;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.upc.sdk.library.OpenmrsAndroid;
import edu.upc.sdk.library.databases.AppDatabase;
import edu.upc.sdk.library.databases.AppDatabaseHelper;
import rx.Observable;

/**
 * The type Visit dao.
 */
@Singleton
public class VisitDAO {

    /**
     * The Context.
     */
    final Context context;
    /**
     * The Observation room dao.
     */
    final ObservationRoomDAO observationRoomDAO;
    /**
     * The Visit room dao.
     */
    final VisitRoomDAO visitRoomDAO;
    /**
     * The Encounter room dao.
     */
    EncounterDAO encounterDAO;

    @Inject
    public VisitDAO() {
        context = OpenmrsAndroid.getInstance().getApplicationContext();
        observationRoomDAO = AppDatabase.getDatabase(context).observationRoomDAO();
        visitRoomDAO = AppDatabase.getDatabase(context).visitRoomDAO();
        encounterDAO = new EncounterDAO();
    }

    public Observable<Boolean> deleteVisitByUuid(String visitUuid) {
        return AppDatabaseHelper.createObservableIO(() -> {
            visitRoomDAO.deleteVisitByUuid(visitUuid);
            return true;
        });
    }
}
